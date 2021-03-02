package com.fr.swift.cloud.jdbc.rpc;

/**
 * @author yee
 * @version 1.1
 * Created by yee on 2019-08-30
 */
public interface JdbcExecutorCreator {
    JdbcExecutor create(String address, int timeout);

    JdbcExecutor create(String host, int port, int timeout);
}
