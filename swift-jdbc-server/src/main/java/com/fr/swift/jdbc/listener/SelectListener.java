package com.fr.swift.jdbc.listener;

import com.fr.swift.jdbc.adaptor.SelectionBeanParser;
import com.fr.swift.jdbc.adaptor.bean.SelectionBean;
import com.fr.swift.jdbc.antlr4.SwiftSqlParseUtil;
import com.fr.swift.jdbc.antlr4.SwiftSqlParser;
import com.fr.swift.jdbc.antlr4.SwiftSqlParserBaseListener;
import com.fr.swift.jdbc.visitor.FunctionVisitor;
import com.fr.swift.jdbc.visitor.OrderByVisitor;
import com.fr.swift.jdbc.visitor.WhereVisitor;
import com.fr.swift.query.info.bean.element.AggregationBean;
import com.fr.swift.query.info.bean.element.DimensionBean;
import com.fr.swift.query.info.bean.element.ToDateFormatFormulaBean;
import com.fr.swift.query.info.bean.element.ToDateFormulaBean;
import com.fr.swift.query.info.bean.element.filter.FilterInfoBean;
import com.fr.swift.query.info.bean.element.filter.impl.AndFilterBean;
import com.fr.swift.query.info.bean.element.filter.impl.DetailFilterInfoBean;
import com.fr.swift.query.info.bean.element.filter.impl.OrFilterBean;
import com.fr.swift.query.info.bean.post.HavingFilterQueryInfoBean;
import com.fr.swift.query.info.bean.post.PostQueryInfoBean;
import com.fr.swift.query.info.bean.query.DetailQueryInfoBean;
import com.fr.swift.query.info.bean.query.GroupQueryInfoBean;
import com.fr.swift.query.info.bean.query.QueryInfoBean;
import com.fr.swift.query.info.bean.type.DimensionType;
import com.fr.swift.util.Strings;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yee
 * @date 2019-07-19
 */
public class SelectListener extends SwiftSqlParserBaseListener implements SelectionBeanParser {
    private QueryInfoBean bean;

    @Override
    public void enterSelect(SwiftSqlParser.SelectContext ctx) {
        if (ctx.subQuery == null) {
            // distinct 只能跟在select 后面   第0号是select  第1号是distinct
            ParseTree child = ctx.getChild(1);
            boolean distinct = child instanceof TerminalNode && ((TerminalNode) child).getSymbol().getType() == SwiftSqlParser.DISTINCT;
            SwiftSqlParser.NameContext table = ctx.table;
            String tableName = table.getText();
            SwiftSqlParser.NamesContext groupBy = ctx.groupBy;
            FilterInfoBean filter = null;
            if (ctx.where != null) {
                filter = ctx.where.accept(new WhereVisitor());
            }

            if (null != groupBy) {
                List<DimensionBean> dimensionBeans = new ArrayList<>();
                for (SwiftSqlParser.NameContext nameContext : groupBy.name()) {
                    dimensionBeans.add(new DimensionBean(DimensionType.GROUP, nameContext.getText()));
                }
                ArrayList<AggregationBean> metricBeans = new ArrayList<>();
                visitColumns(ctx.columns(), dimensionBeans, metricBeans);
                bean = GroupQueryInfoBean.builder(tableName)
                        .setDimensions(dimensionBeans)
                        .setAggregations(metricBeans)
                        .setFilter(filter).build();
            } else {
                SwiftSqlParser.ColumnsContext columns = ctx.columns();
                List<DimensionBean> dimensionBeans = new ArrayList<>();
                List<AggregationBean> metricBeans = new ArrayList<>();
                visitColumns(columns, dimensionBeans, metricBeans);

                if (metricBeans.isEmpty()) {
                    if (distinct) {
                        bean = GroupQueryInfoBean.builder(tableName).setDimensions(dimensionBeans).setFilter(filter).build();
                    } else {
                        if ((dimensionBeans.size() > 1 || dimensionBeans.get(0).getType() != DimensionType.DETAIL_ALL_COLUMN)) {
                            for (DimensionBean dimensionBean : dimensionBeans) {
                                if (dimensionBean.getType() == DimensionType.GROUP_FORMULA) {
                                    dimensionBean.setType(DimensionType.DETAIL_FORMULA);
                                } else {
                                    dimensionBean.setType(DimensionType.DETAIL);
                                }
                            }
                        }
                        bean = DetailQueryInfoBean.builder(tableName).setDimensions(dimensionBeans).setFilter(filter).build();
                    }
                } else {
                    bean = GroupQueryInfoBean.builder(tableName).setDimensions(dimensionBeans).setAggregations(metricBeans).setFilter(filter).build();
                }
            }
        }

    }

    @Override
    public void exitSelect(SwiftSqlParser.SelectContext ctx) {
        if (ctx.subQuery != null) {
            if (ctx.where != null) {
                if (bean instanceof DetailQueryInfoBean) {
                    // 不支持对detail表post
                    return;
                }
                GroupQueryInfoBean groupBean = (GroupQueryInfoBean) bean;
                List<DimensionBean> dimensions = groupBean.getDimensions();
                List<AggregationBean> aggregations = groupBean.getAggregations();
                List<PostQueryInfoBean> postBean = new ArrayList<>();
                FilterInfoBean filter = ctx.where.accept(new WhereVisitor());
                Map<String, FilterInfoBean> map = calFilterForPost(filter, dimensions, aggregations);
                for (Map.Entry<String, FilterInfoBean> entry : map.entrySet()) {
                    postBean.add(new HavingFilterQueryInfoBean(entry.getKey(), entry.getValue()));
                }
                if (!postBean.isEmpty()) {
                    groupBean.setPostAggregations(postBean);
                }
            }
        }
    }

    /**
     * 封装子查询postquery
     *
     * @param filterInfoBean column是转译名的filter
     * @param dimensions     分组字段
     * @param aggregations   聚合字段
     * @return key: aggregation的转译名，value： filterInfoBean column改成字段名的filter
     */
    private Map<String, FilterInfoBean> calFilterForPost(FilterInfoBean filterInfoBean, List<DimensionBean> dimensions, List<AggregationBean> aggregations) {
        Map<String, FilterInfoBean> result = new HashMap<>();
        switch (filterInfoBean.getType()) {
            case OR:
                for (FilterInfoBean infoBean : ((OrFilterBean) filterInfoBean).getFilterValue()) {
                    Map<String, FilterInfoBean> map = calFilterForPost(infoBean, dimensions, aggregations);
                    for (Map.Entry<String, FilterInfoBean> entry : map.entrySet()) {
                        if (result.containsKey(entry.getKey())) {
                            if (result.get(entry.getKey()) instanceof OrFilterBean) {
                                ((OrFilterBean) result.get(entry.getKey())).getFilterValue().add(entry.getValue());
                            } else {
                                List<FilterInfoBean> filters = new ArrayList<>();
                                filters.add(result.get(entry.getKey()));
                                filters.add(entry.getValue());
                                result.put(entry.getKey(), new OrFilterBean(filters));
                            }
                        } else {
                            result.put(entry.getKey(), entry.getValue());
                        }
                    }
                }
                return result;
            case AND:
                for (FilterInfoBean infoBean : ((AndFilterBean) filterInfoBean).getFilterValue()) {
                    Map<String, FilterInfoBean> map = calFilterForPost(infoBean, dimensions, aggregations);
                    for (Map.Entry<String, FilterInfoBean> entry : map.entrySet()) {
                        if (result.containsKey(entry.getKey())) {
                            if (result.get(entry.getKey()) instanceof AndFilterBean) {
                                ((AndFilterBean) result.get(entry.getKey())).getFilterValue().add(entry.getValue());
                            } else {
                                List<FilterInfoBean> filters = new ArrayList<>();
                                filters.add(result.get(entry.getKey()));
                                filters.add(entry.getValue());
                                result.put(entry.getKey(), new AndFilterBean(filters));
                            }
                        } else {
                            result.put(entry.getKey(), entry.getValue());
                        }
                    }
                }
                return result;
            default:
                String column = ((DetailFilterInfoBean) filterInfoBean).getColumn();
                for (AggregationBean aggregation : aggregations) {
                    if (column.equals(aggregation.getAlias())) {
                        ((DetailFilterInfoBean) filterInfoBean).setColumn(aggregation.getColumn());
                        return Collections.singletonMap(aggregation.getAlias(), filterInfoBean);
                    }
                }
                for (DimensionBean dimension : dimensions) {
                    if (column.equals(dimension.getAlias())) {
                        ((DetailFilterInfoBean) filterInfoBean).setColumn(dimension.getColumn());
                        break;
                    }
                }
                for (AggregationBean aggregation : aggregations) {
                    result.put(aggregation.getAlias(), filterInfoBean);
                }
                return result;
        }
    }

    private void visitColumns(SwiftSqlParser.ColumnsContext columns,
                              List<DimensionBean> dimensionBeans,
                              List<AggregationBean> metricBeans) {
        int childCount = columns.getChildCount();
        boolean dimension = true;
        DimensionBean aliasDimension = null;
        AggregationBean aliasAggregation = null;
        Map<String, Integer> nameMap = new HashMap<>();
        for (int i = 0; i < childCount; i++) {
            ParseTree child = columns.getChild(i);
            if (child instanceof TerminalNode) {
                if (((TerminalNode) child).getSymbol().getType() == SwiftSqlParser.MUL) {
                    dimensionBeans.add(new DimensionBean(DimensionType.DETAIL_ALL_COLUMN));
                    break;
                }
            }
            if (child instanceof SwiftSqlParser.SimpleExprContext) {
                SwiftSqlParser.FuncExprContext funcExprContext = ((SwiftSqlParser.SimpleExprContext) child).funcExpr();
                if (funcExprContext != null) {
                    if (funcExprContext.start.getType() == SwiftSqlParser.TODATE) {
                        DimensionBean dimensionBean = new DimensionBean(DimensionType.GROUP_FORMULA, funcExprContext.simpleExpr(0).getText());
                        List<SwiftSqlParser.SimpleExprContext> expers = funcExprContext.simpleExpr();
                        if (expers.size() == 1) {
                            try {
                                long time = Long.parseLong(expers.get(0).getText());
                                dimensionBean.setFormula(new ToDateFormulaBean(funcExprContext.getText(), time));
                            } catch (Exception e) {
                                dimensionBean.setFormula(new ToDateFormulaBean(funcExprContext.getText(), expers.get(0).getText()));
                            }
                        } else {
                            String format = SwiftSqlParseUtil.trimQuote(expers.get(1).getText(), "'");
                            try {
                                long time = Long.parseLong(expers.get(0).getText());
                                dimensionBean.setFormula(new ToDateFormatFormulaBean(funcExprContext.getText(), time, format));
                            } catch (Exception e) {
                                dimensionBean.setFormula(new ToDateFormatFormulaBean(funcExprContext.getText(), expers.get(0).getText(), format));
                            }
                        }
                        dimensionBean.setAlias(funcExprContext.getText());
                        dimensionBeans.add(dimensionBean);
                        dimension = true;
                    } else {
                        metricBeans.add(funcExprContext.accept(new FunctionVisitor()));
                        dimension = false;
                    }
                } else {
                    int type = ((SwiftSqlParser.SimpleExprContext) child).start.getType();
                    switch (type) {
                        case SwiftSqlParser.NUMERIC_LITERAL:
                        case SwiftSqlParser.STRING_LITERAL:
                            // TODO 2019/07/19 直接传简单值暂时不支持
                            break;
                        default:
                            String alias = child.getText();
                            dimensionBeans.add(new DimensionBean(DimensionType.GROUP, child.getText(), rename(nameMap, alias)));
                    }
                    dimension = true;
                }
            } else if (child instanceof TerminalNode) {
                if (((TerminalNode) child).getSymbol().getType() == SwiftSqlParser.AS) {
                    if (dimension) {
                        aliasDimension = dimensionBeans.get(dimensionBeans.size() - 1);
                    } else {
                        aliasAggregation = metricBeans.get(metricBeans.size() - 1);
                    }
                }
            } else if (child instanceof SwiftSqlParser.NameContext) {
                if (null != aliasAggregation) {
                    String alias = child.getText();
                    aliasAggregation.setAlias(rename(nameMap, alias));
                    aliasAggregation = null;
                }
                if (null != aliasDimension) {
                    String alias = child.getText();
                    aliasDimension.setAlias(rename(nameMap, alias));
                    aliasDimension = null;
                }
            }
        }
    }

    private String rename(Map<String, Integer> nameMap, String alias) {
        if (nameMap.containsKey(alias)) {
            int newCount = nameMap.get(alias) + 1;
            nameMap.put(alias, newCount);
            return alias + newCount;
        } else {
            nameMap.put(alias, 0);
            return alias;
        }
    }

    @Override
    public void enterOrderBy(SwiftSqlParser.OrderByContext ctx) {
        bean.setSorts(ctx.accept(new OrderByVisitor()));
    }

    @Override
    public SelectionBean getSelectionBean() {
        return null != bean ? new SelectionBean(Strings.EMPTY, bean.getTableName(), bean) : null;
    }
}
