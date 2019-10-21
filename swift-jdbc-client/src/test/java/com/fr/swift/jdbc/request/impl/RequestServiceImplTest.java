package com.fr.swift.jdbc.request.impl;

import com.fr.swift.api.server.response.ApiResponse;
import com.fr.swift.basic.SwiftRequest;
import com.fr.swift.basic.SwiftResponse;
import com.fr.swift.jdbc.rpc.JdbcExecutor;
import org.easymock.EasyMock;
import org.junit.Test;
import org.powermock.api.easymock.PowerMock;

import static junit.framework.TestCase.assertFalse;

/**
 * @author yee
 * @date 2018-12-24
 */
public class RequestServiceImplTest {

    @Test
    public void apply() {
        // Generate by Mock Plugin
        JdbcExecutor mockJdbcExecutor = PowerMock.createMock(JdbcExecutor.class);
        ApiResponse mockApiResponse = PowerMock.createMock(ApiResponse.class);
        EasyMock.expect(mockApiResponse.isError()).andReturn(false).anyTimes();
        SwiftResponse mockRpcResponse = PowerMock.createMock(SwiftResponse.class);
        EasyMock.expect(mockRpcResponse.getResult()).andReturn(mockApiResponse).anyTimes();
        EasyMock.expect(mockJdbcExecutor.send(EasyMock.notNull(SwiftRequest.class))).andReturn(mockRpcResponse).anyTimes();
        PowerMock.replayAll();
        ApiResponse response = new RequestServiceImpl().apply(mockJdbcExecutor, "user", "pass");
        assertFalse(response.isError());
        PowerMock.verifyAll();
    }


    @Test
    public void apply2() {
        String jsonString = "{\"database\":cube,\"requestType\":\"SQL\",\"auth\":\"authCode\",\"requestId\":\"93eceaf1-4e47-40a1-84e9-814404eb7ad5\",\"sql\":\"select * from tableA\"}";
        JdbcExecutor mockJdbcExecutor = PowerMock.createMock(JdbcExecutor.class);
        ApiResponse mockApiResponse = PowerMock.createMock(ApiResponse.class);
        EasyMock.expect(mockApiResponse.isError()).andReturn(false).anyTimes();
        SwiftResponse mockRpcResponse = PowerMock.createMock(SwiftResponse.class);
        EasyMock.expect(mockRpcResponse.getResult()).andReturn(mockApiResponse).anyTimes();
        EasyMock.expect(mockJdbcExecutor.send(EasyMock.notNull(SwiftRequest.class))).andReturn(mockRpcResponse).anyTimes();
        PowerMock.replayAll();
        ApiResponse response = new RequestServiceImpl().apply(mockJdbcExecutor, jsonString);
        assertFalse(response.isError());
        PowerMock.verifyAll();
    }

    @Test
    public void applyWithRetry() {
        // Generate by Mock Plugin
        JdbcExecutor mockJdbcExecutor = PowerMock.createMock(JdbcExecutor.class);
        ApiResponse mockApiResponse = PowerMock.createMock(ApiResponse.class);
        EasyMock.expect(mockApiResponse.isError()).andReturn(true).times(2);
        EasyMock.expect(mockApiResponse.isError()).andReturn(false).once();
        SwiftResponse mockRpcResponse = PowerMock.createMock(SwiftResponse.class);
        EasyMock.expect(mockRpcResponse.getResult()).andReturn(mockApiResponse).times(3);
        EasyMock.expect(mockJdbcExecutor.send(EasyMock.notNull(SwiftRequest.class))).andReturn(mockRpcResponse).times(3);
        PowerMock.replayAll();
        new RequestServiceImpl().applyWithRetry(mockJdbcExecutor, "user", "pass", 3);
        PowerMock.verifyAll();
    }


    @Test
    public void applyWithRetry2() {
        String jsonString = "{\"database\":cube,\"requestType\":\"SQL\",\"auth\":\"authCode\",\"requestId\":\"93eceaf1-4e47-40a1-84e9-814404eb7ad5\",\"sql\":\"select * from tableA\"}";
        JdbcExecutor mockJdbcExecutor = PowerMock.createMock(JdbcExecutor.class);
        ApiResponse mockApiResponse = PowerMock.createMock(ApiResponse.class);
        EasyMock.expect(mockApiResponse.isError()).andReturn(true).times(2);
        EasyMock.expect(mockApiResponse.isError()).andReturn(false).once();
        SwiftResponse mockRpcResponse = PowerMock.createMock(SwiftResponse.class);
        EasyMock.expect(mockRpcResponse.getResult()).andReturn(mockApiResponse).times(3);
        EasyMock.expect(mockJdbcExecutor.send(EasyMock.notNull(SwiftRequest.class))).andReturn(mockRpcResponse).times(3);
        PowerMock.replayAll();
        new RequestServiceImpl().applyWithRetry(mockJdbcExecutor, jsonString, 3);
        PowerMock.verifyAll();
    }
}