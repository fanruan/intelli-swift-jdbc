package com.fr.swift.jdbc.visitor.where;

import com.fr.swift.jdbc.antlr4.SwiftSqlParseUtil;
import com.fr.swift.jdbc.antlr4.SwiftSqlParser;
import com.fr.swift.jdbc.creator.FilterBeanCreator;
import com.fr.swift.jdbc.visitor.BaseVisitor;
import com.fr.swift.query.info.bean.element.filter.FilterInfoBean;
import com.fr.swift.query.info.bean.element.filter.impl.AndFilterBean;
import com.fr.swift.query.info.bean.element.filter.impl.InFilterBean;
import com.fr.swift.query.info.bean.element.filter.impl.NotFilterBean;
import com.fr.swift.query.info.bean.element.filter.impl.NullFilterBean;
import com.fr.swift.query.info.bean.element.filter.impl.NumberInRangeFilterBean;
import com.fr.swift.query.info.bean.element.filter.impl.OrFilterBean;
import org.antlr.v4.runtime.tree.ErrorNodeImpl;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author yee
 * @date 2019-07-19
 */
public class BoolExprVisitor extends BaseVisitor<FilterInfoBean> {
    @Override
    public FilterInfoBean visitChildren(RuleNode node) {

        if (node instanceof SwiftSqlParser.BoolExprContext) {
            return visitBoolExprContext((SwiftSqlParser.BoolExprContext) node);
        } else if (node instanceof SwiftSqlParser.KeywordBoolExprContext) {
            return visitKeywordBoolExprContext((SwiftSqlParser.KeywordBoolExprContext) node);
        } else {
            return null;
        }
    }

    private FilterInfoBean visitKeywordBoolExprContext(SwiftSqlParser.KeywordBoolExprContext boolExpr) {
        String columnName = SwiftSqlParseUtil.trimQuote(boolExpr.simpleExpr().getText());
        FilterInfoBean filter = null;
        TerminalNode in = boolExpr.IN();
        TerminalNode aNull = boolExpr.NULL();
        if (null != in) {
            Set<String> accept = boolExpr.values().accept(new ValueExprVisitor());
            filter = new InFilterBean(columnName, accept);
        } else if (null != aNull) {
            filter = new NullFilterBean(columnName);
        } else {
            String start = boolExpr.NUMERIC_LITERAL(0).getText();
            String end = boolExpr.NUMERIC_LITERAL(1).getText();
            filter = NumberInRangeFilterBean.builder(columnName)
                    .setStart(start, true).setEnd(end, true)
                    .build();
        }
        if (null != boolExpr.NOT()) {
            return new NotFilterBean(filter);
        }
        return filter;
    }

    private FilterInfoBean visitBoolExprContext(SwiftSqlParser.BoolExprContext boolExpr) {
        SwiftSqlParser.KeywordBoolExprContext keywordBoolExprContext = boolExpr.keywordBoolExpr();
        if (null != keywordBoolExprContext) {
            return keywordBoolExprContext.accept(this);
        }
        List<SwiftSqlParser.BoolExprContext> boolExprs = boolExpr.boolExpr();
        if (boolExprs.isEmpty()) {

            SwiftSqlParser.SimpleExprContext exprColumn = boolExpr.simpleExpr(0);
            SwiftSqlParser.SimpleExprContext exprValue = boolExpr.simpleExpr(1);
            SwiftSqlParser.BoolOpContext exprOp = boolExpr.boolOp();
            FilterBeanCreator<FilterInfoBean> creator = exprOp.accept(new BoolOpVisitor());
            if (null == creator) {
                return visitErrorNode(new ErrorNodeImpl(boolExpr.getStart()));
            }
            String column = SwiftSqlParseUtil.trimQuote(exprColumn.getText());
            SwiftSqlParser.FuncExprContext valueFunc = exprValue.funcExpr();
            Object value = null != valueFunc ?
                    valueFunc.accept(new FilterFuncValueVisitor(exprOp.start.getType()))
                    : SwiftSqlParseUtil.trimQuote(exprValue.getText(), SwiftSqlParseUtil.SINGLE_QUOTE);
            return creator.create(column, value);
        } else {
            List<FilterInfoBean> list = new ArrayList<>();
            for (SwiftSqlParser.BoolExprContext boolExp : boolExprs) {
                list.add(boolExp.accept(this));
            }
            return boolExpr.logicOp().OR() != null ? new OrFilterBean(list) : new AndFilterBean(list);
        }
    }

}
