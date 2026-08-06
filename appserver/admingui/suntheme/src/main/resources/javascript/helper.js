/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

/* global admingui, getClass */

// This file represents the API of the javascript code generated on the server
// side by the renderers.

// TODO cover User API here also to avoid hard-coding javascript code
// to the theme

//////////////////////////////////////////////////////
// PRIVATE UTILITIES
//////////////////////////////////////////////////////

var getClass = {}.toString;

/**
 * Test if the given object is a function.
 * @param {object} object to test
 * @returns {Boolean} true if a function, false otherwise
 */
function __isFunction(object) {
    return object && getClass.call(object) === '[object Function]';
}

/**
 * Register a callback on DOM element that may not be initialized yet.
 * @param {string} type component type
 * @param {string} elt DOM element
 * @param {function} callBack callback to be invoked when ready, only once
 * @returns {undefined}
 */
function __addOnInitCallback(type, elt, callBack) {
    require(['webui/suntheme/' + type], function (module) {
        module.addOnInitCallback(elt, callBack);
    });
}

//////////////////////////////////////////////////////
// WOODSTOCK SPI
//////////////////////////////////////////////////////

/**
 * Register a {@code onBlur} callback on a DOM element that may not be
 *  initialized yet.
 * @param {string} type component type
 * @param {elt} elt dom DOM element firing the event
 * @returns {undefined}
 */
function ws_onblur(type, elt) {
    __addOnInitCallback(type, elt, function (e) {
        e.myonblur();
    });
}

/**
 * Register a {@code onFocus} callback on a DOM element that may not be
 *  initialized yet.
 * @param {string} type component type
 * @param {elt} elt dom DOM element firing the event
 * @returns {undefined}
 */
function ws_onfocus(type, elt) {
    __addOnInitCallback(type, elt, function (e) {
        e.myonfocus();
    });
}

/**
 * Register a {@code onMouseOut} callback on a DOM element that may not be
 * initialized yet.
 * @param {string} type component type
 * @param {elt} elt dom DOM element firing the event
 * @returns {undefined}
 */
function ws_onmouseout(type, elt) {
    __addOnInitCallback(type, elt, function (e) {
        e.myonmouseout();
    });
}

/**
 * Register a {@code onMouseOver} callback on a DOM element that may not be
 *  initialized yet.
 * @param {string} type component type
 * @param {elt} elt dom DOM element firing the event
 * @returns {undefined}
 */
function ws_onmouseover(type, elt) {
    __addOnInitCallback(type, elt, function (e) {
        e.myonmouseover();
    });
}

/**
 * Invoke the {@code onChange} method on the given module with the given id
 * as argument.
 * @param {string} type component type
 * @param {string} eltId DOM element id
 * @returns {undefined}
 */
function ws_changed(type, eltId) {
    require(['webui/suntheme/' + type], function (module) {
        module.changed(eltId);
    });
}

/**
 * Invoke the {@code onChange} method on a given drop down.
 * @param {string} eltId DOM element id of a drop down
 * @returns {undefined}
 */
function ws_dropdown_changed(eltId) {
    admingui.woodstock.dropDownChanged(eltId);
}

/**
 * Submit an hyperlink.
 * @param {dom} elt link DOM element
 * @param {string} formId DOM element id of the form
 * @param {array} params array of name/value sequence, may be {@code null}
 * @returns {undefined}
 */
function ws_hyperlink_submit(elt, formId, params){
    if (elt === null || elt === undefined || params !== null
            && (typeof params !== "object" || params.constructor !== Array)) {
        return;
    }
    admingui.woodstock.hyperLinkSubmit(elt, formId, params);
}

/**
 * Register a callback  that invokes {@code updateButtons} on a DOM element
 * that may not be initialized yet.
 * @param {string} type component type
 * @param {string} eltId DOM element id
 * @returns {undefined}
 */
function ws_update_buttons(type, eltId) {
    var elt = document.getElementById(eltId);
    if (elt !== undefined) {
        __addOnInitCallback(type, elt, function (e) {
            e.updateButtons();
        });
    }
}

/**
 * Register a callback that invokes {@code addCommonTask} on a DOM element
 * that may not be initialized yet.
 * @param {string} eltId DOM element id
 * @param {object} props properties
 * @returns {undefined}
 */
function ws_add_common_task(eltId, props){
    var elt = document.getElementById(eltId);
    if (elt !== undefined) {
        __addOnInitCallback('commonTasksSection', elt, function (e) {
            e.addCommonTask(props);
        });
    }
}

/**
 * Initialize an element.
 * @param {string} type component type
 * @param {object} props properties
 * @returns {undefined}
 */
function ws_init_elt(type, props) {
    require(['webui/suntheme/' + type], function (module) {
        module.init(props);
    });
}

/**
 * Initialize the body.
 * @param {string} viewId View ID
 * @param {string} urlString URL
 * @param {string} defaultFocusElementId the default focus element ID
 * @param {string} focusElementId the requested focus element ID
 * @param {string} focusElementFieldId the focus field ID
 * @returns {undefined}
 */
function ws_init_body(viewId, urlString, defaultFocusElementId, focusElementId,
        focusElementFieldId) {
    require([
        "webui/suntheme/common",
        "webui/suntheme/body"
    ], function (common, body) {
        // The common.body function accepts the scroll cookie
        // information. We should reconsider using cookies and
        // use hidden fields instead. This will provide better
        // interfaces for portals and other frameworks, where
        // cookie processing is not convenient. Also a browser
        // can easily exceed the maximum of 300 cookies.
        // Instead of creating a global variable...
        common.body = new body.body(viewId, urlString, defaultFocusElementId,
                focusElementId, focusElementFieldId);
    });
}

/**
 * Initialize a tree component.
 * @param {Object} props init props
 * @param {string} clientId DOM element id to highlight if nodeId is {@code null}
 * @param {string} nodeId the id of the node to highlight
 * @returns {undefined}
 */
function ws_init_tree(props, clientId, nodeId) {
    require(["webui/suntheme/tree"], function (tree) {
        tree.init(props);
        if (nodeId === null || nodeId === undefined) {
            tree.updateHighlight(clientId);
        } else {
            tree.selectTreeNode(nodeId);
        }
    });
}

/**
 * Set the encoding type for a given DOM element id.
 * @param {string} eltId DOM element id
 * @returns {undefined}
 */
function ws_upload_set_encoding_type(eltId) {
    require(["webui/suntheme/upload"], function (upload) {
        upload.setEncodingType(eltId);
    });
}

/**
 * Trigger widget parsing.
 * @param {array} moduleNames array of DOJO modules to import
 * @returns {undefined}
 */
// TODO, change args to {string} type and {bool}useAjax
function ws_widget_parse(moduleNames) {
    // skip if moduleNames is not an array
    if (typeof moduleNames !== "object"
            || moduleNames.constructor !== Array) {
        return;
    }
    var modules = ["dojo/parser"];
    for (i = 0; i < moduleNames.length; i++) {
        modules.push("webui/suntheme/widget" + moduleNames[i]);
    }
    require(modules, function (parser) {
        parser.parse();
    });
}

//////////////////////////////////////////////////////
// WOODSTOCK USER API
//////////////////////////////////////////////////////

/**
 * Get the selected value of a dropdown element.
 * @param {string} eltId DOM element id
 * @param {function} callback consumer of value 
 * @returns {undefined}
 */
function ws_dropdown_getselected(eltId, callback){
    if(!__isFunction(callback)){
        return;
    }
    require(["webui/suntheme/dropDown"], function(dropDown){
        callback(dropDown.getSelectedValue(eltId));
    });
}

/**
 * Set the checked value of a checkbox element.
 * @param {string} eltId DOM element id
 * @param {bool} value new value
 * @returns {undefined}
 */
function ws_checkbox_setchecked(eltId, value){
    require(["webui/suntheme/checkbox"], function(checkbox){
        checkbox.setChecked(eltId, value);
    });
}

/**
 * Set a field value.
 * @param {string} eltId DOM element id
 * @param {string} value new value
 * @returns {undefined}
 */
function ws_field_set_value(eltId, value){
    require(["webui/suntheme/field"], function(field){
        field.setValue(eltId, value);
    });
}
