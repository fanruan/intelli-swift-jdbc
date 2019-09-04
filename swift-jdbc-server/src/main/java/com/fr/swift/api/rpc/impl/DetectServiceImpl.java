package com.fr.swift.api.rpc.impl;

import com.fr.swift.annotation.SwiftApi;
import com.fr.swift.api.result.OnePageApiResultSet;
import com.fr.swift.api.result.SwiftApiResultSet;
import com.fr.swift.api.rpc.DetectService;
import com.fr.swift.api.server.response.AuthResponse;
import com.fr.swift.api.server.response.AuthResponseImpl;
import com.fr.swift.base.meta.MetaDataColumnBean;
import com.fr.swift.base.meta.SwiftMetaDataBean;
import com.fr.swift.basics.base.selector.ProxySelector;
import com.fr.swift.beans.annotation.SwiftBean;
import com.fr.swift.db.SwiftSchema;
import com.fr.swift.event.global.GetJdbcAddresses;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.property.SwiftProperty;
import com.fr.swift.service.ServiceType;
import com.fr.swift.service.listener.RemoteSender;
import com.fr.swift.source.ListBasedRow;
import com.fr.swift.source.Row;
import com.fr.swift.source.SwiftMetaDataColumn;
import com.fr.swift.source.core.MD5Utils;
import com.fr.swift.util.Strings;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author yee
 * @date 2018/8/23
 */
@SwiftApi(service = DetectService.class)
@SwiftBean
public class DetectServiceImpl implements DetectService {
    @Override
    @SwiftApi
    public AuthResponse detectiveAnalyseAndRealTime(String defaultAddress, String username, String password) {
        // TODO 真的做校验，这里就是个假的校验
        String authCode = MD5Utils.getMD5String(new String[]{username, password});
        AuthResponseImpl response = new AuthResponseImpl();
        response.setAuthCode(authCode);
        try {
            if (SwiftProperty.getProperty().isCluster()) {
                Map<ServiceType, List<String>> map = (Map<ServiceType, List<String>>) ProxySelector.getInstance().getFactory().getProxy(RemoteSender.class).trigger(new GetJdbcAddresses());
                response.setAnalyseAddress(map.get(ServiceType.ANALYSE));
                response.setRealTimeAddress(map.get(ServiceType.REAL_TIME));
            } else {
                response.setRealTimeAddress(Collections.singletonList(defaultAddress));
                response.setAnalyseAddress(Collections.singletonList(defaultAddress));
            }
        } catch (Exception e) {
            SwiftLoggers.getLogger().warn(e);
            response.setRealTimeAddress(Collections.<String>emptyList());
            response.setAnalyseAddress(Collections.<String>emptyList());
        }
        return response;
    }

    @Override
    @SwiftApi
    public SwiftApiResultSet detectiveCatalogs() {
        List<Row> rows = new ArrayList<>();
        for (SwiftSchema value : SwiftSchema.values()) {
            rows.add(new ListBasedRow(Collections.singletonList(value.name())));
        }
        final SwiftMetaDataBean tableCat = new SwiftMetaDataBean(Strings.EMPTY,
                Collections.<SwiftMetaDataColumn>singletonList(
                        new MetaDataColumnBean("TABLE_CAT", Types.VARCHAR)));

        return new OnePageApiResultSet(null, tableCat, rows, rows.size(), false);
    }

}
