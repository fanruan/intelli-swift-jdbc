package com.fr.swift.cloud.api.info.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fr.swift.cloud.api.info.ApiInvocation;
import com.fr.swift.cloud.api.info.RequestType;
import com.fr.swift.cloud.source.Row;

import java.util.List;

/**
 * @author yee
 * @date 2018-12-07
 */
public class InsertRequestInfo extends TableRequestInfo {
    @JsonProperty("selectField")
    private List<String> selectFields;
    @JsonProperty(value = "data")
    private List<Row> data;

    public InsertRequestInfo() {
        super(RequestType.INSERT);
    }

    public List<String> getSelectFields() {
        return selectFields;
    }

    public void setSelectFields(List<String> selectFields) {
        this.selectFields = selectFields;
    }

    public List<Row> getData() {
        return data;
    }

    public void setData(List<Row> data) {
        this.data = data;
    }

    @Override
    public ApiInvocation accept(ApiRequestParserVisitor visitor) {
        return visitor.visit(this);
    }
}
