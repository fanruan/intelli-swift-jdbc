package com.fr.swift.jdbc.visitor.insert;

import com.fr.swift.jdbc.antlr4.SwiftSqlParseUtil;
import com.fr.swift.jdbc.antlr4.SwiftSqlParser;
import com.fr.swift.jdbc.visitor.BaseVisitor;
import com.fr.swift.source.ColumnTypeUtils;
import com.fr.swift.source.ListBasedRow;
import com.fr.swift.source.Row;
import com.fr.swift.source.SwiftMetaDataColumn;
import com.fr.swift.util.DateUtils;
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
