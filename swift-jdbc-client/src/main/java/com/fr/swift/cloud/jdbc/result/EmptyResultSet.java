package com.fr.swift.cloud.jdbc.result;

import com.fr.swift.cloud.config.entity.SwiftMetaDataEntity;
import com.fr.swift.cloud.result.SwiftResultSet;
import com.fr.swift.cloud.source.Row;
import com.fr.swift.cloud.source.SwiftMetaData;

import java.sql.SQLException;

/**
 * @author Marvin
 * @date 7/23/2019
 * @description
 * @since swift 1.1
 */
public enum EmptyResultSet implements SwiftResultSet {

    INSTANCE;

    @Override
    public int getFetchSize() {
        return 0;
    }

    @Override
    public SwiftMetaData getMetaData() throws SQLException {
        return new SwiftMetaDataEntity();
    }

    @Override
    public boolean hasNext() throws SQLException {
        return false;
    }

    @Override
    public Row getNextRow() throws SQLException {
        return null;
    }

    @Override
    public void close() throws SQLException {

    }
}
