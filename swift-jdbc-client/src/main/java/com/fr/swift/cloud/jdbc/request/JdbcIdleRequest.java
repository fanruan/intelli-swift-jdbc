package com.fr.swift.cloud.jdbc.request;

import com.fr.swift.cloud.api.server.IdleService;
import com.fr.swift.cloud.basic.SwiftRequest;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * @author Heng.J
 * @date 2021/7/6
 * @description for heart beat idle detection
 * @since swift-1.2.0
 */
public class JdbcIdleRequest extends SwiftRequest {

    private static final long serialVersionUID = 2410741432264339110L;

    public static JdbcIdleRequest getJdbcHeartbeat() throws Exception {
        JdbcIdleRequest idleRequest = new JdbcIdleRequest();
        Method heartBeat = IdleService.class.getMethod("heartBeat");
        idleRequest.setRequestId(UUID.randomUUID().toString());
        idleRequest.setInterfaceName(IdleService.class.getName());
        idleRequest.setParameters(heartBeat.getParameters());
        idleRequest.setMethodName(heartBeat.getName());
        idleRequest.setParameterTypes(heartBeat.getParameterTypes());
        return idleRequest;
    }
}
