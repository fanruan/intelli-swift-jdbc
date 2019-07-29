package com.fr.swift.jdbc.visitor.where;

import com.fr.swift.jdbc.antlr4.SwiftSqlParseUtil;
import com.fr.swift.jdbc.antlr4.SwiftSqlParser;
import com.fr.swift.jdbc.visitor.BaseVisitor;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author yee
 * @date 2019-07-19
 */
public class ValueExprVisitor extends BaseVisitor<Set<String>> {
    @Override
    public Set<String> visitChildren(RuleNode node) {
        Set<String> set = new HashSet<>();
        if (node instanceof SwiftSqlParser.ValuesContext) {
            for (SwiftSqlParser.ValueContext valueContext : ((SwiftSqlParser.ValuesContext) node).value()) {
                set.addAll(valueContext.accept(this));
            }
        } else if (node instanceof SwiftSqlParser.ValueContext) {
            if (((SwiftSqlParser.ValueContext) node).getStart().getType() == SwiftSqlParser.NUMERIC_LITERAL) {
                return Collections.singleton(((SwiftSqlParser.ValueContext) node).NUMERIC_LITERAL().getText());
            }
            TerminalNode terminalNode = ((SwiftSqlParser.ValueContext) node).STRING_LITERAL();
            String value = SwiftSqlParseUtil.trimQuote(terminalNode.getText(), "'");
            return Collections.singleton(value);
        }
        return set;
    }
}
