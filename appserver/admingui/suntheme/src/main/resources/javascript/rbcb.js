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

        setChecked: function (elementId, checked, type) {
            if (elementId === null || type === null) {
                return false;
            }
            var rbcb = document.getElementById(elementId);
            if (rbcb === null) {
                return false;
            }
            // wrong type
            if (rbcb.type !== type.toLowerCase()) {
                return false;
            }
            // Get boolean value to ensure correct data type.
            rbcb.checked = new Boolean(checked).valueOf();
            return true;
        },

        setDisabled: function (elementId, disabled, type, enabledStyle,
                disabledStyle) {
            if (elementId === null || disabled === null || type === null) {
                // must supply an elementId && state && type
                return false;
            }
            var rbcb = document.getElementById(elementId);
            if (rbcb === null) {
                // specified elementId not found
                return false;
            }
            // wrong type
            if (rbcb.type !== type.toLowerCase()) {
                return false;
            }
            rbcb.disabled = new Boolean(disabled).valueOf();
            if (rbcb.disabled) {
                if (disabledStyle !== null) {
                    rbcb.className = disabledStyle;
                }
            } else {
                if (enabledStyle !== null) {
                    rbcb.className = enabledStyle;
                }
            }
            return true;
        },

        /** 
         * Set the disabled state for all radio buttons with the given controlName.
         * If disabled is set to true, the element is shown with disabled styles.
         *
         * @param elementId The element Id
         * @param formName The name of the form containing the element
         * @param disabled true or false
         * @return true if successful; otherwise, false
         */
        setGroupDisabled: function (controlName, disabled, type, enabledStyle,
                disabledStyle) {
            // Validate params.
            if (controlName === null) {
                return false;
            }
            if (disabled === null) {
                return false;
            }
            if (type === null) {
                return false;
            }

            // Get radiobutton group elements.
            var x = document.getElementsByName(controlName)

            // Set disabled state.
            for (var i = 0; i < x.length; i++) {
                // Get element.
                var element = x[i];
                if (element === null || element.name !== controlName) {
                    continue;
                }
                // Validate element type.
                if (element.type.toLowerCase() !== type) {
                    return false;
                }
                // Set disabled state.
                element.disabled = new Boolean(disabled).valueOf();

                // Set class attribute.
                if (element.disabled) {
                    if (disabledStyle !== null) {
                        element.className = disabledStyle;
                    }
                } else {
                    if (enabledStyle !== null) {
                        element.className = enabledStyle;
                    }
                }
            }
            return true;
        }
    };
});
