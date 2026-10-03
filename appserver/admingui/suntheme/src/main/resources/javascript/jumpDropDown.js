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
    "webui/suntheme/props",
    "webui/suntheme/dropDown"
], function (props, dropDown) {

    return {
        changed: function (elementId) {
            var jumpDropdown = dropDown.getSelectElement(elementId);
            var form = jumpDropdown;
            while (form !== null) {
                form = form.parentNode;
                if (form.tagName === "FORM") {
                    break;
                }
            }
            if (form !== null) {
                var submitterFieldId = elementId + "_submitter";
                document.getElementById(submitterFieldId).value = "true";

                var listItem = jumpDropdown.options;
                for (var cntr = 0; cntr < listItem.length; ++cntr) {
                    if (listItem[cntr].className ===
                            props.jumpDropDown.optionSeparatorClassName
                            || listItem[cntr].className ===
                            props.jumpDropDown.optionGroupClassName) {
                        continue;
                    } else if (listItem[cntr].disabled) {
                        // Regardless if the option is currently selected or not,
                        // the disabled option style should be used when the option
                        // is disabled. So, check for the disabled item first.
                        // See CR 6317842.
                        listItem[cntr].className = props.jumpDropDown.optionDisabledClassName;
                    } else if (listItem[cntr].selected) {
                        listItem[cntr].className = props.jumpDropDown.optionSelectedClassName;
                    } else {
                        listItem[cntr].className = props.jumpDropDown.optionClassName;
                    }
                }
                form.submit();
            }
            return true;
        }
    };
});
