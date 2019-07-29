package com.fr.swift.jdbc.visitor.select;

import com.fr.swift.jdbc.antlr4.SwiftSqlParseUtil;
import com.fr.swift.jdbc.antlr4.SwiftSqlParser;
import com.fr.swift.query.info.bean.element.AggregationBean;
import com.fr.swift.query.info.bean.element.DimensionBean;
import com.fr.swift.query.info.bean.element.ToDateFormatFormulaBean;
import com.fr.swift.query.info.bean.element.ToDateFormulaBean;
import com.fr.swift.query.info.bean.element.filter.FilterInfoBean;
import com.fr.swift.query.info.bean.query.DetailQueryInfoBean;
import com.fr.swift.query.info.bean.query.GroupQueryInfoBean;
import com.fr.swift.query.info.bean.query.QueryInfoBean;
import com.fr.swift.query.info.bean.type.DimensionType;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yee
 * @date 2019-07-25
 */
public class NoGroupByVisitor extends BaseQueryBeanVisitor<QueryInfoBean> {
    private String tableName;
    private FilterInfoBean filter;
    private SwiftSqlParser.ColumnsContext columns;

    public NoGroupByVisitor(String tableName, FilterInfoBean filterInfoBean, SwiftSqlParser.ColumnsContext columns) {
        this.tableName = tableName;
        this.filter = filterInfoBean;
        this.columns = columns;
    }

    @Override
    public QueryInfoBean visitChildren(RuleNode node) {
        List<DimensionBean> dimensionBeans = new ArrayList<>();
        List<AggregationBean> metricBeans = new ArrayList<>();
        visitColumns(columns, dimensionBeans, metricBeans);

        if (metricBeans.isEmpty()) {
            if ((dimensionBeans.size() > 1 || dimensionBeans.get(0).getType() != DimensionType.DETAIL_ALL_COLUMN)) {
                for (DimensionBean dimensionBean : dimensionBeans) {
                    dimensionBean.setType(DimensionType.DETAIL);
                }
            }
            return DetailQueryInfoBean.builder(tableName).setDimensions(dimensionBeans).setFilter(filter).build();
        } else {
            return GroupQueryInfoBean.builder(tableName).setDimensions(dimensionBeans).setAggregations(metricBeans).setFilter(filter).build();
        }
    }

    @Override
    protected DimensionBean visitToDate(SwiftSqlParser.FuncExprContext funcExprContext) {
        DimensionBean dimensionBean = new DimensionBean(DimensionType.DETAIL_FORMULA, funcExprContext.simpleExpr(0).getText());
        List<SwiftSqlParser.SimpleExprContext> expers = funcExprContext.simpleExpr();
        String text = SwiftSqlParseUtil.trimQuote(expers.get(0).getText());
        if (expers.size() == 1) {
            try {
                long time = Long.parseLong(text);
                dimensionBean.setFormula(new ToDateFormulaBean(funcExprContext.getText(), time));
            } catch (Exception e) {
                dimensionBean.setFormula(new ToDateFormulaBean(funcExprContext.getText(), text));
            }
        } else {
            String format = SwiftSqlParseUtil.trimQuote(expers.get(1).getText(), SwiftSqlParseUtil.SINGLE_QUOTE);
            try {
                long time = Long.parseLong(text);
                dimensionBean.setFormula(new ToDateFormatFormulaBean(funcExprContext.getText(), time, format));
            } catch (Exception e) {
                dimensionBean.setFormula(new ToDateFormatFormulaBean(funcExprContext.getText(), text, format));
            }
        }
        dimensionBean.setAlias(funcExprContext.getText());
        return dimensionBean;
    }

    @Override
    protected void visitColumns(SwiftSqlParser.ColumnsContext columns,
                                List<DimensionBean> dimensionBeans,
                                List<AggregationBean> metricBeans) {
        ParseTree child = columns.getChild(0);
        if (child instanceof TerminalNode) {
            if (((TerminalNode) child).getSymbol().getType() == SwiftSqlParser.MUL) {
                dimensionBeans.add(new DimensionBean(DimensionType.DETAIL_ALL_COLUMN));
            }
        } else {
            super.visitColumns(columns, dimensionBeans, metricBeans);
        }
    }
}
