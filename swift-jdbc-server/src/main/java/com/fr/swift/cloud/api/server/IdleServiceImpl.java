package com.fr.swift.cloud.api.server;

import com.fr.swift.cloud.api.service.IdleService;
import com.fr.swift.cloud.api.service.JdbcIdleResponse;
import com.fr.swift.cloud.basics.annotation.ProxyService;
import com.fr.swift.cloud.beans.annotation.SwiftBean;
import com.fr.swift.cloud.log.SwiftLoggers;

/**
 * @author Heng.J
 * @date 2021/7/12
 * @description
 * @since swift-1.2.0
 */
@SwiftBean
@ProxyService(value = IdleService.class, type = ProxyService.ServiceType.EXTERNAL)
public class IdleServiceImpl implements IdleService {

    @Override
    public JdbcIdleResponse heartBeat() {
        SwiftLoggers.getLogger().info("Receive heart beat detection");
        return new JdbcIdleResponse();
    }
}
