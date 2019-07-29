package com.fr.swift.jdbc.visitor.select;

import com.fr.swift.jdbc.antlr4.SwiftSqlParseUtil;
import com.fr.swift.jdbc.antlr4.SwiftSqlParser;
import com.fr.swift.jdbc.visitor.BaseVisitor;
import com.fr.swift.query.aggregator.AggregatorType;
import com.fr.swift.query.info.bean.element.AggregationBean;
import com.fr.swift.query.info.bean.element.MetricBean;
import org.antlr.v4.runtime.tree.RuleNode;

/**
 * 处理聚合函数 todate不作为聚合函数处理
 *
 * @author yee
 * @date 2019-07-19
 */
public class FunctionVisitor extends BaseVisitor<AggregationBean> {

    @Override
    public AggregationBean visitChildren(RuleNode node) {
        SwiftSqlParser.FuncExprContext funcExprContext = (SwiftSqlParser.FuncExprContext) node;
        int type = funcExprContext.funcName().start.getType();
        AggregationBean bean = null;
        String text = funcExprContext.simpleExpr(0).getText();
        text = SwiftSqlParseUtil.trimQuote(text, "`");
        switch (type) {
            case SwiftSqlParser.MAX:
                bean = MetricBean.builder(text, AggregatorType.MAX).build();
                break;
            case SwiftSqlParser.MIN:
                bean = MetricBean.builder(text, AggregatorType.MIN).build();
                break;
            case SwiftSqlParser.SUM:
                bean = MetricBean.builder(text, AggregatorType.SUM).build();
                break;
            case SwiftSqlParser.AVG:
                bean = MetricBean.builder(text, AggregatorType.AVERAGE).build();
                break;
            default:
                bean = MetricBean.builder(text, AggregatorType.COUNT).build();
                break;

        }
        bean.setAlias(funcExprContext.getText());
        return bean;
    }
}
