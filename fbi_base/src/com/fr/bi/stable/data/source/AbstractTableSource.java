package com.fr.bi.stable.data.source;

import com.finebi.cube.api.ICubeColumnIndexReader;
import com.finebi.cube.api.ICubeDataLoader;
import com.finebi.cube.api.ICubeTableService;
import com.fr.base.TableData;
import com.fr.bi.base.BICore;
import com.fr.bi.base.BICoreGenerator;
import com.fr.bi.common.BIMD5CoreWrapper;
import com.fr.bi.common.inter.Traversal;
import com.fr.bi.exception.BIAmountLimitUnmetException;
import com.fr.bi.stable.constant.BIBaseConstant;
import com.fr.bi.stable.constant.BIJSONConstant;
import com.fr.bi.stable.data.db.*;
import com.fr.bi.stable.engine.index.key.IndexKey;
import com.fr.bi.stable.utils.code.BILogger;
import com.fr.general.ComparatorUtils;
import com.fr.json.JSONArray;
import com.fr.json.JSONObject;
import com.fr.stable.xml.XMLPrintWriter;
import com.fr.stable.xml.XMLableReader;

import javax.activation.UnsupportedDataTypeException;
import java.util.*;

/**
 * Created by GUY on 2015/3/3.
 */
public abstract class AbstractTableSource implements ICubeTableSource {

    /**
     *
     */
    private static final long serialVersionUID = -8657998191260725924L;
    //表的唯一标识
    protected Map<String, ICubeFieldSource> fields = new LinkedHashMap<String, ICubeFieldSource>();
    protected PersistentTable dbTable;

    protected AbstractTableSource() {

    }

    @Override
    public String getSourceID() {
        return fetchObjectCore().getIDValue();
    }

    @Override
    public BICore fetchObjectCore() {
        return new BICoreGenerator(this).fetchObjectCore();
    }

    //重新获取数据 guy
    public IPersistentTable reGetBiTable() {
        dbTable = null;
        return getPersistentTable();
    }

    @Override
    public Set<ICubeFieldSource> getParentFields(Set<ICubeTableSource> sources) {
        return new HashSet<ICubeFieldSource>();
    }

    @Override
    public Set<ICubeFieldSource> getFacetFields(Set<ICubeTableSource> sources) {
        return getSelfFields(sources);
    }

    @Override
    public Set<ICubeFieldSource> getSelfFields(Set<ICubeTableSource> sources) {
        Set<ICubeFieldSource> result = new HashSet<ICubeFieldSource>();
        ICubeFieldSource[] fields = getFieldsArray(sources);
        for (ICubeFieldSource field : fields) {
            result.add(field);
        }
        return result;
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public long read4Part(Traversal<BIDataValue> travel, BICubeFieldSource[] field, ICubeDataLoader loader, int start, int end) {
        return read(travel, field, loader);
    }

    @Override
    public JSONObject createPreviewJSONFromCube(ArrayList<String> fields, ICubeDataLoader loader) throws Exception {
        ICubeTableService tableIndex = loader.getTableIndex(this);
        return createPreviewJSONFromTableIndex(fields, tableIndex);
    }

    public JSONObject createPreviewJSONFromTableIndex(ArrayList<String> fields, ICubeTableService tableIndex) throws Exception {
        JSONArray allFieldNamesJo = new JSONArray();
        JSONArray fieldValues = new JSONArray();
        JSONArray fieldTypes = new JSONArray();
        for (PersistentField column : getPersistentTable().getFieldList()) {
            if (!fields.isEmpty() && !fields.contains(column.getFieldName())) {
                continue;
            }
            allFieldNamesJo.put(column.getFieldName());
            fieldTypes.put(column.getBIType());
            JSONArray values = new JSONArray();
            fieldValues.put(values);
            int count = Math.min(tableIndex.getRowCount(), BIBaseConstant.PREVIEW_COUNT);
            for (int i = 0; i < count; i++) {
                values.put(tableIndex.getRowValue(new IndexKey(column.getFieldName()), i));
            }
        }
        return new JSONObject().put(BIJSONConstant.JSON_KEYS.FIELDS, allFieldNamesJo).put(BIJSONConstant.JSON_KEYS.VALUE, fieldValues).put(BIJSONConstant.JSON_KEYS.TYPE, fieldTypes);
    }


    @Override
    public Map<Integer, Set<ICubeTableSource>> createGenerateTablesMap() {
        Map<Integer, Set<ICubeTableSource>> generateTable = new HashMap<Integer, Set<ICubeTableSource>>();
        Set<ICubeTableSource> set = new HashSet<ICubeTableSource>();
        set.add(this);
        generateTable.put(0, set);
        return generateTable;
    }

    @Override
    public IPersistentTable getPersistentTable() {
        return null;
    }

    @Override
    public List<Set<ICubeTableSource>> createGenerateTablesList() {
        List<Set<ICubeTableSource>> generateTable = new ArrayList<Set<ICubeTableSource>>();
        Set<ICubeTableSource> set = new HashSet<ICubeTableSource>();
        set.add(this);
        generateTable.add(set);
        return generateTable;
    }

    @Override
    public boolean isIndependent() {
        return true;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public long read(Traversal<BIDataValue> travel, BICubeFieldSource[] field, ICubeDataLoader loader) {
        return 0;
    }

    @Override
    public Set getFieldDistinctNewestValues(String fieldName, ICubeDataLoader loader, long userId) {
        return null;
    }

    @Override
    public JSONObject createPreviewJSON(ArrayList<String> fields, ICubeDataLoader loader, long userId) throws Exception {
        return null;
    }

    @Override
    public TableData createTableData(List<String> fields, ICubeDataLoader loader, long userId) throws Exception {
        return null;
    }

    @Override
    public Set getFieldDistinctValuesFromCube(String fieldName, ICubeDataLoader loader, long userId) {
        HashSet set = new HashSet();
        ICubeTableService tableIndex = loader.getTableIndex(this);
        ICubeColumnIndexReader getter = tableIndex.loadGroup(new IndexKey(fieldName));
        if (getter != null) {
            Iterator<Map.Entry> it = getter.iterator();
            while (it.hasNext()) {
                set.add(it.next().getKey());
            }
        }
        return set;
    }


    protected PersistentTable createBITable() {
        return new PersistentTable(null, fetchObjectCore().getID().getIdentityValue(), null);
    }

    public Map<String, ICubeFieldSource> getFields() {
        try {
            this.fields = synchronousFieldsInforFromDB();
        } catch (Exception e) {
            BILogger.getLogger().error(e.getMessage(), e);
        }
        return this.fields;
    }

    /**
     * 根据sources获取fields, 用来生成cube,判断cube版本
     *
     * @param sources generatingobjects 的packs的sources
     * @return 字段
     */
    @Override
    public ICubeFieldSource[] getFieldsArray(Set<ICubeTableSource> sources) {
        return getFields().values().toArray(new ICubeFieldSource[getFields().values().size()]);
    }

    private Map<String, ICubeFieldSource> synchronousFieldsInforFromDB() {
        Map<String, ICubeFieldSource> fields = new LinkedHashMap<String, ICubeFieldSource>();
        IPersistentTable bt = getPersistentTable();
        if (bt == null) {
            throw new NullPointerException();
        }
        List<ICubeFieldSource> list = new ArrayList<ICubeFieldSource>();
        for (int i = 0, len = bt.getFieldSize(); i < len; i++) {
            PersistentField column = bt.getField(i);
            ICubeFieldSource field = column.toDBField(this);
            list.add(field);
        }
        for (ICubeFieldSource field : list) {
            String fieldName = field.getFieldName();
            ICubeFieldSource old = this.fields.get(fieldName);
            boolean isUsable = old == null || old.isUsable();
            fields.put(fieldName, field);
        }

        return fields;
    }

    @Override
    public Set<String> getUsedFields(ICubeTableSource source) {
        if (ComparatorUtils.equals(source.fetchObjectCore(), fetchObjectCore())) {
            return getFields().keySet();
        }
        return new HashSet<String>();
    }

    @Override
    public void refresh() {
        PersistentTable temp = dbTable;
        try {
            if (reGetBiTable() == null) {
                dbTable = temp;
            }
        } catch (Exception e) {
            dbTable = temp;
        }

    }

    public void envChange() {
        fields.clear();
        dbTable = null;
    }

    public void setMd5(String md5) {
//        this.md5 = md5;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            BILogger.getLogger().info(e.getMessage());
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AbstractTableSource && ComparatorUtils.equals(fetchObjectCore(), ((AbstractTableSource) (obj)).fetchObjectCore());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
//        result = prime * result
//                + ((fetchObjectCore() == null) ? 0 : md5.hashCode());
        return result;
    }

    @Override
    public JSONObject createJSON() throws Exception {
        JSONObject jo = new JSONObject();
        JSONArray ja = new JSONArray();
        jo.put("md5", fetchObjectCore().getID().getIdentityValue());
        List<JSONObject> stringList = new ArrayList<JSONObject>();
        List<JSONObject> numberList = new ArrayList<JSONObject>();
        List<JSONObject> dateList = new ArrayList<JSONObject>();
        Map<String, ICubeFieldSource> fields = getFields();
        for (Map.Entry<String, ICubeFieldSource> entry : fields.entrySet()) {
            ICubeFieldSource field = entry.getValue();
            stringList.add(field.createJSON());
        }
        ja.put(stringList).put(numberList).put(dateList);
        jo.put("fields", ja);
        return jo;
    }

    @Override
    public void readXML(XMLableReader reader) {
        if (reader.isAttr()) {
//            md5 = reader.getAttrAsString("md5", StringUtils.EMPTY);
        }
    }

    @Override
    public void writeXML(XMLPrintWriter writer) {
//        writer.attr("md5", md5);
    }

    @Override
    public boolean needGenerateIndex() {
        return true;
    }

    /**
     * 所有的source,包括parents
     *
     * @return
     */
    @Override
    public Map<BICore, ICubeTableSource> createSourceMap() {
        Map<BICore, ICubeTableSource> map = new HashMap<BICore, ICubeTableSource>();
        map.put(fetchObjectCore(), this);
        return map;
    }

    public class BITableSourceCore extends BIMD5CoreWrapper {
        public BITableSourceCore(Object... attributes) throws UnsupportedDataTypeException, BIAmountLimitUnmetException {
            this("");
        }

        public BITableSourceCore(String md5) throws UnsupportedDataTypeException, BIAmountLimitUnmetException {
            super(md5);
        }
    }

    @Override
    public SourceFile getSourceFile() {
        return new SourceFile(fetchObjectCore().getID().getIdentityValue());
    }
}