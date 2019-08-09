package com.fr.swift.jdbc.sql;

import com.fr.swift.api.result.BaseApiResultSet;
import com.fr.swift.api.result.SwiftApiResultSet;
import com.fr.swift.api.server.ApiServerService;
import com.fr.swift.jdbc.rpc.invoke.ClientProxy;
import com.fr.swift.structure.Pair;

import java.sql.SQLException;

/**
 * @author yee
 * @date 2018-12-12
 */
public class JdbcSwiftResultSet extends BaseApiResultSet<Pair<String, String>> {
    private static final long serialVersionUID = 5724892335081556009L;
    private SwiftStatementImpl swiftStatement;
    private String queryId;

    JdbcSwiftResultSet(Pair<String, String> info, SwiftApiResultSet resultSet, SwiftStatementImpl swiftStatement) throws SQLException {
        super(info, resultSet.getMetaData(), resultSet.getRows(), resultSet.getRowCount(), resultSet.isOriginHasNextPage());
        this.swiftStatement = swiftStatement;
        this.queryId = info.getValue();
    }

    @Override
    public SwiftApiResultSet queryNextPage(Pair<String, String> queryInfo) throws SQLException {
        return swiftStatement.execute(queryInfo.getKey(), queryInfo.getValue(), swiftStatement.queryExecutor);
    }

    @Override
    public void close() {
        try {
            new ClientProxy(swiftStatement.queryExecutor).getProxy(ApiServerService.class).close(queryId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
