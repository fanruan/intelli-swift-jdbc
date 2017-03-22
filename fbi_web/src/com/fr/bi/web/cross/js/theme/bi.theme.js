/**
 * Created by richie on 15/7/13.
 */

FS.THEME.config4navigation.onAfterInit = function () {
    $('#fs-frame-search').css({
        right: 350
    });
    $('#fs-frame-reg').css({
        right: 400
    });
    BI.requestAsync("fr_bi", "get_design_config_auth", {mode: Consts.BIEDIT}, function (res) {
        var $nav = $('#fs-frame-navi');
        if (FS.isAdmin() || res.design === BICst.REPORT_AUTH.EDIT) {
            var newAnalysis = BI.createWidget({
                type: "bi.icon_text_item",
                cls: "new-analysis-font bi-new-analysis-button",
                text: BI.i18nText('BI-Add_Analysis'),
                height: 60,
                width: 120,
                iconWidth: 20,
                iconHeight: 20
            });
            newAnalysis.on(BI.IconTextItem.EVENT_CHANGE, function () {
                var id = BI.UUID();
                var newAnalysisBox = BI.createWidget({
                    type: "bi.new_analysis_float_box"
                });
                newAnalysisBox.on(BI.NewAnalysisFloatBox.EVENT_CHANGE, function (data) {
                    BI.requestAsync("fr_bi", "add_report", {
                        reportName: data.reportName,
                        reportLocation: data.reportLocation,
                        realTime: data.realTime
                    }, function (res, model) {
                        if (BI.isNotNull(res) && BI.isNotNull(res.reportId)) {
                            FS.tabPane.addItem({
                                id: BICst.BI_REPORT_TAB + res.reportId,
                                title: data.reportName,
                                src: FR.servletURL + "?op=fr_bi&cmd=init_dezi_pane&reportId=" + res.reportId + "&edit=_bi_edit_",
                                showFavorite: "no"
                            });
                        }
                    });
                });
                BI.Popovers.create(id, newAnalysisBox, {width: 400, height: 320}).open(id);
                newAnalysisBox.setTemplateNameFocus();
            });
            newAnalysis.element.css({"position": "relative", "float": "right"});
            $nav.after(newAnalysis.element);
        }
        if (FS.isAdmin() || res.config === true) {
            var dataConfig = BI.createWidget({
                type: "bi.icon_text_item",
                cls: "data-config-font bi-data-config-button",
                text: BI.i18nText("BI-Data_Setting"),
                height: 60,
                width: 120,
                iconWidth: 20,
                iconHeight: 20
            });
            dataConfig.on(BI.IconTextItem.EVENT_CHANGE, function () {
                FS.tabPane.addItem({
                    id: BICst.DATA_CONFIG_TAB,
                    title: BI.i18nText('BI-Data_Setting'),
                    src: FR.servletURL + '?op=fr_bi_configure&cmd=init_configure_pane',
                    showFavorite: "no"
                });
            });
            dataConfig.element.css({"position": "relative", "float": "right"});
            $nav.after(dataConfig.element);
        }
    });
};

/**
 * 在平台外观中添加一个用于设置BI样式的tab
 */
//debugger;
FS.Plugin.LookAndFeelSettings.push({
    item: function () {
        FR.$defaultImport("com/fr/bi/web/js/third/es5-sham.js");
        FR.$defaultImport("com/fr/bi/web/js/third/d3.js");
        FR.$defaultImport("com/fr/bi/web/js/third/raphael.js");
        FR.$defaultImport("com/fr/bi/web/js/third/vancharts-all.js");
        FR.$defaultImport("com/fr/bi/web/js/third/leaflet.js");

        return {
            title: BI.i18nText("BI-BI_Style"),
            content: {
                type: "fs.style_setting"
            }
        }
    },


    action: function () {
        this.container.populate();
    }
});