package com.fr.swift;

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
        String sql1 = "select app_id,year_month,count(app_id) from TemplateDetail";

        String sql2 = "select * from TemplateDetail";

        Class.forName("com.fr.swift.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:swift:remote://127.0.0.1:7000/cube", "cloudswift", "iloveswift");
        query(connection, sql1);
        System.out.println();
        query(connection, sql2);
        connection.close();
    }

    public void query(Connection connection, String sql) throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery(sql);
        ResultSetMetaData metaData = resultSet.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            System.out.print(metaData.getColumnName(i) + "|");
        }
        System.out.println();
        int count = 0;
        while (resultSet.next()) {
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                System.out.print(resultSet.getObject(i) + "|");
            }
            System.out.println();
        }
        resultSet.close();
    }
}
