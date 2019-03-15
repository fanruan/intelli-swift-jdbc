package com.fr.swift.jdbc.rpc;

import com.fr.swift.api.request.RpcSender;
import com.fr.swift.basic.SwiftResponse;
import com.fr.swift.basic.SwiftRequest;

/**
 * @author yee
 * @date 2018/9/6
 */
public interface JdbcExecutor extends JdbcComponent, RpcSender {
    void onRpcResponse(SwiftResponse rpcResponse);

    @Override
    SwiftResponse send(SwiftRequest rpcRequest);
}
