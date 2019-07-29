package com.fr.swift.jdbc.visitor.insert;

import com.fr.swift.jdbc.antlr4.SwiftSqlParseUtil;
import com.fr.swift.jdbc.antlr4.SwiftSqlParser;
import com.fr.swift.jdbc.visitor.BaseVisitor;
import com.fr.swift.source.ListBasedRow;
import com.fr.swift.source.Row;
import org.antlr.v4.runtime.tree.RuleNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yee
 * @date 2019-07-26
 */
public class InsertValueVisitor extends BaseVisitor<Row> {
    @Override
    public Row visitChildren(RuleNode node) {
        SwiftSqlParser.ValuesContext values = (SwiftSqlParser.ValuesContext) node;
        List<Object> data = new ArrayList<>();
        for (SwiftSqlParser.ValueContext valueContext : values.value()) {
            String text = valueContext.getText();
            if (valueContext.start.getType() == SwiftSqlParser.NUMERIC_LITERAL) {
                data.add(Double.parseDouble(text));
            } else {
                data.add(SwiftSqlParseUtil.trimQuote(text, "'"));
            }
        }
        return new ListBasedRow(data);
    }
}
