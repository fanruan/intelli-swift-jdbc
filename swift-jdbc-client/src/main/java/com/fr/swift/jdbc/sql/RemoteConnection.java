package com.fr.swift.jdbc.sql;

import com.fr.swift.jdbc.rpc.JdbcExecutor;
import com.fr.swift.jdbc.rpc.JdbcExecutorCreator;
import com.fr.swift.jdbc.rpc.impl.NettyJdbcExecutorCreator;
import com.fr.swift.jdbc.rpc.impl.SimpleJdbcExecutorCreator;
import com.fr.swift.log.SwiftLoggers;

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
        try {
            Class.forName("io.netty.bootstrap.Bootstrap");
            executorCreator = new NettyJdbcExecutorCreator();
        } catch (ClassNotFoundException e) {
            SwiftLoggers.getLogger().warn("Netty not found. Using default jdbc executor creator");
            executorCreator = new SimpleJdbcExecutorCreator();
        }
    }
}
