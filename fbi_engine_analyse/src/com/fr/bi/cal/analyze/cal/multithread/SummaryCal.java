package com.fr.bi.cal.analyze.cal.multithread;import com.finebi.cube.api.ICubeDataLoader;import com.finebi.cube.api.ICubeTableService;import com.fr.bi.cal.analyze.cal.index.loader.TargetAndKey;import com.fr.bi.cal.analyze.cal.result.Node;import com.fr.bi.stable.gvi.GroupValueIndex;public class SummaryCal implements BISingleThreadCal {    protected Node node;    protected TargetAndKey targetAndKey;    protected GroupValueIndex gvi;    private ICubeTableService ti;    private ICubeDataLoader loader;    public SummaryCal(ICubeTableService ti, Node node, TargetAndKey targetAndKey, GroupValueIndex gvi, ICubeDataLoader loader) {        this.ti = ti;        this.node = node;        this.targetAndKey = targetAndKey;        this.gvi = gvi;        this.loader = loader;    }    @Override    public void cal() {        if (node.getSummaryValue(targetAndKey.getTargetGettingKey()) == null){            targetAndKey.getCalculator().calculateFilterIndex(loader);            targetAndKey.getCalculator().doCalculator(ti, node, gvi, targetAndKey.getTargetGettingKey());        }    }}