package com.fr.swift.adaptor.transformer;

import com.finebi.conf.constant.BIConfConstants;
import com.finebi.conf.constant.ConfConstant.AnalysisType;
import com.finebi.conf.exception.FineAnalysisOperationUnSafe;
import com.finebi.conf.exception.FineEngineException;
import com.finebi.conf.internalimp.analysis.bean.operator.add.AddNewColumnBean;
import com.finebi.conf.internalimp.analysis.bean.operator.add.AddNewColumnValueBean;
import com.finebi.conf.internalimp.analysis.bean.operator.add.EmptyAddNewColumnBean;
import com.finebi.conf.internalimp.analysis.bean.operator.add.expression.AddExpressionValueBean;
import com.finebi.conf.internalimp.analysis.bean.operator.circulate.CirculateOneFieldBean;
import com.finebi.conf.internalimp.analysis.bean.operator.circulate.CirculateTwoFieldValue;
import com.finebi.conf.internalimp.analysis.bean.operator.confselect.ConfSelectBean;
import com.finebi.conf.internalimp.analysis.bean.operator.confselect.ConfSelectBeanItem;
import com.finebi.conf.internalimp.analysis.bean.operator.filter.FilterOperatorBean;
import com.finebi.conf.internalimp.analysis.bean.operator.group.DimensionSelectValue;
import com.finebi.conf.internalimp.analysis.bean.operator.group.DimensionSrcValue;
import com.finebi.conf.internalimp.analysis.bean.operator.group.DimensionValueBean;
import com.finebi.conf.internalimp.analysis.bean.operator.group.GroupBean;
import com.finebi.conf.internalimp.analysis.bean.operator.group.GroupSingleValueBean;
import com.finebi.conf.internalimp.analysis.bean.operator.group.GroupValueBean;
import com.finebi.conf.internalimp.analysis.bean.operator.group.ViewBean;
import com.finebi.conf.internalimp.analysis.bean.operator.join.JoinBean;
import com.finebi.conf.internalimp.analysis.bean.operator.join.JoinBeanValue;
import com.finebi.conf.internalimp.analysis.bean.operator.join.JoinNameItem;
import com.finebi.conf.internalimp.analysis.bean.operator.select.SelectFieldBean;
import com.finebi.conf.internalimp.analysis.bean.operator.select.SelectFieldBeanItem;
import com.finebi.conf.internalimp.analysis.bean.operator.sort.SortBean;
import com.finebi.conf.internalimp.analysis.bean.operator.sort.SortBeanItem;
import com.finebi.conf.internalimp.analysis.bean.operator.trans.ColumnInitalItem;
import com.finebi.conf.internalimp.analysis.bean.operator.trans.ColumnRowTransBean;
import com.finebi.conf.internalimp.analysis.bean.operator.trans.ColumnTransValue;
import com.finebi.conf.internalimp.analysis.bean.operator.union.UnionBean;
import com.finebi.conf.internalimp.analysis.bean.operator.union.UnionBeanValue;
import com.finebi.conf.internalimp.analysis.bean.operator.union.UnionBeanValueTable;
import com.finebi.conf.internalimp.analysis.operator.circulate.FloorItem;
import com.finebi.conf.internalimp.analysis.operator.select.SelectFieldOperator;
import com.finebi.conf.structure.analysis.operator.FineOperator;
import com.finebi.conf.structure.analysis.table.FineAnalysisTable;
import com.finebi.conf.structure.bean.field.FineBusinessField;
import com.finebi.conf.structure.bean.table.FineBusinessTable;
import com.finebi.conf.structure.conf.base.EngineComplexConfTable;
import com.finebi.conf.utils.FineTableUtils;
import com.fr.general.ComparatorUtils;
import com.fr.swift.exception.meta.SwiftMetaDataException;
import com.fr.swift.query.filter.info.FilterInfo;
import com.fr.swift.query.group.GroupType;
import com.fr.swift.query.group.impl.GroupImpl;
import com.fr.swift.segment.column.ColumnKey;
import com.fr.swift.source.DataSource;
import com.fr.swift.source.MetaDataColumn;
import com.fr.swift.source.SwiftMetaData;
import com.fr.swift.source.SwiftMetaDataColumn;
import com.fr.swift.source.SwiftMetaDataImpl;
import com.fr.swift.source.etl.AbstractOperator;
import com.fr.swift.source.etl.ETLOperator;
import com.fr.swift.source.etl.ETLSource;
import com.fr.swift.source.etl.columnfilter.ColumnFilterOperator;
import com.fr.swift.source.etl.columnrowtrans.ColumnRowTransOperator;
import com.fr.swift.source.etl.columnrowtrans.NameText;
import com.fr.swift.source.etl.detail.DetailOperator;
import com.fr.swift.source.etl.formula.ColumnFormulaOperator;
import com.fr.swift.source.etl.groupsum.SumByGroupDimension;
import com.fr.swift.source.etl.groupsum.SumByGroupOperator;
import com.fr.swift.source.etl.groupsum.SumByGroupTarget;
import com.fr.swift.source.etl.join.JoinColumn;
import com.fr.swift.source.etl.join.JoinOperator;
import com.fr.swift.source.etl.selfrelation.OneUnionRelationOperator;
import com.fr.swift.source.etl.selfrelation.TwoUnionRelationOperator;
import com.fr.swift.source.etl.sort.ColumnSortOperator;
import com.fr.swift.source.etl.union.UnionOperator;
import com.fr.swift.source.etl.utils.ETLConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Handsome on 2018/1/30 0030 16:38
 */
class EtlAdaptor {
    static DataSource adaptEtlDataSource(FineBusinessTable table) throws Exception {
        FineAnalysisTable analysis = ((FineAnalysisTable) table);
        List<DataSource> dataSources = new ArrayList<DataSource>();
        FineBusinessTable baseTable = analysis.getBaseTable();
        if (baseTable != null) {
            dataSources.add(IndexingDataSourceFactory.transformDataSource(baseTable));
        } else if (analysis.getOperator().getType() == AnalysisType.SELECT_FIELD) {
            Map<String, List<ColumnKey>> sourceKeyColumnMap = new LinkedHashMap<String, List<ColumnKey>>();
            Map<String, DataSource> sourceKeyDataSourceMap = new LinkedHashMap<String, DataSource>();
            SelectFieldOperator selectFieldOperator = analysis.getOperator();
            List<SelectFieldBeanItem> selectFieldBeanItemList = selectFieldOperator.getValue().getValue();
            for (SelectFieldBeanItem selectFieldBeanItem : selectFieldBeanItemList) {
                FineBusinessTable fineBusinessTable = FineTableUtils.getTableByFieldId(selectFieldBeanItem.getField());
                FineBusinessField fineBusinessField = fineBusinessTable.getFieldByFieldId(selectFieldBeanItem.getField());
                DataSource baseDataSource = IndexingDataSourceFactory.transformDataSource(fineBusinessTable);

                if (sourceKeyColumnMap.containsKey(baseDataSource.getSourceKey().getId())) {
                    sourceKeyColumnMap.get(baseDataSource.getSourceKey().getId()).add(new ColumnKey(fineBusinessField.getName()));
                } else {
                    sourceKeyColumnMap.put(baseDataSource.getSourceKey().getId(), new ArrayList<ColumnKey>());
                    sourceKeyColumnMap.get(baseDataSource.getSourceKey().getId()).add(new ColumnKey(fineBusinessField.getName()));
                }
                if (!sourceKeyDataSourceMap.containsKey(baseDataSource.getSourceKey().getId())) {
                    sourceKeyDataSourceMap.put(baseDataSource.getSourceKey().getId(), baseDataSource);
                }
            }
            List<DataSource> baseDatas = new ArrayList<DataSource>();
            List<SwiftMetaData> swiftMetaDatas = new ArrayList<SwiftMetaData>();
            List<ColumnKey[]> fields = new ArrayList<ColumnKey[]>();

            if (analysis.getBaseTable() != null) {
                baseDatas.add(IndexingDataSourceFactory.transformDataSource(analysis.getBaseTable()));
            }

            for (Map.Entry<String, List<ColumnKey>> entry : sourceKeyColumnMap.entrySet()) {
                DataSource dataSource = sourceKeyDataSourceMap.get(entry.getKey());
                baseDatas.add(dataSource);
                swiftMetaDatas.add(dataSource.getMetadata());
                fields.add(entry.getValue().toArray(new ColumnKey[entry.getValue().size()]));
            }
            ETLOperator operator = new DetailOperator(fields, swiftMetaDatas);
            Map<Integer, String> fieldsInfo = new HashMap<Integer, String>();
            ETLSource etlSource = new ETLSource(baseDatas, operator);
            for (ColumnKey[] columnKeys : fields) {
                for (ColumnKey columnKey : columnKeys) {
                    int index = etlSource.getMetadata().getColumnIndex(columnKey.getName());
                    fieldsInfo.put(index, columnKey.getName());
                }
            }
            ETLSource dataSource = new ETLSource(baseDatas, operator, fieldsInfo);
            return dataSource;
        }
//        if (analysis.getOperator().getType() == AnalysisType.SELECT_FIELD) {
//            return adaptSelectField(analysis);
//        }
        try {
            if (baseTable != null) {
                dataSources.add(IndexingDataSourceFactory.transformDataSource(baseTable));
            }
            FineOperator op = analysis.getOperator();
            dataSources.addAll(fromOperator(op));
            return new ETLSource(dataSources, adaptEtlOperator(op, table));
        } catch (Exception e) {
            return IndexingDataSourceFactory.transformDataSource(baseTable);
        }
    }

    private static DataSource adaptSelectField(FineAnalysisTable analysis) throws Exception {
        Map<String, List<ColumnKey>> sourceKeyColumnMap = new LinkedHashMap<String, List<ColumnKey>>();
        Map<String, DataSource> sourceKeyDataSourceMap = new LinkedHashMap<String, DataSource>();
        SelectFieldOperator selectFieldOperator = analysis.getOperator();
        List<SelectFieldBeanItem> selectFieldBeanItemList = selectFieldOperator.getValue().getValue();
        for (SelectFieldBeanItem selectFieldBeanItem : selectFieldBeanItemList) {
            FineBusinessTable fineBusinessTable = FineTableUtils.getTableByFieldId(selectFieldBeanItem.getField());
            FineBusinessField fineBusinessField = fineBusinessTable.getFieldByFieldId(selectFieldBeanItem.getField());
            DataSource baseDataSource = IndexingDataSourceFactory.transformDataSource(fineBusinessTable);

            if (sourceKeyColumnMap.containsKey(baseDataSource.getSourceKey().getId())) {
                sourceKeyColumnMap.get(baseDataSource.getSourceKey().getId()).add(new ColumnKey(fineBusinessField.getName()));
            } else {
                sourceKeyColumnMap.put(baseDataSource.getSourceKey().getId(), new ArrayList<ColumnKey>());
                sourceKeyColumnMap.get(baseDataSource.getSourceKey().getId()).add(new ColumnKey(fineBusinessField.getName()));
            }
            if (!sourceKeyDataSourceMap.containsKey(baseDataSource.getSourceKey().getId())) {
                sourceKeyDataSourceMap.put(baseDataSource.getSourceKey().getId(), baseDataSource);
            }
        }
        List<DataSource> baseDatas = new ArrayList<DataSource>();
        if (analysis.getBaseTable() != null) {
            baseDatas.add(IndexingDataSourceFactory.transformDataSource(analysis.getBaseTable()));
        }
        if (sourceKeyDataSourceMap.size() == 1) {
            //选字段只选了一张表的情况
            return getSingleTableSelectFieldSource(sourceKeyColumnMap, sourceKeyDataSourceMap, baseDatas);
        } else {
            return getMultiTableSelectFieldSource(sourceKeyColumnMap, sourceKeyDataSourceMap, baseDatas);
        }
    }

    private static DataSource getSingleTableSelectFieldSource(Map<String, List<ColumnKey>> sourceKeyColumnMap, Map<String, DataSource> sourceKeyDataSourceMap, List<DataSource> baseDatas) throws SwiftMetaDataException {
        ETLOperator operator = new DetailOperator(new ArrayList<ColumnKey[]>(), new ArrayList<SwiftMetaData>());
        Map<Integer, String> fieldsInfo = new HashMap<Integer, String>();
        baseDatas.add(sourceKeyDataSourceMap.values().iterator().next());
        ETLSource etlSource = new ETLSource(baseDatas, operator);
        List<ColumnKey> fields = sourceKeyColumnMap.values().iterator().next();
        for (ColumnKey columnKey : fields) {
            int index = etlSource.getMetadata().getColumnIndex(columnKey.getName());
            fieldsInfo.put(index, columnKey.getName());
        }
        ETLSource dataSource = new ETLSource(baseDatas, operator, fieldsInfo);
        return dataSource;
    }


    private static DataSource getMultiTableSelectFieldSource(Map<String, List<ColumnKey>> sourceKeyColumnMap, Map<String, DataSource> sourceKeyDataSourceMap, List<DataSource> baseDatas) throws SwiftMetaDataException {
        List<SwiftMetaData> swiftMetaDatas = new ArrayList<SwiftMetaData>();
        List<ColumnKey[]> fields = new ArrayList<ColumnKey[]>();
        for (Map.Entry<String, List<ColumnKey>> entry : sourceKeyColumnMap.entrySet()) {
            DataSource dataSource = sourceKeyDataSourceMap.get(entry.getKey());
            baseDatas.add(dataSource);
            swiftMetaDatas.add(dataSource.getMetadata());
            fields.add(entry.getValue().toArray(new ColumnKey[entry.getValue().size()]));
        }
        ETLOperator operator = new DetailOperator(fields, swiftMetaDatas);
        Map<Integer, String> fieldsInfo = new HashMap<Integer, String>();
        ETLSource etlSource = new ETLSource(baseDatas, operator);
        for (ColumnKey[] columnKeys : fields) {
            for (ColumnKey columnKey : columnKeys) {
                int index = etlSource.getMetadata().getColumnIndex(columnKey.getName());
                fieldsInfo.put(index, columnKey.getName());
            }
        }
        ETLSource dataSource = new ETLSource(baseDatas, operator, fieldsInfo);
        return dataSource;
    }

    private static List<DataSource> fromOperator(FineOperator op) throws Exception {
        List<DataSource> dataSources = new ArrayList<DataSource>();
        switch (op.getType()) {
            case AnalysisType.JOIN: {
                JoinBeanValue jbv = op.<JoinBean>getValue().getValue();
                FineBusinessTable busiTable = FineTableUtils.getTableByName(jbv.getTable().getName());
                dataSources.add(IndexingDataSourceFactory.transformDataSource(busiTable));
                break;
            }
            case AnalysisType.UNION:
                UnionBeanValue ubv = op.<UnionBean>getValue().getValue();
                for (UnionBeanValueTable table : ubv.getTables()) {
                    try {
                        FineBusinessTable busiTable = FineTableUtils.getTableByName(table.getName());
                        dataSources.add(IndexingDataSourceFactory.transformDataSource(busiTable));
                    } catch (Exception e) {
                        continue;
                    }
                }
                break;
            default:
        }
        return dataSources;
    }

    private static DetailOperator fromSelectFieldBean(SelectFieldBean sfb) throws FineEngineException {
        List<List<ColumnKey>> fieldsList = new ArrayList<List<ColumnKey>>();
        List<SwiftMetaData> metas = new ArrayList<SwiftMetaData>();

        for (SelectFieldBeanItem selectField : sfb.getValue()) {
            String fieldId = selectField.getField();
            FineBusinessTable table = FineTableUtils.getTableByFieldId(fieldId);

            SwiftMetaData meta = toMeta(table);
            String columnName = table.getFieldByFieldId(fieldId).getName();

            if (!metas.contains(meta)) {
                metas.add(meta);
                List<ColumnKey> columns = new ArrayList<ColumnKey>();
                columns.add(new ColumnKey(columnName));
                fieldsList.add(columns);
            } else {
                int index = metas.indexOf(meta);
                fieldsList.get(index).add(new ColumnKey(columnName));
            }
        }

        List<ColumnKey[]> columnsList = new ArrayList<ColumnKey[]>();
        for (List<ColumnKey> columns : fieldsList) {
            columnsList.add(columns.toArray(new ColumnKey[columns.size()]));
        }
        return new DetailOperator(columnsList, metas);
    }

    private static SwiftMetaData toMeta(FineBusinessTable table) {
        String name = table.getName();
        List<SwiftMetaDataColumn> columns = new ArrayList<SwiftMetaDataColumn>();
        for (FineBusinessField field : table.getFields()) {
            columns.add(new MetaDataColumn(field.getName(), field.getType(), field.getSize()));
        }
        return new SwiftMetaDataImpl(name, columns);
    }

    private static JoinOperator fromJoinBean(JoinBean jb) throws FineEngineException {
        JoinBeanValue jbv = jb.getValue();

        if (jbv.getBasis().isEmpty()) {
            throw new FineAnalysisOperationUnSafe("");
        }
        List<JoinColumn> joinColumns = new ArrayList<JoinColumn>();
        for (JoinNameItem joinName : jbv.getResult()) {
            JoinColumn jc = new JoinColumn(joinName.getName(), joinName.getIsLeft(), joinName.getColumnName());
            joinColumns.add(jc);
        }

        List<ColumnKey> leftColumns = new ArrayList<ColumnKey>();
        List<ColumnKey> rightColumns = new ArrayList<ColumnKey>();

        for (List<String> leftRight : jbv.getBasis()) {
            leftColumns.add(new ColumnKey(leftRight.get(0)));
            rightColumns.add(new ColumnKey(leftRight.get(1)));

        }

//        for (String column : jbv.getBasis().get(0).get(0)) {
//            leftColumns.add(new ColumnKey(column));
//        }
//        for (String column : jbv.getBasis().get(0).get(1)) {
//            rightColumns.add(new ColumnKey(column));
//        }

        int type = jbv.getStyle();

        return new JoinOperator(
                joinColumns,
                leftColumns.toArray(new ColumnKey[leftColumns.size()]),
                rightColumns.toArray(new ColumnKey[rightColumns.size()]),
                type);
    }

    private static UnionOperator fromUnionBean(UnionBean ub) {
        UnionBeanValue ubv = ub.getValue();

        List<List<String>> basis = ubv.getBasis();
        int basisSize = basis.size();
        List<List<ColumnKey>> listsOfColumn = new ArrayList<List<ColumnKey>>(basisSize);

        for (int i = 0; i < ubv.getResult().size(); i++) {
//            List<String> columns = basis.get(i);
            listsOfColumn.add(new ArrayList<ColumnKey>());
            listsOfColumn.get(i).add(new ColumnKey(ubv.getResult().get(i)));
            for (List<String> columnKeys : basis) {
                listsOfColumn.get(i).add(new ColumnKey(columnKeys.get(i)));
            }
        }

        return new UnionOperator(listsOfColumn);
    }
    public static ETLOperator adaptEtlOperator(FineOperator op, FineBusinessTable table) throws FineEngineException {
        switch (op.getType()) {
            case AnalysisType.SELECT_FIELD:
                return fromSelectFieldBean(op.<SelectFieldBean>getValue());
            case AnalysisType.JOIN:
                return fromJoinBean(op.<JoinBean>getValue());
            case AnalysisType.UNION:
                return fromUnionBean(op.<UnionBean>getValue());
            case AnalysisType.FILTER:
                return fromColumnFilterBean(op.<FilterOperatorBean>getValue());
            case AnalysisType.CIRCLE_ONE_FIELD_CALCULATE:
                return fromOneUnionRelationBean(op.<CirculateOneFieldBean>getValue());
            case AnalysisType.CIRCLE_TWO_FIELD_CALCULATE:
                return fromTwoUnionRelationBean(op.<CirculateOneFieldBean>getValue());
            case AnalysisType.COLUMN_ROW_TRANS:
                return fromColumnRowTransBean(op.<ColumnRowTransBean>getValue(), table);
            case AnalysisType.CONF_SELECT:
                return fromConfSelectBean(op.<ConfSelectBean>getValue());
            case AnalysisType.SORT:
                return fromColumnSortedBean(op.<SortBean>getValue());
            case AnalysisType.ADD_COLUMN:
                return fromAddNewColumnBean(op.<AddNewColumnBean>getValue());
            case AnalysisType.GROUP:
                return fromSumByGroupBean(op.<GroupBean>getValue());
            default:
        }
        return null;
    }


    private static ETLOperator fromConfSelectBean(ConfSelectBean bean) throws FineEngineException {
        List<List<ColumnKey>> fieldsList = new ArrayList<List<ColumnKey>>();
        List<SwiftMetaData> metas = new ArrayList<SwiftMetaData>();

        for (Map.Entry<String, ConfSelectBeanItem> entry : bean.getValue().entrySet()) {
            String fieldId = entry.getKey();
            FineBusinessTable table = FineTableUtils.getTableByFieldId(fieldId);

            SwiftMetaData meta = toMeta(table);
            String columnName = table.getFieldByFieldId(fieldId).getName();

            if (!metas.contains(meta)) {
                metas.add(meta);
                List<ColumnKey> columns = new ArrayList<ColumnKey>();
                columns.add(new ColumnKey(columnName));
                fieldsList.add(columns);
            } else {
                int index = metas.indexOf(meta);
                fieldsList.get(index).add(new ColumnKey(columnName));
            }
        }

//        for (SelectFieldBeanItem selectField : sfb.getValue()) {
//            String fieldId = selectField.getField();
//            FineBusinessTable table = FineTableUtils.getTableByFieldId(fieldId);
//
//            SwiftMetaData meta = toMeta(table);
//            String columnName = table.getFieldByFieldId(fieldId).getName();
//
//            if (!metas.contains(meta)) {
//                metas.add(meta);
//                List<ColumnKey> columns = new ArrayList<ColumnKey>();
//                columns.add(new ColumnKey(columnName));
//                fieldsList.add(columns);
//            } else {
//                int index = metas.indexOf(meta);
//                fieldsList.get(index).add(new ColumnKey(columnName));
//            }
//        }

        List<ColumnKey[]> columnsList = new ArrayList<ColumnKey[]>();
        for (List<ColumnKey> columns : fieldsList) {
            columnsList.add(columns.toArray(new ColumnKey[columns.size()]));
        }
        return new DetailOperator(columnsList, metas);
    }

    private static ColumnSortOperator fromColumnSortedBean(SortBean bean) throws FineEngineException {
        List<SortBeanItem> sortBeanItemList = bean.getValue();
        Map<String, Integer> fieldsSortedMap = new HashMap<String, Integer>();
        for (SortBeanItem sortBeanItem : sortBeanItemList) {
            fieldsSortedMap.put(sortBeanItem.getName(), sortBeanItem.getSortType());
        }
        if (fieldsSortedMap.isEmpty()) {
            throw new FineAnalysisOperationUnSafe("");
        }
        ColumnSortOperator operator = new ColumnSortOperator(fieldsSortedMap);
        return operator;
    }

    private static SumByGroupOperator fromSumByGroupBean(GroupBean bean) {
        GroupValueBean valueBean = bean.getValue();
        Map<String, DimensionValueBean> dimensionBean = valueBean.getDimensions();
        ViewBean viewBean = valueBean.getView();
        List<String> dimensions = viewBean.getDimension();
        List<String> views = viewBean.getViews();
        if (dimensionBean.isEmpty() || dimensions == null || views == null) {
            return null;
        }
        SumByGroupDimension[] groupDimensions = new SumByGroupDimension[dimensions.size()];
        SumByGroupTarget[] groupTargets = new SumByGroupTarget[views.size()];
        for (int i = 0; i < groupDimensions.length; i++) {
            DimensionValueBean tempBean = dimensionBean.get(dimensions.get(i));
            DimensionSrcValue srcValue = tempBean.getSrc();
            List<DimensionSelectValue> value = tempBean.getValue();
            // 分组类型
            int type = value.get(0).getType();
            SumByGroupDimension sumByGroupDimension = new SumByGroupDimension();
            sumByGroupDimension.setColumnType(ColumnTypeAdaptor.adaptColumnType(tempBean.getFieldType()));
            sumByGroupDimension.setGroup(new GroupImpl(GroupType.values()[type]));
            sumByGroupDimension.setName(srcValue.getFieldName());
            sumByGroupDimension.setNameText(tempBean.getName());
            groupDimensions[i] = sumByGroupDimension;
        }
        for (int i = 0; i < groupTargets.length; i++) {
            DimensionValueBean tempBean = dimensionBean.get(views.get(i));
            DimensionSrcValue srcValue = tempBean.getSrc();
            SumByGroupTarget sumByGroupTarget = new SumByGroupTarget();
            sumByGroupTarget.setColumnType(ColumnTypeAdaptor.adaptColumnType(tempBean.getFieldType()));
            sumByGroupTarget.setName(srcValue.getFieldName());
            sumByGroupTarget.setNameText(tempBean.getName());
            int type = ETLConstant.SUMMARY_TYPE.SUM;
            switch (tempBean.getValue().get(0).getType()) {
                case BIConfConstants.CONF.GROUP.TYPE.SINGLE:
                    type = ((GroupSingleValueBean) tempBean.getValue().get(0)).getValue();
                    break;
                case BIConfConstants.CONF.GROUP.TYPE.DOUBLE:
                    // TODO
                    break;
                default:
                    //TODO
                    break;
            }
            sumByGroupTarget.setSumType(type);
            groupTargets[i] = sumByGroupTarget;
        }
        return new SumByGroupOperator(groupTargets, groupDimensions);
    }

    private static AbstractOperator fromAddNewColumnBean(AddNewColumnBean bean) throws FineEngineException {
        if (bean.getValue() instanceof EmptyAddNewColumnBean) {
            throw new FineAnalysisOperationUnSafe("");
        }
        AddNewColumnValueBean value = bean.getValue();
        switch (value.getType()) {
            case BIConfConstants.CONF.ADD_COLUMN.FORMULA.TYPE: {
                return new ColumnFormulaOperator(value.getName(), ColumnTypeAdaptor.adaptColumnType(value.getType()), ((AddExpressionValueBean) value).getValue());
            }
            default:
        }
        return null;
    }

    private static ColumnFilterOperator fromColumnFilterBean(FilterOperatorBean bean) {
        FilterInfo filterInfo = FilterFactory.transformFilter(bean.getValue());
        return new ColumnFilterOperator(filterInfo);
    }

    private static int findFieldName(List<FineBusinessField> fields, String fieldID) {
        int index = Integer.MIN_VALUE;
        for (int i = 0; i < fields.size(); i++){
            if (ComparatorUtils.equals(fields.get(i).getId(), fieldID)){
                index = i;
                break;
            }
        }
        return index;
    }

    private static ColumnRowTransOperator fromColumnRowTransBean(ColumnRowTransBean bean, FineBusinessTable table) {
        ColumnTransValue value = bean.getValue();
        FineBusinessTable preTable = ((EngineComplexConfTable)table).getBaseTableBySelected(0);
        List<FineBusinessField> fields = preTable.getFields();
        String groupName = fields.get(findFieldName(fields, value.getAccordingField())).getName();
        String lcName = fields.get(findFieldName(fields, value.getFieldId())).getName();
        List<NameText> lcValue = new ArrayList<NameText>();
        for (int i = 0; i < value.getValues().size(); i++) {
            ColumnInitalItem item = value.getValues().get(i);
            NameText nameText = new NameText(item.getOldValue(), item.getNewValue());
            lcValue.add(nameText);
        }
        List<NameText> columns = new ArrayList<NameText>();
        List<String> otherColumnNames = new ArrayList<String>();
        for (int i = 0; i < value.getInitialFields().size(); i++) {
            ColumnInitalItem item = value.getInitialFields().get(i);
            if (!groupName.equals(item.getOldValue()) && !lcName.equals(item.getOldValue())) {

                if (item.isSelected()) {
                    columns.add(new NameText(item.getOldValue(), item.getNewValue()));
                } else {
                    otherColumnNames.add(item.getOldValue());
                }
            }
        }
        return new ColumnRowTransOperator(groupName, lcName, lcValue, columns, otherColumnNames);
    }

    private static OneUnionRelationOperator fromOneUnionRelationBean(CirculateOneFieldBean bean) {
        CirculateTwoFieldValue value = bean.getValue();
        LinkedHashMap<String, Integer> columns = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < value.getFloors().size(); i++) {
            FloorItem item = value.getFloors().get(i);
            columns.put(item.getName(), item.getLength());
        }
        return new OneUnionRelationOperator(value.getIdField(), value.getShowFields(), columns, value.getFieldType(), null);
    }

    private static TwoUnionRelationOperator fromTwoUnionRelationBean(CirculateOneFieldBean bean) {
        CirculateTwoFieldValue value = bean.getValue();
        LinkedHashMap<String, Integer> columns = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < value.getFloors().size(); i++) {
            FloorItem item = value.getFloors().get(i);
            columns.put(item.getName(), item.getLength());
        }
        return new TwoUnionRelationOperator(value.getIdField(), value.getShowFields(), columns, value.getType(), null, value.getParentIdField());
    }
}