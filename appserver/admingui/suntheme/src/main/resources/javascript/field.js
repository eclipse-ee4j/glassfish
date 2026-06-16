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

define(function () {

    return {
        /**
         * Use this function to get the HTML input or textarea element
         * associated with a TextField, PasswordField, HiddenField or TextArea
         * component. 
         * @param elementId The element ID of the field 
         * @return the input or text area element associated with the field
         * component 
         */
        getInputElement: function (elementId) {
            var element = document.getElementById(elementId);
            if (element !== null) {
                if (element.tagName === "INPUT") {
                    return element;
                }
                if (element.tagName === "TEXTAREA") {
                    return element;
                }
            }
            return document.getElementById(elementId + "_field");
        },

        /**
         * Use this function to get the value of the HTML element 
         * corresponding to the Field component
         * @param elementId The element ID of the Field component
         * @return the value of the HTML element corresponding to the 
         * Field component 
         */
        getValue: function (elementId) {
            return this.getInputElement(elementId).value;
        },

        /**
         * Use this function to set the value of the HTML element 
         * corresponding to the Field component
         * @param elementId The element ID of the Field component
         * @param newStyle The new value to enter into the input element
         * Field component 
         */
        setValue: function (elementId, newValue) {
            this.getInputElement(elementId).value = newValue;
        },

        /** 
         * Use this function to get the style attribute for the field. 
         * The style retrieved will be the style on the span tag that 
         * encloses the (optional) label element and the input element. 
         * @param elementId The element ID of the Field component
         */
        getStyle: function (elementId) {
            return this.getInputElement(elementId).style;
        },

        /**
         * Use this function to set the style attribute for the field. 
         * The style will be set on the <span> tag that surrounds the field. 
         * @param elementId The element ID of the Field component
         * @param newStyle The new style to apply
         */
        setStyle: function (elementId, newStyle) {
            this.getInputElement(elementId).style = newStyle;
        },

        /**
         * Use this function to disable or enable a field. As a side effect
         * changes the style used to render the field. 
         *
         * @param elementId The element ID of the field 
         * @param show true to disable the field, false to enable the field
         * @return true if successful; otherwise, false
         */
        setDisabled: function (elementId, disabled) {
            if (elementId === null || disabled === null) {
                // must supply an elementId && state
                return false;
            }
            var textfield = this.getInputElement(elementId);
            if (textfield === null) {
                return false;
            }
            var newState = new Boolean(disabled).valueOf();
            var isTextArea = textfield.className.indexOf("TxtAra_sun4") > -1;
            if (newState) {
                if (isTextArea) {
                    textfield.className = "TxtAraDis_sun4";
                } else {
                    textfield.className = "TxtFldDis_sun4";
                }
            } else {
                if (isTextArea) {
                    textfield.className = "TxtAra_sun4";
                } else {
                    textfield.className = "TxtFld_sun4";
                }
            }
            textfield.disabled = newState;
            return true;
        }
    };
});
