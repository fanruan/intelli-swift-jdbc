package com.fr.swift.jdbc.sql;

import com.fr.swift.api.result.BaseApiResultSet;
import com.fr.swift.api.result.SwiftApiResultSet;
import com.fr.swift.structure.Pair;

import java.sql.SQLException;

/**
 * @author yee
 * @date 2018-12-12
 */
public class JdbcSwiftResultSet extends BaseApiResultSet<Pair<String, String>> {
    private static final long serialVersionUID = 5724892335081556009L;
    private SwiftStatementImpl swiftStatement;

    JdbcSwiftResultSet(Pair<String, String> info, SwiftApiResultSet resultSet, SwiftStatementImpl swiftStatement) throws SQLException {
        super(info, resultSet.getMetaData(), resultSet.getRows(), resultSet.getRowCount(), resultSet.isOriginHasNextPage());
        this.swiftStatement = swiftStatement;
    }

    @Override
    public SwiftApiResultSet queryNextPage(Pair<String, String> queryInfo) throws SQLException {
        return swiftStatement.execute(queryInfo.getKey(), queryInfo.getValue(), swiftStatement.queryExecutor);
    }
}
