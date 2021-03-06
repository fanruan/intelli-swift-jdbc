package com.fr.swift.cloud.api.server;

import com.fr.swift.cloud.api.info.ApiInvocation;
import com.fr.swift.cloud.api.info.AuthRequestInfo;
import com.fr.swift.cloud.api.info.RequestType;
import com.fr.swift.cloud.api.info.api.ApiRequestParserVisitor;
import com.fr.swift.cloud.api.info.api.CreateTableRequestInfo;
import com.fr.swift.cloud.api.info.api.DeleteRequestInfo;
import com.fr.swift.cloud.api.info.api.DropRequestInfo;
import com.fr.swift.cloud.api.info.api.InsertRequestInfo;
import com.fr.swift.cloud.api.info.api.QueryRequestInfo;
import com.fr.swift.cloud.api.info.api.TableRequestInfo;
import com.fr.swift.cloud.api.info.jdbc.ColumnsRequestInfo;
import com.fr.swift.cloud.api.info.jdbc.JdbcRequestParserVisitor;
import com.fr.swift.cloud.api.info.jdbc.SqlRequestInfo;
import com.fr.swift.cloud.api.info.jdbc.TablesRequestInfo;
import com.fr.swift.cloud.api.rpc.DataMaintenanceService;
import com.fr.swift.cloud.api.rpc.DetectService;
import com.fr.swift.cloud.api.rpc.SelectService;
import com.fr.swift.cloud.api.rpc.TableService;
import com.fr.swift.cloud.api.rpc.bean.Column;
import com.fr.swift.cloud.api.server.exception.ApiCrasher;
import com.fr.swift.cloud.api.server.response.error.ParamErrorCode;
import com.fr.swift.cloud.base.json.JsonBuilder;
import com.fr.swift.cloud.db.SwiftDatabase;
import com.fr.swift.cloud.db.impl.SwiftWhere;
import com.fr.swift.cloud.jdbc.adaptor.bean.ColumnBean;
import com.fr.swift.cloud.jdbc.adaptor.bean.CreationBean;
import com.fr.swift.cloud.jdbc.adaptor.bean.DeletionBean;
import com.fr.swift.cloud.jdbc.adaptor.bean.DropBean;
import com.fr.swift.cloud.jdbc.adaptor.bean.InsertionBean;
import com.fr.swift.cloud.jdbc.adaptor.bean.SelectionBean;
import com.fr.swift.cloud.jdbc.adaptor.bean.TruncateBean;
import com.fr.swift.cloud.jdbc.antlr4.SwiftSqlParseUtil;
import com.fr.swift.cloud.jdbc.listener.SwiftSqlParserListenerImpl;
import com.fr.swift.cloud.jdbc.listener.handler.SwiftSqlBeanHandler;
import com.fr.swift.cloud.query.info.bean.element.filter.FilterInfoBean;
import com.fr.swift.cloud.query.info.bean.query.QueryInfoBean;
import com.fr.swift.cloud.util.Crasher;
import com.fr.swift.cloud.util.Strings;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * This class created on 2018/12/10
 *
 * @author Lucifer
 * @description
 * @since Advanced FineBI 5.0
 */
public class SwiftRequestParserVisitor implements JdbcRequestParserVisitor, ApiRequestParserVisitor {

    private static void setProperties(TableRequestInfo requestInfo, String authCode, String schema, String tableName) {
        requestInfo.setAuthCode(authCode);
        requestInfo.setDatabase(SwiftDatabase.fromKey(schema));
        requestInfo.setTable(tableName);
    }

    @Override
    public ApiInvocation visit(final SqlRequestInfo sqlRequestInfo) {
        final String sql = sqlRequestInfo.getSql();
        final String[] schema = {sqlRequestInfo.getDatabase()};
        final ApiInvocation[] result = {null};
        SwiftSqlBeanHandler swiftSqlBeanHandler = new SwiftSqlBeanHandler() {

            @Override
            public void handle(SelectionBean bean) {
                String queryJson = null;
                if (Strings.isNotEmpty(bean.getSchema())) {
                    schema[0] = bean.getSchema();
                }
                try {
                    QueryInfoBean queryInfoBean = bean.getQueryInfoBean();
                    queryInfoBean.setQueryId(sqlRequestInfo.getRequestId());
                    queryJson = JsonBuilder.writeJsonString(queryInfoBean);
                } catch (Exception e) {
                    ApiCrasher.crash(ParamErrorCode.PARAMS_PARSER_ERROR);
                }
                result[0] = createApiInvocation("query", SelectService.class,
                        SwiftDatabase.fromKey(schema[0]), queryJson, sql);
            }

            @Override
            public void handle(InsertionBean bean) {
                InsertRequestInfo requestInfo = new InsertRequestInfo();
                if (Strings.isNotEmpty(bean.getSchema())) {
                    schema[0] = bean.getSchema();
                }
                setProperties(requestInfo, sqlRequestInfo.getAuthCode(), schema[0], bean.getTableName());
                requestInfo.setSelectFields(bean.getFields());
                requestInfo.setData(bean.getRows());
                result[0] = visit(requestInfo);
            }

            @Override
            public void handle(CreationBean bean) {
                CreateTableRequestInfo requestInfo = new CreateTableRequestInfo();
                if (Strings.isNotEmpty(bean.getSchema())) {
                    schema[0] = sqlRequestInfo.getDatabase();
                }
                setProperties(requestInfo, sqlRequestInfo.getAuthCode(), schema[0], bean.getTableName());
                List<Column> columns = new ArrayList<Column>();
                for (ColumnBean columnBean : bean.getFields()) {
                    columns.add(new Column(columnBean.getColumnName(), columnBean.getColumnType()));
                }
                requestInfo.setColumns(columns);
                result[0] = visit(requestInfo);
            }

            @Override
            public void handle(DropBean bean) {
                if (Strings.isNotEmpty(bean.getSchema())) {
                    schema[0] = bean.getSchema();
                }
                TableRequestInfo requestInfo = new DropRequestInfo();
                setProperties(requestInfo, sqlRequestInfo.getAuthCode(), schema[0], bean.getTableName());
                result[0] = visit(requestInfo);
            }

            @Override
            public void handle(TruncateBean bean) {
                if (Strings.isNotEmpty(bean.getSchema())) {
                    schema[0] = bean.getSchema();
                }
                result[0] = createApiInvocation("truncateTable", TableService.class, SwiftDatabase.fromKey(schema[0]), bean.getTableName());
            }

            @Override
            public void handle(DeletionBean bean) {
                DeleteRequestInfo requestInfo = new DeleteRequestInfo();
                if (Strings.isNotEmpty(bean.getSchema())) {
                    schema[0] = bean.getSchema();
                }
                setProperties(requestInfo, sqlRequestInfo.getAuthCode(), schema[0], bean.getTableName());
                try {
                    requestInfo.setWhere(JsonBuilder.writeJsonString(bean.getFilter()));
                } catch (Exception e) {
                    ApiCrasher.crash(ParamErrorCode.PARAMS_PARSER_ERROR);
                }
                result[0] = visit(requestInfo);
            }
        };
        SwiftSqlParseUtil.parse(sql, new SwiftSqlParserListenerImpl(swiftSqlBeanHandler));
        return result[0];
    }

    @Override
    public ApiInvocation visit(ColumnsRequestInfo columnsRequestInfo) {
        String database = columnsRequestInfo.getDatabase();
        String table = columnsRequestInfo.getTable();
        SwiftDatabase swiftSchema = SwiftDatabase.fromKey(database);
        return createApiInvocation("detectiveMetaData", TableService.class, swiftSchema, table);
    }

    @Override
    public ApiInvocation visit(TablesRequestInfo tablesRequestInfo) {
        String database = tablesRequestInfo.getDatabase();
        SwiftDatabase swiftSchema = SwiftDatabase.fromKey(database);
        return createApiInvocation("detectiveAllTable", TableService.class, swiftSchema);
    }

    private ApiInvocation createApiInvocation(String method, Class<?> clazz, Object... arguments) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method1 : methods) {
            Class<?>[] paramTypes = method1.getParameterTypes();
            if (method1.getName().equals(method) && paramTypes.length == arguments.length) {
                boolean match = true;
                for (int i = 0; i < paramTypes.length; i++) {
                    if (null != arguments[i] && !ClassUtils.isAssignable(arguments[i].getClass(), paramTypes[i])) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    return new ApiInvocation(method, clazz, paramTypes, arguments);
                }
            }
        }
        return Crasher.crash(new NoSuchMethodException());
    }

    @Override
    public ApiInvocation visit(AuthRequestInfo authRequestInfo) {
        return createApiInvocation("detectiveAnalyseAndRealTime", DetectService.class, authRequestInfo.getFrom(), authRequestInfo.getSwiftUser(), authRequestInfo.getSwiftPassword());
    }

    @Override
    public ApiInvocation visit(QueryRequestInfo queryRequestInfo) {
        return createApiInvocation("query", SelectService.class, queryRequestInfo.getDatabase(), queryRequestInfo.getQueryJson());
    }

    @Override
    public ApiInvocation visit(CreateTableRequestInfo createTableRequestInfo) {
        return createApiInvocation("createTable", TableService.class, createTableRequestInfo.getDatabase(),
                createTableRequestInfo.getTable(), createTableRequestInfo.getColumns());
    }

    @Override
    public ApiInvocation visit(DeleteRequestInfo deleteRequestInfo) {
        try {
            String filter = deleteRequestInfo.getWhere();
            SwiftWhere where = new SwiftWhere(JsonBuilder.readValue(filter, FilterInfoBean.class));
            return createApiInvocation("delete", DataMaintenanceService.class, deleteRequestInfo.getDatabase(), deleteRequestInfo.getTable(), where);
        } catch (Exception e) {
            return ApiCrasher.crash(ParamErrorCode.PARAMS_PARSER_ERROR);
        }
    }

    @Override
    public ApiInvocation visit(InsertRequestInfo insertRequestInfo) {
        try {
            return createApiInvocation("insert", DataMaintenanceService.class,
                    insertRequestInfo.getDatabase(),
                    insertRequestInfo.getTable(),
                    insertRequestInfo.getSelectFields(),
                    insertRequestInfo.getData());
        } catch (Exception e) {
            return ApiCrasher.crash(ParamErrorCode.PARAMS_PARSER_ERROR);
        }
    }

    @Override
    public ApiInvocation visit(TableRequestInfo tableRequestInfo) {
        RequestType type = tableRequestInfo.getRequestType();
        switch (type) {
            case DROP_TABLE:
                return createApiInvocation("dropTable", TableService.class, tableRequestInfo.getDatabase(), tableRequestInfo.getTable());
            case TRUNCATE_TABLE:
                return createApiInvocation("truncateTable", TableService.class, tableRequestInfo.getDatabase(), tableRequestInfo.getTable());
            case DELETE:
                return visit((DeleteRequestInfo) tableRequestInfo);
            case INSERT:
                return visit((InsertRequestInfo) tableRequestInfo);
            case CREATE_TABLE:
                return visit((CreateTableRequestInfo) tableRequestInfo);
            default:

        }
        return null;
    }
}
