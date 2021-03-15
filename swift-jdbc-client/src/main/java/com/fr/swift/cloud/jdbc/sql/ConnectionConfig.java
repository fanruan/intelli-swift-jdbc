package com.fr.swift.cloud.jdbc.sql;

import com.fr.swift.cloud.jdbc.checker.GrammarChecker;
import com.fr.swift.cloud.jdbc.rpc.JdbcExecutor;

import java.io.File;
import java.sql.SQLException;

/**
 * @author yee
 * @date 2018/11/16
 */
public interface ConnectionConfig {
    /**
     * return username
     *
     * @return
     */
    String swiftUser();

    /**
     * return password
     *
     * @return
     */
    String swiftPassword();

    /**
     * return selected database
     *
     * @return
     */
    String swiftDatabase();

    /**
     * return request sender
     *
     * @return
     */
    JdbcExecutor requestExecutor() throws SQLException;

    /**
     * kerberosPrincipal
     *
     * @return
     */
    String kerberosPrincipal();

    /**
     * kerberosKeytab
     *
     * @return
     */
    File kerberosKeytab();

    GrammarChecker grammarChecker();
}
