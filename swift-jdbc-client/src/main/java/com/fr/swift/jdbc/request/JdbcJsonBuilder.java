package com.fr.swift.jdbc.request;

import com.fr.swift.util.Strings;

import java.util.UUID;

/**
 * @author yee
 */
public class JdbcJsonBuilder {

    public static String buildSqlJson(String sql, String requestId, String database, String authCode) {
        requestId = Strings.isEmpty(requestId) ? UUID.randomUUID().toString() : requestId;

        return "{\"requestId\": \"" +
                requestId +
                "\", \"requestType\": \"SQL\", \"sql\": \"" + sql.replaceAll("\\n", " ") + "\",\"database\": \"" +
                database + "\", \"auth\": \"" + authCode + "\"}";
    }

    public static String buildAuthJson(String username, String password, String fromAddress) {
        return "{\"requestId\": \"" +
                UUID.randomUUID().toString() +
                "\", \"requestType\": \"AUTH\", \"swiftUser\": \"" + username + "\",\"swiftPassword\": \"" +
                password + "\", \"from\": \"" + fromAddress + "\"}";
    }

}
