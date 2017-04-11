package com.fr.bi.cal.analyze.cal.sssecret.diminfo;

import com.finebi.cube.api.ICubeDataLoader;
import com.finebi.cube.api.ICubeTableService;
import com.fr.bi.cal.analyze.cal.sssecret.MetricMergeResult;
import com.fr.bi.cal.analyze.cal.sssecret.mergeiter.MergeIterator;
import com.fr.bi.stable.engine.cal.DimensionIterator;
import com.fr.bi.stable.gvi.GroupValueIndex;

import java.util.Comparator;
import java.util.Iterator;

/**
 * Created by 小灰灰 on 2017/1/11.
 */
public class SimpleMergeIteratorCreator implements MergeIteratorCreator {
    @Override
    public Iterator<MetricMergeResult> createIterator(DimensionIterator[] iterators, int sumLength, GroupValueIndex[] gvis, Comparator c, ICubeTableService[] tis, ICubeDataLoader loader) {
        return new MergeIterator(iterators, gvis, c, sumLength);
    }

    @Override
    public boolean isSimple() {
        return true;
    }
}
