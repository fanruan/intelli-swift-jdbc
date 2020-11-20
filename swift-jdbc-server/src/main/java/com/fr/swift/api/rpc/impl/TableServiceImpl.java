package com.fr.swift.api.rpc.impl;

import com.fr.swift.SwiftContext;
import com.fr.swift.annotation.SwiftApi;
import com.fr.swift.api.rpc.TableService;
import com.fr.swift.api.rpc.bean.Column;
import com.fr.swift.basics.annotation.ProxyService;
import com.fr.swift.beans.annotation.SwiftBean;
import com.fr.swift.config.SwiftConfigConstants;
import com.fr.swift.config.entity.MetaDataColumnEntity;
import com.fr.swift.config.entity.SwiftMetaDataEntity;
import com.fr.swift.config.service.SwiftMetaDataService;
import com.fr.swift.db.AlterTableAction;
import com.fr.swift.db.SwiftDatabase;
import com.fr.swift.db.Table;
import com.fr.swift.db.impl.AddColumnAction;
import com.fr.swift.db.impl.DropColumnAction;
import com.fr.swift.exception.meta.SwiftMetaDataAbsentException;
import com.fr.swift.source.SourceKey;
import com.fr.swift.source.SwiftMetaData;
import com.fr.swift.source.SwiftMetaDataColumn;
import com.fr.swift.util.Crasher;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yee
 * @date 2018/8/27
 */
@ProxyService(value = TableService.class, type = ProxyService.ServiceType.EXTERNAL)
@SwiftApi
@SwiftBean
public class TableServiceImpl implements TableService {
    private SwiftMetaDataService swiftMetaDataService = SwiftContext.get().getBean(SwiftMetaDataService.class);

    @Override
    @SwiftApi
    public SwiftMetaData detectiveMetaData(SwiftDatabase schema, String tableName) throws SwiftMetaDataAbsentException {
        SwiftMetaData swiftMetaData = swiftMetaDataService.getMeta(new SourceKey(tableName));
//        List<SwiftMetaData> metaDataList = swiftMetaDataService.find(ConfigWhereImpl.eq(SwiftConfigConstants.MetaDataConfig.COLUMN_TABLE_NAME, tableName),
//                ConfigWhereImpl.eq(SwiftConfigConstants.MetaDataConfig.COLUMN_SWIFT_SCHEMA, schema));
        if (swiftMetaData == null) {
            throw new SwiftMetaDataAbsentException(tableName);
        }
        return MetaAdaptor.toCubeMeta(swiftMetaData);
    }

    @Override
    @SwiftApi
    public List<SwiftMetaData> detectiveAllTable(SwiftDatabase schema) {
        List<SwiftMetaData> metaDataList = swiftMetaDataService.getAllMetas();
        if (metaDataList.isEmpty()) {
            return Collections.emptyList();
        } else {
            return metaDataList.stream().map(MetaAdaptor::toCubeMeta).collect(Collectors.toList());
        }
    }

    @Override
    @SwiftApi
    public boolean isTableExists(SwiftDatabase schema, String tableName) {
        try {
            return null != detectiveMetaData(schema, tableName);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @SwiftApi
    synchronized
    public int createTable(SwiftDatabase schema, String tableName, List<Column> columns) throws SQLException {
        if (isTableExists(schema, tableName)) {
            Crasher.crash("Table " + tableName + " is already exists");
        }
        if (columns.isEmpty()) {
            Crasher.crash("Table " + tableName + " must contain at lease one column.");
        }
        List<SwiftMetaDataColumn> columnList = new ArrayList<SwiftMetaDataColumn>();
        for (Column column : columns) {
            String columnName = column.getColumnName();
            if (SwiftConfigConstants.KeyWords.COLUMN_KEY_WORDS.contains(columnName.toLowerCase())) {
                throw new SQLException(String.format("%s is a key word! ", columnName));
            }
            columnList.add(new MetaDataColumnEntity(column.getColumnName(), column.getColumnType()));
        }
        SwiftMetaDataEntity swiftMetaData = new SwiftMetaDataEntity.Builder().setSwiftSchema(schema)
                .setTableName(tableName).setId(tableName).setFields(columnList).build();
//        if (swiftMetaDataService.addMetaData(tableName, swiftMetaDataBean)) {
//            return 1;
//        }
//        return -1;
        swiftMetaDataService.saveMeta(swiftMetaData);
        return 1;
    }

    @Override
    @SwiftApi
    public int dropTable(SwiftDatabase schema, String tableName) throws Exception {
//        SwiftMetaData metaData = detectiveMetaData(schema, tableName);
//        truncateTable(metaData);
//        if (swiftMetaDataService.removeMetaDatas(new SourceKey(metaData.getId()))) {
//            return 1;
//        }
//        return 0;
//        swiftMetaDataService.deleteMeta(new SourceKey(metaData.getId()));
        return 1;
    }

    @Override
    @SwiftApi
    public void truncateTable(SwiftDatabase schema, String tableName) throws Exception {
//        SwiftMetaData metaData = detectiveMetaData(schema, tableName);
//        truncateTable(metaData);
    }
//
//    private void truncateTable(SwiftMetaData metaData) throws Exception {
//        String sourceKey = metaData.getId();
//        // 如果是集群，需要让master转发到所有节点删除，因为调用truncate的不一定是master，非master无法调用其他节点进行删除
//        if (ClusterSelector.getInstance().getFactory().isCluster()) {
//            ProxySelector.getInstance().getFactory().getProxy(RemoteSender.class).trigger(new TruncateEvent(sourceKey));
//        } else {
//            // 单机直接调用history的truncate
//            // truncate也应该删除realtime的数据
//            SourceKey source = new SourceKey(sourceKey);
//            try {
//                ProxySelector.getInstance().getFactory().getProxy(ServiceContext.class).truncate(source);
//            } catch (Exception e) {
//                SwiftLoggers.getLogger().error(e);
//            }
//        }
//    }

    @Override
    @SwiftApi(enable = false)
    public boolean addColumn(SwiftDatabase schema, String tableName, Column column) throws SQLException {
        SwiftMetaData metaData = detectiveMetaData(schema, tableName);
        if (metaData.getFieldNames().contains(column.getColumnName())) {
            throw new SQLException("Column " + column.getColumnName() + " is already exists!");
        }
        Table table = com.fr.swift.db.impl.SwiftDatabase.getInstance().getTable(new SourceKey(metaData.getId()));
        SwiftMetaDataColumn metaDataColumn = new MetaDataColumnEntity(column.getColumnName(), column.getColumnType());
        AlterTableAction action = new AddColumnAction(metaDataColumn);
        return alterTable(table, action);
    }

    @Override
    @SwiftApi(enable = false)
    public boolean dropColumn(SwiftDatabase schema, String tableName, String columnName) throws SQLException {
        SwiftMetaData metaData = detectiveMetaData(schema, tableName);
        SwiftMetaDataColumn metaDataColumn = metaData.getColumn(columnName);
        Table table = com.fr.swift.db.impl.SwiftDatabase.getInstance().getTable(new SourceKey(metaData.getId()));
        AlterTableAction action = new DropColumnAction(metaDataColumn);
        return alterTable(table, action);
    }

    private boolean alterTable(Table table, AlterTableAction action) throws SQLException {
        action.alter(table);
        return true;
    }
}
