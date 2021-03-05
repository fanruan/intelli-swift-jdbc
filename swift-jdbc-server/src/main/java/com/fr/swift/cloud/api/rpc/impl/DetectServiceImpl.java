package com.fr.swift.cloud.api.rpc.impl;

import com.fr.swift.cloud.annotation.SwiftApi;
import com.fr.swift.cloud.api.rpc.DetectService;
import com.fr.swift.cloud.api.rpc.user.UserInfoService;
import com.fr.swift.cloud.api.server.exception.ApiUserPasswordException;
import com.fr.swift.cloud.api.server.response.AuthResponse;
import com.fr.swift.cloud.api.server.response.AuthResponseImpl;
import com.fr.swift.cloud.basics.annotation.ProxyService;
import com.fr.swift.cloud.beans.annotation.SwiftBean;
import com.fr.swift.cloud.log.SwiftLoggers;
import com.fr.swift.cloud.source.core.MD5Utils;

import java.util.Collections;

//import com.fr.swift.event.global.GetAnalyseAndRealTimeAddrEvent;
//import com.fr.swift.service.listener.RemoteSender;

/**
 * @author yee
 * @date 2018/8/23
 */
@ProxyService(value = DetectService.class, type = ProxyService.ServiceType.EXTERNAL)
@SwiftApi
@SwiftBean
public class DetectServiceImpl implements DetectService {

    @Override
    @SwiftApi
    public AuthResponse detectiveAnalyseAndRealTime(String defaultAddress, String username, String password) {
        String authCode = MD5Utils.getMD5String(new String[]{username, password});
        AuthResponseImpl response = new AuthResponseImpl();
        if (!UserInfoService.verify(username, password)) {
            throw new ApiUserPasswordException("jdbc username or password error!");
        }
        response.setAuthCode(authCode);
        try {
//            if (SwiftProperty.getProperty().isCluster()) {
////                Map<ServiceType, List<String>> map = (Map<ServiceType, List<String>>) ProxySelector.getInstance().getFactory().getProxy(RemoteSender.class).trigger(new GetAnalyseAndRealTimeAddrEvent());
//                Map<ServiceType, List<String>> map = new HashMap<>();
//                response.setAnalyseAddress(map.get(ServiceType.ANALYSE));
//                response.setRealTimeAddress(map.get(ServiceType.REAL_TIME));
//            } else {
//                response.setRealTimeAddress(Collections.singletonList(defaultAddress));
//                response.setAnalyseAddress(Collections.singletonList(defaultAddress));
//            }
            response.setRealTimeAddress(Collections.singletonList(defaultAddress));
            response.setAnalyseAddress(Collections.singletonList(defaultAddress));
        } catch (Exception e) {
            SwiftLoggers.getLogger().warn(e);
            response.setRealTimeAddress(Collections.<String>emptyList());
            response.setAnalyseAddress(Collections.<String>emptyList());
        }
        return response;
    }

}
