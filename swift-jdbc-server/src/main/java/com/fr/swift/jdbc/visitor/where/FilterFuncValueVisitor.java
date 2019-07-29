package com.fr.swift.jdbc.visitor.where;

import com.fr.swift.jdbc.antlr4.SwiftSqlParseUtil;
import com.fr.swift.jdbc.antlr4.SwiftSqlParser;
import com.fr.swift.jdbc.visitor.BaseVisitor;
import com.fr.swift.structure.Pair;
import com.fr.swift.util.function.Function;
import org.antlr.v4.runtime.tree.ErrorNodeImpl;
import org.antlr.v4.runtime.tree.RuleNode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yee
 * @date 2019-07-26
 */
class FilterFuncValueVisitor extends BaseVisitor {
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
                String value = SwiftSqlParseUtil.trimQuote(expers.get(0).getText(), SwiftSqlParseUtil.SINGLE_QUOTE);
                SimpleDateFormat sdf = null;
                String format;
                if (expers.size() > 1) {
                    format = SwiftSqlParseUtil.trimQuote(expers.get(1).getText(), SwiftSqlParseUtil.SINGLE_QUOTE);
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
                return SwiftSqlParseUtil.trimQuote(node.getText(), SwiftSqlParseUtil.SINGLE_QUOTE);
        }
    }
}
