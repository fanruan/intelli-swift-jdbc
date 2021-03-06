package com.fr.swift.cloud.api.info.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fr.swift.cloud.api.info.ApiInvocation;
import com.fr.swift.cloud.api.info.BaseRequestInfo;
import com.fr.swift.cloud.api.info.RequestType;
import com.fr.swift.cloud.db.SwiftDatabase;

/**
 * @author yee
 * @date 2018-12-07
 */
public class QueryRequestInfo extends BaseRequestInfo<ApiRequestParserVisitor> {
    @JsonProperty(value = "auth")
    private String authCode;

    @JsonProperty(value = "queryJson")
    private String queryJson;

    @JsonProperty("database")
    private SwiftDatabase database;

    public QueryRequestInfo() {
        super(RequestType.JSON_QUERY);
    }

    @Override
    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getQueryJson() {
        return queryJson;
    }

    public void setQueryJson(String queryJson) {
        this.queryJson = queryJson;
    }

    public SwiftDatabase getDatabase() {
        return database;
    }

    public void setDatabase(SwiftDatabase database) {
        this.database = database;
    }

    @Override
    public ApiInvocation accept(ApiRequestParserVisitor visitor) {
        return visitor.visit(this);
    }
}
