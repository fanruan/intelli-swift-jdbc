package com.fr.swift.cloud.jdbc.rpc.invoke;

import com.fr.swift.cloud.basic.SwiftResponse;
import com.fr.swift.cloud.jdbc.rpc.JdbcConnector;
import com.fr.swift.cloud.jdbc.rpc.JdbcSelector;
import com.fr.swift.cloud.jdbc.thread.JdbcThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author yee
 * @date 2018/9/6
 */
public abstract class BaseSelector<T extends JdbcConnector> implements JdbcSelector<T> {
    private ExecutorService singleNotifyThread;

    public BaseSelector() {
    }

    @Override
    public void fireRpcResponse(final T connector, final SwiftResponse object) {
        singleNotifyThread.submit(new Runnable() {
            @Override
            public void run() {
                connector.fireRpcResponse(object);
            }
        });
    }

    @Override
    public void fireRpcException(final T connector, final Exception object) {
        singleNotifyThread.submit(new Runnable() {
            @Override
            public void run() {
                connector.handlerException(object);
            }
        });
    }

    @Override
    public void handlerException(Exception e) {
//        SwiftLoggers.getLogger().error(e);
    }

    @Override
    public void start() {
        if (null == singleNotifyThread || singleNotifyThread.isShutdown()) {
            singleNotifyThread = Executors.newSingleThreadExecutor(new JdbcThreadFactory.Builder().setName("swift-jdbc-notify-thread").build());
        }
        setUpSelector();
    }

    /**
     * 启动selector
     */
    protected abstract void setUpSelector();

    @Override
    public void stop() {
        singleNotifyThread.shutdown();
        shutdownSelector();
    }

    /**
     * 停止selector
     */
    protected abstract void shutdownSelector();
}
