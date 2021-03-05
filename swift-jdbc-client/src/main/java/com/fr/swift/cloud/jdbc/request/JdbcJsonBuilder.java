package com.fr.swift.cloud.jdbc.request;

import com.fr.swift.cloud.util.Strings;

import java.util.UUID;

/**
 * @author yee
 */
public class JdbcJsonBuilder {

    public static String buildSqlJson(String sql, String requestId, String database, String swiftUser) {
        requestId = Strings.isEmpty(requestId) ? UUID.randomUUID().toString() : requestId;

        return "{\"requestId\": \"" +
                requestId +
                "\", \"requestType\": \"SQL\", \"sql\": \"" + sql.replaceAll("\\n", " ") + "\",\"database\": \"" +
                database + "\", \"swiftUser\": \"" + swiftUser + "\"}";
    }

    public static String buildAuthJson(String username, String password, String fromAddress) {
        return "{\"requestId\": \"" +
                UUID.randomUUID().toString() +
                "\", \"requestType\": \"AUTH\", \"swiftUser\": \"" + username + "\",\"swiftPassword\": \"" +
                password + "\", \"from\": \"" + fromAddress + "\"}";
    }

}
