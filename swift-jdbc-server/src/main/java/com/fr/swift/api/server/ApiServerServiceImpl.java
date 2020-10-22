package com.fr.swift.api.server;

import com.fr.swift.SwiftContext;
import com.fr.swift.annotation.SwiftApi;
import com.fr.swift.api.info.ApiInvocation;
import com.fr.swift.api.info.RequestInfo;
import com.fr.swift.api.server.exception.ApiCrasher;
import com.fr.swift.api.server.exception.ApiRequestRuntimeException;
import com.fr.swift.api.server.exception.ApiUserPasswordException;
import com.fr.swift.api.server.response.ApiResponse;
import com.fr.swift.api.server.response.ApiResponseImpl;
import com.fr.swift.api.server.response.error.ParamErrorCode;
import com.fr.swift.api.server.response.error.ServerErrorCode;
import com.fr.swift.base.json.JsonBuilder;
import com.fr.swift.basics.annotation.ProxyService;
import com.fr.swift.basics.base.ProxyServiceRegistry;
import com.fr.swift.beans.annotation.SwiftBean;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.service.ServiceContext;

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
        ApiResponse response = new ApiResponseImpl();
        SwiftLoggers.getLogger().info("Receive jdbc request body {}", request);
        try {
            RequestInfo requestInfo = JsonBuilder.readValue(request, RequestInfo.class);
            ApiInvocation invocation = requestInfo.accept(new SwiftRequestParserVisitor());
            Object object = invokeRequest(invocation);
            response.setResult((Serializable) object);
        } catch (ApiRequestRuntimeException re) {
            SwiftLoggers.getLogger().error("Handle jdbc request {} with error", request, re);
            response.setThrowable(re);
            response.setStatusCode(re.getStatusCode());
        } catch (ApiUserPasswordException ue) {
            SwiftLoggers.getLogger().error("Handle jdbc request {} with error", request, ue);
            response.setThrowable(ue);
            response.setStatusCode(ParamErrorCode.USER_PASSWORD_ERROR);
        } catch (Throwable e) {
            SwiftLoggers.getLogger().error("Handle jdbc request {} with error", request, e);
            response.setThrowable(e);
            response.setStatusCode(ParamErrorCode.PARAMS_PARSER_ERROR);
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
            if(ie.getTargetException().getClass() == ApiUserPasswordException.class) {
                throw ie.getTargetException();
            } else {
                return ApiCrasher.crash(ServerErrorCode.SERVER_INVOKE_ERROR, ie);
            }
        } catch (Exception e) {
            return ApiCrasher.crash(ServerErrorCode.SERVER_INVOKE_ERROR, e);
        }
    }
}
