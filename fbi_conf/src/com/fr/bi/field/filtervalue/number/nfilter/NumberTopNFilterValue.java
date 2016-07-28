package com.fr.bi.field.filtervalue.number.nfilter;

import com.finebi.cube.api.ICubeDataLoader;
import com.fr.bi.conf.report.widget.field.filtervalue.NFilterValue;
import com.fr.bi.stable.report.key.TargetGettingKey;
import com.fr.bi.stable.report.result.LightNode;
import com.fr.bi.field.target.key.sum.AvgKey;


public class NumberTopNFilterValue extends NumberNFilterValue implements NFilterValue{
    /**
	 * 
	 */
	private static final long serialVersionUID = -3103968806193045243L;
	public static String XML_TAG = "NumberTopNFilterValue";

    /**
     * 是否显示记录
     *
     * @param node      节点
     * @param targetKey 指标信息
     * @return 是否显示
     */
    @Override
    public boolean showNode(LightNode node, TargetGettingKey targetKey, ICubeDataLoader loader) {
        LightNode parentNode = node.getParent();
        double nline = parentNode.getChildTOPNValueLine(targetKey, n);
        //FIXME 不存在的值怎么处理呢
        Number targetValue = node.getSummaryValue(targetKey);
        //FIXME 汇总方式求平均时node.getSummaryValue(targetKey)拿不到值，只能算一下
        if(targetKey.getTargetKey() instanceof AvgKey){
            String targetName = targetKey.getTargetName();
            AvgKey avgKey = (AvgKey) targetKey.getTargetKey();
            TargetGettingKey sumGettingKey = new TargetGettingKey(avgKey.getSumKey(), targetName);
            TargetGettingKey countGettingKey = new TargetGettingKey(avgKey.getCountKey(), targetName);
            Number sumValue = node.getSummaryValue(sumGettingKey);
            Number countValue = node.getSummaryValue(countGettingKey);
            double avgValue = 0;
            if (sumValue != null && countValue != null) {
                avgValue = sumValue.doubleValue() / countValue.doubleValue();
            }
            targetValue = avgValue;
        }
        return targetValue == null ? false : targetValue.doubleValue() >= nline;
    }
}