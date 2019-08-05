package com.fr.swift.jdbc.request.impl;

import com.fr.swift.api.server.ApiServerService;
import com.fr.swift.api.server.response.ApiResponse;
import com.fr.swift.jdbc.request.JdbcJsonBuilder;
import com.fr.swift.jdbc.request.JdbcRequestService;
import com.fr.swift.jdbc.rpc.JdbcExecutor;
import com.fr.swift.jdbc.rpc.invoke.ClientProxy;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * @author yee
 * @date 2018/11/20
 */
public class RequestServiceImpl implements JdbcRequestService {
    @Override
    public ApiResponse apply(JdbcExecutor sender, String user, String password) {
        return apply(sender, JdbcJsonBuilder.buildAuthJson(user, password, ""));
    }

    @Override
    @SuppressWarnings("unchecked")
    public ApiResponse apply(JdbcExecutor sender, final String requestJson) {
        ClientProxy proxy = new ClientProxy(sender);
        return proxy.getProxy(ApiServerService.class).dispatchRequest(requestJson);
    }

    @Override
    public ApiResponse applyWithRetry(JdbcExecutor sender, String user, String password, int retryTime) {
        return applyWithRetry(sender, JdbcJsonBuilder.buildAuthJson(user, password, ""), retryTime);
    }


    @Override
    @SuppressWarnings("unchecked")
    public ApiResponse applyWithRetry(JdbcExecutor sender, String requestJson, int retryTime) {
        ApiResponse response = null;
        for (int i = 0; i < retryTime; i++) {
            try {
                response = apply(sender, requestJson);
                if (!response.isError()) {
                    return response;
                }
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (final Exception e) {
                return new ApiResponse() {
                    @Override
                    public int statusCode() {
                        return UNKNOWN_ERROR;
                    }

                    @Override
                    public String description() {
                        return e.getMessage();
                    }

                    @Override
                    public boolean isError() {
                        return true;
                    }

                    @Override
                    public Serializable result() {
                        return null;
                    }

                    @Override
                    public void setResult(Serializable result) {

                    }

                    @Override
                    public void setThrowable(Throwable throwable) {

                    }

                    @Override
                    public void setStatusCode(int statusCode) {

                    }
                };
            }
        }
        return response;
    }
}
