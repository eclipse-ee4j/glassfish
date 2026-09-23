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

require([
    "webui/suntheme/common"
], function (common) {

    return {
        /**
         * Helper function to add a widget or string to the given
         * parent node. If props is an object containing a _widgetType
         * value, a widget will be added to the given parent node per the
         * specified position. If props contains a _modules array,
         * the given resources shall be retrieved before creating the
         * widget. If props is a string, it will be added as the contents
         * of the given parent node.
         *
         * Note: The position argument can be null for strings. However,
         * if strings must be appended to the same DOM node, use "last".
         *
         * @param parentNode The parent node used to add widget.
         * @param props Key-Value pairs of properties.
         * @param position The position (e.g., first, last, etc.) to add widget.
         */
        addFragment: function (parentNode, props, position) {
            // TODO re-implement or remove
            return true;
        },

        /**
         * Helper function to create a widget. This function assumes that
         * an HTML element is used as a place holder for the document
         * fragment (i.e., widget).
         *
         * Note: If props does not contain id and _widgetType properties,
         * a widget shall not be created. If props contains a _modules
         * array, the given resources shall be retrieved before creating
         * the widget.
         *
         * @param props Key-Value pairs of properties.
         */
        createWidget: function (props) {
            // TODO re-implement or remove
            return true;
        },

        /**
         * Helper function to obtain a module resource.
         *
         * @param module The module resource to retrieve.
         */
        require: function (module) {
            // TODO re-implement or remove
            return true;
        },

        /**
         * This function is used to set core properties for the given DOM
         * node with the following Object literals.
         *
         * <ul>
         *  <li>accesskey</li>
         *  <li>className</li>
         *  <li>dir</li>
         *  <li>id</li>
         *  <li>lang</li>
         *  <li>style</li>
         *  <li>title</li>
         *  <li>tabIndex</li>
         *  <li>visible</li>
         * </ul>
         *
         * @param domNode The DOM node to assign properties to.
         * @param props Key-Value pairs of properties.
         */
        setCoreProperties: function (domNode, props) {
            if (domNode == null || props == null) {
                return false;
            }
            if (props.accesskey) {
                domNode.setAttribute("accesskey", props.accesskey);
            }
            if (props.className) {
                domNode.setAttribute("class", props.className);
            }
            if (props.dir) {
                domNode.setAttribute("dir", props.dir);
            }
            if (props.id) {
                domNode.setAttribute("id", props.id);
            }
            if (props.lang) {
                domNode.setAttribute("lang", props.lang);
            }
            if (props.style) {
                domNode.style.cssText = props.style; // Required for IE?
            }
            if (props.title) {
                domNode.setAttribute("title", props.title);
            }
            if (props.tabIndex) {
                domNode.setAttribute("tabindex", props.tabIndex);
            }
            if (props.visible !== null) {
                common.setVisibleElement(domNode, props.visible);
            }
            return true;
        },

        /**
         * This function is used to set JavaScript properties for the given DOM
         * node with the following Object literals.
         *
         * <ul>
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
         * </ul>
         *
         * @param domNode The DOM node to assign properties to.
         * @param props Key-Value pairs of properties.
         */
        setJavaScriptProperties: function (domNode, props) {
            if (domNode === null || props === null) {
                return false;
            }

            // Note: IE does not recognize JSON strings as JavaScript. In order
            // for events to work properly, an anonymous function must be created.
            var is_ie = common.browser.is_ie;
            if (props.onBlur) {
                domNode.setAttribute("onblur", (is_ie && typeof props.onBlur === 'string')
                        ? new Function("event", props.onBlur)
                        : props.onBlur);
            }
            if (props.onClick) {
                domNode.setAttribute("onclick", (is_ie && typeof props.onClick === 'string')
                        ? new Function("event", props.onClick)
                        : props.onClick);
            }
            if (props.onDblClick) {
                domNode.setAttribute("ondblclick", (is_ie && typeof props.onDblClick === 'string')
                        ? new Function("event", props.onDblClick)
                        : props.onDblClick);
            }
            if (props.onFocus) {
                domNode.setAttribute("onfocus", (is_ie && typeof props.onFocus === 'string')
                        ? new Function("event", props.onFocus)
                        : props.onFocus);
            }
            if (props.onKeyDown) {
                domNode.setAttribute("onkeydown", (is_ie && typeof props.onKeyDown === 'string')
                        ? new Function("event", props.onKeyDown)
                        : props.onKeyDown);
            }
            if (props.onKeyPress) {
                domNode.setAttribute("onkeypress", (is_ie && typeof props.onKeyPress === 'string')
                        ? new Function("event", props.onKeyPress)
                        : props.onKeyPress);
            }
            if (props.onKeyUp) {
                domNode.setAttribute("onkeyup", (is_ie && typeof props.onKeyUp === 'string')
                        ? new Function("event", props.onKeyUp)
                        : props.onKeyUp);
            }
            if (props.onMouseDown) {
                domNode.setAttribute("onmousedown", (is_ie && typeof props.onMouseDown === 'string')
                        ? new Function("event", props.onMouseDown)
                        : props.onMouseDown);
            }
            if (props.onMouseOut) {
                domNode.setAttribute("onmouseout", (is_ie && typeof props.onMouseOut === 'string')
                        ? new Function("event", props.onMouseOut)
                        : props.onMouseOut);
            }
            if (props.onMouseOver) {
                domNode.setAttribute("onmouseover", (is_ie && typeof props.onMouseOver === 'string')
                        ? new Function("event", props.onMouseOver)
                        : props.onMouseOver);
            }
            if (props.onMouseUp) {
                domNode.setAttribute("onmouseup", (is_ie && typeof props.onMouseUp === 'string')
                        ? new Function("event", props.onMouseUp)
                        : props.onMouseUp);
            }
            if (props.onMouseMove) {
                domNode.setAttribute("onmousemove", (is_ie && typeof props.onMouseMove === 'string')
                        ? new Function("event", props.onMouseMove)
                        : props.onMouseMove);
            }
            return true;
        }
    };
});
