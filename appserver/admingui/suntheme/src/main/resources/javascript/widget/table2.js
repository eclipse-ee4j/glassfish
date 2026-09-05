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

/**
 * This function will be invoked when creating a Dojo widget. Please see
 * webui.suntheme.widget.table2.setProps for a list of supported
 * properties.
 *
 * Note: This is considered a private API, do not use.
 */
require([
    "webui/suntheme/common",
    "webui/suntheme/widget/common",
    "dojo/_base/declare",
    "dijit/_WidgetBase",
    "dijit/_OnDijitClickMixin",
    "dijit/_TemplatedMixin",
    "dojo/text!webui/suntheme../templates/table2.html"
], function (common, widgetCommon, declare, _WidgetBase,
        _OnDijitClickMixin, _TemplatedMixin, template) {

    /**
     * This function is used to set widget properties with the
     * following Object literals.
     *
     * <ul>
     *  <li>actions</li>
     *  <li>filterText</li>
     *  <li>id</li>
     *  <li>rowGroups</li>
     *  <li>title</li>
     *  <li>width</li>
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

        // Set container width.
        if (props.width) {
            this.style.width = this.width;
        }

        // Set widget properties.
        var widget = dojo.widget.byId(this.id);
        if (widget === null) {
            return false;
        }

        // Add title.
        if (props.title) {
            widgetCommon.addFragment(widget.titleContainer, props.title);
            common.setVisibleElement(widget.titleContainer, true);
        }

        // Add actions.
        if (props.actions) {
            widgetCommon.addFragment(widget.actionsContainer, props.actions);
            common.setVisibleElement(widget.actionsContainer, true);
        }

        // Add row groups.
        if (props.rowGroups) {
            widget.rowGroupsContainer.innerHTML = null; // Clear contents.
            for (var i = 0; i < props.rowGroups.length; i++) {
                var rowGroupsClone = widget.rowGroupsContainer;

                // Clone nodes.
                if (i + 1 < props.rowGroups.length) {
                    rowGroupsClone = widget.rowGroupsContainer.cloneNode(true);
                    widget.marginContainer.insertBefore(rowGroupsClone, widget.rowGroupsContainer);
                }
                widgetCommon.addFragment(rowGroupsClone, props.rowGroups[i], "last");
            }
        }
        return true;
    };

    return declare([_WidgetBase, _OnDijitClickMixin, _TemplatedMixin], {
        templateString: template,
        // Set public functions.
        setProps: setProps,
        // Set private functions (private functions/props prefixed with "_").
        // TBD...
        postCreate: function () {
            // Set ids.
            if (this.id) {
                this.actionsContainer.id = this.id + "_actionsContainer";
                this.filterPanelContainer.id = this.id + "_filterPanelContainer";
                this.marginContainer.id = this.id + "_marginContainer";
                this.preferencesPanelContainer.id = this.id + "_preferencesPanelContainer";
                this.sortPanelContainer.id = this.id + "_sortPanelContainer";
                this.rowGroupsContainer.id = this.id + "_rowGroupsContainer";
                this.titleContainer.id = this.id + "_titleContainer";
                this.tableFooterContainer.id = this.id + "_tableFooterContainer";
            }
            setProps({
                actions: this.actions,
                filterText: this.filterText,
                id: this.id,
                rowGroups: this.rowGroups,
                templatePath: this.templatePath,
                title: this.title,
                width: this.width
            });
        }
    });
});

