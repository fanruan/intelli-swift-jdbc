package com.fr.swift.cloud.api.info.api;

import com.fr.swift.cloud.api.info.RequestType;

public class DropRequestInfo extends TableRequestInfo {
    public DropRequestInfo() {
        super(RequestType.DROP_TABLE);
    }
}
