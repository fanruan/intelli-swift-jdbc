package com.fr.swift.cloud.jdbc.rpc.invoke;

import com.fr.swift.cloud.basic.SwiftRequest;
import com.fr.swift.cloud.basic.SwiftResponse;
import com.fr.swift.cloud.jdbc.rpc.JdbcExecutor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * @author yee
 * @date 2018/8/26
 */
public class ClientProxy {
    private JdbcExecutor remoteExecutor;

    public ClientProxy(JdbcExecutor remoteExecutor) {
        this.remoteExecutor = remoteExecutor;
    }

    public <T> T getProxy(Class<T> proxyClass) {
        return (T) Proxy.newProxyInstance(proxyClass.getClassLoader(), new Class[]{proxyClass}, new ProxyHandler(proxyClass, remoteExecutor));
    }

    private class ProxyHandler implements InvocationHandler {
        private Class proxyClass;
        private JdbcExecutor remoteExecutor;

        public ProxyHandler(Class proxyClass, JdbcExecutor remoteExecutor) {
            this.proxyClass = proxyClass;
            this.remoteExecutor = remoteExecutor;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
            SwiftRequest request = new SwiftRequest();
            request.setRequestId(UUID.randomUUID().toString());
            request.setInterfaceName(proxyClass.getName());
            request.setParameters(args);
            request.setMethodName(method.getName());
            request.setParameterTypes(method.getParameterTypes());
            SwiftResponse response = remoteExecutor.send(request);
            return response.getResult();
        }
    }
}
