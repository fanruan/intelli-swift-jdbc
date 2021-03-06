package com.fr.swift.cloud.jdbc.visitor.select;

import com.fr.swift.cloud.jdbc.antlr4.SwiftSqlParseUtil;
import com.fr.swift.cloud.jdbc.antlr4.SwiftSqlParser;
import com.fr.swift.cloud.jdbc.visitor.BaseVisitor;
import com.fr.swift.cloud.query.info.bean.element.SortBean;
import com.fr.swift.cloud.query.sort.SortType;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yee
 * @date 2019-07-19
 */
public class OrderByVisitor extends BaseVisitor<List<SortBean>> {
    @Override
    public List<SortBean> visitChildren(RuleNode node) {
        SwiftSqlParser.OrderByContext order = (SwiftSqlParser.OrderByContext) node;
        List<SortBean> result = new ArrayList<>();
        SortBeanVisitor sortBeanVisitor = new SortBeanVisitor();
        for (ParseTree child : order.children) {
            SortBean accept = child.accept(sortBeanVisitor);
            if (null != accept) {
                result.add(accept);
            }
        }
        return result;
    }

    private class SortBeanVisitor extends AbstractParseTreeVisitor<SortBean> {
        private SortBean sortBean;

        @Override
        public SortBean visitChildren(RuleNode node) {
            sortBean = new SortBean(SortType.ASC, SwiftSqlParseUtil.trimQuote(node.getText()));
            return sortBean;
        }

        @Override
        public SortBean visitTerminal(TerminalNode node) {
            switch (node.getSymbol().getType()) {
                case SwiftSqlParser.ASC:
                    sortBean.setType(SortType.ASC);
                    break;
                case SwiftSqlParser.DESC:
                    sortBean.setType(SortType.DESC);
                    break;
                default:
            }
            return null;
        }
    }
}
