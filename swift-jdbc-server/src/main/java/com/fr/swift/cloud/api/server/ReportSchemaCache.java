package com.fr.swift.cloud.api.server;

import com.fr.swift.cloud.db.Table;
import com.fr.swift.cloud.db.impl.SwiftDatabase;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: lucifer
 * @Description:
 * @Date: Created in 2020/12/8
 */
public class ReportSchemaCache {

    private final Map<String, Table> reportTables;

    private final Cache<Object, Object> sqlCache;

    private final static ReportSchemaCache INSTANCE = new ReportSchemaCache();

    private ReportSchemaCache() {
        List<Table> tablesBySchema = SwiftDatabase.getInstance().getTablesBySchema(com.fr.swift.cloud.db.SwiftDatabase.REPORT);
        reportTables = Collections.unmodifiableMap(tablesBySchema.stream().collect(Collectors.toMap(t -> t.getSourceKey().getId(), t -> t)));
        sqlCache = CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(2, TimeUnit.MINUTES).build();
    }

    public static ReportSchemaCache get() {
        return INSTANCE;
    }

    public boolean isReport(String tableName) {
        return reportTables.containsKey(tableName);
    }

    public Cache cache() {
        return sqlCache;
    }
}