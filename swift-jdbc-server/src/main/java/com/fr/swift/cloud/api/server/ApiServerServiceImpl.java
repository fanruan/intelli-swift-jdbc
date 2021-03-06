package com.fr.swift.cloud.api.server;

import com.alibaba.fastjson.JSONObject;
import com.fr.swift.cloud.SwiftContext;
import com.fr.swift.cloud.annotation.SwiftApi;
import com.fr.swift.cloud.api.info.ApiInvocation;
import com.fr.swift.cloud.api.info.RequestInfo;
import com.fr.swift.cloud.api.server.exception.ApiCrasher;
import com.fr.swift.cloud.api.server.exception.ApiRequestRuntimeException;
import com.fr.swift.cloud.api.server.exception.ApiUserPasswordException;
import com.fr.swift.cloud.api.server.response.ApiResponse;
import com.fr.swift.cloud.api.server.response.ApiResponseImpl;
import com.fr.swift.cloud.api.server.response.error.ParamErrorCode;
import com.fr.swift.cloud.api.server.response.error.ServerErrorCode;
import com.fr.swift.cloud.base.json.JsonBuilder;
import com.fr.swift.cloud.basics.annotation.ProxyService;
import com.fr.swift.cloud.basics.base.ProxyServiceRegistry;
import com.fr.swift.cloud.beans.annotation.SwiftBean;
import com.fr.swift.cloud.log.SwiftLoggers;
import com.fr.swift.cloud.service.ServiceContext;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class created on 2018/12/10
 *
 * @author Lucifer
 * @description
 * @since Advanced FineBI 5.0
 */
@SwiftBean
@ProxyService(value = ApiServerService.class, type = ProxyService.ServiceType.EXTERNAL)
public class ApiServerServiceImpl implements ApiServerService {

    @Override
    @SwiftApi
    public ApiResponse dispatchRequest(String request) {
        long start = System.currentTimeMillis();
        ApiResponse response = new ApiResponseImpl();
        JSONObject jsonRequest = JSONObject.parseObject(request);
        SwiftLoggers.getLogger().info("Receive jdbc request user :" + jsonRequest.get("swiftUser")
                + " requestId: " + jsonRequest.get("requestId")
                + " requestType: " + jsonRequest.get("requestType")
                + " sql: " + jsonRequest.get("sql"));
        try {
            RequestInfo requestInfo = JsonBuilder.readValue(request, RequestInfo.class);
            ApiInvocation invocation = requestInfo.accept(new SwiftRequestParserVisitor());
            Object object = invokeRequest(invocation);
            response.setResult((Serializable) object);
        } catch (ApiRequestRuntimeException re) {
            SwiftLoggers.getLogger().error("Handle jdbc error {}", re);
            response.setThrowable(re);
            response.setStatusCode(re.getStatusCode());
        } catch (ApiUserPasswordException ue) {
            SwiftLoggers.getLogger().error("Handle jdbc error {}", ue);
            response.setThrowable(ue);
            response.setStatusCode(ParamErrorCode.USER_PASSWORD_ERROR);
        } catch (Throwable e) {
            SwiftLoggers.getLogger().error("Handle jdbc error {}", e);
            response.setThrowable(e);
            response.setStatusCode(ParamErrorCode.PARAMS_PARSER_ERROR);
        } finally {
            SwiftLoggers.getLogger().info("handler jdbc request user :" + jsonRequest.get("swiftUser")
                    + " requestId: " + jsonRequest.get("requestId")
                    + " requestType: " + jsonRequest.get("requestType")
                    + " cost: " + (System.currentTimeMillis() - start) + "ms");
        }
        return response;
    }

    @Override
    public void close(String queryId) throws Exception {
        SwiftContext.get().getBean(ServiceContext.class).clearQuery(queryId);
    }

    private Object invokeRequest(ApiInvocation invocation) throws Throwable {
        Class<?> aClass = invocation.getTarget();
        Class<?>[] parameterTypes = invocation.getParameterTypes();
        Object[] arguments = invocation.getArguments();
        String methodName = invocation.getMethodName();
        try {
            Method method = aClass.getMethod(methodName, parameterTypes);
            return method.invoke(ProxyServiceRegistry.get().getExternalService(aClass), arguments);
        } catch (InvocationTargetException ie) {
            SwiftLoggers.getLogger().error(ie);
            if (ie.getTargetException().getClass() == ApiUserPasswordException.class) {
                throw ie.getTargetException();
            } else {
                return ApiCrasher.crash(ServerErrorCode.SERVER_INVOKE_ERROR, ie);
            }
        } catch (Exception e) {
            SwiftLoggers.getLogger().error(e);
            return ApiCrasher.crash(ServerErrorCode.SERVER_INVOKE_ERROR, e);
        }
    }
}
