package com.fr.swift.jdbc.server.handler;

import com.fr.swift.annotation.SwiftApi;
import com.fr.swift.api.server.exception.ApiCrasher;
import com.fr.swift.api.server.response.error.ServerErrorCode;
import com.fr.swift.basic.Request;
import com.fr.swift.basic.SwiftResponse;
import com.fr.swift.log.SwiftLoggers;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author yee
 * @version 1.1
 * Created by yee on 2019-09-03
 */
public class JdbcServerHandler extends SimpleChannelInboundHandler<Request> {

    private Map<String, Object> apiServices;

    public JdbcServerHandler(Map<String, Object> apiServices) {
        this.apiServices = apiServices;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, final Request request) {
        SwiftLoggers.getLogger().debug("Receive request " + request.getRequestId());
        SwiftResponse response = new SwiftResponse();
        response.setRequestId(request.getRequestId());
        try {
            Object result = handle(request);
            response.setResult(result);
        } catch (Throwable e) {
            SwiftLoggers.getLogger().error("handle result failure", e);
            response.setException(e);
        }
        ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) {
                SwiftLoggers.getLogger().debug("Send response for request " + request.getRequestId());
            }
        });
    }

    private Object handle(Request request) throws Exception {
        String serviceName = request.getInterfaceName();
        return handle(request, apiServices.get(serviceName));

    }

    private Object handle(Request request, Object serviceBean) throws Exception {
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        checkApiEnable(methodName, method);
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);
    }

    private void checkApiEnable(String methodName, Method method) {
        SwiftApi api = method.getAnnotation(SwiftApi.class);
        if (null != api && !api.enable()) {
            ApiCrasher.crash(ServerErrorCode.SERVER_INVOKE_ERROR, methodName + " is invalid on remote machine");
        }
    }
}
