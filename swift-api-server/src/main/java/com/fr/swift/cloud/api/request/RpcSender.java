package com.fr.swift.cloud.api.request;

import com.fr.swift.cloud.basic.SwiftRequest;
import com.fr.swift.cloud.basic.SwiftResponse;

/**
 * @author yee
 * @date 2018/8/26
 */
public interface RpcSender {
    /**
     * send rpc request
     *
     * @param request rpc request
     * @return rpc response
     * @throws Exception the method might be throw exception
     * @see SwiftRequest
     * @see SwiftResponse
     */
    SwiftResponse send(SwiftRequest request) throws Exception;
}
