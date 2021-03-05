package com.fr.swift.cloud.jdbc.rpc.invoke;

import com.fr.swift.cloud.basic.SwiftRequest;
import com.fr.swift.cloud.basic.SwiftResponse;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author yee
 * @date 2018/8/26
 */
public class CallBackSync implements Future<SwiftResponse> {

    private String rpcId;

    /**
     * 请求发送包
     */
    private SwiftRequest request;

    /**
     * 请求返回数据包
     */
    private SwiftResponse response;

    public CallBackSync(String rpcId, SwiftRequest request) {
        this.rpcId = rpcId;
        this.request = request;
    }

    public SwiftRequest getRequest() {
        return request;
    }

    public void setRequest(SwiftRequest request) {
        this.request = request;
    }

    public SwiftResponse getResponse() {
        return response;
    }

    public void setResponse(SwiftResponse response) {
        this.response = response;
    }

    public String getRpcId() {
        return rpcId;
    }

    public void setRpcId(String rpcId) {
        this.rpcId = rpcId;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return response != null;
    }

    @Override
    public SwiftResponse get() {
        return response;
    }

    @Override
    public SwiftResponse get(long timeout, TimeUnit unit) {
        return null;
    }

}