package com.fr.swift.jdbc.visitor.where;

import com.fr.swift.jdbc.antlr4.SwiftSqlParser;
import com.fr.swift.jdbc.creator.FilterBeanCreator;
import com.fr.swift.jdbc.visitor.BaseVisitor;
import com.fr.swift.query.filter.SwiftDetailFilterType;
import com.fr.swift.query.info.bean.element.filter.FilterInfoBean;
import com.fr.swift.query.info.bean.element.filter.impl.InFilterBean;
import com.fr.swift.query.info.bean.element.filter.impl.NotFilterBean;
import com.fr.swift.query.info.bean.element.filter.impl.NumberInRangeFilterBean;
import com.fr.swift.query.info.bean.element.filter.impl.StringOneValueFilterBean;
import com.fr.swift.structure.Pair;
import org.antlr.v4.runtime.tree.RuleNode;

/**
 * @author yee
 * @date 2019-07-19
 */
public class BoolOpVisitor extends BaseVisitor<FilterBeanCreator<FilterInfoBean>> {
    private static final String LIKE = "%";

    @Override
    public FilterBeanCreator<FilterInfoBean> visitChildren(RuleNode node) {
        if (node instanceof SwiftSqlParser.BoolOpContext) {
            final int type = ((SwiftSqlParser.BoolOpContext) node).getStart().getType();
            switch (type) {
                case SwiftSqlParser.EQ:
                    return new FilterBeanCreator<FilterInfoBean>() {
                        @Override
                        public FilterInfoBean create(String column, Object value) {
                            if (value instanceof Pair) {
                                return NumberInRangeFilterBean.builder(column)
                                        .setStart(((Pair) value).getKey().toString(), false)
                                        .setEnd(((Pair) value).getValue().toString(), false).build();
                            }
                            return new InFilterBean(column, value);
                        }
                    };
                case SwiftSqlParser.NEQ:
                    return new FilterBeanCreator<FilterInfoBean>() {
                        @Override
                        public FilterInfoBean create(String column, Object value) {
                            FilterInfoBean bean = null;
                            if (value instanceof Pair) {
                                bean = NumberInRangeFilterBean.builder(column)
                                        .setStart(((Pair) value).getKey().toString(), false)
                                        .setEnd(((Pair) value).getValue().toString(), false).build();
                            } else {
                                bean = new InFilterBean(column, value);
                            }
                            return new NotFilterBean(bean);
                        }
                    };
                case SwiftSqlParser.GREATER:
                    return new FilterBeanCreator<FilterInfoBean>() {
                        @Override
                        public FilterInfoBean create(String column, Object value) {
                            return NumberInRangeFilterBean.builder(column).setStart(value.toString(), false).build();
                        }
                    };
                case SwiftSqlParser.GEQ:
                    return new FilterBeanCreator<FilterInfoBean>() {
                        @Override
                        public FilterInfoBean create(String column, Object value) {
                            return NumberInRangeFilterBean.builder(column).setStart(value.toString(), true).build();
                        }
                    };
                case SwiftSqlParser.LESS:
                    return new FilterBeanCreator<FilterInfoBean>() {
                        @Override
                        public FilterInfoBean create(String column, Object value) {
                            return NumberInRangeFilterBean.builder(column).setEnd(value.toString(), false).build();
                        }
                    };
                case SwiftSqlParser.LEQ:
                    return new FilterBeanCreator<FilterInfoBean>() {
                        @Override
                        public FilterInfoBean create(String column, Object value) {
                            return NumberInRangeFilterBean.builder(column).setEnd(value.toString(), true).build();
                        }
                    };
                case SwiftSqlParser.LIKE:
                    return new FilterBeanCreator<FilterInfoBean>() {
                        @Override
                        public FilterInfoBean create(String column, Object value) {
                            String s = value.toString();
                            SwiftDetailFilterType filterType = null;
                            if (s.startsWith(LIKE) && s.startsWith(LIKE)) {
                                filterType = SwiftDetailFilterType.STRING_LIKE;
                                s = s.substring(1, s.length() - 1);
                            } else if (s.startsWith(LIKE)) {
                                filterType = SwiftDetailFilterType.STRING_ENDS_WITH;
                                s = s.substring(1);
                            } else if (s.endsWith(LIKE)) {
                                filterType = SwiftDetailFilterType.STRING_STARTS_WITH;
                                s = s.substring(0, s.length() - 1);
                            } else {
                                // do nothing
                            }
                            return new StringOneValueFilterBean(column, filterType, s);
                        }
                    };
                default:
            }
        }
        return null;
    }
}
