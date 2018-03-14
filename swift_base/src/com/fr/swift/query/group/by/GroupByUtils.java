package com.fr.swift.query.group.by;

import com.fr.swift.query.aggregator.Aggregator;
import com.fr.swift.query.aggregator.AggregatorValue;
import com.fr.swift.query.filter.detail.DetailFilter;
import com.fr.swift.query.group.by.paging.GroupByPagingIterator;
import com.fr.swift.query.sort.Sort;
import com.fr.swift.query.sort.SortType;
import com.fr.swift.result.GroupByResultSet;
import com.fr.swift.result.GroupByResultSetImpl;
import com.fr.swift.result.KeyValue;
import com.fr.swift.result.RowIndexKey;
import com.fr.swift.segment.column.Column;
import com.fr.swift.segment.column.DictionaryEncodedColumn;
import com.fr.swift.structure.iterator.RowTraversal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 这边group by作用于cube的分块数据（segment）
 * Created by Lyon on 2018/2/27.
 */
public class GroupByUtils {

    public static GroupByResultSet query(List<Column> dimensions, List<Column> metrics, List<Aggregator> aggregators,
                                         DetailFilter filter, List<Sort> indexSorts, int[] cursor, int pageSize) {
        boolean[] asc;
        if (indexSorts == null) {
            asc = new boolean[dimensions.size()];
            Arrays.fill(asc, true);
        } else {
            asc = getSorts(indexSorts);
        }
        Iterator<KeyValue<RowIndexKey, RowTraversal>> groupByIterator = new MultiDimensionGroupBy(dimensions, filter, cursor, asc);
        if (pageSize != -1) {
            // 分页的情况
            groupByIterator = new GroupByPagingIterator(pageSize, groupByIterator);
        }
        List<KeyValue<RowIndexKey, AggregatorValue[]>> rowResult = new ArrayList<KeyValue<RowIndexKey, AggregatorValue[]>>();
        List<Map<Integer, Object>> globalDictionaries= createGlobalDictionaries(dimensions.size());
        List<DictionaryEncodedColumn> dictionaries = getDictionaries(dimensions);
        while (groupByIterator.hasNext()) {
            KeyValue<RowIndexKey, RowTraversal> keyValue = groupByIterator.next();
            int[] key = keyValue.getKey().getKey();
            AggregatorValue[] values = aggregateRow(keyValue.getValue(), metrics, aggregators);
            rowResult.add(new KeyValue<RowIndexKey, AggregatorValue[]>(toGlobalIndex(key, dictionaries), values));
            updateGlobalDictionaries(key, globalDictionaries, dictionaries);
        }
        return new GroupByResultSetImpl(rowResult.iterator(), globalDictionaries, indexSorts);
    }

    private static boolean[] getSorts(List<Sort> sorts) {
        // 这边sorts的size能保证和维度的size相同吗？
        boolean[] asc = new boolean[sorts.size()];
        Arrays.fill(asc, false);
        if (sorts == null) {
            return asc;
        }
        for (int i = 0; i < asc.length; i++) {
            if (sorts.get(i).getSortType() == SortType.ASC) {
                asc[i] = true;
            }
        }
        return asc;
    }

    private static List<DictionaryEncodedColumn> getDictionaries(List<Column> dimensions) {
        List<DictionaryEncodedColumn> dictionaries = new ArrayList<DictionaryEncodedColumn>();
        for (Column column : dimensions) {
            dictionaries.add(column.getDictionaryEncodedColumn());
        }
        return dictionaries;
    }

    private static void updateGlobalDictionaries(int[] segmentIndexes, List<Map<Integer, Object>> globalDictionaries,
                                                 List<DictionaryEncodedColumn> dictionaries) {
        for (int i = 0; i < segmentIndexes.length; i++) {
            if (segmentIndexes[i] == -1) {
                continue;
            }
            int globalIndex = dictionaries.get(i).getGlobalIndexByIndex(segmentIndexes[i]);
            if (globalDictionaries.get(i).containsKey(globalIndex)) {
                continue;
            }
            globalDictionaries.get(i).put(globalIndex, dictionaries.get(i).getValue(segmentIndexes[i]));
        }
    }

    private static List<Map<Integer, Object>> createGlobalDictionaries(int dimensionSize) {
        List<Map<Integer, Object>> dictionaries = new ArrayList<Map<Integer, Object>>();
        for (int i = 0; i < dimensionSize; i++) {
            dictionaries.add(new HashMap<Integer, Object>());
        }
        return dictionaries;
    }

    private static RowIndexKey toGlobalIndex(int[] segmentIndexes, List<DictionaryEncodedColumn> dictionaries) {
        int[] globalIndexes = new int[segmentIndexes.length];
        Arrays.fill(globalIndexes, -1);
        for (int i = 0; i < segmentIndexes.length; i++) {
            if (segmentIndexes[i] == -1) {
                continue;
            }
            globalIndexes[i] = dictionaries.get(i).getGlobalIndexByIndex(segmentIndexes[i]);
        }
        return new RowIndexKey(globalIndexes);
    }

    private static AggregatorValue[] aggregateRow(RowTraversal traversal, List<Column> metrics,
                                                  List<Aggregator> aggregators) {
        AggregatorValue[] values = new AggregatorValue[metrics.size()];
        for (int i = 0; i < metrics.size(); i++) {
            // 如果指标比较多，这边也可以增加多线程计算
            values[i] = aggregators.get(i).aggregate(traversal, metrics.get(i));
        }
        return values;
    }
}
