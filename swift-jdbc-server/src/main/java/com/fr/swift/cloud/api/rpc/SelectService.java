package com.fr.swift.cloud.api.rpc;

import com.fr.swift.cloud.api.result.SwiftApiResultSet;
import com.fr.swift.cloud.db.SwiftDatabase;

/**
 * @author yee
 * @date 2018/8/23
 */
public interface SelectService extends ApiService {
    /**
     * 查询接口
     *
     * @param database
     * @param queryJson 查询json字符串
     * @return
     * @throws Exception
     */
    SwiftApiResultSet query(SwiftDatabase database, String queryJson) throws Exception;

    /**
     * 查询接口，带入原始sql
     *
     * @param database
     * @param queryJson
     * @param sql
     * @return
     * @throws Exception
     */
    SwiftApiResultSet query(SwiftDatabase database, String queryJson, String sql) throws Exception;
}
