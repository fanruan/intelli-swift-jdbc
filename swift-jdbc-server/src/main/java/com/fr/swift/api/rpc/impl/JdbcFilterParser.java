package com.fr.swift.api.rpc.impl;

import com.fr.swift.query.query.QueryBean;
import com.fr.swift.segment.SegmentKey;

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
