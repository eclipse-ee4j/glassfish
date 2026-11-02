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
    "webui/suntheme/field",
    "webui/suntheme/common"
],function (field, common) {

    return {
        addOnInitCallback: common.addOnInitCallback,
        /**
         * This function is used to initialize HTML element properties with the
         * following Object literals.
         *
         * <ul>
         *  <li>id</li>
         *  <li>datePickerId</li>
         *  <li>dateFieldId</li>
         *  <li>dateClass</li>
         *  <li>selectedClass</li>
         *  <li>edgeClass</li>
         *  <li>edgeSelectedClass</li>
         *  <li>todayClass</li>
         *  <li>dateFormat</li>
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

            // Set given properties on domNode.
            Object.extend(domNode, props);
            domNode.dateLinkId = props.datePickerId + ":dateLink";

            // Set functions.
            domNode.setSelected = this.setSelected;
            domNode.setDateValue = this.setDateValue;
            domNode.isToday = this.isToday;
            common.setInitialized(domNode);
        },

        setDateValue: function (value, link) {
            field.setValue(this.dateFieldId, value);
            this.setSelected(link);
        },

        setSelected: function (link) {
            if (link === null) {
                return;
            }

            var dateLink;
            var linkNum = 0;

            // Remove any prior highlight 
            while (linkNum < 42) {
                dateLink = document.getElementById(this.dateLinkId + linkNum);
                if (dateLink === null) {
                    break;
                }

                if (dateLink.className === this.edgeSelectedClass) {
                    dateLink.className = this.edgeClass;
                } else if (dateLink.className === this.selectedClass) {
                    if (this.isToday(dateLink.title)) {
                        dateLink.className = this.todayClass;
                    } else {
                        dateLink.className = this.dateClass;
                    }
                }
                linkNum++;
            }

            // apply the selected style to highlight the selected link
            if (link.className === this.dateClass ||
                    link.className === this.todayClass) {
                link.className = this.selectedClass;
            } else if (link.className === this.edgeClass) {
                link.className = this.edgeSelectedClass;
            }
            this.currentSelection = link;
        },

        // Find out if date is today's date
        isToday: function (date) {
            var todaysDate = new Date();
            var pattern = new String(this.dateFormat);
            var yearIndex = pattern.indexOf("yyyy");
            var monthIndex = pattern.indexOf("MM");
            var dayIndex = pattern.indexOf("dd");
            var currYear = todaysDate.getFullYear();
            var currMonth = todaysDate.getMonth() + 1;
            var currDay = todaysDate.getDate();

            if (currYear === parseInt(date.substr(yearIndex, 4))
                    && currMonth === parseInt(date.substr(monthIndex, 2))
                    && currDay === parseInt(date.substr(dayIndex, 2))) {
                return true;
            }
            return false;
        }
    };
});
