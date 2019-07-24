package com.fr.swift.jdbc.visitor;

import com.fr.swift.jdbc.antlr4.SwiftSqlParseUtil;
import com.fr.swift.jdbc.antlr4.SwiftSqlParser;
import com.fr.swift.jdbc.creator.FilterBeanCreator;
import com.fr.swift.query.info.bean.element.filter.FilterInfoBean;
import com.fr.swift.query.info.bean.element.filter.impl.AndFilterBean;
import com.fr.swift.query.info.bean.element.filter.impl.OrFilterBean;
import com.fr.swift.structure.Pair;
import com.fr.swift.util.Strings;
import com.fr.swift.util.function.Function;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ErrorNodeImpl;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author yee
 * @date 2019-07-19
 */
public class BoolExprVisitor extends AbstractParseTreeVisitor<FilterInfoBean> {
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
        String columnName = null;
        Queue<FilterBeanCreator<FilterInfoBean>> queue = new LinkedBlockingQueue<>();
        Queue values = new LinkedBlockingQueue<>();
        for (ParseTree child : boolExpr.children) {
            if (child instanceof SwiftSqlParser.SimpleExprContext) {
                columnName = child.getText();
            } else if (child instanceof TerminalNode) {
                int type = ((TerminalNode) child).getSymbol().getType();
                if (type == SwiftSqlParser.NUMERIC_LITERAL || type == SwiftSqlParser.STRING_LITERAL) {
                    values.offer(child.getText());
                } else {
                    FilterBeanCreator<FilterInfoBean> accept = child.accept(new com.fr.swift.jdbc.visitor.BoolOpVisitor());
                    if (null != accept) {
                        queue.offer(accept);
                    }
                }
            } else if (child instanceof SwiftSqlParser.ValuesContext || child instanceof SwiftSqlParser.ValueContext) {
                values.offer(child.accept(new ValueExprVisitor()));
            } else {
                // do nothing
            }
        }
        FilterInfoBean result = null;
        while (queue.peek() != null) {
            FilterBeanCreator<FilterInfoBean> poll = queue.poll();
            // between and
            if (poll.type() == SwiftSqlParser.BETWEEN) {
                List filterValue = new ArrayList();
                filterValue.add(values.poll());
                filterValue.add(values.poll());
                return poll.create(columnName, filterValue);
            }
            // is not null   is null  in values
            if (null == result) {
                result = poll.create(columnName, values.peek() != null ? values.poll() : null);
            } else {
                result = poll.create(columnName, result);
            }
        }
        return result;
    }

    private FilterInfoBean visitBoolExprContext(SwiftSqlParser.BoolExprContext boolExpr) {
        SwiftSqlParser.KeywordBoolExprContext keywordBoolExprContext = boolExpr.keywordBoolExpr();
        if (null != keywordBoolExprContext) {
            return keywordBoolExprContext.accept(this);
        }
        List<SwiftSqlParser.BoolExprContext> boolExprs = boolExpr.boolExpr();
        if (boolExprs.isEmpty()) {
            String column = null;
            Object value = null;
            FilterBeanCreator<FilterInfoBean> creator = null;
            for (int i = 0; i < boolExpr.getChildCount(); i++) {
                ParseTree child = boolExpr.getChild(i);
                if (child instanceof SwiftSqlParser.SimpleExprContext) {
                    if (Strings.isEmpty(column)) {
                        column = child.getText();
                    } else {
                        SwiftSqlParser.FuncExprContext funcExprContext = ((SwiftSqlParser.SimpleExprContext) child).funcExpr();
                        if (null != funcExprContext) {
                            value = funcExprContext.accept(new FilterFuncValueVisitor(creator.type()));
                        } else {
                            value = SwiftSqlParseUtil.trimQuote(child.getText(), "'");
                        }
                    }
                } else if (child instanceof SwiftSqlParser.BoolOpContext) {
                    creator = child.accept(new com.fr.swift.jdbc.visitor.BoolOpVisitor());
                } else if (child instanceof SwiftSqlParser.ValuesContext || child instanceof SwiftSqlParser.ValueContext) {
                    value = child.accept(new ValueExprVisitor());
                } else {
                    // do nothing
                }
            }
            if (null == creator) {
                return visitErrorNode(new ErrorNodeImpl(boolExpr.getStart()));
            }
            return creator.create(column, value);
        } else {
            List<FilterInfoBean> list = new ArrayList<>();
            for (SwiftSqlParser.BoolExprContext boolExp : boolExprs) {
                list.add(boolExp.accept(this));
            }
            return boolExpr.logicOp().OR() != null ? new OrFilterBean(list) : new AndFilterBean(list);
        }
    }

    @Override
    public FilterInfoBean visitErrorNode(ErrorNode node) {
        throw new RuntimeException(node.getText());
    }

    /**
     * @author yee
     * @date 2019-07-19
     */
    public class ValueExprVisitor extends AbstractParseTreeVisitor<Set<String>> {
        @Override
        public Set<String> visitChildren(RuleNode node) {
            Set<String> set = new HashSet<>();
            if (node instanceof SwiftSqlParser.ValuesContext) {
                for (SwiftSqlParser.ValueContext valueContext : ((SwiftSqlParser.ValuesContext) node).value()) {
                    set.addAll(valueContext.accept(this));
                }
            } else if (node instanceof SwiftSqlParser.ValueContext) {
                if (((SwiftSqlParser.ValueContext) node).getStart().getType() == SwiftSqlParser.NUMERIC_LITERAL) {
                    return Collections.singleton(((SwiftSqlParser.ValueContext) node).NUMERIC_LITERAL().getText());
                }
                TerminalNode terminalNode = ((SwiftSqlParser.ValueContext) node).STRING_LITERAL();
                String value = SwiftSqlParseUtil.trimQuote(terminalNode.getText(), "'");
                return Collections.singleton(value);
            }
            return set;
        }
    }

    private class FilterFuncValueVisitor extends AbstractParseTreeVisitor {
        private Map<String, Integer> formatMap;
        private int filterType;
        private Function<Pair<Calendar, Integer>, Long> calDate = new Function<Pair<Calendar, Integer>, Long>() {
            @Override
            public Long apply(Pair<Calendar, Integer> p) {
                Integer calendarField = p.getValue();
                Calendar calendar = p.getKey();
                calendar.add(calendarField, 1);
                return calendar.getTimeInMillis();
            }
        };

        public FilterFuncValueVisitor(int filterType) {
            this.filterType = filterType;
            this.formatMap = new HashMap<>();
            this.formatMap.put("yyyy", Calendar.YEAR);
            this.formatMap.put("yyyy-MM", Calendar.MONTH);
            this.formatMap.put("yyyy-MM-dd", Calendar.DAY_OF_YEAR);
            this.formatMap.put("yyyy-MM-dd HH", Calendar.HOUR);
            this.formatMap.put("yyyy-MM-dd HH:mm", Calendar.MINUTE);
            this.formatMap.put("yyyy-MM-dd HH:mm:ss", Calendar.SECOND);
        }

        @Override
        public Object visitChildren(RuleNode node) {
            SwiftSqlParser.FuncExprContext funcExprContext = (SwiftSqlParser.FuncExprContext) node;
            switch (funcExprContext.start.getType()) {
                case SwiftSqlParser.TODATE:
                    List<SwiftSqlParser.SimpleExprContext> expers = funcExprContext.simpleExpr();
                    String value = SwiftSqlParseUtil.trimQuote(expers.get(0).getText(), "'");
                    SimpleDateFormat sdf = null;
                    String format;
                    if (expers.size() > 1) {
                        format = SwiftSqlParseUtil.trimQuote(expers.get(1).getText(), "'");
                        if (!this.formatMap.containsKey(format)) {
                            visitErrorNode(new ErrorNodeImpl(expers.get(1).start));
                        }
                    } else {
                        format = "yyyy-MM-dd HH:mm:ss";
                    }
                    sdf = new SimpleDateFormat(format);
                    try {
                        long time = sdf.parse(value).getTime();
                        Calendar instance = Calendar.getInstance();
                        instance.setTimeInMillis(time);

                        long maxTime = calDate.apply(Pair.of(instance, formatMap.get(format)));
                        switch (filterType) {
                            case SwiftSqlParser.EQ:
                            case SwiftSqlParser.NEQ:
                            case SwiftSqlParser.IN:
                                return Pair.of(time, maxTime);
                            case SwiftSqlParser.GREATER:
                                return maxTime - 1;
                            default:
                                return time;
                        }
                    } catch (ParseException e) {
                        visitErrorNode(new ErrorNodeImpl(expers.get(0).start));
                    }
                default:
                    // TODO 其他方法的过滤暂时不支持
                    return SwiftSqlParseUtil.trimQuote(node.getText(), "'");
            }
        }
    }
}
