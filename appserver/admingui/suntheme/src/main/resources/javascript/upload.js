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

define([
    "webui/suntheme/field"
], function (field) {

    return {
        /**
         * Use this function to get the HTML input element associated with the
         * Upload component.  
         * @param elementId The element ID of the Upload
         * @return the input element associated with the Upload component 
         */
        getInputElement: function (elementId) {
            var element = document.getElementById(elementId);
            if (element.tagName === "INPUT") {
                return element;
            }
            return document.getElementById(elementId + "_com.sun.webui.jsf.upload");
        },

        /**
         * Use this function to disable or enable a upload. As a side effect
         * changes the style used to render the upload. 
         *
         * @param elementId The element ID of the upload 
         * @param show true to disable the upload, false to enable the upload
         * @return true if successful; otherwise, false
         */
        setDisabled: function (elementId, disabled) {
            if (elementId === null || disabled === null) {
                // must supply an elementId && state
                return false;
            }
            var input = this.getInputElement(elementId);
            if (input === null) {
                // specified elementId not found
                return false;
            }
            // Disable field using setDisabled function -- do not hard code styles here.
            return field.setDisabled(input.id, disabled);
        },

        setEncodingType: function (elementId) {
            var upload = this.getInputElement(elementId);
            var form = upload;
            while (form !== null) {
                form = form.parentNode;
                if (form.tagName === "FORM") {
                    break;
                }
            }
            if (form !== null) {
                // form.enctype does not work for IE, but works Safari
                // form.encoding works on both IE and Firefox, but does not work for Safari
                // form.enctype = "multipart/form-data";

                // convert all characters to lowercase to simplify testing
                var agent = navigator.userAgent.toLowerCase();

                if (agent.indexOf('safari') !== -1) {
                    // form.enctype works for Safari
                    // form.encoding does not work for Safari
                    form.enctype = "multipart/form-data";
                } else {
                    // form.encoding works for IE, FireFox
                    form.encoding = "multipart/form-data";
                }
            }
            return false;
        }
    };
});
