package com.fr.swift.cloud.jdbc.checker;

import com.fr.swift.cloud.jdbc.sql.SqlBean;
import com.fr.swift.cloud.jdbc.sql.SwiftPreparedStatement;

import java.sql.SQLException;

/**
 * check sql is available or not
 * @author yee
 * @date 2018/11/16
 */
public interface GrammarChecker {
    /**
     * check sql is available or not
     *
     * @param sql         sql which is ready to check grammar
     * @param paramValues sql parameters if the statement is prepared
     * @return sql request info
     * @throws SQLException the method would throw SQLException when
     *                      (1) prepared sql contains NullValue.INSTANCE
     *                      (2) paramValues are not match prepared sql's parameters
     * @see SwiftPreparedStatement.NullValue
     */
    SqlBean check(String sql, Object... paramValues) throws SQLException;
}
