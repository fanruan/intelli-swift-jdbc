package com.fr.swift.cloud.api.rpc.impl;

import com.fr.swift.cloud.query.query.QueryBean;
import com.fr.swift.cloud.segment.SegmentKey;

import java.util.List;

/**
 * @Author: lucifer
 * @Description:
 * @Date: Created in 2020/12/10
 */
public interface JdbcFilterParser {

    /**
     * jdbc专用解析querybean的filter，用于组成querybean时先做一次块过滤
     *
     * @param queryBean
     * @return
     */
    List<SegmentKey> getSegmentKeys(QueryBean queryBean);
}
