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

    return {
        addOnInitCallback: common.addOnInitCallback,
        /**
         * This function is used to initialize HTML element properties with the
         * following Object literals.
         *
         * <ul>
         *  <li>id</li>
         *  <li>separator<li>
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

            // Note that _available, and _selected are not a facets
            // and therefore do not require the use of "facetid" as discussed below

            // The select element from which selections are made
            domNode.availableList = document.getElementById(props.id + "_available");

            // The options of the select element from which selections are made
            domNode.availableOptions = domNode.availableList.options;

            // The select element in which selections are shown
            domNode.selectedList = document.getElementById(props.id + "_selected");

            // The options of the select element in which selections are shown
            domNode.selectedOptions = domNode.selectedList.options;

            // Bug 6338492 -
            //     ALL: If a component supports facets or children is must be a
            //      NamingContainer
            // Since AddRemove has become a NamingContainer the id's for
            // the facet children are prefixed with the AddRemove id
            // in addition to their own id, which also has the
            // AddRemove id, as has been the convention for facets. This introduces
            // a redundancy in the facet id so the add button now looks like
            //
            // "formid:addremoveid:addremoveid_addButton"
            //
            // It used to be "formid:addremoveid_addButton"
            // It would be better to encapsulate that knowledge in the
            // AddRemove renderer as does FileChooser which has the
            // same problem but because the select elements are not
            // facets in AddRemove they really do only have id's of the
            // form "formid:addremoveid_list_value". Note that
            // in these examples the "id" parameter is "formid:addremoveid"
            //
            // Therefore for now, locate the additional prefix here as the
            // "facet" id. Assume that id never ends in ":" and if there is
            // no colon, id is the same as the component id.
            var componentid = props.id;
            var colon_index = componentid.lastIndexOf(':');
            if (colon_index !== -1) {
                componentid = props.id.substring(colon_index + 1);
            }
            var facetid = props.id + ":" + componentid;

            domNode.addButton = document.getElementById(facetid + "_addButton");
            domNode.addAllButton = document.getElementById(facetid + "_addAllButton");
            domNode.removeButton = document.getElementById(facetid + "_removeButton");
            domNode.removeAllButton = document.getElementById(facetid + "_removeAllButton");
            domNode.moveUpButton = document.getElementById(facetid + "_moveUpButton");
            domNode.moveDownButton = document.getElementById(facetid + "_moveDownButton");

            // _list_value and _item_list are not facets and do not need facetid
            domNode.selectedValues = document.getElementById(props.id + "_list_value");

            // Calculate the value indices
            var itemString = document.getElementById(props.id + "_item_list");
            if (itemString !== null) {
                var string = new String(itemString.value).valueOf();
                domNode.allValues = string.split(props.separator);
            } else {
                domNode.allValues = new Array();
            }

            // attach AddRemove object methods
            domNode.add = this.add;
            domNode.addAll = this.addAll;
            domNode.remove = this.remove;
            domNode.removeAll = this.removeAll;
            domNode.moveUp = this.moveUp;
            domNode.moveDown = this.moveDown;
            domNode.updateButtons = this.updateButtons;
            domNode.calculateIndex = this.calculateIndex;
            domNode.moveOption = this.moveOption;
            domNode.updateValue = this.updateValue;
            domNode.allowMultipleAdditions = this.allowMultipleAdditions;
            domNode.availableOnChange = this.availableOnChange;
            domNode.selectedOnChange = this.selectedOnChange;
            common.setInitialized(domNode);
        },

        add: function () {
            if (this.availableOptions.selectedIndex === -1) {
                return;
            }

            var sort = this.sort && (this.moveUpButton === null);

            // deselect everything in the selected list
            this.selectedList.selectedIndex = -1;
            return this.moveOption(this.availableOptions, this.selectedOptions,
                    this.selectedList, sort);
        },

        remove: function () {
            if (this.selectedOptions.selectedIndex === -1) {
                return;
            }

            // deselect everything in the selected list
            this.availableList.selectedIndex = -1;
            return this.moveOption(this.selectedOptions, this.availableOptions,
                    this.availableList, this.sort);
        },

        moveOption: function (moveFromOptions, moveToOptions, moveToList, sort) {
            var index = moveFromOptions.selectedIndex;
            if (index === -1) {
                return;
            }

            // keep moving selected items until there aren't any more valid ones
            while (index !== -1 && index < moveFromOptions.length - 1) {
                var lastOption = moveToOptions.length - 1;

                // This is the option we're moving
                var curSelection = moveFromOptions[index];

                // This is the index where we insert the option...
                var insertionIndex = 0;

                // ...and this is the option at that index
                var insertionOption;

                if (sort) {
                    // If there are no buttons to move the selected items up or
                    // down, then we preserve the sorting order of the available
                    // items. We calculate the index of the selected item (based
                    // on the indices assigned when parsing the allValues
                    // variable), and then we check each selected item until we
                    // reach an item with a higher index.
                    var itemIndex = this.calculateIndex(curSelection.value);
                    for (var counter = 0; counter < lastOption + 1; ++counter) {
                        insertionOption = moveToOptions[counter];
                        if (itemIndex < this.calculateIndex(insertionOption.value)) {
                            insertionIndex = counter;
                            break;
                        }
                    }
                } else {
                    // If there are buttons to move the options around, then we
                    // simply add the new items in the last position
                    insertionIndex = lastOption;
                    insertionOption = moveToOptions[lastOption];
                }

                // To insert the item, Mozilla works different from Windows
                // and Opera.
                if (moveFromOptions.remove === null) {
                    // Case 1: Mozilla
                    moveToList.add(curSelection, insertionOption);
                } else {
                    // Case 2: Windows and Opera
                    moveFromOptions.remove(index);
                    moveToOptions.add(curSelection, insertionIndex);
                }

                // Make sure the item is selected (this is needed for Opera)
                moveToOptions[insertionIndex].selected = true;

                // Update the options
                lastOption++;

                // Get the next selected index.
                index = moveFromOptions.selectedIndex;
            }
            this.updateValue();
            this.updateButtons();
            return false;
        },

        addAll: function () {
            var numOptions = this.availableOptions.length - 1;
            for (var index = 0; index < numOptions; ++index) {
                if (this.availableOptions[index].disabled === false) {
                    this.availableOptions[index].selected = true;
                }
            }
            return this.add();
        },

        removeAll: function () {
            var numOptions = this.selectedOptions.length - 1;
            for (var index = 0; index < numOptions; ++index) {
                if (this.selectedOptions[index].disabled === false) {
                    this.selectedOptions[index].selected = true;
                }
            }
            return this.remove();
        },

        // The original allowed items to be moved on both lists. Surely we
        // only sort items on the selected list?
        // This does not work on Mozilla
        moveUp: function () {
            // We will not move the last item - it's the separator
            var numOptions = this.selectedOptions.length - 1;

            // If there aren't at least two more selected items, then there is
            // nothing to move
            if (numOptions < 2) {
                return;
            }

            // Start by examining the first item
            var index = 0;

            // We're not going to move the first item. Instead, we will start
            // on the first selected item that is below an unselected
            // item. We identify the first unselected item on the list, and
            // then we will start on next item after that
            while (this.selectedOptions[index].selected) {
                ++index;
                if (index === numOptions) {
                    // We've reached the last item - no more items below it so
                    // we return
                    return;
                }
            }

            // Start on the item below this one
            ++index;

            for (index; index < numOptions; ++index) {
                if (this.selectedOptions[index].selected === true) {
                    var curOption = this.selectedOptions[index];
                    if (this.selectedOptions.remove === null) {
                        // For Mozilla
                        this.selectedOptions[index] = null;
                        this.selectedList.add(curOption,
                                this.selectedOptions[index - 1]);
                    } else {
                        // Windows and Opera do
                        this.selectedOptions.remove(index);
                        this.selectedOptions.add(curOption, index - 1);
                    }
                    // This is needed for Opera only
                    this.selectedOptions[index - 1].selected = true;
                }
            }
            this.updateValue();
            this.updateButtons();
            return false;
        },

        // The original allowed items to be moved on both lists. Surely we
        // only sort items on the selected list?
        // This does not work on Mozilla
        moveDown: function () {
            // Last option is numOption -1. That is the separator and we don't
            // move it. We start by examining the second to last item.
            var index = this.selectedOptions.length - 2;

            // If this number is less than zero, there was nothing on the list
            // and we return
            if (index < 0) {
                return;
            }

            // We're not going to move the last item. Instead, we will start
            // on the last selected item that is above an unselected
            // item. We identify the last unselected item before the separator
            // and then we start with the item above that one.
            while (this.selectedOptions[index].selected) {
                --index;
                if (index === 0) {
                    // We've reached the first item - no item above it so we
                    // return
                    return;
                }
            }

            // Start on the item above this one
            --index;

            for (index; index > -1; --index) {
                if (this.selectedOptions[index].selected === true) {
                    var curOption = this.selectedOptions[index];
                    if (this.selectedOptions.remove === null) {
                        // For Mozilla
                        this.selectedOptions[index] = null;
                        this.selectedList.add(curOption,
                                this.selectedOptions[index + 1]);
                    } else {
                        // Windows and Opera do
                        this.selectedOptions.remove(index);
                        this.selectedOptions.add(curOption, index + 1);
                    }
                    // This is needed for Opera only
                    this.selectedOptions[index + 1].selected = true;
                }
            }
            this.updateValue();
            this.updateButtons();
            return false;
        },

        updateButtons: function () {

            var numOptions = this.availableOptions.length - 1;
            var setting;

            //disabled items should not be moved and buttons should not be enabled
            //for selected disabled items (IE problem)
            for (var i = 0; i < numOptions; ++i) {
                if (this.availableOptions[i].disabled === true) {

                    this.availableOptions[i].selected = false;
                }
            }

            var index = this.availableOptions.selectedIndex;

            // The Add button is enabled if there is at least one option
            // to select from and at least one item is selected
            if (this.addButton !== null) {
                setting = numOptions < 1 || index === -1;
                if (this.addButton.setDisabled !== null) {
                    this.addButton.setDisabled(setting);
                } else {
                    this.addButton.disabled = setting;
                }
            }

            // The Add All button is enabled if there is at least one option
            // to select from, and disabled otherwise
            if (this.addAllButton !== null) {
                var counter = 0;
                // If available item list is disabled then AddAll button should be disabled
                // irrespective of options element in list.
                if (this.availableList.disabled === false) {
                    for (index = 0; index < numOptions; ++index) {
                        if (this.availableOptions[index].disabled === false) {
                            ++counter;
                        }
                    }
                }
                setting = (counter < 1);
                if (this.addAllButton.setDisabled !== null) {
                    this.addAllButton.setDisabled(setting);
                } else {
                    this.addAllButton.disabled = setting;
                }
            }

            // The remaining buttons are enabled/disabled based on the
            // items on the selected list
            index = this.selectedOptions.selectedIndex;
            numOptions = this.selectedOptions.length - 1;

            if (this.removeAllButton !== null) {
                var counter = 0;
                // If selected item list is disabled then RemoveAll button should be disabled
                // irrespective of options element in list.
                if (this.selectedList.disabled === false) {
                    for (index = 0; index < numOptions; ++index) {
                        if (this.selectedOptions[index].disabled === false) {
                            ++counter;
                        }
                    }
                }
                setting = (counter < 1);
                if (this.removeAllButton.setDisabled !== null) {
                    this.removeAllButton.setDisabled(setting);
                } else {
                    this.removeAllButton.disabled = setting;
                }
            }

            // If there are no selected items or if none of them are selected,
            // we disable Remove, Move Up, Move Down
            index = this.selectedOptions.selectedIndex;
            var noItems = numOptions < 1 || index === -1;
            if (this.removeButton !== null) {
                if (this.removeButton.setDisabled !== null) {
                    this.removeButton.setDisabled(noItems);
                } else {
                    this.removeButton.disabled = noItems;
                }
            }

            // The Move Up button is enabled (setting = false) provided that
            // there is at least one selected item that is below an unselected item
            if (this.moveUpButton !== null) {
                setting = true;
                if (noItems !== true) {
                    // Find the first un-selected option, then see if there is
                    // a selected option below that one
                    var found = false;
                    var unselected = -1;
                    for (index = 0; index < numOptions; ++index) {
                        if (unselected === -1) {
                            if (this.selectedOptions[index].selected === false) {
                                unselected = index;
                            }
                        } else {
                            if (this.selectedOptions[index].selected === true) {
                                setting = false;
                                break;
                            }
                        }
                    }
                }
                if (this.moveUpButton.setDisabled !== null) {
                    this.moveUpButton.setDisabled(setting);
                } else {
                    this.moveUpButton.disabled = setting;
                }
            }

            // The Move Down button is enabled (setting = false) provided that
            // there is at least one unselected item below a selected item.
            if (this.moveDownButton !== null) {
                setting = true;
                if (noItems !== true) {
                    for (index = this.selectedOptions.selectedIndex;
                            index < numOptions;
                            ++index) {
                        if (this.selectedOptions[index].selected === false) {
                            setting = false;
                        }
                    }
                }
                if (this.moveDownButton.setDisabled !== null) {
                    this.moveDownButton.setDisabled(setting);
                } else {
                    this.moveDownButton.disabled = setting;
                }
            }
            // set the focus to the list which has some selected item(s).
            // this needs to be done to shift the focus from disabled button (Mozilla)
            if (this.selectedOptions.selectedIndex > -1)
                this.selectedList.focus();
            else if (this.availableOptions.selectedIndex > -1)
                this.availableList.focus();
            return false;
        },

        calculateIndex: function (value, lastIndex) {
            var string = new String(value).valueOf()
            for (var counter = 0; counter < this.allValues.length; counter++) {
                if (string === this.allValues[counter]) {
                    return counter;
                }
            }
            // Something went wrong. Return the index before the separator
            return this.allValues.length - 2;
        },

        updateValue: function () {
            // Remove the options from the select that holds the actual
            // selected values
            while (this.selectedValues.length > 0) {
                this.selectedValues.remove(0);
            }

            // Create a new array consisting of the options marked as selected
            // on the official list
            var newOptions = new Array();
            var cntr = 0;
            var newOption;

            while (cntr < this.selectedOptions.length - 1) {
                newOption = document.createElement("option");
                if (this.selectedOptions[cntr].text !== null) {
                    newOption.text = this.selectedOptions[cntr].text;
                }
                if (this.selectedOptions[cntr].value !== null) {
                    newOption.value = this.selectedOptions[cntr].value;
                }
                newOption.selected = true;
                newOptions[newOptions.length] = newOption;
                ++cntr;
            }

            //need to test IE browser, refer go GLASSFISH-16903
            cntr = 0;
            var ua = navigator.userAgent;
            ;
            var ie = false;
            if (ua.indexOf("MSIE") > -1) {
                ie = true;
            }
            while (cntr < newOptions.length) {
                if (ie) {
                    this.selectedValues.add(newOptions[cntr], cntr);
                } else {
                    this.selectedValues.add(newOptions[cntr], null);
                }
                //this.selectedValues.children[this.selectedValues.children.length-1] = newOptions[cntr];
                ++cntr;
            }
            /*
             if(this.selectedOptions.remove == null) {
             // For Mozilla
             while(cntr < newOptions.length) {
             this.selectedValues.add(newOptions[cntr], null);
             ++cntr;
             }
             } else {
             // Windows and Opera do
             while(cntr < newOptions.length) {
             this.selectedValues.add(newOptions[cntr], cntr);
             ++cntr;
             }
             }
             */
            return true;
        },

        allowMultipleAdditions: function () {
            // Replace the add and remove functions with functions which
            // leave the available items as they are
            this.add = this.multipleAdd;
            this.remove = this.multipleRemove;
        },

        multipleAdd: function () {
            this.selectedList.selectedIndex = -1;
            var index = this.availableOptions.selectedIndex;
            if (index === -1) {
                return;
            }

            // keep moving selected items until there aren't any more valid ones
            while (index !== -1 && index < this.availableOptions.length - 1) {
                var lastOption = this.selectedOptions.length - 1;

                // This is the option we're moving
                var curSelection = this.availableOptions[index];
                curSelection.selected = false;
                var addSelection = new Option();
                addSelection.text = curSelection.text;
                addSelection.value = curSelection.value;

                // This is the index where we insert the option...
                var insertionIndex = 0;
                // ...and this is the option at that index
                var insertionOption;

                // If there are no buttons to move the selected items up or
                // down, then we preserve the sorting order of the available
                // items. We calculate the index of the selected item (based
                // on the indices assigned when parsing the allValues
                // variable), and then we check each selected item until we
                // reach an item with a higher index.

                // We sort if there are no move buttons
                var sort = (this.moveUpButton === null);

                if (sort) {
                    var itemIndex = this.calculateIndex(curSelection.value);
                    for (var counter = 0; counter < lastOption + 1; ++counter) {
                        insertionOption = this.selectedOptions[counter];
                        if (itemIndex < this.calculateIndex(insertionOption.value)) {
                            insertionIndex = counter;
                            break;
                        }
                    }
                } else {
                    // If there are buttons to move the options around, then we
                    // simply add the new items in the last position
                    insertionIndex = lastOption;
                    insertionOption = this.selectedOptions[lastOption];
                }

                // To insert the item, Mozilla works different from Windows
                // and Opera.
                if (this.selectedOptions.remove === null) {
                    // Case 1: Mozilla
                    this.selectedList.add(addSelection, insertionOption);
                } else {
                    // Case 2: Windows and Opera
                    this.selectedOptions.add(addSelection, insertionIndex);
                }

                // Make sure the item is selected (this is needed for Opera)
                this.selectedOptions[insertionIndex].selected = true;

                // Update the options
                lastOption++;

                // Get the next selected index.
                index = this.availableOptions.selectedIndex;
            }
            this.updateValue();
            this.updateButtons();
            return false;
        },

        multipleRemove: function () {
            this.availableList.selectedIndex = -1;
            var index = this.selectedOptions.selectedIndex;
            if (index === -1) {
                return;
            }

            while (index < this.selectedOptions.length - 1) {
                if (this.selectedOptions[index].selected) {
                    if (this.selectedOptions.remove === null) {
                        // Case 1: Mozilla
                        this.selectedOptions[index] = null;
                    } else {
                        // Case 2: Windows and Opera
                        this.selectedOptions.remove(index);
                    }
                } else {
                    index++;
                }
            }
            this.updateValue();
            this.updateButtons();
            return false;
        },

        availableOnChange: function () {
            this.selectedList.selectedIndex = -1;
            this.updateButtons();
            return false;
        },

        selectedOnChange: function () {
            this.availableList.selectedIndex = -1;
            this.updateButtons();
            return false;
        }
    };
});
