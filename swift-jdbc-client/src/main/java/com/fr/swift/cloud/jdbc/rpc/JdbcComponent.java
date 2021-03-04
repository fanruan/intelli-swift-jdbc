package com.fr.swift.cloud.jdbc.rpc;

import java.sql.SQLException;

/**
 * @author yee
 * @date 2018/9/6
 */
public interface JdbcComponent {
    /**
     * 启动
     */
    void start() throws SQLException;

    /**
     * 停止
     */
    void stop();

    /**
     * 处理异常
     *
     * @param e
     */
    void handlerException(Exception e);
}
