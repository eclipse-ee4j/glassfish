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

define(["webui/suntheme/common", "webui/suntheme/props"], function (common, props) {

    return {
        addOnInitCallback: common.addOnInitCallback,

        /**
         * This function is used to initialize HTML element properties with the
         * following Object literals.
         *
         * <ul>
         *  <li>disabled</li>
         *  <li>icon</li>
         *  <li>id</li>
         *  <li>mini</li>
         *  <li>secondary</li>
         * </ul>
         *
         * Note: This is considered a private API, do not use.
         *
         * @param props Key-Value pairs of properties.
         */
        init: function (props) {
            if (props === null || props.id === null) {
                return false;
            }
            var domNode = document.getElementById(props.id);
            if (domNode === null) {
                return false;
            }
            if(common.fireInitCallBacks(domNode)){
                return true;
            }

            // Save given properties with the DOM node for later updates.
            Object.extend(domNode, {
                icon: new Boolean(props.icon).valueOf(),
                id: props.id,
                isOneOfOurButtons: true,
                mini: new Boolean(props.mini).valueOf(),
                mydisabled: new Boolean(props.disabled).valueOf(),
                secondary: new Boolean(props.secondary).valueOf()
            });

            // Set style classes
            if (domNode.icon === true) {
                domNode.classNamePrimary = "Btn3_sun4";
                domNode.classNamePrimaryDisabled = "Btn3Dis_sun4";
                domNode.classNamePrimaryHov = "Btn3Hov_sun4";

                // Currently not used in theme.
                domNode.classNamePrimaryMini = "";
                domNode.classNamePrimaryMiniDisabled = "";
                domNode.classNamePrimaryMiniHov = "";
                domNode.classNameSecondary = "";
                domNode.classNameSecondaryDisabled = "";
                domNode.classNameSecondaryHov = "";
                domNode.classNameSecondaryMini = "";
                domNode.classNameSecondaryMiniDisabled = "";
                domNode.classNameSecondaryMiniHov = "";
            } else {
                domNode.classNamePrimary = "Btn1_sun4";
                domNode.classNamePrimaryDisabled = "Btn1Dis_sun4";
                domNode.classNamePrimaryHov = "Btn1Hov_sun4";
                domNode.classNamePrimaryMini = "Btn1Mni_sun4";
                domNode.classNamePrimaryMiniDisabled = "Btn1MniDis_sun4";
                domNode.classNamePrimaryMiniHov = "Btn1MniHov_sun4";
                domNode.classNameSecondary = "Btn2_sun4";
                domNode.classNameSecondaryDisabled = "Btn2Dis_sun4";
                domNode.classNameSecondaryHov = "Btn2Hov_sun4";
                domNode.classNameSecondaryMini = "Btn2Mni_sun4";
                domNode.classNameSecondaryMiniDisabled = "Btn2MniDis_sun4";
                domNode.classNameSecondaryMiniHov = "Btn2MniHov_sun4";
            }

            // Set functions
            domNode.isSecondary = this.isSecondary;
            domNode.setSecondary = this.setSecondary;
            domNode.isPrimary = this.isPrimary;
            domNode.setPrimary = this.setPrimary;
            domNode.isMini = this.isMini;
            domNode.setMini = this.setMini;
            domNode.getDisabled = this.getDisabled;
            domNode.setDisabled = this.setDisabled;
            domNode.getVisible = this.getVisible;
            domNode.setVisible = this.setVisible;
            domNode.getText = this.getText;
            domNode.setText = this.setText;
            domNode.doClick = this.click;
            domNode.myonblur = this.onblur;
            domNode.myonfocus = this.onfocus;
            domNode.myonmouseover = this.onmouseover;
            domNode.myonmouseout = this.onmouseout;

            // Set button state.
            domNode.setDisabled(domNode.mydisabled);
            domNode.setSecondary(domNode.secondary);
            domNode.setMini(domNode.mini);
            common.setInitialized(domNode);
        },

        /**
         * Simulate a mouse click in a button. 
         *
         * @return true if successful; otherwise, false
         */
        click: function () {
            this.click();
            return true;
        },

        /**
         * Get the textual label of a button. 
         *
         * @return The element value or null
         */
        getText: function () {
            return this.value;
        },

        /**
         * Set the textual label of a button. 
         *
         * @param text The element value
         * @return true if successful; otherwise, false
         */
        setText: function (text) {
            if (text === null) {
                return false;
            }

            this.value = text;
            return true;
        },

        /**
         * Use this function to show or hide a button. 
         *
         * @param show true to show the element, false to hide the element
         * @return true if successful; otherwise, false
         */
        setVisible: function (show) {
            if (show === null) {
                return false;
            }
            // Get element.
            common.setVisibleElement(this, show);

            return true;
        },

        /**
         * Use this function to find whether or not this is visible according to our
         * spec.
         *
         * @return true if visible; otherwise, false
         */
        getVisible: function () {
            // Get element.
            styles = common.splitStyleClasses(this);
            if (styles === null) {
                return true;
            }
            return !common.checkStyleClasses(styles,
                    props.hiddenClassName);
        },

        /**
         * Test if button is set as "primary".
         *
         * @return true if primary; otherwise, false for secondary
         */
        isPrimary: function () {
            return !this.isSecondary();
        },

        /**
         * Set button as "primary".
         *
         * @param primary true for primary, false for secondary
         * @return true if successful; otherwise, false
         */
        setPrimary: function (primary) {
            return this.setSecondary(!primary);
        },

        /**
         * Test if button is set as "secondary".
         *
         * @return true if secondary; otherwise, false for primary
         */
        isSecondary: function () {
            return this.secondary;
        },

        /**
         * Set button as "secondary".
         *
         * @param secondary true for secondary, false for primary
         * @return true if successful; otherwise, false
         */
        setSecondary: function (secondary) {
            if (secondary === null || this.mydisabled) {
                return false;
            }
            var stripType;
            var stripTypeHov;
            var newType;

            if (this.secondary === false && secondary === true) {
                //change from primary to secondary
                if (this.mini) {
                    stripTypeHov = this.classNamePrimaryMiniHov;
                    stripType = this.classNamePrimaryMini;
                    newType = this.classNameSecondaryMini;
                } else {
                    stripTypeHov = this.classNamePrimaryHov;
                    stripType = this.classNamePrimary;
                    hovType = this.classNameSecondaryHov;
                    newType = this.classNameSecondary;
                }
            } else if (this.secondary === true && secondary === false) {
                //change from secondary to primary
                if (this.mini) {
                    //this is currently a mini button
                    stripTypeHov = this.classNameSecondaryMiniHov;
                    stripType = this.classNameSecondaryMini;
                    newType = this.classNamePrimaryMini;
                } else {
                    stripTypeHov = this.classNameSecondaryHov;
                    stripType = this.classNameSecondary;
                    newType = this.classNamePrimary;
                }
            } else {
                // don't need to do anything
                return false;
            }
            common.stripStyleClass(this, stripTypeHov);
            common.stripStyleClass(this, stripType);
            common.addStyleClass(this, newType);
            this.secondary = secondary;
            return this.secondary;
        },

        /**
         * Test if button is set as "mini".
         *
         * @return true if mini; otherwise, false
         */
        isMini: function () {
            return this.mini;
        },

        /**
         * Set button as "mini".
         *
         * @param mini true for mini, false for standard button
         * @return true if successful; otherwise, false
         */
        setMini: function (mini) {
            if (mini === null || this.mydisabled) {
                return false;
            }
            var stripType;
            var stripTypeHov;
            var newType;
            if (this.mini === true && mini === false) {
                //change from mini to nonmini
                if (!this.secondary) {
                    //this is currently a primary button
                    stripTypeHov = this.classNamePrimaryMiniHov;
                    stripType = this.classNamePrimaryMini;
                    newType = this.classNamePrimary;
                } else {
                    stripTypeHov = this.classNameSecondaryMiniHov;
                    stripType = this.classNameSecondaryMini;
                    newType = this.classNameSecondary;
                }
            } else if (this.mini === false && mini === true) {
                if (!this.secondary) {
                    //this is currently a primary button
                    stripTypeHov = this.classNamePrimaryHov;
                    stripType = this.classNamePrimary;
                    newType = this.classNamePrimaryMini;
                } else {
                    stripTypeHov = this.classNameSecondaryHov;
                    stripType = this.classNameSecondary;
                    newType = this.classNameSecondaryMini;
                }
            } else {
                // don't need to do anything
                return false;
            }
            common.stripStyleClass(this, stripTypeHov);
            common.stripStyleClass(this, stripType);
            common.addStyleClass(this, newType);
            this.mini = mini;
            return this.mini;
        },

        /**
         * Test disabled state of button.
         *
         * @return true if disabled; otherwise, false
         */
        getDisabled: function () {
            return this.mydisabled;
        },

        /**
         * Test disabled state of button.
         *
         * @param disabled true if disabled; otherwise, false
         * @return true if successful; otherwise, false
         */
        setDisabled: function (disabled) {
            if (disabled === null || this.mydisabled === disabled) {
                return false;
            }
            var stripType;
            var stripHovType;
            var newType;
            if (!this.secondary) {
                //this is currently a primary button
                if (this.mini) {
                    if (disabled === false) {
                        stripType = this.classNamePrimaryMiniDisabled;
                        stripHovType = this.classNamePrimaryMiniDisabled;
                        newType = this.classNamePrimaryMini;
                    } else {
                        stripType = this.classNamePrimaryMini;
                        stripHovType = this.classNamePrimaryMiniHov;
                        newType = this.classNamePrimaryMiniDisabled;
                    }
                } else { // not mini
                    if (disabled === false) {
                        stripType = this.classNamePrimaryDisabled;
                        stripHovType = this.classNamePrimaryDisabled;
                        newType = this.classNamePrimary;
                    } else {
                        stripType = this.classNamePrimary;
                        stripHovType = this.classNamePrimaryHov;
                        newType = this.classNamePrimaryDisabled;
                    }
                }
            } else {
                //this is currently a secondary button
                if (this.mini) {
                    if (disabled === false) {
                        stripType = this.classNameSecondaryMiniDisabled;
                        stripHovType = this.classNameSecondaryMiniDisabled;
                        newType = this.classNameSecondaryMini;
                    } else {
                        stripType = this.classNameSecondaryMini;
                        stripHovType = this.classNameSecondaryMiniHov;
                        newType = this.classNameSecondaryMiniDisabled;
                    }
                } else { // not mini
                    if (disabled === false) {
                        stripType = this.classNameSecondaryDisabled;
                        stripHovType = this.classNameSecondaryDisabled;
                        newType = this.classNameSecondary;
                    } else {
                        stripType = this.classNameSecondary;
                        stripHovType = this.classNameSecondaryHov;
                        newType = this.classNameSecondaryDisabled;
                    }
                }
            }
            common.stripStyleClass(this, stripHovType);
            common.stripStyleClass(this, stripType);
            common.addStyleClass(this, newType);
            this.mydisabled = disabled;
            this.disabled = disabled;
            return true;
        },

        /**
         * Set CSS styles for onblur event.
         *
         * @return true if successful; otherwise, false
         */
        onblur: function () {
            if (this.mydisabled === true) {
                return true;
            }
            var stripType;
            var newType;
            if (!this.secondary) {
                if (this.mini) {
                    stripType = this.classNamePrimaryMiniHov;
                    newType = this.classNamePrimaryMini;
                } else {
                    stripType = this.classNamePrimaryHov;
                    newType = this.classNamePrimary;
                }
            } else { //is secondary 
                if (this.mini) {
                    stripType = this.classNameSecondaryMiniHov;
                    newType = this.classNameSecondaryMini;
                } else {
                    stripType = this.classNameSecondaryHov;
                    newType = this.classNameSecondary;
                }
            }
            // This code can generate a JavaScript error if the cursor quickly moves
            // off the button while the page is being refreshed.
            try {
                common.stripStyleClass(this, stripType);
                common.addStyleClass(this, newType);
            } catch (e) {
            }
            return true;
        },

        /**
         * Set CSS styles for onmouseout event.
         *
         * @return true if successful; otherwise, false
         */
        onmouseout: function () {
            if (this.mydisabled === true) {
                return true;
            }

            var stripType;
            var newType;
            if (!this.secondary) {
                if (this.mini) {
                    stripType = this.classNamePrimaryMiniHov;
                    newType = this.classNamePrimaryMini;
                } else {
                    stripType = this.classNamePrimaryHov;
                    newType = this.classNamePrimary;
                }
            } else { //is secondary 
                if (this.mini) {
                    stripType = this.classNameSecondaryMiniHov;
                    newType = this.classNameSecondaryMini;
                } else {
                    stripType = this.classNameSecondaryHov;
                    newType = this.classNameSecondary;
                }
            }
            // This code can generate a JavaScript error if the cursor quickly moves
            // off the button while the page is being refreshed.
            try {
                common.stripStyleClass(this, stripType);
                common.addStyleClass(this, newType);
            } catch (e) {
            }
            return true;
        },

        /**
         * Set CSS styles for onfocus event.
         *
         * @return true if successful; otherwise, false
         */
        onfocus: function () {
            if (this.mydisabled === true) {
                return true;
            }
            var stripType;
            var newType;
            if (!this.secondary) {
                if (this.mini) {
                    stripType = this.classNamePrimaryMini;
                    newType = this.classNamePrimaryMiniHov;
                } else {
                    stripType = this.classNamePrimary;
                    newType = this.classNamePrimaryHov;
                }
            } else { //is secondary 
                if (this.mini) {
                    stripType = this.classNameSecondaryMini;
                    newType = this.classNameSecondaryMiniHov;
                } else {
                    stripType = this.classNameSecondary;
                    newType = this.classNameSecondaryHov;
                }
            }
            // This code can generate a JavaScript error if the cursor quickly moves
            // off the button while the page is being refreshed.
            try {
                common.stripStyleClass(this, stripType);
                common.addStyleClass(this, newType);
            } catch (e) {
            }
            return true;
        },

        /**
         * Set CSS styles for onmouseover event.
         *
         * @return true if successful; otherwise, false
         */
        onmouseover: function () {
            if (this.mydisabled === true) {
                return false;
            }
            var stripType;
            var newType;
            if (!this.secondary) {
                if (this.mini) {
                    stripType = this.classNamePrimaryMini;
                    newType = this.classNamePrimaryMiniHov;
                } else {
                    stripType = this.classNamePrimary;
                    newType = this.classNamePrimaryHov;
                }
            } else { //is secondary 
                if (this.mini) {
                    stripType = this.classNameSecondaryMini;
                    newType = this.classNameSecondaryMiniHov;
                } else {
                    stripType = this.classNameSecondary;
                    newType = this.classNameSecondaryHov;
                }
            }
            // This code can generate a JavaScript error if the cursor quickly moves
            // off the button while the page is being refreshed.
            try {
                common.stripStyleClass(this, stripType);
                common.addStyleClass(this, newType);
            } catch (e) {
            }
            return true;
        }
    };

});
