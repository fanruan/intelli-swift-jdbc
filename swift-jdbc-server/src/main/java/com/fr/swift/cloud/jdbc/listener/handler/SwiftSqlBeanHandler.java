package com.fr.swift.cloud.jdbc.listener.handler;

import com.fr.swift.cloud.jdbc.adaptor.bean.CreationBean;
import com.fr.swift.cloud.jdbc.adaptor.bean.DeletionBean;
import com.fr.swift.cloud.jdbc.adaptor.bean.DropBean;
import com.fr.swift.cloud.jdbc.adaptor.bean.InsertionBean;
import com.fr.swift.cloud.jdbc.adaptor.bean.SelectionBean;
import com.fr.swift.cloud.jdbc.adaptor.bean.TruncateBean;

/**
 * @author yee
 * @date 2019-07-20
 */
public interface SwiftSqlBeanHandler {
    /**
     * 处理查询
     *
     * @param bean
     */
    void handle(SelectionBean bean);

    /**
     * 处理插入
     *
     * @param bean
     */
    void handle(InsertionBean bean);

    /**
     * 处理建表
     *
     * @param bean
     */
    void handle(CreationBean bean);

    /**
     * 处理删表
     *
     * @param bean
     */
    void handle(DropBean bean);

    /**
     * 处理清空表
     *
     * @param bean
     */
    void handle(TruncateBean bean);

    /**
     * 处理删数据
     *
     * @param bean
     */
    void handle(DeletionBean bean);
}
