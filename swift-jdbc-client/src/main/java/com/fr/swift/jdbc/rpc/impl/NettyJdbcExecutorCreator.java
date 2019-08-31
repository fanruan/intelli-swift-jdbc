package com.fr.swift.jdbc.rpc.impl;

import com.fr.swift.jdbc.rpc.JdbcExecutor;
import com.fr.swift.jdbc.rpc.JdbcExecutorCreator;
import com.fr.swift.jdbc.rpc.connection.JdbcNettyConnector;
import com.fr.swift.jdbc.rpc.invoke.JdbcNettyExecutor;

/**
 * @author yee
 * @version 1.1
 * Created by yee on 2019-08-30
 */
public class NettyJdbcExecutorCreator implements JdbcExecutorCreator {
    @Override
    public JdbcExecutor create(String address, int timeout) {
        return new JdbcNettyExecutor(new JdbcNettyConnector(address), timeout);
    }

    @Override
    public JdbcExecutor create(String host, int port, int timeout) {
        return new JdbcNettyExecutor(new JdbcNettyConnector(host, port), timeout);
    }

}
