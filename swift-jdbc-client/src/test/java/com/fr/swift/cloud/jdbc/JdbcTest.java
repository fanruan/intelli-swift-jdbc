package com.fr.swift.cloud.jdbc;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * @author yee
 * @date 2019-03-08
 */
public class JdbcTest {
    @Test
    public void test() throws ClassNotFoundException, SQLException {
        String sql = "select todate(currentTime), eventType from test_yiguan where price > 100";
        Class.forName("com.fr.swift.cloud.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:swift:remote://127.0.0.1:7000/cube");
        ResultSet resultSet = connection.createStatement().executeQuery(sql);
        ResultSetMetaData metaData = resultSet.getMetaData();
//        for (int i = 0; i < metaData.getColumnCount(); i++) {
//            System.out.println(metaData.getColumnName(i + 1));
//        }
        int count = 0;
        while (resultSet.next() && count++ < 200) {
            System.out.println(resultSet.getObject(1) + ", " + resultSet.getObject(2));
        }
        resultSet.close();
        connection.close();
    }
}
