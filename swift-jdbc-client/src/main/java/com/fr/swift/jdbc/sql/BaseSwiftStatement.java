package com.fr.swift.jdbc.sql;

import com.fr.swift.jdbc.checker.GrammarChecker;
import com.fr.swift.jdbc.request.JdbcJsonBuilder;
import com.fr.swift.jdbc.rpc.JdbcExecutor;

import java.sql.SQLException;

/**
 * @author yee
 * @date 2018-12-03
 */
public abstract class BaseSwiftStatement implements SwiftStatement {
    protected BaseSwiftConnection connection;
    protected GrammarChecker grammarChecker;

    BaseSwiftStatement(BaseSwiftConnection connection) {
        this.connection = connection;
        this.grammarChecker = connection.getConfig().grammarChecker();
    }

    <T> T execute(String sql, String requestId, JdbcExecutor executor) throws SQLException {
        String newSql = JdbcJsonBuilder.buildSqlJson(sql, requestId, connection.getConfig().swiftDatabase(), connection.driver.holder.getAuthCode());
        return (T) connection.executeQueryInternal(newSql, executor);
    }

}
