package com.fr.swift.cloud.jdbc.sql;

import com.fr.swift.cloud.jdbc.rpc.JdbcExecutor;
import com.fr.swift.cloud.jdbc.rpc.JdbcExecutorCreator;
import com.fr.swift.cloud.jdbc.rpc.impl.NettyJdbcExecutorCreator;
import com.fr.swift.cloud.jdbc.rpc.impl.SimpleJdbcExecutorCreator;
import com.fr.swift.cloud.log.SwiftLoggers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Properties;

/**
 * @author yee
 * @date 2018/11/16
 */
public class RemoteConnection extends BaseSwiftConnection {

    private JdbcExecutorCreator executorCreator;

    RemoteConnection(UnregisteredDriver driver, Properties properties) {
        super(driver, properties);
        init();
    }

    @Override
    protected JdbcExecutor createJdbcExecutor(String host, int port) {
        return executorCreator.create(host, port, connectionTimeout());
    }

    @Override
    protected JdbcExecutor createJdbcExecutor(String address) {
        return executorCreator.create(address, connectionTimeout());
    }

    private void init() {
        executorCreator = (JdbcExecutorCreator) Proxy.newProxyInstance(RemoteConnection.class.getClassLoader(), new Class[]{JdbcExecutorCreator.class}, new InvocationHandler() {
            private NettyJdbcExecutorCreator nettyCreator = new NettyJdbcExecutorCreator();
            private SimpleJdbcExecutorCreator simpleCreator = new SimpleJdbcExecutorCreator();

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                try {
                    return method.invoke(nettyCreator, args);
                } catch (Exception e) {
                    SwiftLoggers.getLogger().error("Invoke NettyCreator Error. Using default", e);
                    return method.invoke(simpleCreator, args);
                }
            }
        });
    }
}
