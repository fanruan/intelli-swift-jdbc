package com.fr.swift.cloud.jdbc.rpc.connection;

import com.fr.swift.cloud.jdbc.rpc.invoke.BaseConnector;
import com.fr.swift.cloud.jdbc.rpc.invoke.JdbcNettyHandler;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author yee
 * @version 1.1
 * Created by yee on 2019-08-30
 */
public class JdbcNettyConnector extends BaseConnector {

    private AtomicBoolean isStarted = new AtomicBoolean();

    private JdbcNettyHandler jdbcNettyHandler;

    public JdbcNettyConnector(String address) {
        super(address);
    }

    public JdbcNettyConnector(String host, int port) {
        super(host, port);
    }

    @Override
    public void notifySend() throws Exception {
        rpcExecutors.get(0).send(getRequest());
    }

    @Override
    public void start() throws SQLException {
        if (isStarted.getAndSet(true)) {
            return;
        }
        try {
            this.jdbcNettyHandler = JdbcNettyPool.getInstance().borrowObject(getAddress());
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    @Override
    public void stop() {
        if (!isStarted.getAndSet(false)) {
            return;
        }
        JdbcNettyPool.getInstance().returnObject(getAddress(), jdbcNettyHandler);
        this.jdbcNettyHandler = null;
    }

    public JdbcNettyHandler getJdbcNettyHandler() {
        return jdbcNettyHandler;
    }

    public void updateNettyHandler(JdbcNettyHandler jdbcNettyHandler) {
        this.jdbcNettyHandler = jdbcNettyHandler;
    }
}
