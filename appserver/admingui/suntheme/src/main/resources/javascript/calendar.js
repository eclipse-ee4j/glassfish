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
    "webui/suntheme/common",
    "webui/suntheme/listbox",
    "webui/suntheme/field"
], function (common, listbox, field) {

    return {
        addOnInitCallback: common.addOnInitCallback,
        /**
         * This function is used to initialize HTML element properties with the
         * following Object literals.
         *
         * <ul>
         *  <li>id</li>
         *  <li>firstDay</li>
         *  <li>fieldId</li>
         *  <li>patternId</li>
         *  <li>calendarToggleId</li>
         *  <li>datePickerId</li>
         *  <li>monthMenuId</li>
         *  <li>yearMenuId</li>
         *  <li>rowId</li>
         *  <li>showButtonSrc</li>
         *  <li>hideButtonSrc</li>
         *  <li>dateFormat</li>
         *  <li>dateClass</li>
         *  <li>edgeClass</li>
         *  <li>selectedClass</li>
         *  <li>edgeSelectedClass</li>
         *  <li>todayClass</li>
         *  <li>hiddenClass</li>
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

            domNode.field = document.getElementById(props.fieldId);
            domNode.pattern = document.getElementById(props.patternId);
            domNode.calendarToggle = document.getElementById(props.calendarToggleId);
            domNode.datePickerId = props.datePickerId;
            domNode.dateLinkId = props.datePickerId + ":dateLink";
            domNode.lastRow = document.getElementById(props.rowId);
            domNode.monthMenu = listbox.getSelectElement(props.monthMenuId);
            domNode.yearMenu = listbox.getSelectElement(props.yearMenuId);

            // Set functions.
            domNode.toggle = this.toggleCalendar;
            domNode.dayClicked = this.dayClicked;
            domNode.decreaseMonth = this.decreaseMonth;
            domNode.increaseMonth = this.increaseMonth;
            domNode.redrawCalendar = this.redrawCalendar;
            domNode.setCurrentValue = this.setCurrentValue;
            domNode.setDisabled = this.setDisabled;
            domNode.setInitialFocus = this.setInitialFocus;
            domNode.formatDate = this.formatDate;
            domNode.ieStackingContextFix = this.ieStackingContextFix;
            domNode.ieGetShim = this.ieGetShim;
            domNode.ieShowShim = this.ieShowShim;
            domNode.ieHideShim = this.ieHideShim;
            domNode.setSelectedValue = this.setSelectedValue;
            domNode.setLimitedSelectedValue = this.setLimitedSelectedValue;
            domNode.redrawPopup = this.redrawPopup;
            common.setInitialized(domNode);
        },

        /* This function is used by the day links in the calendar display to
         * set the date in the textfield. The day argument is the day as an
         * int, the monthFix is -1, 0, +1 and is used by the last days of the 
         * previous month and the next days of the next month.
         */
        dayClicked: function (link) {
            //store old value
            var oldFieldValue = this.field.value;

            // get the current year
            var year = parseInt(this.yearMenu.options[this.yearMenu.selectedIndex].value);

            // get the current month
            var month = parseInt(this.monthMenu.options[this.monthMenu.selectedIndex].value);
            var day = parseInt(link.innerHTML);
            var monthFix = 0;
            var className = link.className;
            if (className === this.edgeClass) {
                if (day > 20) {
                    monthFix = -1;
                } else if (day < 7) {
                    monthFix = 1;
                }
            }

            if (monthFix !== 0) {
                if (monthFix === -1) {
                    if (month === 1) {
                        month = 13;
                        year--;
                    }
                } else {
                    if (month === 12) {
                        month = 0;
                        year++;
                    }
                }
                month = month + monthFix;
            }
            this.field.value = this.formatDate(month, day, year);
            this.toggle();

            //manually call onchange if appropriate
            if (this.field.value !== oldFieldValue
                    && (typeof this.field.onchange === 'function')) {
                this.field.onchange();
            }

        },

        decreaseMonth: function () {
            // If the monthMenu has a zero value, set it to January
            // (that's what it will have appeared like in the browser).
            // This can happen on IE. 
            if (this.monthMenu.value === null) {
                this.monthMenu.value = this.monthMenu.options[0].value;
            }
            var month = parseInt(this.monthMenu.value);
            if (month === 1) {
                // If the yearMenu has no value (can happen on IE) 
                // set it to the first available year 
                // (that's what it will have appeared like in the browser).
                if (this.yearMenu.value === null) {
                    this.yearMenu.value = this.yearMenu.options[0].value;
                } else if (this.yearMenu.value === this.yearMenu.options[0].value) {
                    // No need to redraw the calendar in this case, we don't
                    // change anything.
                } else {
                    // Decrease the year by one and set the month to December
                    var year = parseInt(this.yearMenu.value);
                    year--;
                    this.yearMenu.value = year;
                    month = 12;
                }
            } else {
                month--;
            }
            this.monthMenu.value = month;
            this.redrawCalendar(false);
        },

        increaseMonth: function () {
            // If the monthMenu has a zero value, set it to January
            // (that's what it will have appeared like in the browser).
            // This can happen on IE. 
            if (this.monthMenu.value === null) {
                this.monthMenu.value = this.monthMenu.options[0].value;
            }
            var month = parseInt(this.monthMenu.value);
            if (month === 12) {
                // If the yearMenu has no value (can happen on IE) 
                // set it to the first available year 
                // (that's what it will have appeared like in the browser).

                var numOptions = this.yearMenu.options.length;
                if (this.yearMenu.value === null) {
                    this.yearMenu.value = this.yearMenu.options[0].value;
                } else if (this.yearMenu.value ===
                        this.yearMenu.options[numOptions - 1].value) {
                    // No need to redraw the calendar in this case, we don't
                    // change anything.
                } else {
                    // Increase the year by one and set the month to January
                    var year = parseInt(this.yearMenu.value);
                    year++;
                    this.yearMenu.value = year;
                    month = 1;
                }
            } else {
                month++;
            }
            this.monthMenu.value = month;
            this.redrawCalendar(false);
        },

        redrawCalendar: function (initialize) {
            var selected = 0;   //if 1 - 31, will show that day as highlighted
            var today = 0;

            //selectedYear, selectedMonth, selectedDay:
            //the date to show as highlighted (this.currentValue or today's date)
            //provided that the user is viewing that month and year
            var selectedYear = null;
            var selectedMonth = null;
            var selectedDay = null;
            if (this.currentValue !== null) {
                selectedYear = this.currentValue.getFullYear();
                selectedMonth = this.currentValue.getMonth() + 1;
                selectedDay = this.currentValue.getDate();
            }

            var todayDate = new Date();
            var todayYear = todayDate.getFullYear();
            var todayMonth = todayDate.getMonth() + 1;
            var todayDay = todayDate.getDate();

            if (initialize) {
                //set showMonth as selected in the monthMenu
                //set showYear as selected in the yearMenu
                //use todayMonth and todayYear as "backups" (in case this.currentValue is null)
                var showMonth = todayMonth;
                var showYear = todayYear;
                if (this.currentValue !== null) {
                    //we have a currentValue, so use that for showMonth and showYear
                    showMonth = selectedMonth;
                    showYear = selectedYear;
                }
                this.setLimitedSelectedValue(this.monthMenu, showMonth);
                this.setLimitedSelectedValue(this.yearMenu, showYear);

            } else {
                //mbohm: preserving the following logic, but to my knowledge, it should not occur.
                if (this.yearMenu.value === null || this.monthMenu.value === null) {
                    this.yearMenu.value = this.yearMenu.options[0].value;
                    this.monthMenu.value = this.monthMenu.options[0].value;
                }
            }

            //set selected
            var yearMenuValue = parseInt(this.yearMenu.value);
            var monthMenuValue = parseInt(this.monthMenu.value);
            if (this.currentValue !== null && selectedYear === yearMenuValue &&
                    selectedMonth === monthMenuValue) {
                selected = selectedDay;
            }

            //set today
            if (todayYear === yearMenuValue &&
                    todayMonth === monthMenuValue) {
                today = todayDay;
            }

            var month = parseInt(this.monthMenu.value);
            month--;
            var year = parseInt(this.yearMenu.value);

            // construct a date object for the newly displayed month
            var first = new Date(year, month, 1);
            var numDays = 31;
            var last = new Date(year, month, numDays + 1);

            while (last.getDate() !== 1) {
                numDays--;
                last = new Date(year, month, numDays + 1);
            }

            // determine what day of the week the 1st of the new month is
            var linkNum = 0;
            var link;

            // Fill in any number of days before the first day of the month
            // On Firefox at least, JavaScript treats Sunday as the first day 
            // of the week regardless of the browser's locale. 
            // We have to compensate for the fact that Sunday is not the first
            // day of the week everywhere.
            var firstDay = first.getDay();

            // In JavaScript (unlike java.util.Calendar), Sunday is 0. 
            if (firstDay !== this.firstDay) {
                var backDays = (firstDay - this.firstDay + 7) % 7;
                var oneDayInMs = 86400000; // 1000 * 60 * 60 * 24;
                var day = new Date(first.getTime() - backDays * oneDayInMs);
                // assert day == first day of week of previous month
                while (day.getMonth() !== month) {
                    link = document.getElementById(this.dateLinkId + linkNum);
                    link.title = this.formatDate(day.getMonth() + 1, day.getDate(),
                            day.getFullYear());
                    link.className = this.edgeClass;
                    link.innerHTML = day.getDate();
                    day.setTime(day.getTime() + oneDayInMs);
                    linkNum++;
                }
            }

            var counter = 0;
            while (counter < numDays) {
                link = document.getElementById(this.dateLinkId + linkNum);
                link.innerHTML = ++counter;
                if (counter === selected) {
                    link.className = this.selectedClass;
                } else if (counter === today) {
                    link.className = this.todayClass;
                } else {
                    link.className = this.dateClass;
                }
                link.title = this.formatDate(first.getMonth() + 1, counter,
                        first.getFullYear());
                linkNum++;
            }

            if (linkNum < 35) {
                counter = 1;
                while (linkNum < 35) {
                    link = document.getElementById(this.dateLinkId + linkNum);
                    link.className = this.edgeClass;
                    link.innerHTML = counter;
                    link.title = this.formatDate(first.getMonth() + 2, counter,
                            first.getFullYear());
                    linkNum++;
                    counter++;
                }
                this.lastRow.style.display = "none";
            } else if (linkNum === 35) {
                this.lastRow.style.display = "none";
            } else {
                counter = 1;
                while (linkNum < 42) {
                    link = document.getElementById(this.dateLinkId + linkNum);
                    link.className = this.edgeClass;
                    link.innerHTML = counter;
                    link.title = this.formatDate(first.getMonth() + 2, counter,
                            first.getFullYear());
                    linkNum++;
                    counter++;
                }
                this.lastRow.style.display = "";
            }
        },

        setCurrentValue: function () {
            var curDate = this.field.value;
            var matches = true;
            if (curDate === "") {
                this.currentValue = null;
                return;
            }

            var pattern = new String(this.dateFormat);
            var yearIndex = pattern.indexOf("yyyy");
            var monthIndex = pattern.indexOf("MM");
            var dayIndex = pattern.indexOf("dd");

            // If the format is invalid, set the current value to null
            if (yearIndex < 0 || monthIndex < 0 || dayIndex < 0) {
                this.currentValue = null;
                return;
            }

            var counter = 0;
            var number;
            var selectedDate = new Date();
            var found = 0;
            var dateString;

            while (counter < curDate.length) {
                if (counter === yearIndex) {
                    try {
                        number = parseInt(curDate.substr(counter, 4));
                        if (isNaN(number)) {
                            this.currentValue = null;
                            return;
                        }
                        var index = 0;
                        var foundYear = false;
                        while (index < this.yearMenu.length) {
                            if (number === this.yearMenu.options[index].value) {
                                selectedDate.setFullYear(number);
                                ++found;
                                foundYear;
                            }
                            index++;
                        }
                        if (!foundYear) {
                            break;
                        }
                    } catch (e) {
                    }
                } else if (counter === monthIndex) {
                    try {
                        dateString = curDate.substr(counter, 2);
                        // This is a workaround for Firefox! 
                        // parseInt() returns 0 for values 08 and 09
                        // while for example 07 works! 
                        if (dateString.charAt(0) === '0') {
                            dateString = dateString.substr(1, 1);
                        }
                        number = parseInt(dateString);
                        if (isNaN(number)) {
                            this.currentValue = null;
                            return;
                        }
                        selectedDate.setMonth(number - 1);
                        ++found;
                    } catch (e) {
                    }
                } else if (counter === dayIndex) {
                    try {
                        dateString = curDate.substr(counter, 2);
                        // This is a workaround for Firefox! 
                        // parseInt() returns 0 for values 08 and 09
                        // while all other leading zeros work
                        if (dateString.charAt(0) === '0') {
                            dateString = dateString.substr(1, 1);
                        }
                        number = parseInt(dateString);
                        if (isNaN(number)) {
                            this.currentValue = null;
                            return;
                        }
                        selectedDate.setDate(number);
                        ++found;
                    } catch (e) {
                    }
                }
                ++counter;
            }

            if (found === 3) {
                this.currentValue = selectedDate;
            } else {
                this.currentValue = null;
            }
            return;
        },

        toggleCalendar: function () {
            var div = document.getElementById(this.datePickerId);
            div.style.position = "absolute";
            div.style.left = "5px";
            div.style.top = "24px";

            if (div.style.display === "block") {
                // hide the calendar popup
                div.style.display = "none";
                this.calendarToggle.src = this.showButtonSrc;
                this.ieStackingContextFix(div);
            } else {
                this.setCurrentValue();
                this.redrawCalendar(true);

                // display the calendar popup
                div.style.display = "block";
                this.calendarToggle.src = this.hideButtonSrc;

                // place focus on the month menu
                this.setInitialFocus();

                // workaround for initial display problem on mozilla
                // the problem manifests itself as follows: 
                // click the link to make the calendar show
                // click one of the triangular buttons - the display
                // "contracts"
                // ...except it doesn't work!
                //var actualClass = link.className;
                //link.className = "DatBldLnk";
                //link.className = actualClass;

                this.ieStackingContextFix(div);
                this.redrawPopup();
            }
        },

        findPosX: function (obj) {
            var curleft = 0;
            if (obj.offsetParent) {
                while (obj.offsetParent) {
                    curleft += obj.offsetLeft;
                    obj = obj.offsetParent;
                }
            } else if (obj.x) {
                curleft += obj.x;
            }
            return curleft;
        },

        findPosY: function (obj) {
            var curtop = 0;
            if (obj.offsetParent) {
                while (obj.offsetParent) {
                    curtop += obj.offsetTop;
                    obj = obj.offsetParent;
                }
            } else if (obj.y) {
                curtop += obj.y;
            }
            return curtop;
        },

        setDisabled: function (disabled) {
            field.setDisabled(this.field.id, disabled);
            var span = this.calendarToggle.parentNode;
            if (disabled) {
                span.style.display = "none";
            } else {
                span.style.display = "block";
            }
            if (disabled) {
                common.addStyleClass(this.pattern, this.hiddenClass);
            } else {
                common.stripStyleClass(this.pattern, this.hiddenClass);
            }
        },

        setInitialFocus: function () {
            var pattern = new String(this.dateFormat);
            var yearIndex = pattern.indexOf("yyyy");
            var monthIndex = pattern.indexOf("MM");

            if (yearIndex < monthIndex) {
                this.yearMenu.focus();
            } else {
                this.monthMenu.focus();
            }
        },

        formatDate: function (month, day, year) {
            var date = new String(this.dateFormat);
            if (month > 12) {
                month = month % 12;
                if (month === 1) {
                    year++;
                }
            }
            date = date.replace("yyyy", new String(year));
            if (month < 10) {
                date = date.replace("MM", "0" + new String(month));
            } else {
                date = date.replace("MM", new String(month));
            }
            if (day < 10) {
                date = date.replace("dd", "0" + new String(day));
            } else {
                date = date.replace("dd", new String(day));
            }
            return date;
        },

        // Function worksaround IE bug where popup calendar appears under
        // other components (eeg 2005-11-11)

        /**
         * div = Main popup div with class="CalPopShdDiv"
         */
        ieStackingContextFix: function (div) {
            // Test for IE and return if not
            if (!document.all) {
                return;
            }

            if (div.style.display === "block") {
                // This popup should be displayed

                // Get the current zIndex for the div
                var divZIndex = div.currentStyle.zIndex;

                // Propogate the zIndex up the offsetParent tree
                var tag = div.offsetParent;
                while (tag !== null) {
                    var position = tag.currentStyle.position;
                    if (position === "relative" || position === "absolute") {
                        // Save any zIndex so it can be restored
                        tag.raveOldZIndex = tag.style.zIndex;
                        // Change the zIndex
                        tag.style.zIndex = divZIndex;
                    }
                    tag = tag.offsetParent;
                }

                // Hide controls unaffected by z-index
                this.ieShowShim(div);
            } else {
                // This popup should be hidden so restore zIndex-s
                var tag = div.offsetParent;
                while (tag !== null) {
                    var position = tag.currentStyle.position;
                    if (position === "relative" || position === "absolute") {
                        if (tag.raveOldZIndex !== null) {
                            tag.style.zIndex = tag.raveOldZIndex;
                        }
                    }
                    tag = tag.offsetParent;
                }
                this.ieHideShim(div);
            }
        },

        /**
         * Gets or creates an iframe shim for popup used to hide windowed
         * components in IE 5.5 and above. Assumes popup has id.
         */
        ieGetShim: function (popup) {
            var shimId = popup.id + "_shim";
            var shim = document.getElementById(shimId);
            if (shim === null) {
                shim = document.createElement(
                        '<iframe style="display: none;" src="javascript:false;"' +
                        ' frameBorder="0" scrolling="no"></iframe>');
                shim.id = shimId;
                if (popup.offsetParent === null) {
                    document.body.appendChild(shim);
                } else {
                    popup.offsetParent.appendChild(shim);
                }
            }
            return shim;
        },

        ieShowShim: function (popup) {
            var shim = this.ieGetShim(popup);
            shim.style.position = "absolute";
            shim.style.left = popup.style.left;
            shim.style.top = popup.style.top;
            shim.style.width = popup.offsetWidth;
            shim.style.height = popup.offsetHeight;
            shim.style.zIndex = popup.currentStyle.zIndex - 1;
            shim.style.display = "block";
        },

        ieHideShim: function (popup) {
            var shim = this.ieGetShim(popup);
            shim.style.display = "none";
        },

        setSelectedValue: function (select, val) {
            for (var i = 0; i < select.length; i++) {
                if (select.options[i].value === val) {
                    select.selectedIndex = i;
                    return;
                }
            }
            select.selectedIndex = -1;
        },

        /**
         * Set the value of a SELECT, but limit value to min and max
         */
        setLimitedSelectedValue: function (select, value) {
            var min = select.options[0].value;
            var max = select.options[select.length - 1].value;
            if (value < min) {
                select.selectedIndex = 0;
            } else if (value > max) {
                select.selectedIndex = select.length - 1;
            } else {
                this.setSelectedValue(select, value);
            }
            return;
        },

        /**
         * Workaround gecko scrunched table bug and force a redraw
         */
        redrawPopup: function () {
            // Force a redraw of the popup header controls by changing the selected
            // month which will call the onChange handler to redraw.
            var oldIndex = this.monthMenu.selectedIndex;
            this.monthMenu.selectedIndex = 0;
            this.monthMenu.selectedIndex = oldIndex;

            // Redraw the popup grid with the date numbers
            this.redrawCalendar(false);
        }
    };
});
