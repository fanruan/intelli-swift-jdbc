package com.fr.swift.cloud.jdbc.rpc;


import com.fr.swift.cloud.basic.SwiftRequest;
import com.fr.swift.cloud.basic.SwiftResponse;

/**
 * @author yee
 * @date 2018/9/6
 */
public interface JdbcConnector extends JdbcComponent {
    /**
     * 发送响应
     *
     * @param object
     */
    void fireRpcResponse(SwiftResponse object);

    /**
     * 注册执行器
     *
     * @param executor
     */
    void registerExecutor(JdbcExecutor executor);

    /**
     * 发送对象
     *
     * @param rpc
     * @param timeout
     * @return
     */
    boolean sendRpcObject(SwiftRequest rpc, int timeout) throws Exception;

    void notifySend() throws Exception;
}
