package com.fr.swift.jdbc.visitor.select;

import com.fr.swift.exception.meta.SwiftMetaDataException;
import com.fr.swift.jdbc.antlr4.SwiftSqlParseUtil;
import com.fr.swift.jdbc.antlr4.SwiftSqlParser;
import com.fr.swift.query.aggregator.AggregatorType;
import com.fr.swift.query.info.bean.element.AggregationBean;
import com.fr.swift.query.info.bean.element.DimensionBean;
import com.fr.swift.query.info.bean.element.MetricBean;
import com.fr.swift.query.info.bean.element.filter.FilterInfoBean;
import com.fr.swift.query.info.bean.query.GroupQueryInfoBean;
import com.fr.swift.query.info.bean.type.DimensionType;
import com.fr.swift.source.ColumnTypeConstants;
import com.fr.swift.source.ColumnTypeUtils;
import com.fr.swift.source.SwiftMetaData;
import com.fr.swift.source.SwiftMetaDataColumn;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNodeImpl;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yee
 * @date 2019-07-25
 */
public class GroupByVisitor extends BaseQueryBeanVisitor<GroupQueryInfoBean> {
    private String tableName;
    private FilterInfoBean filterInfoBean;
    private SwiftSqlParser.ColumnsContext columns;
    private SwiftMetaData metaData;

    public GroupByVisitor(String tableName, SwiftMetaData metaData,
                          FilterInfoBean filterInfoBean, SwiftSqlParser.ColumnsContext columns) {
        this.metaData = metaData;
        this.tableName = tableName;
        this.filterInfoBean = filterInfoBean;
        this.columns = columns;
    }

    @Override
    public GroupQueryInfoBean visitChildren(RuleNode node) {
        SwiftSqlParser.SelectContext ctx = (SwiftSqlParser.SelectContext) node;
        List<DimensionBean> dimensionBeans = new ArrayList<>();
        if (null != ctx.groupBy) {
            for (SwiftSqlParser.NameContext nameContext : ctx.groupBy.name()) {
                final DimensionBean dimension = new DimensionBean(DimensionType.GROUP, SwiftSqlParseUtil.trimQuote(nameContext.getText()));
                dimension.setAlias(rename(nameMap, dimension.getColumn()));
                dimensionBeans.add(dimension);
            }
        }
        List<AggregationBean> metricBeans = new ArrayList<>();
        visitColumns(columns, dimensionBeans, metricBeans);
        return GroupQueryInfoBean.builder(tableName)
                .setDimensions(dimensionBeans)
                .setAggregations(metricBeans)
                .setFilter(filterInfoBean).build();
    }

    @Override
    protected DimensionBean visitToDate(SwiftSqlParser.FuncExprContext ctx) {
        return null;
    }

    @Override
    protected void visitColumns(SwiftSqlParser.ColumnsContext columns, List<DimensionBean> dimensionBeans, List<AggregationBean> metricBeans) {
        int childCount = columns.getChildCount();
        AggregationBean aliasAggregation = null;
        if (columns.getChild(0) instanceof TerminalNode) {
            if (((TerminalNode) columns.getChild(0)).getSymbol().getType() == SwiftSqlParser.MUL) {
                try {
                    for (int i = 0; i < metaData.getColumnCount(); i++) {
                        final SwiftMetaDataColumn column = metaData.getColumn(i + 1);
                        if (!nameMap.containsKey(column.getName())) {
                            metricBeans.add(getAggregationBean(column.getName(), columns.start));
                        }
                    }
                } catch (Exception e) {
                    visitErrorNode(new ErrorNodeImpl(columns.start));
                }
            }
        } else {
            for (int i = 0; i < childCount; i++) {
                ParseTree child = columns.getChild(i);
                if (child instanceof SwiftSqlParser.SimpleExprContext) {
                    SwiftSqlParser.FuncExprContext funcExprContext = ((SwiftSqlParser.SimpleExprContext) child).funcExpr();
                    if (funcExprContext != null) {
                        if (funcExprContext.start.getType() == SwiftSqlParser.TODATE) {
                            visitErrorNode(new ErrorNodeImpl(funcExprContext.start));
                        } else {
                            metricBeans.add(funcExprContext.accept(new FunctionVisitor()));
                        }
                    } else {
                        int type = ((SwiftSqlParser.SimpleExprContext) child).start.getType();
                        switch (type) {
                            case SwiftSqlParser.NUMERIC_LITERAL:
                            case SwiftSqlParser.STRING_LITERAL:
                                // TODO 单值不支持直接
                                visitErrorNode(new ErrorNodeImpl(((SwiftSqlParser.SimpleExprContext) child).start));
                                break;
                            default:
                                String alias = SwiftSqlParseUtil.trimQuote(child.getText());
                                AggregationBean aggregationBean = getAggregationBean(alias, ((SwiftSqlParser.SimpleExprContext) child).start);
                                if (null != aggregationBean) {
                                    metricBeans.add(aggregationBean);
                                }
                        }
                    }
                } else if (child instanceof TerminalNode) {
                    if (((TerminalNode) child).getSymbol().getType() == SwiftSqlParser.AS) {
                        aliasAggregation = metricBeans.get(metricBeans.size() - 1);
                    }
                } else if (child instanceof SwiftSqlParser.NameContext) {
                    if (null != aliasAggregation) {
                        String alias = SwiftSqlParseUtil.trimQuote(child.getText());
                        aliasAggregation.setAlias(rename(nameMap, alias));
                        aliasAggregation = null;
                    }
                }
            }
        }
    }

    private AggregationBean getAggregationBean(String column, Token token) {
        try {
            final SwiftMetaDataColumn metaDataColumn = metaData.getColumn(column);
            AggregationBean bean = null;
            if (ColumnTypeUtils.getClassType(metaDataColumn) == ColumnTypeConstants.ClassType.STRING) {
                bean = MetricBean.builder(column, AggregatorType.DISTINCT).build();
                bean.setAlias(rename(nameMap, column));
            } else {
                bean = MetricBean.builder(column, AggregatorType.COUNT).build();
                bean.setAlias(rename(nameMap, "COUNT(" + column + ")"));
            }
            return bean;
        } catch (SwiftMetaDataException e) {
            visitErrorNode(new ErrorNodeImpl(token));
        }
        return null;
    }
}
