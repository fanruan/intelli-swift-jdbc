package com.fr.swift.cloud.api.info.api;

import com.fr.swift.cloud.api.info.RequestType;

public class TruncateRequestInfo extends TableRequestInfo {
    public TruncateRequestInfo() {
        super(RequestType.TRUNCATE_TABLE);
    }
}
