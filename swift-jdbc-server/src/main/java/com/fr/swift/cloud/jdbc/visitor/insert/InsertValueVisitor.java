package com.fr.swift.cloud.jdbc.visitor.insert;

import com.fr.swift.cloud.jdbc.antlr4.SwiftSqlParseUtil;
import com.fr.swift.cloud.jdbc.antlr4.SwiftSqlParser;
import com.fr.swift.cloud.jdbc.visitor.BaseVisitor;
import com.fr.swift.cloud.source.ColumnTypeUtils;
import com.fr.swift.cloud.source.ListBasedRow;
import com.fr.swift.cloud.source.Row;
import com.fr.swift.cloud.source.SwiftMetaDataColumn;
import com.fr.swift.cloud.util.DateUtils;
import org.antlr.v4.runtime.tree.RuleNode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author yee
 * @date 2019-07-26
 */
public class InsertValueVisitor extends BaseVisitor<Row> {
    private List<SwiftMetaDataColumn> columns;

    public InsertValueVisitor(List<SwiftMetaDataColumn> columns) {
        this.columns = columns;
    }

    @Override
    public Row visitChildren(RuleNode node) {
        SwiftSqlParser.ValuesContext values = (SwiftSqlParser.ValuesContext) node;
        List<Object> data = new ArrayList<>();
        List<SwiftSqlParser.ValueContext> valueContexts = values.value();
        for (int i = 0; i < valueContexts.size(); i++) {
            String text = SwiftSqlParseUtil.trimQuote(valueContexts.get(i).getText(), SwiftSqlParseUtil.SINGLE_QUOTE);
            switch (ColumnTypeUtils.getClassType(columns.get(i))) {
                case INTEGER:
                    data.add(Integer.parseInt(text));
                    break;
                case LONG:
                    data.add(Long.parseLong(text));
                    break;
                case DATE:
                    Date date = DateUtils.string2Date(text);
                    data.add(null != date ? date.getTime() : null);
                    break;
                case DOUBLE:
                    data.add(Double.parseDouble(text));
                    break;
                case STRING:
                    data.add(text);
                    break;
                default:
            }
        }
        return new ListBasedRow(data);
    }
}
