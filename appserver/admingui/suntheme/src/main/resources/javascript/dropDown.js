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
    "webui/suntheme/common"
], function (common) {

    getSelectElement = function (elementId) {
        var element = document.getElementById(elementId);
        if (element !== null) {
            if (element.tagName === "SELECT") {
                return element;
            }
        }
        return document.getElementById(elementId + "_list");
    };

    return {

        /**
         * Use this function to access the HTML select element that makes up
         * the dropDown.
         *
         * @param elementId The component id of the JSF component (this id is
         * assigned to the span tag enclosing the HTML elements that make up
         * the dropDown).
         * @return a reference to the select element. 
         */
        getSelectElement: getSelectElement,

        /**
         * This function is invoked by the choice onselect action to set the
         * selected, and disabled styles.
         *
         * Page authors should invoke this function if they set the 
         * selection using JavaScript.
         *
         * @param elementId The component id of the JSF component (this id is
         * rendered in the div tag enclosing the HTML elements that make up
         * the list).
         * @return true if successful; otherwise, false
         */
        changed: function (elementId) {
            var listItem = getSelectElement(elementId).options;

            //disabled items should not be selected (IE problem)
            //So setting selectedIndex = -1 for disabled items.

            if (common.browser.is_ie) {
                for (var i = 0; i < listItem.length; ++i) {
                    if (listItem[i].disabled === true &&
                            listItem[i].selected === true) {

                        listItem.selectedIndex = -1;
                    }
                }
            }

            for (var cntr = 0; cntr < listItem.length; ++cntr) {
                if (listItem[cntr].className === "MnuStdOptSep_sun4"
                        || listItem[cntr].className === "MnuStdOptGrp_sun4") {
                    continue;
                } else if (listItem[cntr].disabled) {
                    // Regardless if the option is currently selected or not,
                    // the disabled option style should be used when the option
                    // is disabled. So, check for the disabled item first.
                    // See CR 6317842.
                    listItem[cntr].className = "MnuStdOptDis_sun4";
                } else if (listItem[cntr].selected) {
                    listItem[cntr].className = "MnuStdOptSel_sun4";
                } else {
                    // This does not work on Opera 7. There is a bug such that if 
                    // you touch the option at all (even if I explicitly set
                    // selected to false!), it goes back to the original
                    // selection. 
                    listItem[cntr].className = "MnuStdOpt_sun4";
                }
            }
            return true;
        },

        /**
         * Set the disabled state for given dropdown element Id. If the disabled 
         * state is set to true, the element is shown with disabled styles.
         *
         * Page authors should invoke this function if they dynamically
         * enable or disable a dropdown using JavaScript.
         * 
         * @param elementId The component id of the JSF component (this id is
         * rendered in the div tag enclosing the HTML elements that make up
         * the list).
         * @param disabled true or false
         * @return true if successful; otherwise, false
         */
        setDisabled: function (elementId, disabled) {
            var choice = getSelectElement(elementId);
            if (disabled) {
                choice.disabled = true;
                choice.className = "MnuStdDis_sun4";
            } else {
                choice.disabled = false;
                choice.className = "MnuStd_sun4";
            }
            return true;
        },

        /**
         * Invoke this JavaScript function to get the value of the first
         * selected option on the dropDown. If no option is selected, this
         * function returns null. 
         *
         * @param elementId The component id of the JSF component (this id is
         * rendered in the div tag enclosing the HTML elements that make up
         * the list).
         * @return The value of the selected option, or null if none is
         * selected. 
         */
        getSelectedValue: function (elementId) {
            var dropDown = getSelectElement(elementId);
            var index = dropDown.selectedIndex;
            if (index === -1) {
                return null;
            } else {
                return dropDown.options[index].value;
            }
        },

        /**
         * Invoke this JavaScript function to get the label of the first
         * selected option on the dropDown. If no option is selected, this
         * function returns null.
         * 
         * @param elementId The component id of the JSF component (this id is
         * rendered in the div tag enclosing the HTML elements that make up
         * the list).
         * @return The label of the selected option, or null if none is
         * selected. 
         */
        getSelectedLabel: function (elementId) {
            var dropDown = getSelectElement(elementId);
            var index = dropDown.selectedIndex;
            if (index === -1) {
                return null;
            } else {
                return dropDown.options[index].label;
            }
        }
    };
});
