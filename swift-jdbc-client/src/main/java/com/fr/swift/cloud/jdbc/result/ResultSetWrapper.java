package com.fr.swift.cloud.jdbc.result;

import com.fr.swift.cloud.jdbc.exception.Exceptions;
import com.fr.swift.cloud.result.SwiftResultSet;
import com.fr.swift.cloud.source.SwiftMetaData;
import com.fr.swift.cloud.util.Crasher;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author pony
 * @date 2018/8/17
 */
public class ResultSetWrapper extends BaseResultSet {
    private SwiftResultSet resultSet;
    private Map<String, Integer> label2Index;

    public ResultSetWrapper(SwiftResultSet resultSet) {
        this.resultSet = resultSet;
        try {
            initLabel2IndexMap();
        } catch (SQLException e) {
            Crasher.crash(new SQLException("Can't init Label2IndexMap"));
        }
    }

    public ResultSetWrapper(SwiftResultSet resultSet, Map<String, Integer> label2Index) {
        this.resultSet = resultSet;
        this.label2Index = label2Index;
    }

    @Override
    public boolean next() throws SQLException {
        if (resultSet.hasNext()) {
            current = resultSet.getNextRow();
            return true;
        }
        return false;
    }

    @Override
    public void close() throws SQLException {
        resultSet.close();
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return new MetadataWrapper(resultSet.getMetaData());
    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {
        if (label2Index == null || !label2Index.containsKey(columnLabel)) {
            throw Exceptions.sql(columnLabel + " not found.");
        }
        return label2Index.get(columnLabel);
    }

    private void initLabel2IndexMap() throws SQLException {
        label2Index = new HashMap<String, Integer>();
        SwiftMetaData metaData = resultSet.getMetaData();
        if (null != metaData) {
            List<String> fieldNames = metaData.getFieldNames();
            for (String fieldName : fieldNames) {
                label2Index.put(fieldName, metaData.getColumnIndex(fieldName));
            }
        }
    }
}
