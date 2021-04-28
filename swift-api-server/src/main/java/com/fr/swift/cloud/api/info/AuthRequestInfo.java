package com.fr.swift.cloud.api.info;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author yee
 * @date 2018-12-03
 */
public class AuthRequestInfo extends BaseRequestInfo<RequestParserVisitor> {
    @JsonProperty(value = "swiftUser")
    private String swiftUser;
    @JsonProperty(value = "swiftPassword")
    private String swiftPassword;
    @JsonProperty("from")
    private String from;

    public AuthRequestInfo() {
        super(RequestType.AUTH);
    }

    public AuthRequestInfo(String swiftUser, String swiftPassword) {
        super(RequestType.AUTH);
        this.swiftUser = swiftUser;
        this.swiftPassword = swiftPassword;
    }

    public String getSwiftUser() {
        return swiftUser;
    }

    public String getSwiftPassword() {
        return swiftPassword;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public ApiInvocation accept(RequestParserVisitor visitor) {
        return visitor.visit(this);
    }
}
