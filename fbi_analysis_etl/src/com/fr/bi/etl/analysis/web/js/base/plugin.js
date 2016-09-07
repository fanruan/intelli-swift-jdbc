BI.Plugin = BI.Plugin || {}

BI.Plugin.registerWidget("bi.detail_detail_select_data_level0_node", function(el){
    if (BI.Utils.getTableTypeByID(el['id']) == ETLCst.BUSINESS_TABLE_TYPE.ANALYSIS_TYPE){
        return BI.extend({}, el, {type: "bi.analysis_detail_detail_select_data_level0_node"});
    }
});
BI.Plugin.registerWidget("bi.detail_select_data_level0_node", function(el){
    if (BI.Utils.getTableTypeByID(el['id']) == ETLCst.BUSINESS_TABLE_TYPE.ANALYSIS_TYPE){
        return BI.extend({}, el, {type: "bi.analysis_detail_select_data_level0_node"});
    }
});

BI.Plugin.registerWidget("bi.detail_detail_select_data_level0_node", function(el){
    if (BI.Utils.getTableTypeByID(el['id']) == ETLCst.BUSINESS_TABLE_TYPE.ANALYSIS_TYPE){
        return BI.extend({}, el, {type: "bi.select_data_level" + ETLCst.BUSINESS_TABLE_TYPE.ANALYSIS_TYPE + "_node"});
    }
});
BI.Plugin.DATA_STYLE_TAB_ITEM = BI.Plugin.DATA_STYLE_TAB_ITEM || [];
(function () {
    var registered = BI.Plugin.getWidget("bi.data_style_tab");
    if (BI.isNull(registered)){
        BI.Plugin.registerWidget("bi.data_style_tab", function(el){
            var _createMainModel = function (wId) {
                var self = this, model = {}, items = [];
                var widget = BI.Utils.getWidgetCalculationByID(wId);
                widget.view = widget.view || {};
                var dim1 = widget.view[BICst.REGION.DIMENSION1] || [];
                var dim2 = widget.view[BICst.REGION.DIMENSION2] || [];
                widget.view[BICst.REGION.DIMENSION1] = BI.concat(dim1, dim2);
                widget.view[BICst.REGION.DIMENSION2] = [];
                var usedDimensions = {}, hasUsed = false;
                var fields = [];
                if(BI.isNotNull(widget.view)) {
                    BI.each([BICst.REGION.DIMENSION1], function (idx, item) {
                        BI.each(widget.view[item], function (idx, id) {
                            var dimension = widget.dimensions[id];
                            if (dimension.used === true){
                                var field_type =  BI.Utils.getFieldTypeByID(dimension._src["field_id"]);
                                if (field_type === BICst.COLUMN.DATE
                                    && dimension.group.type !== BICst.GROUP.YMD
                                    && dimension.group.type !== BICst.GROUP.YMDHMS){
                                    field_type = BICst.COLUMN.NUMBER;
                                } else if(field_type === BICst.COLUMN.NUMBER
                                    && widget.type !== BICst.WIDGET.DETAIL
                                    && (BI.isNull(dimension.group) || dimension.group.type !== BICst.GROUP.ID_GROUP)){
                                    field_type = BICst.COLUMN.STRING;
                                }
                                if(BI.isNull(field_type)){
                                    field_type = BICst.COLUMN.NUMBER;
                                }
                                fields.push({
                                    field_name : dimension.name,
                                    field_type : field_type
                                });
                                hasUsed = true;
                            }
                        })
                    })
                    BI.each([BICst.REGION.TARGET1, BICst.REGION.TARGET2, BICst.REGION.TARGET3], function (idx, item) {
                        BI.each(widget.view[item], function (idx, id) {
                            var dimension = widget.dimensions[id];
                            if (dimension.used === true){
                                fields.push({
                                    field_name : dimension.name,
                                    field_type : BICst.COLUMN.NUMBER
                                });
                                hasUsed = true;
                            }
                        })
                    })
                }
                if (hasUsed === true){
                    var table = {
                        value : BI.UUID(),
                        table_name : widget.name,
                        operator : widget,
                        etlType : ETLCst.ETL_TYPE.SELECT_NONE_DATA
                    }
                    table[ETLCst.FIELDS] = fields;
                    items.push(table);
                }
                model[BI.AnalysisETLMainModel.TAB] = {items:items};
                return model;
            }
            var tabEl = BI.extend({}, el, { type :"bi.data_style_tab_etl"});
            var items = [{
                el: tabEl,
                top: 0,
                bottom: 0,
                left: 0,
                right: 0
            },{
                el:{
                    type : 'bi.left_pointer_button',
                    pointerWidth : ETLCst.ENTERBUTTON.POINTERWIDTH,
                    title:BI.i18nText("BI-SPA_Detail"),
                    iconCls : "icon-add",
                    height : ETLCst.ENTERBUTTON.HEIGHT,
                    width : ETLCst.ENTERBUTTON.WIDTH,
                    text: BI.i18nText('BI-new_analysis_result_table'),
                    handler:function () {
                        BI.createWidget({
                            type : "bi.analysis_etl_main",
                            model : _createMainModel(el.wId),
                            element:BI.Layers.create(ETLCst.ANALYSIS_LAYER, "body")
                        })
                    }
                },
                left:0,
                top:ETLCst.ENTERBUTTON.GAP,
                bottom:0,
                right:0
            }];
            return BI.extend({}, el, {type: "bi.absolute", items: BI.concat(items, BI.Plugin.DATA_STYLE_TAB_ITEM)});
        });
    }
})();