package com.fr.swift.jdbc.sql;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yee
 * @date 2019/9/16
 */
public enum JdbcMetaMap {
    //
    PROD {
        @Override
        protected Map<String, Integer> init() {
            Map<String, Integer> metaMap = new HashMap<String, Integer>();
            metaMap.put("PROCEDURE_CAT", 1);
            metaMap.put("PROCEDURE_SCHEM", 2);
            metaMap.put("PROCEDURE_NAME", 3);
            metaMap.put("reserved1", 4);
            metaMap.put("reserved2", 5);
            metaMap.put("reserved3", 6);
            metaMap.put("REMARKS", 7);
            metaMap.put("PROCEDURE_TYPE", 8);
            metaMap.put("SPECIFIC_NAME", 9);
            return Collections.unmodifiableMap(metaMap);
        }
    },
    FUNC {
        @Override
        protected Map<String, Integer> init() {
            Map<String, Integer> metaMap = new HashMap<String, Integer>();
            metaMap.put("FUNCTION_CAT", 1);
            metaMap.put("FUNCTION_SCHEM", 2);
            metaMap.put("FUNCTION_NAME", 3);
            metaMap.put("REMARKS", 4);
            metaMap.put("FUNCTION_TYPE", 5);
            metaMap.put("SPECIFIC_NAME", 6);
            return Collections.unmodifiableMap(metaMap);
        }
    },
    UTDS {
        @Override
        protected Map<String, Integer> init() {
            Map<String, Integer> metaMap = new HashMap<String, Integer>();
            metaMap.put("TYPE_CAT", 1);
            metaMap.put("TYPE_SCHEM", 2);
            metaMap.put("TYPE_NAME", 3);
            metaMap.put("CLASS_NAME", 4);
            metaMap.put("DATA_TYPE", 5);
            metaMap.put("REMARKS", 6);
            metaMap.put("BASE_TYPE", 7);
            return Collections.unmodifiableMap(metaMap);
        }
    },
    TABLES {
        @Override
        protected Map<String, Integer> init() {
            Map<String, Integer> metaMap = new HashMap<String, Integer>();
            metaMap.put("TABLE_CAT", 1);
            metaMap.put("TABLE_SCHEMA", 2);
            metaMap.put("TABLE_NAME", 3);
            metaMap.put("TABLE_TYPE", 4);
            metaMap.put("REMARKS", 5);
            metaMap.put("TYPE_NAME", 6);
            return Collections.unmodifiableMap(metaMap);
        }
    },
    COLUMNS {
        @Override
        protected Map<String, Integer> init() {
            Map<String, Integer> metaMap = new HashMap<String, Integer>();
            metaMap.put("TABLE_CAT", 1);
            metaMap.put("TABLE_NAME", 2);
            metaMap.put("REMARKS", 3);
            metaMap.put("COLUMN_NAME", 4);
            metaMap.put("DATA_TYPE", 5);
            metaMap.put("COLUMN_SIZE", 6);
            metaMap.put("DECIMAL_DIGITS", 7);
            return Collections.unmodifiableMap(metaMap);
        }
    };
    protected Map<String, Integer> metaMap;

    JdbcMetaMap() {
        metaMap = init();
    }

    protected abstract Map<String, Integer> init();

    public Map<String, Integer> getMetaMap() {
        return metaMap;
    }
}
