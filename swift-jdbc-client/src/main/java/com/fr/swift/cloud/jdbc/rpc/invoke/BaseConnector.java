package com.fr.swift.cloud.jdbc.rpc.invoke;

import com.fr.swift.cloud.basic.SwiftRequest;
import com.fr.swift.cloud.basic.SwiftResponse;
import com.fr.swift.cloud.jdbc.exception.Exceptions;
import com.fr.swift.cloud.jdbc.rpc.JdbcConnector;
import com.fr.swift.cloud.jdbc.rpc.JdbcExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author yee
 * @date 2018/9/6
 */
public abstract class BaseConnector implements JdbcConnector {
    protected String host = "localhost";
    protected int port = 7000;
    private ConcurrentLinkedQueue<SwiftRequest> sendQueueCache = new ConcurrentLinkedQueue<SwiftRequest>();
    protected List<JdbcExecutor> rpcExecutors;

    public BaseConnector(String host, int port) {
        this.host = host;
        this.port = port == -1 ? 7000 : port;
        this.rpcExecutors = new ArrayList<JdbcExecutor>();
    }

    public BaseConnector(String address) {
        String[] array = address.split(":");
        host = array[0];
        if (array.length > 1) {
            port = Integer.parseInt(array[1]);
        }
        rpcExecutors = new ArrayList<JdbcExecutor>();
    }

    public BaseConnector() {
        rpcExecutors = new ArrayList<JdbcExecutor>();
    }

    @Override
    public void fireRpcResponse(SwiftResponse object) {
        for (JdbcExecutor rpcExecutor : rpcExecutors) {
            rpcExecutor.onRpcResponse(object);
        }
    }

    @Override
    public void registerExecutor(JdbcExecutor executor) {
        rpcExecutors.add(executor);
    }

    @Override
    public boolean sendRpcObject(SwiftRequest rpc, int timeout) throws Exception {
        int cost = 0;
        while (!sendQueueCache.offer(rpc)) {
            cost += 3;
            try {
                Thread.sleep(3);
            } catch (InterruptedException e) {
                throw Exceptions.runtime("", e);
            }
            if (timeout > 0 && cost > timeout) {
                throw Exceptions.timeout();
            }
        }
        this.notifySend();
        return true;
    }

    @Override
    public void handlerException(Exception e) {
//        SwiftLoggers.getLogger().error(e);
        stop();
    }

    public SwiftRequest getRequest() {
        return sendQueueCache.poll();
    }

    public boolean isNeedToSend() {
        return null != sendQueueCache.peek();
    }

    public String getKey() {
        return String.format("%s:%s", host, port);
    }
}
