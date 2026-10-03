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
    "webui/suntheme/common",
    "webui/suntheme/widget/common",
    "dojo/topic",
    "dojo/on",
    "dojo/_base/declare",
    "dijit/_WidgetBase",
    "dijit/_OnDijitClickMixin",
    "dijit/_TemplatedMixin",
    "dojo/text!webui/suntheme../templates/table2RowGroup.html"
], function (common, widgetCommon, topic, on, declare, _WidgetBase,
        _OnDijitClickMixin, _TemplatedMixin, template) {

    /**
     * This function is used to add columns with the following Object
     * literals.
     *
     * <ul>
     *  <li>columns</li>
     * </ul>
     *
     * @param props Key-Value pairs of properties.
     */
    addColumns = function (props) {
        if (props === null || props.columns === null) {
            return false;
        }

        // Get widget.
        var widget = dojo.widget.byId(this.id);
        if (widget === null) {
            return false;
        }

        // Containers are visible if at least one header/footer exists.
        var headerVisible = false;
        var footerVisible = false;

        for (var i = 0; i < props.columns.length; i++) {
            var col = props.columns[i];
            var headerClone = widget.colHeaderNode;
            var footerClone = widget.colFooterNode;

            // Clone nodes.
            if (i + 1 < props.columns.length) {
                headerClone = headerClone.cloneNode(true);
                footerClone = footerClone.cloneNode(true);

                // Append nodes.
                widget.colHeaderContainer.insertBefore(headerClone, widget.colHeaderNode);
                widget.colFooterContainer.insertBefore(footerClone, widget.colFooterNode);
            }

            // Set properties.
            headerClone.id = col.id + "_colHeader" + i;
            footerClone.id = col.id + "_colFooter" + i;
            if (col.width) {
                headerClone.style.width = footerClone.style.width = col.width;
            }

            // Add text.
            if (col.headerText) {
                widgetCommon.addFragment(headerClone, col.headerText);
                headerVisible = true;
            }
            if (col.footerText) {
                widgetCommon.addFragment(footerClone, col.footerText);
                footerVisible = true;
            }
        }

        // Show containers.
        common.setVisibleElement(widget.colHeaderContainer, headerVisible);
        common.setVisibleElement(widget.colFooterContainer, footerVisible);
    };

    /**
     * This function is used to set rows with the following Object
     * literals.
     *
     * <ul>
     *  <li>first</li>
     *  <li>rows</li>
     * </ul>
     *
     * @param props Key-Value pairs of properties.
     */
    addRows = function (props) {
        if (props === null || props.first === null || props.rows === null) {
            return false;
        }

        // Get nodes.
        var widget = dojo.widget.byId(this.id);
        if (widget === null) {
            return false;
        }

        // Reject duplicate AJAX requests.
        if (this._first !== props.first) {
            return;
        }

        // For each row found, clone row container.
        for (var i = 0; i < props.rows.length; i++) {
            var cols = props.rows[i]; // Get next column.
            var rowId = this.id + ":" + (this._first + i);

            // Get row node.
            var rowNode = document.getElementById(this.id + "_rowNode");
            if (rowNode === null) {
                continue;
            }

            // Clone row container.
            rowNode.id = rowId + "_rowNode"; // Set id so we can locate row nodes in clone.
            var rowContainerClone = widget.rowContainer.cloneNode(true); // Clone with new row id.
            widget.tbodyContainer.appendChild(rowContainerClone); // Add row container clone.

            // Set properties.
            rowContainerClone.id = rowId;
            common.setVisibleElement(rowContainerClone, true); // Set visible.

            // Get row node from clone.
            rowNode.id = this.id + "_rowNode"; // Restore id before retrieving new node.
            rowNode = document.getElementById(rowId + "_rowNode");
            if (rowNode === null) {
                continue;
            }

            // For each column found, clone row node.
            for (var k = 0; k < cols.length; k++) {
                var col = this._props.columns[k]; // Get default column props.
                var colId = col.id.replace(this.id, rowId);
                var rowNodeClone = rowNode;

                // Clone node.
                if (k + 1 < cols.length) {
                    rowNodeClone = rowNode.cloneNode(true);
                    rowContainerClone.insertBefore(rowNodeClone, rowNode);
                }

                // Set properties.
                rowNodeClone.id = colId;
                if (col.width) {
                    rowNodeClone.style.width = col.width;
                }

                // Add cell data.
                widgetCommon.addFragment(rowNodeClone, cols[k], "last");
            }
        }

        // Set rows text upon first display only.
        if (this._first === 0) {
            this._setHeight();
            this._setRowsText();
        }

        // Set resize event -- hack for Moz/Firefox.
        if (common.browser.is_nav === true) {
            this._resize();
        }

        // Set first row value.
        this._first += props.rows.length;

        return true;
    };

    /**
     * This closure is used to process resize events.
     */
    resize = {

        /**
         * Helper function to create callback for resize event.
         *
         * @param id The HTML element id of the widget.
         */
        createCallback: function (id) {
            if (id !== null) {
                // New literals are created every time this function
                // is called, and it's saved by closure magic.
                return function (evt) {
                    // Note: The evt param for this event is not required here.
                    var domNode = document.getElementById(id);
                    if (domNode === null) {
                        return null;
                    } else {
                        domNode._resize();
                    }
                };
            }
        },

        /**
         * Process resize event.
         */
        processEvent: function () {
            // Get row id.
            var rowId = this.id + ":0";

            // Get height offset of each visible column.
            for (var i = 0; i < this._props.columns.length; i++) {
                var col = this._props.columns[i]; // Get default column props.
                var colId = col.id.replace(this.id, rowId);

                // Get row node.
                var rowNode = document.getElementById(colId);
                if (rowNode === null) {
                    continue;
                }

                // Get nodes.
                var columnHeaderNode = document.getElementById(col.id + "_colHeader" + i);
                var columnFooterNode = document.getElementById(col.id + "_colFooter" + i);

                // Set column header/footer width.
                if (columnHeaderNode) {
                    columnHeaderNode.style.width = rowNode.offsetWidth + "px";
                }
                if (columnFooterNode) {
                    columnFooterNode.style.width = rowNode.offsetWidth + "px";
                }
            }

            // Set group header/footer width.
            var widget = dojo.widget.byId(this.id);
            if (widget === null) {
                return false;
            }
            widget.groupHeaderNode.style.width = this.scrollWidth + "px";
            widget.groupFooterNode.style.width = this.scrollWidth + "px";

            return true;
        }
    };

    /**
     * This function is used to set the scrollable height after rows have
     * been added.
     */
    setHeight = function () {
        // Get height offset of each visible row.
        var offset = 0;
        for (var i = this._currentRow; i < this._currentRow + this._props.maxRows; i++) {
            var rowContainer = document.getElementById(this.id + ":" + i);
            if (rowContainer !== null) {
                offset += rowContainer.offsetHeight;
            } else {
                break;
            }
        }

        // Set height offset.
        if (offset > 0) {
            this.style.height = offset + "px";
        }
        return true;
    };

    /**
     * This function is used to set widget properties with the
     * following Object literals.
     *
     * <ul>
     *  <li>columns</li>
     *  <li>footerText</li>
     *  <li>headerText</li>
     *  <li>height</li>
     *  <li>id</li>
     *  <li>maxRows</li>
     *  <li>totalRows</li>
     * </ul>
     *
     * @param props Key-Value pairs of properties.
     */
    setProps = function (props) {
        if (props === null) {
            return false;
        }

        // Save properties for later updates.
        if (this._props) {
            Object.extend(this._props, props); // Override existing values, if any.
        } else {
            this._props = props;
        }

        // Set DOM node properties.
        widgetCommon.setCoreProperties(this, props);
        widgetCommon.setJavaScriptProperties(this, props);

        // Set widget properties.
        var widget = dojo.widget.byId(this.id);
        if (widget === null) {
            return false;
        }

        // Add header.
        if (props.headerText) {
            widgetCommon.addFragment(widget.groupHeaderTextNode, props.headerText);
            common.setVisibleElement(widget.groupHeaderContainer, true);
        }

        // Add footer.
        if (props.footerText) {
            widgetCommon.addFragment(widget.groupFooterNode, props.footerText);
            common.setVisibleElement(widget.groupFooterContainer, true);
        }

        // Add columns.
        if (props.columns) {
            // To do: Clear contents?
            this._addColumns({
                columns: props.columns
            });

            // Set colspan -- only works for IE.
            widget.groupHeaderNode.colSpan = props.columns.length;
            widget.groupFooterNode.colSpan = props.columns.length;
        }

        // To Do: Hack for A11Y testing.
        widget.tableContainer.summary = "This is a row group";

        return true;
    };

    /**
     * This function is used to set rows text (e.g., "1 - 5 of 20").
     */
    setRowsText = function () {
        // Get widget.
        var widget = dojo.widget.byId(this.id);
        if (widget === null) {
            return false;
        }

        // To do: Localize & add filter text.

        // Add title augment.
        var firstRow = this._currentRow + 1;
        var lastRow = Math.min(this._props.totalRows,
                this._currentRow + this._props.maxRows);
        widget.groupHeaderRowsTextNode.innerHTML = "Items: " + firstRow + " - " +
                lastRow + " of " + this._props.totalRows;

        return true;
    };

    /**
     * This closure is used to process scroll events.
     */
    scroll = {
        /**
         * Event topics for custom AJAX implementations to listen for.
         */
        beginEventTopic: "webui_widget_table2RowGroup_scroll_begin",
        endEventTopic: "webui_widget_table2RowGroup_scroll_end",

        /**
         * Process scroll event.
         *
         * @param evt Event generated by scroll bar.
         */
        processEvent: function (evt) {
            if (evt === null) {
                return false;
            }

            // Get DOM node.
            var domNode;
            if (evt.currentTarget) {
                domNode = evt.currentTarget;
            } else {
                return false;
            }

            // Publish an event to retrieve rows.
            if (domNode._first < domNode._props.totalRows
                    && domNode._currentRow % domNode._props.maxRows === 0) {
                scroll.publishBeginEvent(evt);
            }

            // Set current row based on scroll position and row offset.
            var first = 0; // First row in range.
            var last = Math.min(domNode._props.totalRows,
                    domNode._first + domNode._props.maxRows) - 1; // Last row in range.
            var scrollTop = domNode.scrollTop + 5; // Offset for borders.
            while (first < last) {
                var mid = Math.floor((first + last) / 2); // Index of midpoint.
                var rowContainer = document.getElementById(domNode.id + ":" + mid);
                if (rowContainer === null) {
                    break;
                }
                // Test if scroll position matches row offset.
                if (scrollTop < rowContainer.offsetTop) {
                    last = mid; // Search left half.
                } else if (scrollTop >= rowContainer.offsetTop) {
                    first = mid + 1; // Search right half.
                }
            }
            domNode._currentRow = Math.max(0, first - 1);

            // Set rows text.
            domNode._setRowsText();

            return true;
        },

        /**
         * Publish an event for custom AJAX implementations to listen for.
         *
         * @param evt Event generated by scroll bar.
         */
        publishBeginEvent: function (evt) {
            topic.publish(scroll.beginEventTopic, evt);
            return true;
        },

        /**
         * Publish an event for custom AJAX implementations to listen for.
         *
         * @param props Key-Value pairs of properties of the widget.
         */
        publishEndEvent: function (props) {
            topic.publish(scroll.endEventTopic, props);
            return true;
        }
    };

    return declare([_WidgetBase, _OnDijitClickMixin, _TemplatedMixin], {
        templateString: template,
        // Set public functions.
        setProps: setProps,
        scroll: scroll,
        // Set private properties (private functions/props prefixed with "_").
        _addCollumns: addColumns,
        _addRows: addRows,
        _resize: resize,
        _setHeight: setHeight,
        _setRowsText: setRowsText,
        _first: 0, // Index used to obtain rows.
        _currentRow: 0, // Current row in view.
        postCreate: function () {
            // Set ids.
            if (this.id) {
                this.colFooterContainer.id = this.id + "_colFooterContainer";
                this.colFooterNode.id = this.id + "_colFooterNode";
                this.colHeaderContainer.id = this.id + "_colHeaderContainer";
                this.colHeaderNode.id = this.id + "_colHeaderNode";
                this.groupFooterContainer.id = this.id + "_groupFooterContainer";
                this.groupFooterNode.id = this.id + "_groupFooter";
                this.groupHeaderContainer.id = this.id + "_groupHeaderContainer";
                this.groupHeaderNode.id = this.id + "_groupHeader";
                this.groupHeaderTextNode.id = this.id + "_groupHeaderText";
                this.groupHeaderRowsTextNode.id = this.id + "_groupHeaderRowsTextNode";
                this.rowContainer.id = this.id + "_rowContainer";
                this.rowNode.id = this.id + "_rowNode";
                this.tableContainer.id = this.id + "_tableContainer";
                this.tbodyContainer.id = this.id + "_tbodyContainer";
                this.tfootContainer.id = this.id + "_tfootContainer";
                this.theadContainer.id = this.id + "_theadContainer";
            }

            // Set events.
            on(this, "scroll", scroll.processEvent);

            // Resize hack for Moz/Firefox.
            if (common.browser.is_nav === true) {
                on(window, "resize", resize.createCallback(this.id));
            }

            // Set properties.
            setProps({
                columns: this.columns,
                footerText: this.footerText,
                headerText: this.headerText,
                height: this.height,
                id: this.id,
                maxRows: (this.maxRows > 0) ? this.maxRows : 5,
                templatePath: this.templatePath,
                totalRows: (this.totalRows > 0) ? this.totalRows : 0
            });

            // Add initial rows.
            scroll.processEvent({
                currentTarget: this
            });
        }
    });
});
