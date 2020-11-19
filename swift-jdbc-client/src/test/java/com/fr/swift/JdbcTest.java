package com.fr.swift;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yee
 * @date 2019-03-08
 */
public class JdbcTest {
    @Test
    public void test() throws ClassNotFoundException, SQLException, InterruptedException {
//        String sql1 = "select app_id,year_month,count(app_id) from TemplateDetail";
//
//        String sql2 = "select * from TemplateDetail";

        Class.forName("com.fr.swift.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:swift:remote://127.0.0.1:7000/cube", "cloudswift", "iloveswift");
        List<String> list = new ArrayList<>();
//        list.add("select count(swift_app_id) from ConsumeProportion");
//        list.add("select count(swift_app_id) from ConsumeTime");
//        list.add("select count(swift_app_id) from Days");
//        list.add("select count(swift_app_id) from DownTimeAnalysis");
//        list.add("select count(swift_app_id) from DownTimes");
//        list.add("select count(swift_app_id) from IsFr");
//        list.add("select count(swift_app_id) from JarVersion");
//        list.add("select count(swift_app_id) from Last2MonthDowntime");
//        list.add("select count(swift_app_id) from Last2MonthTimesAndPersonTimes");
//        list.add("select count(swift_app_id) from Last3MonthDowntime");
//        list.add("select count(swift_app_id) from Last3MonthUpload");
//        list.add("select count(swift_app_id) from LastMonthDowntime");
//        list.add("select count(swift_app_id) from LastMonthTimesAndPersonTimes");
//        list.add("select count(swift_app_id) from LastMonthUpload");
//        list.add("select count(swift_app_id) from MemoryBalanceScore");
//        list.add("select count(swift_app_id) from MemoryBalanceScoreHastep");
//        list.add("select count(swift_app_id) from MemoryConclusion");
//        list.add("select count(swift_app_id) from MemoryDayTimes");
//        list.add("select count(swift_app_id) from MemRatioCPU");
//        list.add("select count(swift_app_id) from ReportConsumeProportion");
//        list.add("select count(swift_app_id) from RetentionDeclineTopTen");
//        list.add("select count(swift_app_id) from RetentionRiseTopTen");
//        list.add("select count(swift_app_id) from SQLConsumeProportion");
//        list.add("select count(swift_app_id) from TemplateRank");
//        list.add("select count(swift_app_id) from TimesAndUserNum");
//        list.add("select count(swift_app_id) from VisitDays");
//        list.add("select count(swift_app_id) from VisitTimesAndUserTimes");
        list.add("select count(swift_app_id) from YearMonth");
        for (String sql : list) {
            query(connection, "select * from MemRatioCPU where swift_app_id = '836d15a6-8c39-46d0-bbd1-2b5733f74408' order by cpu desc ");
        }
        connection.close();


//        long query1 = System.currentTimeMillis();
//        System.out.println(query1-start);
//        query(connection, sql2);
//        long query2 = System.currentTimeMillis();
//        System.out.println(query2-query1);
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
