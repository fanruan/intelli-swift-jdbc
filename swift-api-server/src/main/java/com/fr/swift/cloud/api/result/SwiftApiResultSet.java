package com.fr.swift.cloud.api.result;

import com.fr.swift.cloud.result.DetailResultSet;
import com.fr.swift.cloud.result.SerializableResultSet;
import com.fr.swift.cloud.source.Row;
import com.fr.swift.cloud.source.SwiftMetaData;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * this is a result set for api or jdbc
 *
 * @author yee
 * @date 2018-12-12
 */
public interface SwiftApiResultSet<T> extends SerializableResultSet, DetailResultSet {
    /**
     * get column label to column index map
     *
     * @return column label to column index map
     * @throws SQLException calculate from swift metadata would cause SQLException
     * @see SwiftMetaData
     */
    Map<String, Integer> getLabel2Index() throws SQLException;

    /**
     * call next page result set
     *
     * @param queryInfo it is some information for query next page.
     *                  it might be a query JSON string or a swift statement.
     * @return it is next page result. it would be null if there is no next page.
     * @throws SQLException query might cause SQLException
     */
    SwiftApiResultSet queryNextPage(T queryInfo) throws SQLException;

    /**
     * get all rows of current page
     *
     * @return all date of current page
     */
    List<Row> getRows();

    /**
     * mark if there is next page for this result set
     *
     * @return return true if there is next page.
     */
    boolean isOriginHasNextPage();

}
