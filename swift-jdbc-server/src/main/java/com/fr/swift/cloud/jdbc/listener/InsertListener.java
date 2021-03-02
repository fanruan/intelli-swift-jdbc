package com.fr.swift.cloud.jdbc.listener;

import com.fr.swift.cloud.SwiftContext;
import com.fr.swift.cloud.config.service.SwiftMetaDataService;
import com.fr.swift.cloud.exception.meta.SwiftMetaDataException;
import com.fr.swift.cloud.jdbc.adaptor.InsertionBeanParser;
import com.fr.swift.cloud.jdbc.adaptor.bean.InsertionBean;
import com.fr.swift.cloud.jdbc.antlr4.SwiftSqlParseUtil;
import com.fr.swift.cloud.jdbc.antlr4.SwiftSqlParser;
import com.fr.swift.cloud.jdbc.antlr4.SwiftSqlParserBaseListener;
import com.fr.swift.cloud.jdbc.visitor.insert.InsertValueVisitor;
import com.fr.swift.cloud.source.Row;
import com.fr.swift.cloud.source.SourceKey;
import com.fr.swift.cloud.source.SwiftMetaData;
import com.fr.swift.cloud.source.SwiftMetaDataColumn;
import org.antlr.v4.runtime.tree.ErrorNodeImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yee
 * @date 2019-07-19
 */
public class InsertListener extends SwiftSqlParserBaseListener implements InsertionBeanParser {
    private InsertionBean insertionBean;
    private SwiftMetaDataService metaDataService = SwiftContext.get().getBean(SwiftMetaDataService.class);

    @Override
    public void enterInsert(SwiftSqlParser.InsertContext ctx) {
        List<Row> rows = new ArrayList<>();
        String tableName = ctx.name().getText();
        List<String> fields = new ArrayList<>();
        List<SwiftMetaDataColumn> metaDataColumns = new ArrayList<>();
        SwiftMetaData metaData = metaDataService.getMeta(new SourceKey(tableName));
        if (null == metaData) {
            visitErrorNode(new ErrorNodeImpl(ctx.table.start));
        }
        try {
            if (ctx.columnNames != null) {
                for (SwiftSqlParser.NameContext nameContext : ctx.columnNames.name()) {

                    String column = SwiftSqlParseUtil.trimQuote(nameContext.getText());
                    metaDataColumns.add(metaData.getColumn(column));
                    fields.add(column);
                }
            } else {
                for (int i = 0; i < metaData.getColumnCount(); i++) {
                    metaDataColumns.add(metaData.getColumn(i + 1));
                }
            }
        } catch (SwiftMetaDataException e) {
            visitErrorNode(new ErrorNodeImpl(null != ctx.columnNames ? ctx.columnNames.start : ctx.start));
        }
        InsertValueVisitor visitor = new InsertValueVisitor(metaDataColumns);
        for (SwiftSqlParser.ValuesContext value : ctx.values()) {
            rows.add(value.accept(visitor));
        }
        insertionBean = new InsertionBean();
        insertionBean.setFields(fields);
        insertionBean.setRows(rows);
        insertionBean.setTableName(tableName);
    }

    @Override
    public InsertionBean getInsertionBean() {
        return insertionBean;
    }

}
