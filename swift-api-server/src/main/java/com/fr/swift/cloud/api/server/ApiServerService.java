package com.fr.swift.cloud.api.server;

import com.fr.swift.cloud.api.server.response.ApiResponse;

/**
 * @author yee
 * @date 2018/11/20
 */
public interface ApiServerService {
    ApiResponse dispatchRequest(String request);

    void close(String queryId) throws Exception;
}
