package com.fr.swift.cloud.jdbc.rpc.invoke;

import com.fr.swift.cloud.basic.SwiftRequest;
import com.fr.swift.cloud.basic.SwiftResponse;
import com.fr.swift.cloud.jdbc.rpc.JdbcExecutor;
import com.fr.swift.cloud.jdbc.rpc.connection.JdbcNettyConnector;
import com.fr.swift.cloud.jdbc.rpc.connection.JdbcNettyPool;

import java.sql.SQLException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author yee
 * @version 1.1
 * Created by yee on 2019-08-30
 */
public class JdbcNettyExecutor implements JdbcExecutor {
    private JdbcNettyConnector connector;
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private SwiftResponse response;
    private long timeout;

    public JdbcNettyExecutor(JdbcNettyConnector connector, long timeout) {
        this.connector = connector;
        this.timeout = timeout;
    }

    @Override
    public void onRpcResponse(SwiftResponse rpcResponse) {

    }

    @Override
    public SwiftResponse send(SwiftRequest rpcRequest) throws Exception {
        JdbcNettyHandler handler = connector.getJdbcNettyHandler();
        if (!handler.isActive()) {
            JdbcNettyPool.getInstance().returnObject(connector.getKey(), handler);
            JdbcNettyPool.getInstance().invalidateObject(connector.getKey(), handler);
            handler = JdbcNettyPool.getInstance().borrowObject(connector.getKey());
            connector.updateNettyHandler(handler);
        }
        return handler.send(rpcRequest);
    }

    @Override
    public void start() throws SQLException {
        this.connector.registerExecutor(this);
        this.connector.start();
    }

    @Override
    public void stop() {
        connector.stop();
    }

    @Override
    public void handlerException(Exception e) {
        connector.handlerException(e);
    }
}
