/**
 * 图片组件
 * Created by GameJian on 2016/1/26.
 */
BI.UploadImage = BI.inherit(BI.Widget, {
    _defaultConfig: function () {
        return BI.extend(BI.UploadImage.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-upload-image"
        })
    },

    _init: function () {
        BI.UploadImage.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.label = BI.createWidget({
            type: "bi.text_button",
            trigger: "dblclick",
            cls: "upload-image-text-button-label",
            whiteSpace: "normal",
            text: BI.i18nText("BI-DoubleClick_To_Upload_Image")
        });

        this.file = BI.createWidget({
            type: "bi.multifile_editor",
            accept: "*.jpg;*.png;*.gif;*.bmp;*.jpeg;",
            maxSize: 1024 * 1024 * 100
        });

        this.img = BI.createWidget({
            type: "bi.image_button",
            invalid: true,
            width: "100%",
            height: "100%"
        });

        this.label.on(BI.TextButton.EVENT_CHANGE, function () {
            if (self.isValid()) {
                self.file.select();
            }
        });

        this.file.on(BI.MultifileEditor.EVENT_CHANGE, function (data) {
            this.upload();
        });
        //直接把图片保存到resource目录下面
        this.file.on(BI.MultifileEditor.EVENT_UPLOADED, function () {
            var files = this.getValue();
            var file = files[files.length - 1];
            var attachId = file.attach_id, fileName = file.filename;
            var src = FR.servletURL + "?op=fr_bi&cmd=get_uploaded_image&image_id=" + attachId + "_" + fileName;
            BI.requestAsync("fr_bi_dezi", "save_upload_image", {
                attach_id: attachId
            }, function (res) {
                self.img.setSrc(src);
                self._check();
                self._setSize("auto", "auto");
                self.fireEvent(BI.UploadImage.EVENT_CHANGE, src);
            })
        });

        this.upload = BI.createWidget({
            type: "bi.icon_button",
            cls: "upload-image-icon-button img-upload-font",
            title: BI.i18nText("BI-Upload_Image"),
            height: 24,
            width: 24
        });

        this.upload.on(BI.IconButton.EVENT_CHANGE, function () {
            if (self.isValid()) {
                self.file.select();
            }
        });

        this.del = BI.createWidget({
            type: "bi.icon_button",
            cls: "upload-image-icon-button img-shutdown-font",
            title: BI.i18nText("BI-Delete"),
            height: 24,
            width: 24
        });

        this.del.on(BI.IconButton.EVENT_CHANGE, function () {
            self.fireEvent(BI.UploadImage.EVENT_DESTROY);
        });

        this.size = BI.createWidget({
            type: "bi.image_button_size_combo",
            cls: "upload-image-icon-button"
        });

        this.size.on(BI.ImageButtonSizeCombo.EVENT_CHANGE, function () {
            self._sizeChange(self.size.getValue());
            self.fireEvent(BI.UploadImage.EVENT_CHANGE, arguments)
        });

        this.href = BI.createWidget({
            type: "bi.image_button_href",
            cls: "upload-image-icon-button"
        });

        this.href.on(BI.ImageButtonHref.EVENT_CHANGE, function () {
            if (BI.isNotEmptyString(self.href.getValue())) {
                self.img.setValid(true)
            } else {
                self.img.setValid(false)
            }
            self.fireEvent(BI.UploadImage.EVENT_CHANGE, arguments)
        });

        this.img.on(BI.ImageButton.EVENT_CHANGE, function () {
            window.open(BI.Func.formatAddress(self.href.getValue()));
        });

        BI.createWidget({
            type: "bi.absolute",
            element: this.element,
            items: [{
                el: {
                    type: "bi.absolute",
                    scrollable: false,
                    items: [{
                        el: this.img
                    }]
                },
                left: 0,
                right: 0,
                top: 0,
                bottom: 0
            }, {
                el: this.label,
                left: 10,
                right: 10,
                top: 10,
                bottom: 10
            }, {
                el: this.del,
                right: 4,
                top: 8
            }, {
                el: this.href,
                right: 36,
                top: 8
            }, {
                el: this.size,
                right: 68,
                top: 8
            }, {
                el: this.upload,
                right: 100,
                top: 8
            }]
        });

        this.setToolbarVisible(false);
        this.img.invisible();
    },

    _check: function () {
        var f = BI.isNotEmptyString(this.img.getSrc());
        this.label.setVisible(!f);
        this.img.visible(f);
    },

    _setSize: function (w, h) {
        this.img.setImageWidth(w);
        this.img.setImageHeight(h)
    },

    _sizeChange: function (size) {
        var self = this, o = this.options;
        switch (size) {
            case BI.ImageButtonSize.ORIGINAL:
                self._setSize("auto", "auto");
                break;
            case BI.ImageButtonSize.EQUAL:
                self._setSize("auto", "auto");
                var width = this.img.getImageWidth(), height = this.img.getImageHeight();
                var W = this.element.width(), H = this.element.height();
                if (W / H > width / height) {
                    self._setSize("auto", "100%");
                } else {
                    self._setSize("100%", "auto");
                }
                break;
            case BI.ImageButtonSize.WIDGET_SIZE:
                self._setSize("100%", "100%");
                break;
        }
    },

    setToolbarVisible: function (v) {
        this.upload.setVisible(v);
        this.size.setVisible(v);
        this.href.setVisible(v);
        this.del.setVisible(v);
    },

    getValue: function () {
        return {href: this.href.getValue(), size: this.size.getValue(), src: this.img.getSrc()}
    },

    setValue: function (v) {
        var self = this;
        v || (v = {});
        if (BI.isNotEmptyString(v.href)) {
            self.img.setValid(true)
        }
        this.href.setValue(v.href);
        this.size.setValue(v.size);
        this.img.setSrc(v.src);
        this._check();
        this._sizeChange(v.size)
    },

    resize: function () {
        this._sizeChange(this.size.getValue());
    }
});

BI.UploadImage.EVENT_DESTROY = "EVENT_DESTROY";
BI.UploadImage.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut("bi.upload_image", BI.UploadImage);