package com.fr.swift.jdbc.sql;

import com.fr.swift.jdbc.druid.sql.SQLUtils;
import com.fr.swift.jdbc.exception.Exceptions;
import com.fr.swift.jdbc.info.SqlRequestInfo;
import com.fr.swift.jdbc.rpc.JdbcExecutor;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yee
 * @date 2018/11/19
 */
public class SwiftPreparedStatement extends SwiftStatementImpl implements PreparedStatement, SwiftStatement {
    public static final Pattern VALUE_POS_PATTERN = Pattern.compile("\\?");
    private String sql;
    private List values;

    public SwiftPreparedStatement(BaseSwiftConnection connection, String sql, JdbcExecutor query, JdbcExecutor maintain) {
        super(connection, query, maintain);
        SQLUtils.parseStatements(sql, null);
        this.sql = sql;
        this.values = new ArrayList();
        final Matcher matcher = SwiftPreparedStatement.VALUE_POS_PATTERN.matcher(sql);
        while (matcher.find()) {
            this.values.add(NullValue.INSTANCE);
        }
    }

    private int checkIndex(final int index) throws SQLException {
        if (index >= 1 && index <= this.values.size()) {
            return index - 1;
        }
        throw Exceptions.sql(String.format("Position %d is not valid. ", index));
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        SqlRequestInfo info = grammarChecker.check(sql, values);
        Object result = execute(info, queryExecutor);
        return null;
    }

    @Override
    public int executeUpdate() throws SQLException {
        SqlRequestInfo info = grammarChecker.check(sql, values);
        Object result = execute(info, maintainExecutor);
        return 0;
    }

    @Override
    public void setNull(final int parameterIndex, final int sqlType) throws SQLException {
        this.setObject(parameterIndex, NullValue.NULL);
    }

    @Override
    public void setBoolean(final int parameterIndex, final boolean x) throws SQLException {
        this.setObject(parameterIndex, x);
    }

    @Override
    public void setByte(final int parameterIndex, final byte x) throws SQLException {
        this.setObject(parameterIndex, x);
    }

    @Override
    public void setShort(final int parameterIndex, final short x) throws SQLException {
        this.setObject(parameterIndex, x);
    }

    @Override
    public void setInt(final int parameterIndex, final int x) throws SQLException {
        this.setObject(parameterIndex, x);
    }

    @Override
    public void setLong(final int parameterIndex, final long x) throws SQLException {
        this.setObject(parameterIndex, x);
    }

    @Override
    public void setFloat(final int parameterIndex, final float x) throws SQLException {
        this.setObject(parameterIndex, x);
    }

    @Override
    public void setDouble(final int parameterIndex, final double x) throws SQLException {
        this.setObject(parameterIndex, x);
    }

    @Override
    public void setBigDecimal(final int parameterIndex, final BigDecimal x) throws SQLException {
        this.setObject(parameterIndex, x);
    }

    @Override
    public void setString(final int parameterIndex, final String x) throws SQLException {
        this.setObject(parameterIndex, x);
    }

    @Override
    public void setBytes(final int parameterIndex, final byte[] x) throws SQLException {
    }

    @Override
    public void setDate(final int parameterIndex, final Date x) throws SQLException {
        this.setObject(parameterIndex, x.getTime());
    }

    @Override
    public void setTime(final int parameterIndex, final Time x) throws SQLException {
        this.setObject(parameterIndex, x.getTime());
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) {

    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) {

    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) {

    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) {

    }

    @Override
    public void clearParameters() throws SQLException {
        Collections.fill(this.values, NullValue.INSTANCE);
    }

    @Override
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType) throws SQLException {
        this.setObject(parameterIndex, x);
    }

    @Override
    public void setObject(final int parameterIndex, final Object x) throws SQLException {
        this.values.set(this.checkIndex(parameterIndex), x);
    }

    @Override
    public boolean execute() {
        return false;
    }

    @Override
    public void addBatch() {

    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) {

    }

    @Override
    public void setRef(int parameterIndex, Ref x) {

    }

    @Override
    public void setBlob(int parameterIndex, Blob x) {

    }

    @Override
    public void setClob(int parameterIndex, Clob x) {

    }

    @Override
    public void setArray(int parameterIndex, Array x) {

    }

    @Override
    public ResultSetMetaData getMetaData() {
        return null;
    }

    @Override
    public void setDate(final int parameterIndex, final Date x, final Calendar cal) throws SQLException {
        this.setDate(parameterIndex, x);
    }

    @Override
    public void setTime(final int parameterIndex, final Time x, final Calendar cal) throws SQLException {
        this.setTime(parameterIndex, x);
    }

    @Override
    public void setTimestamp(final int parameterIndex, final Timestamp x, final Calendar cal) throws SQLException {
        this.setTimestamp(parameterIndex, x);
    }

    @Override
    public void setNull(final int parameterIndex, final int sqlType, final String typeName) throws SQLException {
        this.setNull(parameterIndex, sqlType);
    }

    @Override
    public void setURL(int parameterIndex, URL x) {

    }

    @Override
    public ParameterMetaData getParameterMetaData() {
        return null;
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) {

    }

    @Override
    public void setNString(final int parameterIndex, final String value) throws SQLException {
        this.setObject(parameterIndex, value);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) {

    }

    @Override
    public void setNClob(int parameterIndex, NClob value) {

    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) {

    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) {

    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) {

    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) {

    }

    @Override
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType, final int scaleOrLength) throws SQLException {
        this.setObject(parameterIndex, x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) {

    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) {

    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) {

    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) {

    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) {

    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) {

    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) {

    }

    @Override
    public void setClob(int parameterIndex, Reader reader) {

    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) {

    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) {

    }

    @Override
    public void close() {
        values.clear();
        super.close();
    }

    @Override
    public int getMaxFieldSize() {
        return 0;
    }

    @Override
    public void setMaxFieldSize(int max) {

    }

    @Override
    public int getMaxRows() {
        return 0;
    }

    @Override
    public void setMaxRows(int max) {

    }

    @Override
    public void setEscapeProcessing(boolean enable) {

    }

    @Override
    public int getQueryTimeout() {
        return 0;
    }

    @Override
    public void setQueryTimeout(int seconds) {

    }

    @Override
    public void cancel() {

    }

    @Override
    public SQLWarning getWarnings() {
        return null;
    }

    @Override
    public void clearWarnings() {

    }

    @Override
    public void setCursorName(String name) {

    }

    @Override
    public boolean execute(String sql) {
        return false;
    }

    @Override
    public ResultSet getResultSet() {
        return null;
    }

    @Override
    public int getUpdateCount() {
        return 0;
    }

    @Override
    public boolean getMoreResults() {
        return false;
    }

    @Override
    public int getFetchDirection() {
        return 0;
    }

    @Override
    public void setFetchDirection(int direction) {

    }

    @Override
    public int getFetchSize() {
        return 0;
    }

    @Override
    public void setFetchSize(int rows) {

    }

    @Override
    public String getObjId() {
        return sql;
    }

    @Override
    public void reset() {
        super.reset();
        values.clear();
    }

    public enum NullValue {
        INSTANCE,
        NULL;

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }
}
