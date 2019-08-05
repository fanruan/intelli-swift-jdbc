package com.fr.swift.jdbc.visitor.select;

import com.fr.swift.jdbc.antlr4.SwiftSqlParser;
import com.fr.swift.query.info.bean.element.AggregationBean;
import com.fr.swift.query.info.bean.element.DimensionBean;
import com.fr.swift.query.info.bean.element.filter.FilterInfoBean;
import com.fr.swift.query.info.bean.query.GroupQueryInfoBean;
import com.fr.swift.query.info.bean.type.DimensionType;
import org.antlr.v4.runtime.tree.ErrorNodeImpl;
import org.antlr.v4.runtime.tree.RuleNode;

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

    public GroupByVisitor(String tableName, FilterInfoBean filterInfoBean, SwiftSqlParser.ColumnsContext columns) {
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
                dimensionBeans.add(new DimensionBean(DimensionType.GROUP, nameContext.getText()));
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
        visitErrorNode(new ErrorNodeImpl(ctx.start));
        return null;
    }
}
