package com.fr.swift.cloud.jdbc.result;

import com.fr.swift.cloud.result.SwiftResultSet;
import com.fr.swift.cloud.source.Row;
import com.fr.swift.cloud.source.SwiftMetaData;

import java.sql.SQLException;
import java.util.Iterator;

/**
 *
 * @author pony
 * @date 2018/8/17
 */
public class IteratorBasedResultSet implements SwiftResultSet {
    private Iterator<Row> iterator;

    public IteratorBasedResultSet(Iterator<Row> iterator) {
        this.iterator = iterator;
    }

    @Override
    public int getFetchSize() {
        return 0;
    }

    @Override
    public SwiftMetaData getMetaData() throws SQLException {
        return null;
    }

    @Override
    public boolean hasNext() throws SQLException {
        return iterator.hasNext();
    }

    @Override
    public Row getNextRow() throws SQLException {
        return iterator.next();
    }

    @Override
    public void close() throws SQLException {

    }
}
