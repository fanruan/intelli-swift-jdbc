package com.fr.swift.jdbc.visitor.select;

import com.fr.swift.jdbc.antlr4.SwiftSqlParser;
import com.fr.swift.jdbc.visitor.BaseVisitor;
import com.fr.swift.query.info.bean.element.AggregationBean;
import com.fr.swift.query.info.bean.element.DimensionBean;
import com.fr.swift.query.info.bean.query.QueryInfoBean;
import com.fr.swift.query.info.bean.type.DimensionType;
import org.antlr.v4.runtime.tree.ErrorNodeImpl;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yee
 * @date 2019-07-25
 */
public abstract class BaseQueryBeanVisitor<T extends QueryInfoBean> extends BaseVisitor<T> {
    @Override
    public abstract T visitChildren(RuleNode node);

    protected abstract DimensionBean visitToDate(SwiftSqlParser.FuncExprContext ctx);


    protected void visitColumns(SwiftSqlParser.ColumnsContext columns,
                                List<DimensionBean> dimensionBeans,
                                List<AggregationBean> metricBeans) {
        int childCount = columns.getChildCount();
        boolean dimension = true;
        DimensionBean aliasDimension = null;
        AggregationBean aliasAggregation = null;
        Map<String, Integer> nameMap = new HashMap<>();
        for (int i = 0; i < childCount; i++) {
            ParseTree child = columns.getChild(i);
            if (child instanceof SwiftSqlParser.SimpleExprContext) {
                SwiftSqlParser.FuncExprContext funcExprContext = ((SwiftSqlParser.SimpleExprContext) child).funcExpr();
                if (funcExprContext != null) {
                    if (funcExprContext.start.getType() == SwiftSqlParser.TODATE) {
                        dimensionBeans.add(visitToDate(funcExprContext));
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
                            // TODO 单值不支持直接
                            visitErrorNode(new ErrorNodeImpl(((SwiftSqlParser.SimpleExprContext) child).start));
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
}
