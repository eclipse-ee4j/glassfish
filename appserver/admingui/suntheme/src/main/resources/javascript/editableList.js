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
    "webui/suntheme/listbox",
    "webui/suntheme/field",
    "webui/suntheme/common"
], function (listbox, field, common) {

    return {
        addOnInitCallback: common.addOnInitCallback,
        /**
         * This function is used to initialize HTML element properties with the
         * following Object literals.
         *
         * <ul>
         *  <li>id</li>
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

            // Not a facet does not have "extra" editable list id.

            // child elements
            // Get the field by calling the field getInputMethod
            // because only it knows about the underlying structure
            // of the rendered field component
            domNode.list = listbox.getSelectElement(props.id + "_list");

            // Bug 6338492 -
            //     ALL: If a component supports facets or children is must be a
            //      NamingContainer
            // Since EditableList has become a NamingContainer the id's for
            // the facet children are prefixed with the EditableList id
            // in addition to their own id, which also has the 
            // EditableList id, as has been the convention for facets. This introduces
            // a redundancy in the facet id so the add button now looks like
            //
            // "formid:editablelistid:editablelistid:editablelistid_addButton"
            //
            // It used to be "formid:editablelistid_addButton"
            // It would be better to encapsulate that knowledge in the
            // EditableList renderer as does FileChooser which has the
            // same problem but because the select elements are not
            // facets in EditableList they really do only have id's of the
            // form "formid:addremoveid_list". Note that 
            // in these examples the "id" parameter is "formid:editablelistid"
            //
            // Therefore for now, locate the additional prefix here as the
            // "facet" id. Assume that id never ends in ":" and if there is
            // no colon, id is the same as the component id.
            //
            var componentid = props.id;
            var colon_index = componentid.lastIndexOf(':');
            if (colon_index !== -1) {
                componentid = props.id.substring(colon_index + 1);
            }
            var facetid = props.id + ":" + componentid;

            // Get the field by calling the field getInputMethod
            // because only it knows about the underlying structure
            // of the rendered field component
            //
            domNode.field =
                    field.getInputElement(facetid /* + "_field"*/);
            domNode.addButton = document.getElementById(facetid + "_addButton");
            domNode.removeButton = document.getElementById(facetid + "_removeButton");

            // attach methods
            domNode.add = this.add;
            domNode.enableAdd = this.enableAdd;
            domNode.enableRemove = this.enableRemove;
            domNode.setAddDisabled = this.setAddDisabled;
            domNode.setRemoveDisabled = this.setRemoveDisabled;
            domNode.updateButtons = this.updateButtons;
            domNode.setDisabled = this.setDisabled;
            common.setInitialized(domNode);
        },

        add: function (elementId) {
            this.enableAdd();
            this.addButton.click();
        },

        enableAdd: function () {
            var disabled = (this.field.value === "");
            this.setAddDisabled(disabled);
        },

        setAddDisabled: function (disabled) {
            if (this.addButton.setDisabled !== null) {
                this.addButton.setDisabled(disabled);
            } else {
                this.addButton.disabled = disabled;
            }
        },

        enableRemove: function () {
            var disabled = (this.list.selectedIndex === -1);
            this.setRemoveDisabled(disabled);
        },

        setRemoveDisabled: function (disabled) {
            if (this.removeButton.setDisabled !== null) {
                this.removeButton.setDisabled(disabled);
            } else {
                this.removeButton.disabled = disabled;
            }
        },

        updateButtons: function () {
            this.enableAdd();
            this.enableRemove();
        },

        setDisabled: function (disabled) {
            if (this.addButton.setDisabled !== null) {
                this.addButton.setDisabled(disabled);
            } else {
                this.addButton.disabled = disabled;
            }
            if (this.removeButton.setDisabled !== null) {
                this.removeButton.setDisabled(disabled);
            } else {
                this.removeButton.disabled = disabled;
            }
            this.field.disabled = disabled;
            this.list.disabled = disabled;
        }
    };
});
