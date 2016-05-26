package com.fr.bi.field.filtervalue.date.nonefilter;

import com.finebi.cube.conf.table.BusinessTable;
import com.finebi.cube.api.ICubeDataLoader;
import com.finebi.cube.api.ICubeTableService;
import com.fr.bi.stable.engine.index.utils.TableIndexUtils;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.report.key.TargetGettingKey;
import com.fr.bi.stable.report.result.DimensionCalculator;
import com.fr.bi.stable.report.result.LightNode;


public class DateNullFilterValue extends DateNoneValueFilterValue {

    /**
     *
     */
    private static final long serialVersionUID = 5953373068751248573L;

    /**
     * 获取过滤后的索引
     *
     * @return 过滤索引
     */
    @Override
    public GroupValueIndex createFilterIndex(DimensionCalculator dimension, BusinessTable target, ICubeDataLoader loader, long userId) {
        ICubeTableService ti = loader.getTableIndex(dimension.getField().getTableBelongTo().getTableSource());
        GroupValueIndex gvi = TableIndexUtils.createLinkNullGVI(ti, dimension.getRelationList(), loader);
        return gvi == null ? ti.getNullGroupValueIndex(dimension.createKey()) : gvi;
    }

    @Override
    public boolean isMatchValue(Long v) {
        return v == null;
    }

    @Override
    public boolean showNode(LightNode node, TargetGettingKey targetKey, ICubeDataLoader loader) {
        return false;
    }
}