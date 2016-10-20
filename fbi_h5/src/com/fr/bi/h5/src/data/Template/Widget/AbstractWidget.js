/**
 * AbstractWidget
 * Created by Young's on 2016/10/12.
 */
import Immutable from 'immutable'
import {each, invariant, isNil, find, findKey, remove, isEqual, size, keys, isNumeric, isString} from 'core';
import {Fetch} from 'lib'
import DimensionFactory from './Dimensions/DimensionFactory'

function _getQuarterStartMonth(date) {
    var quarterStartMonth = 0;
    var nowMonth = date.getMonth();
    if (nowMonth < 3) {
        quarterStartMonth = 0;
    }
    if (2 < nowMonth && nowMonth < 6) {
        quarterStartMonth = 3;
    }
    if (5 < nowMonth && nowMonth < 9) {
        quarterStartMonth = 6;
    }
    if (nowMonth > 8) {
        quarterStartMonth = 9;
    }
    return quarterStartMonth;
}

//获得指定日期所在季度的起始日期
function _getQuarterStartDate(date) {
    return new Date(date.getFullYear(), _getQuarterStartMonth(date), 1);
}

//获得指定日期所在季度的结束日期
function _getQuarterEndDate(date) {
    var quarterEndMonth = _getQuarterStartMonth(date) + 2;
    return new Date(date.getFullYear(), quarterEndMonth, date.getMonthDays(quarterEndMonth));
}

//指定日期n个月之前或之后的日期
function _getOffsetMonth(date, n) {
    var dt = new Date(date);
    dt.setMonth(dt.getMonth() + parseInt(n));
    return dt;
}

//指定日期n个季度之前或之后的日期
function _getOffsetQuarter(date, n) {
    var dt = new Date(date);
    dt.setMonth(dt.getMonth() + n * 3);
    return dt;
}

class AbstractWidget {
    constructor($widget, wId, template) {
        this.$widget = $widget;
        this.wId = wId;
        this.template = template;
    }

    $get() {
        return this.$widget;
    }

    get$DimensionById(id) {
        invariant(this.isDimensionById(id), id + "不是维度id");
        return this.$widget.getIn(['dimensions', id]);
    }

    get$TargetById(id) {
        invariant(this.isTargetById(id), id + "不是指标id");
        return this.$widget.getIn(['dimensions', id])
    }

    get$DimensionOrTargetById(id) {
        if (this.isDimensionById(id)) {
            return this.get$DimensionById(id);
        }
        return this.get$TargetById(id);
    }

    getDimensionById(id) {
        return DimensionFactory.createDimension(this.get$DimensionById(id), id, this);
    }

    getTargetById(id) {
        return DimensionFactory.createTarget(this.get$TargetById(id), id, this);
    }

    getDimensionOrTargetById(id) {
        if (this.isDimensionById(id)) {
            return DimensionFactory.createDimension(this.get$DimensionById(id), id, this);
        }
        return DimensionFactory.createTarget(this.get$TargetById(id), id, this);
    }

    getAllDimensionIds() {
        if (this._dimensionIds) {
            return this._dimensionIds;
        }
        let result = [];
        this.$widget.get('view').forEach(($id, key)=> {
            if (parseInt(key) < BICst.REGION.TARGET1) {
                result = result.concat($id.toArray());
            }
        });
        this._dimensionIds = result;
        return result;
    }

    getAllTargetIds() {
        if (this._targetIds) {
            return this._targetIds;
        }
        let result = [];
        this.$widget.get('view').forEach(($id, key)=> {
            if (parseInt(key) >= BICst.REGION.TARGET1) {
                result = result.concat($id.toArray());
            }
        });
        this._targetIds = result;
        return result;
    }

    isDimensionById(id) {
        const dimensionIds = this.getAllDimensionIds();
        return dimensionIds.indexOf(id) > -1;
    }

    isDimensionByViewId(viewId) {
        return parseInt(viewId, 10) < BICst.REGION.TARGET1;
    }

    isTargetById(id) {
        const targetIds = this.getAllTargetIds();
        return targetIds.indexOf(id) > -1;
    }

    getAllDimensionAndTargetIds() {
        return this.getAllDimensionIds().concat(this.getAllTargetIds());
    }

    getAllUsedDimensionAndTargetIds() {
        const ids = this.getAllDimensionAndTargetIds();
        const result = [];
        ids.forEach((id)=> {
            const $dim = this.get$DimensionOrTargetById(id);
            if (DimensionFactory.createDimension($dim, id, this).isUsed()) {
                result.push(id);
            }
        });
        return result;
    }

    getAllUsedDimensionIds() {
        const ids = this.getAllDimensionIds();
        const result = [];
        ids.forEach((id)=> {
            const $dim = this.get$DimensionById(id);
            if (DimensionFactory.createDimension($dim, id, this).isUsed()) {
                result.push(id);
            }
        });
        return result;
    }

    getAllUsedTargetIds() {
        const ids = this.getAllTargetIds();
        const result = [];
        ids.forEach((id)=> {
            const $dim = this.get$TargetById(id);
            if (DimensionFactory.createTarget($dim, id, this).isUsed()) {
                result.push(id);
            }
        });
        return result;
    }

    getRowDimensionIds() {
        let result = [];
        this.$widget.get('view').forEach(($id, key)=> {
            if (parseInt(key) === BICst.REGION.DIMENSION1) {
                result = result.concat($id.toArray());
            }
        });
        return result;
    }

    getColDimensionIds() {
        let result = [];
        this.$widget.get('view').forEach(($id, key)=> {
            if (parseInt(key) === BICst.REGION.DIMENSION2) {
                result = result.concat($id.toArray());
            }
        });
        return result;
    }

    getType() {
        return this.$widget.get('type');
    }

    getName() {
        return this.$widget.get('name');
    }

    createJson() {
        return this.$widget.toJS();
    }

    getWidgetBounds() {
        return this.$widget.get('bounds').toJS() || {};
    }

    getWidgetLinkage() {
        return this.$widget.get('linkages').toJS() || [];
    }

    getWidgetView() {
        return this.$widget.get('view').toJS() || {};
    }

    getWidgetSubType() {
        return this.$widget.get('sub_type');
    }

    getSortType() {
        const $sort = this.$widget.get('sort');
        if ($sort) {
            const type = $sort.get('type');
            if (!isNil(type)) {
                return type;
            }
        }
        return BICst.SORT.NONE;
    }


    getWidgetValue() {
        return this.$widget.get('value').toJS();
    }

    getRegionTypeByDimensionID(dId) {
        var view = this.getWidgetView();
        return findKey(view, function (regionType, dIds) {
            if (dIds.indexOf(dId) > -1) {
                return true;
            }
        });
    }

    isControl() {
        return false;
    }

    getControlCalculations(notcontain) {
        var self = this, filterValues = [];
        //控件
        var widgetIds = this.template.getAllWidgetIds();
        widgetIds.forEach((id, i)=> {
            if (!this.template.getWidgetById(id).isControl()) {
                return;
            }
            if (id === notcontain) {
                return;
            }
            //去掉自身和在自身之后创建的控件
            if (!isNil(notcontain) && this.template.isControlWidgetByWidgetId(notcontain)
                && this.template.getWidgetInitTimeByID(id) > this.template.getWidgetInitTimeByID(notcontain)) {
                return;
            }
            var widget = this.template.getWidgetById(id);
            var value = widget.getWidgetValue();
            if (!isNil(value)) {
                var dimensionIds = widget.getAllDimensionAndTargetIds();
                dimensionIds.forEach((dimId, i) => {
                    var dimension = self.template.getDimensionById(dimId);
                    var fValue = value, fType = "";
                    if (isNil(fValue) || (isString(value) && value.length === 0 )|| size(value) === 0) {
                        return;
                    }
                    var filter = null;
                    switch (widget.getType()) {
                        case BICst.WIDGET.STRING:
                        case BICst.WIDGET.LIST_LABEL:
                            fType = BICst.TARGET_FILTER_STRING.BELONG_VALUE;
                            filter = {
                                filter_type: fType,
                                filter_value: fValue,
                                _src: {field_id: dimension.getFieldId()}
                            };
                            break;
                        case BICst.WIDGET.SINGLE_SLIDER:
                        case BICst.WIDGET.INTERVAL_SLIDER:
                        case BICst.WIDGET.NUMBER:
                            fType = BICst.TARGET_FILTER_NUMBER.BELONG_VALUE;
                            filter = {
                                filter_type: fType,
                                filter_value: fValue,
                                _src: {field_id: dimension.getFieldId()}
                            };
                            break;
                        case BICst.WIDGET.DATE:
                            fType = BICst.FILTER_DATE.BELONG_DATE_RANGE;
                            var start = fValue.start, end = fValue.end;
                            fValue = {};
                            if (BI.isNotNull(start)) {
                                start = this._parseComplexDate(start);
                                fValue.start = start;
                            }
                            if (BI.isNotNull(end)) {
                                end = this._parseComplexDate(end);
                                fValue.end = end;
                            }
                            filter = {
                                filter_type: fType,
                                filter_value: fValue,
                                _src: {field_id: dimension.getFieldId()}
                            };
                            break;
                        case BICst.WIDGET.MONTH:
                            fType = BICst.FILTER_DATE.EQUAL_TO;
                            var year = fValue.year, month = fValue.month;
                            if (BI.isNumeric(year)) {
                                filterValues.push({
                                    filter_type: BICst.FILTER_DATE.EQUAL_TO,
                                    filter_value: {group: BICst.GROUP.Y, values: year},
                                    _src: {field_id: dimension.getFieldId()}
                                });
                            }
                            if (!isNumeric(month)) {
                                return;
                            }
                            fValue = {group: BICst.GROUP.M, values: month + 1};
                            filter = {
                                filter_type: fType,
                                filter_value: fValue,
                                _src: {field_id: dimension.getFieldId()}
                            };
                            break;
                        case BICst.WIDGET.QUARTER:
                            fType = BICst.FILTER_DATE.EQUAL_TO;
                            var quarter = fValue.quarter, year = fValue.year;
                            if (isNumeric(year)) {
                                filterValues.push({
                                    filter_type: BICst.FILTER_DATE.EQUAL_TO,
                                    filter_value: {group: BICst.GROUP.Y, values: year},
                                    _src: {field_id: dimension.getFieldId()}
                                });
                            }
                            if (!isNumeric(quarter)) {
                                return;
                            }
                            fValue = {group: BICst.GROUP.S, values: quarter};
                            filter = {
                                filter_type: fType,
                                filter_value: fValue,
                                _src: {field_id: dimension.getFieldId()}
                            };
                            break;
                        case BICst.WIDGET.YEAR:
                            fType = BICst.FILTER_DATE.EQUAL_TO;
                            fValue = {group: BICst.GROUP.Y, values: fValue};
                            filter = {
                                filter_type: fType,
                                filter_value: fValue,
                                _src: {field_id: dimension.getFieldId()}
                            };
                            break;
                        case BICst.WIDGET.YMD:
                            fType = BICst.FILTER_DATE.EQUAL_TO;
                            fValue = {group: BICst.GROUP.YMD, values: this._parseComplexDate(fValue)};
                            filter = {
                                filter_type: fType,
                                filter_value: fValue,
                                _src: {field_id: dimension.getFieldId()}
                            };
                            break;
                    }
                    !isNil(filter) && filterValues.push(filter);
                });

                //树控件过滤条件设置,不能对每个纬度单独设置过滤条件
                if (widget.getType() === BICst.WIDGET.TREE) {
                    var viewDimensionIds = widget.getWidgetView()[BICst.REGION.DIMENSION1];
                    var treeValue = [];
                    createTreeFilterValue(treeValue, value, 0, viewDimensionIds);
                    filter = {
                        filter_type: BICst.FILTER_TYPE.OR,
                        filter_value: treeValue
                    };
                    filterValues.push(filter);
                }

                if (widget.getType() === BICst.WIDGET.TREE_LABEL) {
                    var viewDimensionIds = widget.getWidgetView()[BICst.REGION.DIMENSION1];
                    var treeValue = [];
                    createTreeLabelFilterValue(treeValue, value, 0, viewDimensionIds);
                    filter = {
                        filter_type: BICst.FILTER_TYPE.OR,
                        filter_value: treeValue
                    };
                    filterValues.push(filter);
                }

                if (value.length === 1) {
                    var filter = value[0];
                    self._parseFilter(filter);
                    filterValues.push(filter);
                }
            }
        });
        return filterValues;

        function createTreeFilterValue(result, v, floor, dimensionIds, fatherFilterValue) {
            var dimension = self.template.getDimensionById(dimensionIds[floor]);
            v.forEach((child, value)=> {
                    var leafFilterObj = {
                        filter_type: BICst.TARGET_FILTER_STRING.BELONG_VALUE,
                        filter_value: {
                            type: BI.Selection.Multi,
                            value: [value]
                        },
                        _src: dimension.getDimensionSrc()
                    };
                    if (size(child) === 0) {
                        var filterObj = {
                            filter_type: BICst.FILTER_TYPE.AND,
                            filter_value: []
                        };
                        filterObj.filter_value.push(leafFilterObj);
                        !isNil(fatherFilterValue) && filterObj.filter_value.push(fatherFilterValue);
                        result.push(filterObj);
                    } else {
                        createTreeFilterValue(result, child, floor + 1, dimensionIds, leafFilterObj);
                    }
                }
            );
        }

        function createTreeLabelFilterValue(result, v, floor, dimensionIds, fatherFilterValue) {
            var dimension = self.template.getDimensionById(dimensionIds[floor]);
            v.forEach((child, value)=> {
                    var leafFilterObj = {
                        filter_type: BICst.TARGET_FILTER_STRING.BELONG_VALUE,
                        filter_value: {
                            type: value === "_*_" ? BI.Selection.All : BI.Selection.Multi,
                            value: [value]
                        },
                        // _src: {field_id: self.getFieldIDByDimensionID(dimensionIds[floor])}
                        _src: dimension.getDimensionSrc()
                    };
                    if (size(child) === 0) {
                        var filterObj = {
                            filter_type: BICst.FILTER_TYPE.AND,
                            filter_value: []
                        };
                        filterObj.filter_value.push(leafFilterObj);
                        !isNil(fatherFilterValue) && filterObj.filter_value.push(fatherFilterValue);
                        result.push(filterObj);
                    } else {
                        if (leafFilterObj.filter_value.type === BI.Selection.All) {
                            leafFilterObj = fatherFilterValue
                        }
                        createTreeLabelFilterValue(result, child, floor + 1, dimensionIds, leafFilterObj);
                    }
                }
            );
        }
    }

    getData(options) {

    }

    getWidgetCalculationByID() {
        var self = this;
        var filterValues = [];

        //钻取条件  对于交叉表，要考虑的不仅仅是used，还有行表头与列表头之间的钻取问题
        var drill = this.getDrill();
        if (!isNil(drill) && this.getType() !== BICst.WIDGET.MAP) {
            drill.forEach((drArray, drId)=>{
                if (drArray.length === 0) {
                    return;
                }
                var dimension = this.getDimensionById(drId);
                !isNil(dimension) && dimension.setUsed(false);
                this.set$Dimension(dimension.$get(), drId);
                drArray.forEach((drill, i)=>{
                    var dimension = this.getDimensionById(drill.dId);
                    if (!isNil(dimension)) {
                        dimension.setUsed(i === drArray.length - 1);
                        this.set$Dimension(dimension.$get(), drill.dId);
                        var drillRegionType = this.getRegionTypeByDimensionID(drId);
                        //从原来的region中pop出来
                        var tempRegionType = this.getRegionTypeByDimensionID(drill.dId);
                        var view = this.getWidgetView();
                        var dIndex = view[drillRegionType].indexOf(drId);
                        remove(view[tempRegionType], function(s){
                            return isEqual(s, drill.dId);
                        });
                        view.splice(dIndex, 0, drill.dId);
                        this.setWidgetView(view);
                    }
                    drArray[i].values.forEach((v, i) => {
                        var filterValue = parseSimpleFilter(v);
                        if (!isNil(filterValue)) {
                            filterValues.push(filterValue);
                        }
                    });
                });
            });
        }

        //所有控件过滤条件（考虑有查询按钮的情况）
        filterValues = filterValues.concat(
            this.template.isQueryControlExist() && !this.isControl() ?
                this.$widget.get("control_filters") : this.getControlCalculations());

        //联动 由于这个clicked现在放到了自己的属性里，直接拿就好了
        var linkages = this.getLinkageValues();
        linkages.forEach((linkValue, cId)=>{
            linkValue.forEach((v, i)=>{
                var filterValue = parseSimpleFilter(v);
                if (!isNil(filterValue)) {
                    filterValues.push(filterValue);
                }
            });
            var transferFilter = this.template.getWSTransferFilterById(this.template.getWidgetIDByDimensionID(cId));
            if (transferFilter === true) {
                this.template.getWidgetIDByDimensionID(cId).getDimensionById(cId);
                var tarFilter = this.template.getDimensionFilterValueByID(cId);
                if (!isNil(tarFilter)) {
                    this._parseFilter(tarFilter);
                    if (!isNil(tarFilter) && size(tarFilter) === 0) {
                        filterValues.push(tarFilter);
                    }
                }
            }
        });

        //联动传递指标过滤条件  找到联动链上的所有的组件，获取当前点击的指标的过滤条件  感觉有点浮夸的功能
        var allLinksWIds = [];

        getLinkedIds(allLinksWIds);
        allLinksWIds.forEach((lId, i)=>{
            var lLinkages = this.getLinkageValues();
            lLinkages.forEach((linkValue, cId)=>{
                var lTransferFilter = this.template.getWSTransferFilterById(this.template.getWidgetIDByDimensionID(cId));
                if (lTransferFilter === true) {
                    var lTarFilter = this.template.getDimensionFilterValueByID(cId);
                    if (!isNil(lTarFilter)) {
                        this._parseFilter(lTarFilter);
                        filterValues.push(lTarFilter);
                    }
                }
            });
            //还应该拿到所有的联动过来的组件的钻取条件 也是给跪了
            var linkDrill = this.getDrill();
            if (!isNil(linkDrill)) {
                linkDrill.forEach((drArray, drId)=>{
                    if (drArray.length === 0) {
                        return;
                    }
                    drArray.forEach((drill, id)=>{
                        drArray[i].values.forEach((v, i) => {
                            var filterValue = parseSimpleFilter(v);
                            if (!isNill(filterValue)) {
                                filterValues.push(filterValue);
                            }
                        });
                    });
                });
            }
        });


        //日期类型的过滤条件
        var dimensions = this.getAllDimensionAndTargetIds();
        dimensions.forEach((dimension, dId)=>{
            var filterValue = this.getDimensionById(dId).getFilterValue() || {};
            this._parseFilter(filterValue);
            this.set$Dimension(dimension.$get(), dId);
        });

        //考虑表头上指标过滤条件的日期类型
        var target_filter = this.getWidgetFilterValue();
        target_filter.forEach((filter, tId)=>{
            this._parseFilter(filter)
        });
        this.setWidgetFilterValue(target_filter);
        this.setWidgetFilter({filter_type: BICst.FILTER_TYPE.AND, filter_value: filterValues});
        this.setWidgetRealData(true);

        function getLinkedIds(links) {
            var allWIds = self.template.getAllWidgetIds();
            allWIds.forEach((aWid, i)=> {
                var linkages = self.template.getWidgetLinkageByID(aWid);
                linkages.forEach((link, i)=> {
                    if (link.to === self.wid) {
                        links.push(self.template.getWidgetIDByDimensionID(link.from));
                        getLinkedIds(self.template.getWidgetIDByDimensionID(link.from), links);
                    }
                });
            });
        }
    }

    getWidgetSettings() {
        return this.$widget.get('settings').toJS();
    }

    getFieldIDByDimensionID(did) {
        var dimension = this.getDimensionById(did);
        if (!isNil(dimension)) {
            return dimension.getFieldId();
        }
    }

    //settings  ---- start ----

    isFreeze() {
        const isFreeze = this.$widget.getIn(['settings', 'freeze_dim']);
        return isNil(isFreeze) ? true : isFreeze;
    }

    getWSTableForm() {
        var ws = this.getWidgetSettings();
        return isNil(ws.table_form) ? ws.table_form :
            BICst.DEFAULT_CHART_SETTING.table_form;
    }

    getWSThemeColor() {
        var ws = this.getWidgetSettings();
        return isNil(ws.theme_color) ? ws.theme_color :
            BICst.DEFAULT_CHART_SETTING.theme_color;
    }

    getWSTableStyle() {
        var ws = this.getWidgetSettings();
        return isNil(ws.table_style) ? ws.table_style :
            BICst.DEFAULT_CHART_SETTING.table_style;
    }

    getWSTransferFilter() {
        var ws = this.getWidgetSettings();
        return isNil(ws.transfer_filter) ? ws.transfer_filter :
            BICst.DEFAULT_CHART_SETTING.transfer_filter;
    }

    getWSShowNumber() {
        var ws = this.getWidgetSettings();
        return isNil(ws.show_number) ? ws.show_number :
            BICst.DEFAULT_CHART_SETTING.show_number;
    }

    getWSShowRowTotal() {
        var ws = this.getWidgetSettings();
        return isNil(ws.show_row_total) ? ws.show_row_total :
            BICst.DEFAULT_CHART_SETTING.show_row_total;
    }

    getWSShowColTotal() {
        var ws = this.getWidgetSettings();
        return isNil(ws.show_col_total) ? ws.show_col_total :
            BICst.DEFAULT_CHART_SETTING.show_col_total;
    }

    getWSOpenRowNode() {
        var ws = this.getWidgetSettings();
        return isNil(ws.open_row_node) ? ws.open_row_node :
            BICst.DEFAULT_CHART_SETTING.open_row_node;
    }

    getWSOpenColNode() {
        var ws = this.getWidgetSettings();
        return isNil(ws.open_col_node) ? ws.open_col_node :
            BICst.DEFAULT_CHART_SETTING.open_col_node;
    }

    getWSMaxRow() {
        var ws = this.getWidgetSettings();
        return isNil(ws.max_row) ? ws.max_row :
            BICst.DEFAULT_CHART_SETTING.max_row;
    }

    getWSMaxCol() {
        var ws = this.getWidgetSettings();
        return isNil(ws.max_col) ? ws.max_col :
            BICst.DEFAULT_CHART_SETTING.max_col;
    }

    getWSFreezeDim() {
        var ws = this.getWidgetSettings();
        return isNil(ws.freeze_dim) ? ws.freeze_dim :
            BICst.DEFAULT_CHART_SETTING.freeze_dim;
    }

    getWSFreezeFirstColumnById() {
        var ws = this.getWidgetSettings();
        return isNil(ws.freeze_first_column) ? ws.freeze_first_column :
            BICst.DEFAULT_CHART_SETTING.freeze_first_column;
    }

    isShowWidgetRealData() {
        return this.$widget.get('real_data');
    }


    isDimensionExist(did) {
        return this.getAllDimensionIds().indexOf(did) > -1;
    }

    getWidgetInitTime() {
        return this.$widget.get('init_time') || new Date().getTime();
    }

    getClicked() {
        return this.$widget.get('clicked') || {};
    }

    getDrill() {
        var clicked = this.getClicked();
        var drills = {};
        each(clicked, (dId, value)=> {
            if (this.isDimensionExist(dId) && this.isDimensionById(dId)) {
                drills[dId] = value;
            }
        });
        return drills;
    }

    getLinkageValues() {
        var clicked = this.getClicked();
        var drills = {};
        each(clicked, (dId, value)=> {
            if (this.isDimensionExist(dId) && !this.isDimensionById(dId)) {
                drills[dId] = value;
            }
        });
        return drills;
    }

    getWidgetFilterValue(wid) {
        if (this.template.isWidgetExistByID(wid)) {
            return this.$widget.get('filter_value').toJS() || {};
        }
        return {};
    }

    setWidgetValue(value) {
        this.$widget = this.$widget.set('value', Immutable.fromJS(value));
        return this;
    }

    setWidgetView(view) {
        this.$widget = this.$widget.set('view', Immutable.fromJS(view));
        return this;
    }

    setWidgetFilter(filter){
        this.$widget = this.$widget.set('filter', Immutable.fromJS(filter));
        return this;
    }

    setWidgetFilterValue(filter_value){
        this.$widget = this.$widget.set('filter_value', Immutable.fromJS(filter_value));
        return this;
    }

    setWidgetRealData(real_data){
        this.$widget = this.$widget.set('real_data', Immutable.fromJS(real_data));
        return this;
    }

    set$Dimension($dimension, dId) {
        this.$widget = this.$widget.setIn(['dimensions', dId], $dimension);
        return this;
    }

    setSortType(type) {
        this.$widget = this.$widget.setIn(['sort', 'type'], type);
        return this;
    }

    setSortTarget(dId) {
        this.$widget = this.$widget.setIn(['sort', 'sort_target'], dId);
        return this;
    }

    _parseFilter(filter) {
        var filterType = filter.filter_type, filterValue = filter.filter_value;
        if (filterType === BICst.FILTER_TYPE.AND || filterType === BICst.FILTER_TYPE.OR) {
            filterValue.forEach((value, i) => {
                this._parseFilter(value);
            });
        }
        if (filterType === BICst.FILTER_DATE.BELONG_DATE_RANGE || filterType === BICst.FILTER_DATE.NOT_BELONG_DATE_RANGE) {
            var start = filterValue.start, end = filterValue.end;
            if (!isNil(start)) {
                filterValue.start = this._parseComplexDate(start);
            }
            if (!isNil(end)) {
                var endTime = this._parseComplexDate(end);
                if (!isNil(endTime)) {
                    filterValue.end = new Date(endTime).getOffsetDate(1).getTime() - 1
                } else {
                    delete filterValue.end;
                }
            }
        }
        if (filterType === BICst.FILTER_DATE.BELONG_WIDGET_VALUE || filterType === BICst.FILTER_DATE.NOT_BELONG_WIDGET_VALUE) {
            var filterWId = filterValue.wId, filterValueType = filterValue.filter_value.type;
            var wValue = this.template.getWidgetById(filterWId).getWidgetValue();
            if (!this.template.isWidgetExistByID(filterWId) || isNil(wValue)) {
                return;
            }
            switch (filterValueType) {
                case BICst.SAME_PERIOD:
                    if (!isNil(wValue.start)) {
                        filterValue.start = this._parseComplexDate(wValue.start);
                    }
                    if (!isNil(wValue.end)) {
                        var endTime = this._parseComplexDate(wValue.end);
                        if (!isNil(endTime)) {
                            filterValue.end = new Date(endTime).getOffsetDate(1).getTime() - 1;
                        } else {
                            delete filterValue.end;
                        }
                    }
                    break;
                case BICst.LAST_SAME_PERIOD:
                    if (!isNil(wValue.start) && !isNil(wValue.end)) {
                        var s = this._parseComplexDate(wValue.start);
                        var e = this._parseComplexDate(wValue.end);
                        if (!isNil(s) && !isNil(e)) {
                            filterValue.start = new Date(2 * s - e).getOffsetDate(-1).getTime();
                        } else {
                            delete filterValue.start
                        }
                        if (!isNil(s)) {
                            filterValue.end = new Date(s).getTime() - 1;
                        } else {
                            delete filterValue.end;
                        }
                    } else if (!isNil(wValue.start) && !isNil(wValue.start.year)) {
                        filterValue.start = this._parseComplexDate(wValue.start);
                    } else if (!isNil(wValue.end) && !isNil(wValue.end.year)) {
                        filterValue.end = this._parseComplexDate(wValue.end);
                    }
                    break;
                case BICst.YEAR_QUARTER:
                case BICst.YEAR_MONTH:
                case BICst.YEAR_WEEK:
                case BICst.YEAR_DAY:
                case BICst.MONTH_WEEK:
                case BICst.MONTH_DAY:
                case BICst.YEAR:
                    var date = getDateControlValue(filterWId);
                    if (!isNil(date)) {
                        var value = getOffSetDateByDateAndValue(date, filterValue.filter_value);
                        filterValue.start = value.start;
                        if (!isNil(value.end)) {
                            filterValue.end = new Date(value.end).getOffsetDate(1).getTime() - 1;
                        }
                    }
                    break;
            }
        }
        if (filterType === BICst.FILTER_DATE.EARLY_THAN) {
            var date = getDateControlValue(filterValue.wId);
            if (!isNil(date)) {
                var value = getOffSetDateByDateAndValue(date, filterValue.filter_value);
                if (!isNil(value.start)) {
                    filterValue.end = new Date(value.start).getTime() - 1;
                }
            }
        }
        if (filterType === BICst.FILTER_DATE.LATER_THAN) {
            var date = getDateControlValue(filterValue.wId);
            if (!isNil(date)) {
                var value = getOffSetDateByDateAndValue(date, filterValue.filter_value);
                if (!isNil(value.start)) {
                    filterValue.start = new Date(value.start).getTime();
                }
            }
        }
        if (filterType === BICst.FILTER_DATE.EQUAL_TO || filterType === BICst.FILTER_DATE.NOT_EQUAL_TO) {
            if (isNil(filterValue)) {
                filterValue = {};
            } else {
                filterValue.values = this._parseComplexDate(filterValue);
                filterValue.group = BICst.GROUP.YMD;
            }
        }
        return filter;
        //日期偏移值
        function getOffSetDateByDateAndValue(date, value) {
            var type = value.type, value = value.value;
            var fPrevOrAfter = value.foffset === 0 ? -1 : 1;
            var sPrevOrAfter = value.soffset === 0 ? -1 : 1;
            var start, end;
            start = end = date;
            var ydate = new Date((date.getFullYear() + fPrevOrAfter * value.fvalue), date.getMonth(), date.getDate());
            switch (type) {
                case BICst.YEAR:
                    start = new Date((date.getFullYear() + fPrevOrAfter * value.fvalue), 0, 1);
                    end = new Date(start.getFullYear(), 11, 31);
                    break;
                case BICst.YEAR_QUARTER:
                    ydate = _getOffsetQuarter(ydate, sPrevOrAfter * value.svalue);
                    start = _getQuarterStartDate(ydate);
                    end = _getQuarterEndDate(ydate);
                    break;
                case BICst.YEAR_MONTH:
                    ydate = _getOffsetMonth(ydate, sPrevOrAfter * value.svalue);
                    start = new Date(ydate.getFullYear(), ydate.getMonth(), 1);
                    end = new Date(ydate.getFullYear(), ydate.getMonth(), (ydate.getLastDateOfMonth()).getDate());
                    break;
                case BICst.YEAR_WEEK:
                    start = ydate.getOffsetDate(sPrevOrAfter * 7 * value.svalue);
                    end = start.getOffsetDate(6);
                    break;
                case BICst.YEAR_DAY:
                    start = ydate.getOffsetDate(sPrevOrAfter * value.svalue);
                    end = start;
                    break;
                case BICst.MONTH_WEEK:
                    var mdate = _getOffsetMonth(date, fPrevOrAfter * value.fvalue);
                    start = mdate.getOffsetDate(sPrevOrAfter * 7 * value.svalue);
                    end = start.getOffsetDate(6);
                    break;
                case BICst.MONTH_DAY:
                    var mdate = _getOffsetMonth(date, fPrevOrAfter * value.fvalue);
                    start = mdate.getOffsetDate(sPrevOrAfter * value.svalue);
                    end = start;
                    break;
            }
            return {
                start: start.getTime(),
                end: end.getTime()
            }
        }

        //对于维度的条件，很有可能是一个什么属于分组 这边处理 （没放到构造的地方处理是因为“其他”）
        function parseStringFilter4Group(dId, value) {
            var dimension = self.template.getDimensionById(dId);
            var group = dimension.getGroup();
            var details = group.details;
            var groupMap = {};
            details.forEach((detail, i)=> {
                groupMap[detail.id] = [];
                detail.content.forEach((content, i)=> {
                    groupMap[detail.id].push(content.value);
                });
            })
            var groupNames = keys(groupMap), ungroupName = group.ungroup2OtherName;
            if (group.ungroup2Other === BICst.CUSTOM_GROUP.UNGROUP2OTHER.SELECTED) {
                groupNames.push(BICst.UNGROUP_TO_OTHER);
            }
            // 对于drill和link 一般value的数组里只有一个值
            var v = value[0];
            if (groupNames.contains(v)) {
                if (v === ungroupName) {
                    var vs = [];
                    groupMap.forEach((gv, gk)=> {
                        gk !== v && (vs = vs.concat(gv));
                    })
                    return {
                        filter_type: BICst.TARGET_FILTER_STRING.NOT_BELONG_VALUE,
                        filter_value: {type: BI.Selection.Multi, value: vs},
                        _src: {field_id: dimension.getFieldId()}
                    }
                }
                return {
                    filter_type: BICst.TARGET_FILTER_STRING.BELONG_VALUE,
                    filter_value: {type: BI.Selection.Multi, value: groupMap[v]},
                    _src: {field_id: dimension.getFieldId()}
                }
            }
            return {
                filter_type: BICst.TARGET_FILTER_STRING.BELONG_VALUE,
                filter_value: {type: BI.Selection.Multi, value: value},
                _src: {field_id: dimension.getFieldId()}
            }
        }

        function parseNumberFilter4Group(dId, v) {
            var value = v[0];
            var dimension = self.template.getDimensionById(dId);
            var group = dimension.getGroup();
            var groupValue = group.group_value, groupType = group.type;
            var groupMap = {};
            if (isNil(groupValue) && isNil(groupType)) {
                //没有分组为自动分组 但是这个时候维度中无相关分组信息，暂时截取来做
                var sIndex = value.indexOf("-");
                var min = value.slice(0, sIndex), max = value.slice(sIndex + 1);
                return {
                    filter_type: BICst.TARGET_FILTER_NUMBER.BELONG_VALUE,
                    filter_value: {
                        min: min,
                        max: max,
                        closemin: true,
                        closemax: false
                    },
                    _src: {field_id: dimension.getFieldID()}
                }
            }
            if (groupType === BICst.GROUP.AUTO_GROUP) {
                //坑爹，要自己算分组名称出来
                var groupInterval = groupValue.group_interval, max = groupValue.max, min = groupValue.min;
                while (min < max) {
                    var newMin = min + groupInterval;
                    groupMap[min + "-" + newMin] = {
                        min: min,
                        max: newMin,
                        closemin: true,
                        closemax: newMin >= max
                    };
                    min = newMin;
                }
                return {
                    filter_type: BICst.TARGET_FILTER_NUMBER.BELONG_VALUE,
                    filter_value: groupMap[value],
                    _src: {field_id: dimension.getFieldId()}
                };
            }
            if (groupType === BICst.GROUP.ID_GROUP) {
                return {
                    filter_type: BICst.TARGET_FILTER_NUMBER.BELONG_VALUE,
                    filter_value: {
                        min: value,
                        max: value,
                        closemin: true,
                        closemax: true
                    },
                    _src: {field_id: dimension.getFieldId()}
                }
            }
            var groupNodes = groupValue.group_nodes, useOther = groupValue.use_other;
            var oMin, oMax;
            groupNodes.forEach((node, i)=> {
                i === 0 && (oMin = node.min);
                i === groupNodes.length - 1 && (oMax = node.max);
                groupMap[node.id] = {
                    min: node.min,
                    max: node.max,
                    closemin: node.closemin,
                    closemax: node.closemax
                }
            });
            if (!isNil(groupMap[value])) {
                return {
                    filter_type: BICst.TARGET_FILTER_NUMBER.BELONG_VALUE,
                    filter_value: groupMap[value],
                    _src: {field_id: dimension.getFieldId()}
                };
            } else if (value === BICst.UNGROUP_TO_OTHER) {
                return {
                    filter_type: BICst.TARGET_FILTER_NUMBER.NOT_BELONG_VALUE,
                    filter_value: {
                        min: oMin,
                        max: oMax,
                        closemin: true,
                        closemax: true
                    },
                    _src: {field_id: dimension.getFieldId()}
                };
            }
        }

        function parseSimpleFilter(v) {
            var dId = v.dId;
            var dimension = self.template.getDimensionById(dId);
            var dType = dimension.getType();
            switch (dType) {
                case BICst.TARGET_TYPE.STRING:
                    return parseStringFilter4Group(dId, v.value);
                case BICst.TARGET_TYPE.NUMBER:
                    return parseNumberFilter4Group(dId, v.value);
                case BICst.TARGET_TYPE.DATE:
                    var groupType = dimension.getGroupType();
                    return {
                        filter_type: BICst.FILTER_DATE.EQUAL_TO,
                        filter_value: {values: v.value[0], group: groupType},
                        _src: {field_id: dimension.getFieldId()}
                    };
            }
        }

        //获取日期控件的值
        function getDateControlValue(wid) {
            var self = this;
            if (!self.template.isWidgetExistByID(wid)) {
                return null;
            }
            var widget = self.template.getWidgetById(wid);
            var widgetType = widget.getType();
            var wValue = widget.getWidgetValue();
            var date = null;
            switch (widgetType) {
                case BICst.WIDGET.YEAR:
                    if (isNumeric(wValue)) {
                        date = new Date(wValue, 0, 1);
                    }
                    break;
                case BICst.WIDGET.MONTH:
                    if (!isNil(wValue) && isNumeric(wValue.year)) {
                        date = new Date(wValue.year, isNumeric(wValue.month) ? wValue.month : 0, 1);
                    }
                    break;
                case BICst.WIDGET.QUARTER:
                    if (!isNil(wValue) && isNumeric(wValue.year)) {
                        var quarter = wValue.quarter;
                        date = new Date(wValue.year, isNumeric(quarter) ? (quarter * 3 - 1) : 0, 1);
                    }
                    break;
                case BICst.WIDGET.YMD:
                    if (!isNil(wValue)) {
                        var v = self._parseComplexDate(wValue);
                        if (!isNil(v)) {
                            date = new Date(v);
                        }
                    }
                    break;
            }
            return date;
        }
    }

    _parseComplexDate(v) {
        var self = this;
        if (v.type === BICst.MULTI_DATE_PARAM) {
            return parseComplexDateForParam(v.value);
        } else {
            return parseComplexDateCommon(v);
        }
        function parseComplexDateForParam(value) {
            var widgetInfo = value.widgetInfo, offset = value.offset;
            if (isNil(widgetInfo) || isNil(offset)) {
                return;
            }
            var paramdate;
            var wWid = widgetInfo.wId, se = widgetInfo.startOrEnd;
            if (!isNil(wWid) && !isNil(se)) {
                var wWValue = self.template.getWidgetById(wWid).getWidgetValue();
                if (isNil(wWValue) || size(wWValue) === 0) {
                    return;
                }
                if (se === BI.MultiDateParamPane.start && !isNil(wWValue.start)) {
                    paramdate = parseComplexDateCommon(wWValue.start);
                }
                if (se === BI.MultiDateParamPane.end && !isNil(wWValue.end)) {
                    paramdate = parseComplexDateCommon(wWValue.end);
                }
            } else {
                if (isNil(widgetInfo.wId) || isNil(self.template.getWidgetById(widgetInfo.wId).getWidgetValue())) {
                    return;
                }
                paramdate = parseComplexDateCommon(self.template.getWidgetById(widgetInfo.wId).getWidgetValue());
            }
            if (!isNil(paramdate)) {
                return parseComplexDateCommon(offset, new Date(paramdate));
            }
        }

        function parseComplexDateCommon(v, consultedDate) {
            var type = v.type, value = v.value;
            var date = isNil(consultedDate) ? new Date() : consultedDate;
            var currY = date.getFullYear(), currM = date.getMonth(), currD = date.getDate();
            date = new Date(date.getFullYear(), date.getMonth(), date.getDate());
            if (isNil(type) && !isNil(v.year)) {
                return new Date(v.year, v.month, v.day).getTime();
            }
            switch (type) {
                case BICst.MULTI_DATE_YEAR_PREV:
                    return new Date(currY - 1 * value, currM, currD).getTime();
                case BICst.MULTI_DATE_YEAR_AFTER:
                    return new Date(currY + 1 * value, currM, currD).getTime();
                case BICst.MULTI_DATE_YEAR_BEGIN:
                    return new Date(currY, 0, 1).getTime();
                case BICst.MULTI_DATE_YEAR_END:
                    return new Date(currY, 11, 31).getTime();

                case BICst.MULTI_DATE_MONTH_PREV:
                    return _getOffsetMonth(date, -1 * value).getTime();
                case BICst.MULTI_DATE_MONTH_AFTER:
                    return _getOffsetMonth(date, value).getTime();
                case BICst.MULTI_DATE_MONTH_BEGIN:
                    return new Date(currY, currM, 1).getTime();
                case BICst.MULTI_DATE_MONTH_END:
                    return new Date(currY, currM, (date.getLastDateOfMonth()).getDate()).getTime();

                case BICst.MULTI_DATE_QUARTER_PREV:
                    return _getOffsetQuarter(date, -1 * value).getTime();
                case BICst.MULTI_DATE_QUARTER_AFTER:
                    return _getOffsetQuarter(date, value).getTime();
                case BICst.MULTI_DATE_QUARTER_BEGIN:
                    return _getQuarterStartDate(date).getTime();
                case BICst.MULTI_DATE_QUARTER_END:
                    return _getQuarterEndDate(date).getTime();

                case BICst.MULTI_DATE_WEEK_PREV:
                    return date.getOffsetDate(-7 * value).getTime();
                case BICst.MULTI_DATE_WEEK_AFTER:
                    return date.getOffsetDate(7 * value).getTime();

                case BICst.MULTI_DATE_DAY_PREV:
                    return date.getOffsetDate(-1 * value).getTime();
                case BICst.MULTI_DATE_DAY_AFTER:
                    return date.getOffsetDate(1 * value).getTime();
                case BICst.MULTI_DATE_DAY_TODAY:
                    return date.getTime();
                case BICst.MULTI_DATE_CALENDAR:
                    return new Date(value.year, value.month, value.day).getTime();

            }
        }
    }
}

export default AbstractWidget;