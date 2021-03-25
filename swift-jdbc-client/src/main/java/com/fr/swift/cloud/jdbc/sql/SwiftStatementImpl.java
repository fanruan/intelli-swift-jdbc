package com.fr.swift.cloud.jdbc.sql;

import com.fr.swift.cloud.api.info.jdbc.SqlRequestInfo;
import com.fr.swift.cloud.api.result.SwiftApiResultSet;
import com.fr.swift.cloud.jdbc.result.MaintainResultSet;
import com.fr.swift.cloud.jdbc.result.ResultSetWrapper;
import com.fr.swift.cloud.jdbc.rpc.JdbcExecutor;
import com.fr.swift.cloud.source.ListBasedRow;
import com.fr.swift.cloud.source.Row;
import com.fr.swift.cloud.structure.Pair;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Collections;
import java.util.UUID;

/**
 * @author yee
 * @date 2018/11/19
 */
public class SwiftStatementImpl extends BaseSwiftStatement {
    protected JdbcExecutor queryExecutor;
    protected JdbcExecutor maintainExecutor;
    protected String id;

    SwiftStatementImpl(BaseSwiftConnection connection, JdbcExecutor queryExecutor, JdbcExecutor maintainExecutor) throws SQLException {
        super(connection);
        this.queryExecutor = queryExecutor;
        this.maintainExecutor = maintainExecutor;
        reset();
        this.id = UUID.randomUUID().toString();
    }

    /**
     * if it is a select statement then return result set
     * else wrap affected row count to a result set.
     *
     * @param sql it is a query statement normally but it can be a maintain statement.
     * @return return query result set or maintain affected row count
     * @throws SQLException the method would throw SQLException when got error from server
     */
    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        SqlBean info = grammarChecker.check(sql);
        String requestId = UUID.randomUUID().toString();
        String newSql = info.getSql();
        if (info.isSelect()) {
            SwiftApiResultSet<SqlRequestInfo> result = execute(newSql, requestId, queryExecutor);
            return new ResultSetWrapper(new JdbcSwiftResultSet(Pair.of(newSql, requestId), result, this), result.getLabel2Index());
        } else {
            int result = executeUpdate(newSql);
            return new MaintainResultSet(Collections.<Row>singletonList(new ListBasedRow(result)).iterator(), Collections.singletonList("affects"));
        }
    }

    /**
     * if it is a select statement then return the row count of the query
     * else return update affected row count.
     *
     * @param sql it is a maintain statement normally but it also can be a query statement.
     * @return if parameter sql was a query statement the method would return row count of result set.
     * if parameter sql was a maintain statement the method would return affected row count.
     * @throws SQLException the method would throw SQLException when got error from server
     */
    @Override
    public int executeUpdate(String sql) throws SQLException {
        SqlBean info = grammarChecker.check(sql);
        return executeUpdate(info);
    }

    int executeUpdate(SqlBean info) throws SQLException {
        String requestId = UUID.randomUUID().toString();
        if (info.isSelect()) {
            SwiftApiResultSet<SqlRequestInfo> result = execute(info.getSql(), requestId, queryExecutor);
            return result.getRowCount();
        }
        Object obj = execute(info.getSql(), requestId, maintainExecutor);
        try {
            return 1;
//            return ReflectUtils.parseNumber(obj).intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void close() {
        connection.holder.idle(this);
        queryExecutor.stop();
        maintainExecutor.stop();
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return 0;
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {

    }

    @Override
    public int getMaxRows() throws SQLException {
        return 0;
    }

    @Override
    public void setMaxRows(int max) throws SQLException {

    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {

    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return 0;
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {

    }

    @Override
    public void cancel() throws SQLException {

    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public void setCursorName(String name) throws SQLException {

    }

    @Override
    public boolean execute(String sql) throws SQLException {
        return false;
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return null;
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return 0;
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return false;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {

    }

    @Override
    public int getFetchDirection() throws SQLException {
        return ResultSet.FETCH_FORWARD;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {

    }

    @Override
    public int getFetchSize() throws SQLException {
        return 0;
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return ResultSet.CONCUR_READ_ONLY;
    }

    @Override
    public int getResultSetType() throws SQLException {
        return ResultSet.TYPE_FORWARD_ONLY;
    }

    @Override
    public void addBatch(String sql) throws SQLException {

    }

    @Override
    public void clearBatch() throws SQLException {

    }

    @Override
    public int[] executeBatch() throws SQLException {
        return new int[0];
    }

    @Override
    public Connection getConnection() throws SQLException {
        return connection;
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return false;
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return null;
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return 0;
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return 0;
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return 0;
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return false;
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return false;
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return false;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return 0;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {

    }

    @Override
    public boolean isPoolable() throws SQLException {
        return false;
    }

    @Override
    public void closeOnCompletion() throws SQLException {

    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public String getObjId() {
        return id;
    }

    @Override
    public void reset() throws SQLException {
        queryExecutor.start();
        maintainExecutor.start();
    }
}
