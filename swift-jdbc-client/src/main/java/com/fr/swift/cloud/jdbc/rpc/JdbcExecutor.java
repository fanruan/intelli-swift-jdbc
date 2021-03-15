package com.fr.swift.cloud.jdbc.rpc;

import com.fr.swift.cloud.api.request.RpcSender;
import com.fr.swift.cloud.basic.SwiftRequest;
import com.fr.swift.cloud.basic.SwiftResponse;

/**
 * @author yee
 * @date 2018/9/6
 */
public interface JdbcExecutor extends JdbcComponent, RpcSender {
    void onRpcResponse(SwiftResponse swiftResponse);

    @Override
    SwiftResponse send(SwiftRequest rpcRequest) throws Exception;
}
