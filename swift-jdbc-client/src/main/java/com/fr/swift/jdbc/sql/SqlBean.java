package com.fr.swift.jdbc.sql;

/**
 * @author yee
 * @date 2019-07-29
 */
public class SqlBean {
    private String sql;
    private boolean select;

    public SqlBean(String sql, boolean select) {
        this.sql = sql;
        this.select = select;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }
}
