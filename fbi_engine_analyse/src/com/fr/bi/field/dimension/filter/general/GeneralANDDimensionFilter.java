package com.fr.bi.field.dimension.filter.general;


import com.finebi.cube.api.ICubeDataLoader;
import com.finebi.cube.conf.table.BusinessTable;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.report.result.DimensionCalculator;
import com.fr.bi.stable.report.result.LightNode;
import com.fr.bi.stable.report.result.TargetCalculator;

import java.util.Map;

public class GeneralANDDimensionFilter extends GeneralDimensionFilter {


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GeneralANDDimensionFilter)) {
            return false;
        }

        return super.equals(o);
    }

    @Override
    public GroupValueIndex createFilterIndex(DimensionCalculator dimension, BusinessTable target, ICubeDataLoader loader, long userId) {
        GroupValueIndex index = null;
        for (int i = 0; i < childs.length; i++) {
            GroupValueIndex gvi = childs[i] == null ? null : childs[i].createFilterIndex(dimension, target, loader, userId);
            if (index == null) {
                index = gvi;
            } else {
                index = index.AND(gvi);
            }
        }
        return index;
    }

    @Override
    public boolean showNode(LightNode node, Map<String, TargetCalculator> targetsMap, ICubeDataLoader loader) {
        for (int i = 0, len = childs.length; i < len; i++) {
            if (childs[i] != null && (!childs[i].showNode(node, targetsMap, loader))) {
                return false;
            }
        }
        return true;
    }
}