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
    "webui/suntheme/rbcb"
], function (rbcb) {

    return {
        /**
         * Set the disabled state for the given radiobutton element Id. If the disabled 
         * state is set to true, the element is shown with disabled styles.
         *
         * @param elementId The element Id
         * @param disabled true or false
         * @return true if successful; otherwise, false
         */
        setDisabled: function (elementId, disabled) {
            return rbcb.setDisabled(elementId, disabled,
                    "radio", "Rb", "RbDis");
        },

        /**
         * Set the disabled state for all the radio buttons in the radio button
         * group identified by controlName. If disabled
         * is set to true, the check boxes are displayed with disabled styles.
         *
         * @param controlName The radio button group control name
         * @param disabled true or false
         * @return true if successful; otherwise, false
         */
        setGroupDisabled: function (controlName, disabled) {
            return rbcb.setGroupDisabled(controlName,
                    disabled, "radio", "Rb", "RbDis");
        },

        /**
         * Set the checked property for a radio button with the given element Id.
         *
         * @param elementId The element Id
         * @param checked true or false
         * @return true if successful; otherwise, false
         */
        setChecked: function (elementId, checked) {
            return rbcb.setChecked(elementId, checked,
                    "radio");
        }
    };
});
