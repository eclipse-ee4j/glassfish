/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

/**
 * Button widget.See setProps for a list of supported properties.
 * Note: This is considered a private API, do not use.
 */
require([
    "webui/suntheme/button", "webui/suntheme/common",
    "webui/suntheme/widget/props",
    "webui/suntheme/widget/common",
    "dojo/on",
    "dojo/_base/declare",
    "dijit/_WidgetBase",
    "dijit/_OnDijitClickMixin",
    "dijit/_TemplatedMixin",
    "dojo/text!webui/suntheme../templates/button.html"
], function (button, common, props, widgetCommon, on, declare, _WidgetBase,
        _OnDijitClickMixin, _TemplatedMixin, template) {

    /**
     * Helper function to create callback for onBlur event.
     *
     * @param id The HTML element id used to invoke the callback.
     */
    createOnBlurCallback = function (id) {
        if (id !== null) {
            // New literals are created every time this function
            // is called, and it's saved by closure magic.
            return function (evt) {
                document.getElementById(id)._onblur();
            };
        }
    };

    /**
     * Helper function to create callback for onFocus event.
     *
     * @param id The HTML element id used to invoke the callback.
     */
    createOnFocusCallback = function (id) {
        if (id !== null) {
            // New literals are created every time this function
            // is called, and it's saved by closure magic.
            return function (evt) {
                document.getElementById(id)._onfocus();
            };
        }
    };

    /**
     * Helper function to create callback for onMouseOut event.
     *
     * @param id The HTML element id used to invoke the callback.
     */
    createOnMouseOutCallback = function (id) {
        if (id !== null) {
            // New literals are created every time this function
            // is called, and it's saved by closure magic.
            return function (evt) {
                document.getElementById(id)._onmouseout();
            };
        }
    };

    /**
     * Helper function to create callback for onMouseOver event.
     *
     * @param id The HTML element id used to invoke the callback.
     */
    createOnMouseOverCallback = function (id) {
        if (id !== null) {
            // New literals are created every time this function
            // is called, and it's saved by closure magic.
            return function (evt) {
                document.getElementById(id)._onmouseover();
            };
        }
    };

    /**
     * Helper function to obtain widget class names.
     */
    getClassName = function () {
        // To Do: The this.mydisabledcheck is just a hack
        // so I can use the old button JS for now...
        var className = null;
        if (this._props.mini === true && this._props.primary === true) {
            className = (this.disabled === true || this.mydisabled === true)
                    ? props.button.primaryMiniDisabledClassName
                    : props.button.primaryMiniClassName;
        } else if (this._props.mini === true) {
            className = (this.disabled === true || this.mydisabled === true)
                    ? props.button.secondaryMiniDisabledClassName
                    : props.button.secondaryMiniClassName;
        } else if (this._props.primary === true) {
            className = (this.disabled === true || this.mydisabled === true)
                    ? props.button.primaryDisabledClassName
                    : props.button.primaryClassName;
        } else {
            className = (this.disabled === true || this.mydisabled === true)
                    ? props.button.secondaryDisabledClassName
                    : props.button.secondaryClassName;
        }
        return (this._props.className)
                ? className + " " + this._props.className
                : className;
    };

    /**
     * This function is used to set widget properties with the
     * following Object literals.
     *
     * <ul>
     *  <li>alt</li>
     *  <li>align</li>
     *  <li>className</li>
     *  <li>contents</li>
     *  <li>dir</li>
     *  <li>disabled</li>
     *  <li>id</li>
     *  <li>lang</li>
     *  <li>mini</li>
     *  <li>name</li>
     *  <li>onBlur</li>
     *  <li>onClick</li>
     *  <li>onDblClick</li>
     *  <li>onFocus</li>
     *  <li>onKeyDown</li>
     *  <li>onKeyPress</li>
     *  <li>onKeyUp</li>
     *  <li>onMouseDown</li>
     *  <li>onMouseOut</li>
     *  <li>onMouseOver</li>
     *  <li>onMouseUp</li>
     *  <li>onMouseMove</li>
     *  <li>primary</li>
     *  <li>style</li>
     *  <li>tabIndex</li>
     *  <li>title</li>
     *  <li>type</li>
     *  <li>value</li>
     *  <li>visible</li>
     * </ul>
     *
     * @param props Key-Value pairs of properties.
     */
    setProps = function (props) {
        if (props === null) {
            return false;
        }

        // Save properties for later updates.
        if (this._props) {
            // Override existing values, if any.
            Object.extend(this._props, props);
        } else {
            this._props = props;
        }

        // TODO: The following is just a hack
        // so I can use the old button JS for now...
        this.classNamePrimary = props.button.primaryClassName;
        this.classNamePrimaryDisabled = props.button.primaryDisabledClassName;
        this.classNamePrimaryHov = props.button.primaryHovClassName;
        this.classNamePrimaryMini = props.button.primaryMiniClassName;
        this.classNamePrimaryMiniDisabled = props.button.primaryMiniDisabledClassName;
        this.classNamePrimaryMiniHov = props.button.primaryMiniHovClassName;
        this.classNameSecondary = props.button.secondaryClassName;
        this.classNameSecondaryDisabled = props.button.secondaryDisabledClassName;
        this.classNameSecondaryHov = props.button.secondaryHovClassName;
        this.classNameSecondaryMini = props.button.secondaryMiniClassName;
        this.classNameSecondaryMiniDisabled = props.button.secondaryMiniDisabledClassName;
        this.classNameSecondaryMiniHov = props.button.secondaryMiniHovClassName;
        this.mini = this._props.mini;
        this.mydisabled = this._props.disabled;
        this.primary = this._props.primary;
        this.secondary = !this.primary;

        // Set DOM node properties.
        widgetCommon.setCoreProperties(this, props);
        widgetCommon.setJavaScriptProperties(this, props);

        if (props.alt) {
            this.setAttribute("alt", props.alt);
        }
        if (props.align) {
            this.setAttribute("align", props.align);
        }
        if (props.dir) {
            this.setAttribute("dir", props.dir);
        }
        if (props.disabled !== null) {
            this.disabled = props.disabled;
        }
        if (props.name) {
            this.setAttribute("name", props.name);
        }
        if (props.value) {
            this.setAttribute("value", props.value);
        }
        this.setAttribute("type", props.type ? props.type : "submit");

        // Set style class.
        common.addStyleClass(this, this._getClassName());

        // Set contents.
        if (props.contents) {
            widgetCommon.addFragment(this, props.contents);
        }
        return true;
    };

    return declare([_WidgetBase, _OnDijitClickMixin, _TemplatedMixin], {
        templateString: template,
        // Set public functions. Note: Except for update, all are deprecated.
        isSecondary: button.isSecondary,
        setSecondary: button.setSecondary,
        isPrimary: button.isPrimary,
        setPrimary: button.setPrimary,
        isMini: button.isMini,
        setMini: button.setMini,
        getDisabled: button.getDisabled,
        setDisabled: button.setDisabled,
        getVisible: button.getVisible,
        setVisible: button.setVisible,
        getText: button.getText,
        setText: button.setText,
        doClick: button.click,
        setProps: this.setProps,
        // Set private functions (private functions/props prefixed with "_").
        _getClassName: this.getClassName,
        _onblur: button.onblur,
        _onfocus: button.onfocus,
        _onmouseover: button.onmouseover,
        _onmouseout: button.onmouseout,
        postCreate: function () {
            // Set events.
            on(this, "blur", createOnBlurCallback(this.id));
            on(this, "focus", createOnFocusCallback(this.id));
            on(this, "mouseout", createOnMouseOutCallback(this.id));
            on(this, "mouseover", createOnMouseOverCallback(this.id));

            // Set properties.
            setProps({
                alt: this.alt,
                align: this.align,
                className: this.className,
                contents: this.contents,
                dir: this.dir,
                disabled: (this.disabled !== null) ? this.disabled : false,
                id: this.id,
                lang: this.lang,
                mini: (this.mini !== null) ? this.mini : false,
                name: this.name,
                onBlur: this.onBlur,
                onClick: this.onClick,
                onDblClick: this.onDblClick,
                onFocus: this.onFocus,
                onKeyDown: this.onKeyDown,
                onKeyPress: this.onKeyPress,
                onKeyUp: this.onKeyUp,
                onMouseDown: this.onMouseDown,
                onMouseOut: this.onMouseOut,
                onMouseOver: this.onMouseOver,
                onMouseUp: this.onMouseUp,
                onMouseMove: this.onMouseMove,
                primary: (this.primary !== null) ? this.primary : false,
                style: this.style,
                tabIndex: this.tabIndex,
                templatePath: this.templatePath,
                title: this.title,
                type: this.type,
                value: this.value,
                visible: this.visible
            });
        }
    });
});
