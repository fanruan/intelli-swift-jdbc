package com.fr.swift.jdbc.listener;

import com.fr.swift.SwiftContext;
import com.fr.swift.config.service.SwiftMetaDataService;
import com.fr.swift.jdbc.adaptor.SelectionBeanParser;
import com.fr.swift.jdbc.adaptor.bean.SelectionBean;
import com.fr.swift.jdbc.antlr4.SwiftSqlParseUtil;
import com.fr.swift.jdbc.antlr4.SwiftSqlParser;
import com.fr.swift.jdbc.antlr4.SwiftSqlParserBaseListener;
import com.fr.swift.jdbc.visitor.select.GroupByVisitor;
import com.fr.swift.jdbc.visitor.select.NoGroupByVisitor;
import com.fr.swift.jdbc.visitor.select.OrderByVisitor;
import com.fr.swift.jdbc.visitor.where.BoolExprVisitor;
import com.fr.swift.query.filter.SwiftDetailFilterType;
import com.fr.swift.query.info.bean.element.AggregationBean;
import com.fr.swift.query.info.bean.element.DimensionBean;
import com.fr.swift.query.info.bean.element.LimitBean;
import com.fr.swift.query.info.bean.element.filter.FilterInfoBean;
import com.fr.swift.query.info.bean.element.filter.impl.ComplexFilterInfoBean;
import com.fr.swift.query.info.bean.element.filter.impl.DetailFilterInfoBean;
import com.fr.swift.query.info.bean.element.filter.impl.NotFilterBean;
import com.fr.swift.query.info.bean.post.HavingFilterQueryInfoBean;
import com.fr.swift.query.info.bean.post.PostQueryInfoBean;
import com.fr.swift.query.info.bean.query.DetailQueryInfoBean;
import com.fr.swift.query.info.bean.query.GroupQueryInfoBean;
import com.fr.swift.query.info.bean.query.QueryInfoBean;
import com.fr.swift.source.SourceKey;
import com.fr.swift.source.SwiftMetaData;
import com.fr.swift.util.Strings;
import org.antlr.v4.runtime.tree.ErrorNodeImpl;
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
    private SwiftMetaDataService metaDataService = SwiftContext.get().getBean(SwiftMetaDataService.class);

    @Override
    public void enterSelect(SwiftSqlParser.SelectContext ctx) {
        if (ctx.subQuery == null) {
            // distinct 只能跟在select 后面   第0号是select  第1号是distinct
            ParseTree child = ctx.getChild(1);
            boolean distinct = child instanceof TerminalNode && ((TerminalNode) child).getSymbol().getType() == SwiftSqlParser.DISTINCT;
            String tableName = SwiftSqlParseUtil.trimQuote(ctx.table.getText());
            SwiftMetaData metaData = metaDataService.getMeta(new SourceKey(tableName));
            if (null == metaData) {
                visitErrorNode(new ErrorNodeImpl(ctx.table.start));
            }
            FilterInfoBean filter = null;
            if (ctx.where != null) {
                List<FilterInfoBean> filters = new ArrayList<>();
                for (ParseTree c : ctx.where.children) {
                    FilterInfoBean accept = c.accept(new BoolExprVisitor());
                    if (null != accept) {
                        filters.add(accept);
                    }
                }
                filter = filters.size() == 1 ? filters.get(0) : ComplexFilterInfoBean.and(filters);
            }
            LimitBean limit = null;
            if (ctx.limit != null) {
                String text = ctx.limit.getText();
                limit = new LimitBean(Integer.valueOf(text));

            }
            bean = null != ctx.groupBy || distinct ? ctx.accept(new GroupByVisitor(tableName, metaData, filter, ctx.columns(), limit))
                    : ctx.accept(new NoGroupByVisitor(tableName, filter, ctx.columns(), limit));
        }
    }

    @Override
    public void exitSelect(SwiftSqlParser.SelectContext ctx) {
        if (ctx.subQuery != null && ctx.where != null && !(bean instanceof DetailQueryInfoBean)) {
            List<FilterInfoBean> filters = new ArrayList<>();
            for (ParseTree c : ctx.where.children) {
                FilterInfoBean accept = c.accept(new BoolExprVisitor());
                if (null != accept) {
                    filters.add(accept);
                }
            }
            FilterInfoBean filter = filters.size() == 1 ? filters.get(0) : ComplexFilterInfoBean.and(filters);
            GroupQueryInfoBean groupBean = (GroupQueryInfoBean) bean;
            List<DimensionBean> dimensions = groupBean.getDimensions();
            List<AggregationBean> aggregations = groupBean.getAggregations();
            List<PostQueryInfoBean> postBean = new ArrayList<>();
            Map<String, FilterInfoBean> map = calFilterForPost(filter, dimensions, aggregations);
            for (Map.Entry<String, FilterInfoBean> entry : map.entrySet()) {
                postBean.add(new HavingFilterQueryInfoBean(entry.getKey(), entry.getValue()));
            }
            if (!postBean.isEmpty()) {
                groupBean.setPostAggregations(postBean);
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
        SwiftDetailFilterType type = filterInfoBean.getType();
        switch (type) {
            case OR:
            case AND:
                List<FilterInfoBean> filterValue = ((ComplexFilterInfoBean) filterInfoBean).getFilterValue();
                for (FilterInfoBean infoBean : filterValue) {
                    Map<String, FilterInfoBean> map = calFilterForPost(infoBean, dimensions, aggregations);
                    for (Map.Entry<String, FilterInfoBean> entry : map.entrySet()) {
                        if (result.containsKey(entry.getKey())) {
                            if (result.get(entry.getKey()) instanceof ComplexFilterInfoBean) {
                                ((ComplexFilterInfoBean) result.get(entry.getKey())).getFilterValue().add(entry.getValue());
                            } else {
                                List<FilterInfoBean> filters = new ArrayList<>();
                                filters.add(result.get(entry.getKey()));
                                filters.add(entry.getValue());
                                result.put(entry.getKey(), type == SwiftDetailFilterType.AND ? ComplexFilterInfoBean.and(filters) : ComplexFilterInfoBean.or(filters));
                            }
                        } else {
                            result.put(entry.getKey(), entry.getValue());
                        }
                    }
                }
                return result;
            case NOT:
                FilterInfoBean filter = (FilterInfoBean) filterInfoBean.getFilterValue();
                Map<String, FilterInfoBean> filters = calFilterForPost(filter, dimensions, aggregations);
                for (Map.Entry<String, FilterInfoBean> entry : filters.entrySet()) {
                    result.put(entry.getKey(), new NotFilterBean(entry.getValue()));
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

    @Override
    public void enterOrderBy(SwiftSqlParser.OrderByContext ctx) {
        bean.setSorts(ctx.accept(new OrderByVisitor()));
    }

    @Override
    public SelectionBean getSelectionBean() {
        return null != bean ? new SelectionBean(Strings.EMPTY, bean.getTableName(), bean) : null;
    }
}
