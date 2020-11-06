package com.fr.swift.api.rpc.user;

import java.util.Date;

/**
 * @author Hoky
 * @date 2020/10/26
 */
public class JDBCUserInfo {
    private String password;
    private Date createTime;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
