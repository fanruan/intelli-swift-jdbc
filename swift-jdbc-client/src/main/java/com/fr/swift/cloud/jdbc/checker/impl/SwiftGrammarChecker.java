package com.fr.swift.cloud.jdbc.checker.impl;

import com.fr.swift.cloud.jdbc.antlr4.SwiftSqlParseUtil;
import com.fr.swift.cloud.jdbc.checker.GrammarChecker;
import com.fr.swift.cloud.jdbc.exception.Exceptions;
import com.fr.swift.cloud.jdbc.sql.SqlBean;
import com.fr.swift.cloud.jdbc.sql.SwiftPreparedStatement;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yee
 * @date 2018-12-03
 */
public class SwiftGrammarChecker implements GrammarChecker {
    private static final Pattern COLUMN_QUERY_PATTERN = Pattern.compile("where 1 = 2", Pattern.CASE_INSENSITIVE);

    @Override
    public SqlBean check(String sql, Object... paramValues) throws SQLException {
        Matcher columnMatcher = COLUMN_QUERY_PATTERN.matcher(sql);
        while (columnMatcher.find()) {
            throw new SQLException(String.format("[%s] not supported!", sql));
        }

        Matcher matcher = SwiftPreparedStatement.VALUE_POS_PATTERN.matcher(sql);

        int paramCount = 0;
        while (matcher.find()) {
            paramCount++;
        }
        if (paramCount > 0) {
            sql = getRealSql(sql, Arrays.asList(paramValues), paramCount);
        }
        try {
            return new SqlBean(sql, SwiftSqlParseUtil.isSelect(sql));
        } catch (Exception e) {
            throw Exceptions.sqlIncorrect(sql, e);
        }
    }

    private String getRealSql(String sql, List values, int size) throws SQLException {
        if (values.size() != size) {
            throw Exceptions.sql(String.format("Expect parameter count is %d but get %d", size, values.size()));
        }
        if (values.contains(SwiftPreparedStatement.NullValue.INSTANCE)) {
            throw Exceptions.sql(String.format("Parameter index %d must be set.", values.indexOf(SwiftPreparedStatement.NullValue.INSTANCE) + 1));
        }
        String tmp = sql.trim();
        for (final Object value : values) {
            String valueStr = null;
            if (value instanceof String) {
                valueStr = "'" + value + "'";
            } else if (value instanceof Date) {
                valueStr = String.valueOf(((Date) value).getTime());
            } else {
                valueStr = value.toString();
            }
            tmp = tmp.replaceFirst(SwiftPreparedStatement.VALUE_POS_PATTERN.pattern(), valueStr);
        }
        return tmp;
    }
}
