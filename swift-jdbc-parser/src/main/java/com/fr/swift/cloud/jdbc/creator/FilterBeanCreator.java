package com.fr.swift.cloud.jdbc.creator;

import com.fr.swift.cloud.query.query.FilterBean;

/**
 * @author yee
 * @date 2019-07-19
 */
public interface FilterBeanCreator<T extends FilterBean> {
    /**
     * 创建FilterBean
     *
     * @param column
     * @param value
     * @return
     */
    T create(String column, Object value);
}
