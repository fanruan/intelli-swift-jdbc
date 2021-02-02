package com.fr.swift.api.rpc.impl;

import com.fr.swift.SwiftContext;
import com.fr.swift.annotation.SwiftApi;
import com.fr.swift.api.result.OnePageApiResultSet;
import com.fr.swift.api.result.SwiftApiResultSet;
import com.fr.swift.api.rpc.SelectService;
import com.fr.swift.api.rpc.TableService;
import com.fr.swift.api.server.ReportSchemaCache;
import com.fr.swift.basics.annotation.ProxyService;
import com.fr.swift.beans.annotation.SwiftBean;
import com.fr.swift.db.SwiftDatabase;
import com.fr.swift.query.QueryRunnerProvider;
import com.fr.swift.query.info.bean.query.AbstractSingleTableQueryInfoBean;
import com.fr.swift.query.info.bean.query.QueryBeanFactory;
import com.fr.swift.query.query.QueryBean;
import com.fr.swift.result.DetailResultSet;
import com.fr.swift.result.SwiftResultSet;
import com.fr.swift.segment.SegmentKey;
import com.fr.swift.source.Row;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yee
 * @date 2018/8/23
 */
@ProxyService(value = SelectService.class, type = ProxyService.ServiceType.EXTERNAL)
@SwiftApi
@SwiftBean
public class SelectServiceImpl implements SelectService {
    private TableService tableService = SwiftContext.get().getBean(TableService.class);

    @Override
    @SwiftApi
    public SwiftApiResultSet query(SwiftDatabase database, String queryJson) {
        try {
            QueryBean queryBean = QueryBeanFactory.create(queryJson);
            return query(database, queryBean, 2000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SwiftApiResultSet query(SwiftDatabase database, String queryJson, String sql) throws Exception {
        QueryBean queryBean = QueryBeanFactory.create(queryJson);
        String tableName = queryBean.getTableName();
        if (ReportSchemaCache.get().isReport(tableName)) {
            JdbcFilterParser parser = SwiftContext.get().getBean(JdbcFilterParser.class);
            List<SegmentKey> segmentKeys = parser.getSegmentKeys(queryBean);
            queryBean.setSegments(segmentKeys.stream().map(s -> s.getId()).collect(Collectors.toSet()));
            return (SwiftApiResultSet) ReportSchemaCache.get().cache().get(sql, () -> query(database, queryBean, Integer.MAX_VALUE));
        }
        return query(database, queryBean, 2000);
    }

    private SwiftApiResultSet query(SwiftDatabase database, QueryBean queryBean, int fetchSize) throws SQLException {
        if (queryBean instanceof AbstractSingleTableQueryInfoBean) {
            // fetchSize设成500 别问为什么 试出来的
            ((AbstractSingleTableQueryInfoBean) queryBean).setFetchSize(fetchSize);
            String tableName = queryBean.getTableName();
            // 检查当前数据库下是否有这张表
            tableService.detectiveMetaData(database, tableName);
            SwiftResultSet resultSet = QueryRunnerProvider.getInstance().query(queryBean);
            return getPageResultSet(resultSet);
        }
        throw new UnsupportedOperationException();
    }

    // 不管分组还是明细，api返回的都是行结果
    private SwiftApiResultSet getPageResultSet(SwiftResultSet resultSet) throws SQLException {
        List<Row> rows = new ArrayList<>();
        int fetchSize = resultSet.getFetchSize();
        int count = 0;
        while (resultSet.hasNext() && count < fetchSize) {
            rows.add(resultSet.getNextRow());
            count++;
        }
        int rowCount = count;
        if (resultSet instanceof DetailResultSet) {
            rowCount = ((DetailResultSet) resultSet).getRowCount();
        }
        return new OnePageApiResultSet(null, MetaAdaptor.toCubeMeta(resultSet.getMetaData()), rows, rowCount, resultSet.hasNext());
    }
}
