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
         * This function is used to submit a hyperlink.
         *
         * @params hyperlink The hyperlink element
         * @params formId The form id
         * @params params Name value pairs
         */
        submit: function (hyperlink, formId, params) {
            //params are name value pairs but all one big string array
            //so params[0] and params[1] form the name and value of the first param
            var theForm = document.getElementById(formId);
            var oldTarget = theForm.target;
            var oldAction = theForm.action;
            theForm.action += "?" + hyperlink.id + "_submittedField=" + hyperlink.id;
            if (params !== null) {
                for (var i = 0; i < params.length; i++) {
                    theForm.action += "&" + params[i] + "=" + params[i + 1];
                    i++;
                }
            }
            if (hyperlink.target !== null) {
                theForm.target = hyperlink.target;
            }
            theForm.submit();
            // Fix for CR 6469040 - Hyperlink:Does not work correctly in
            // frames environment. 
            if (hyperlink.target !== null) {
                theForm.target = oldTarget;
                theForm.action = oldAction;
            }
            return false;
        },

        /**
         * Use this function to access the HTML img element that makes up
         * the icon hyperlink. 
         *
         * @param elementId The component id of the JSF component (this id is
         * assigned to the outter most tag enclosing the HTML img element).
         * @return a reference to the img element. 
         */
        getImgElement: function (elementId) {
            // Image hyperlink is now a naming container and the img element id 
            // includes the ImageHyperlink parent id.
            if (elementId !== null) {
                var parentid = elementId;
                var colon_index = elementId.lastIndexOf(":");
                if (colon_index !== -1) {
                    parentid = elementId.substring(colon_index + 1);
                }
                return document.getElementById(elementId + ":" + parentid + "_image");
            }
            return document.getElementById(elementId + "_image");
        }
    };
});
