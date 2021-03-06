package com.fr.swift.cloud.jdbc.rpc.invoke;

import com.fr.swift.cloud.basic.SwiftRequest;
import com.fr.swift.cloud.basic.SwiftResponse;
import com.fr.swift.cloud.jdbc.exception.Exceptions;
import com.fr.swift.cloud.jdbc.rpc.JdbcConnector;
import com.fr.swift.cloud.jdbc.rpc.JdbcExecutor;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author yee
 * @date 2018/8/26
 */
public class SimpleExecutor implements JdbcExecutor {
    /**
     * 默认超时30秒
     */
    private static final int DEFAULT_TIMEOUT = 30000;
    private int timeout;
    private JdbcConnector connector;
    private ReentrantLock lock;
    private Condition condition;
    private ConcurrentHashMap<String, CallBackSync> rpcCache = new ConcurrentHashMap<String, CallBackSync>();


    public SimpleExecutor(JdbcConnector connector) {
        this(connector, DEFAULT_TIMEOUT);
    }

    public SimpleExecutor(JdbcConnector connector, int timeout) {
        this.timeout = timeout;
        this.connector = connector;
        this.connector.registerExecutor(this);
        this.lock = new ReentrantLock();
        this.condition = lock.newCondition();
    }

    @Override
    public SwiftResponse send(SwiftRequest request) throws Exception {
        CallBackSync sync = new CallBackSync(request.getRequestId(), request);
        rpcCache.put(request.getRequestId(), sync);
        lock.lock();
        try {
            connector.sendRpcObject(request, timeout);
            condition.await(timeout, TimeUnit.MILLISECONDS);
            SwiftResponse response = sync.getResponse();
            if (response == null) {
                throw Exceptions.timeout("connection timeout");
            }
            rpcCache.remove(sync.getRpcId());
            if (response.getException() != null) {
                throw Exceptions.runtime(response.getException().getMessage(), response.getException());
            }
            return response;
        } catch (InterruptedException e) {
            throw Exceptions.timeout();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void start() throws SQLException {
        connector.start();
    }

    @Override
    public void stop() {
        connector.stop();
    }

    @Override
    public void handlerException(Exception e) {
        connector.handlerException(e);
    }

    @Override
    public void onRpcResponse(SwiftResponse rpc) {
        CallBackSync sync = rpcCache.get(rpc.getRequestId());
        if (sync != null && sync.getRpcId().equals(rpc.getRequestId())) {
            sync.setResponse(rpc);
            lock.lock();
            try {
                condition.signal();
            } finally {
                lock.unlock();
            }
        }
    }
}
