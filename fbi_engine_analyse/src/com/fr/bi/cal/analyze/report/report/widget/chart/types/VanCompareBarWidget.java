package com.fr.bi.cal.analyze.report.report.widget.chart.types;

import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.bi.field.target.target.BISummaryTarget;
import com.fr.bi.stable.constant.BIReportConstant;
import com.fr.json.JSONArray;
import com.fr.json.JSONException;
import com.fr.json.JSONObject;

/**
 * Created by eason on 2017/2/27.
 */
public class VanCompareBarWidget extends VanCompareColumnWidget{

    protected int numberLevel(String dimensionID){
        int level = BIReportConstant.TARGET_STYLE.NUM_LEVEL.NORMAL;
        try {
            JSONObject settings = this.getDetailChartSetting();
            level = settings.optInt("leftYNumberLevel", level);
        }catch (Exception e){
            BILoggerFactory.getLogger().error(e.getMessage(),e);
        }

        return level;
    }

    public boolean isInverted(){
        return true;
    }

    protected void dealYAxisDiffDefaultSettings(JSONObject settings) throws JSONException {
    }

    protected JSONArray dealSeriesWithEmptyAxis(JSONArray series) throws JSONException{
        for(int i = 0, len = series.length(); i < len; i++){
            JSONObject ser = series.getJSONObject(i);

            int yAxisIndex = ser.optInt("yAxis");
            if(yAxisIndex == 0){
                ser.put("xAxis", 1);
                JSONArray datas = ser.optJSONArray("data");
                String valueKey = this.valueKey();
                for (int j = 0, size = datas.length(); j < size; j++) {
                    JSONObject point = datas.getJSONObject(j);
                    point.put(valueKey, -point.optDouble(valueKey));
                }
            }

            ser.put("yAxis", 0);
        }

        return series;
    }

    protected String valueFormatFunc(BISummaryTarget dimension, boolean isTooltip) {

        int index = yAxisIndex(dimension.getValue());

        String format = this.valueFormat(dimension, isTooltip);

        return index == 0 ? String.format("function(){return BI.contentFormat(-arguments[0], \"%s\")}", format)
                : String.format("function(){return BI.contentFormat(arguments[0], \"%s\")}", format);
    }

    protected String labelString(int yAxis){
        return "Math.abs(arguments[0])";
    }

}
