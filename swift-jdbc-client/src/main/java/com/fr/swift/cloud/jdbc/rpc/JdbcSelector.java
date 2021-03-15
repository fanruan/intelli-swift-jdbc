package com.fr.swift.cloud.jdbc.rpc;

import com.fr.swift.cloud.basic.SwiftResponse;

/**
 * @author yee
 * @date 2018/9/6
 */
public interface JdbcSelector<T extends JdbcConnector> extends JdbcComponent {
    void register(T connector);

    void notifySend();

    void fireRpcResponse(T connector, SwiftResponse object);

    void fireRpcException(T connector, Exception object);
}
