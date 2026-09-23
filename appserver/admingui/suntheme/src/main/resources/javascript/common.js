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
    "webui/suntheme/browser",
    "webui/suntheme/props"
], function (browser, props) {

    splitStyleClasses = function (element) {
        if (element !== null && element.className !== null) {
            return element.className.split(" ");
        } else {
            return null;
        }
    };

    escapeString = function (s, delimiter, escapeChar) {
        if (s === null) {
            return null;
        }
        if (delimiter === null) {
            return s;
        }
        if (escapeChar === null) {
            return null;
        }

        // Escape occurrences of delimiter with 
        // escapeChar and the delimiter.
        //
        // First escape the escape char.
        //
        var escape_escapeChar = escapeChar;
        if (escapeChar === "\\") {
            escape_escapeChar = escapeChar + escapeChar;
        }

        var rx = new RegExp(escape_escapeChar, "g");
        var s1 = s.replace(rx, escapeChar + escapeChar);

        rx = new RegExp(delimiter, "g");
        return s1.replace(rx, escapeChar + delimiter);
    };

    unescapeString = function (s, delimiter, escapeChar) {
        if (s === null) {
            return null;
        }
        if (delimiter === null) {
            return s;
        }
        if (escapeChar === null) {
            return null;
        }

        // UnEscape occurrences of delimiter with 
        // single instance of the delimiter
        //
        var escape_escapeChar = escapeChar;
        if (escapeChar === "\\") {
            escape_escapeChar = escapeChar + escapeChar;
        }

        // First unescape the escape char.
        //
        var rx = new RegExp(escape_escapeChar + escape_escapeChar, "g");
        var s1 = s.replace(rx, escapeChar);

        // Now replace escaped delimters
        //
        rx = new RegExp(escape_escapeChar + delimiter, "g");
        return s1.replace(rx, delimiter);
    };

    unescapeStrings = function (escapedString, delimiter, escapeChar) {
        if (escapedString === null || escapedString === "") {
            return null;
        }
        if (delimiter === null || delimiter === "") {
            return escapedString;
        }
        if (escapeChar === null || escapeChar === "") {
            return null;
        }

        // Need to do this character by character.
        var selections = new Array();
        var index = 0;
        var escseen = 0;
        var j = 0;

        for (var i = 0; i < escapedString.length; ++i) {
            if (escapedString.charAt(i) === delimiter) {
                if (escseen % 2 === 0) {
                    selections[index++] = escapedString.slice(j, i);
                    j = i + 1;
                }
            }
            if (escapedString.charAt(i) === escapeChar) {
                ++escseen;
                continue;
            } else {
                escseen = 0;
            }
        }

        // Capture the last split.
        selections[index] = escapedString.slice(j);

        // Now unescape each selection
        var unescapedArray = new Array();

        // Now replace escaped delimiters
        // i.e.  "\," with ","
        for (i = 0; i < selections.length; ++i) {
            unescapedArray[i] = unescapeString(
                    selections[i], delimiter, escapeChar);
        }
        return unescapedArray;
    };

    insertHiddenField = function (elementId, elementValue, parentForm) {
        // We have to assume that there is only one element
        // with elementId. document.getElementById, returns
        // the first one it finds, which appears to be the 
        // first one created dynamically, if more than one 
        // element is created dynamically with the same id.
        //
        // appendChild just appends even if there is an element
        // with the same id that exists.
        //
        // The assumption is that there should only be 
        // one element in the document with such an id.
        //
        // If the elementId exists just modifiy its value
        // instead of creating and appending.
        //
        var element = document.getElementById(elementId);
        if (element !== null) {
            element.value = elementValue;
            return;
        }

        var newElement = document.createElement('input');
        newElement.type = 'hidden';
        newElement.id = elementId;
        newElement.value = elementValue;
        newElement.name = elementId;
        parentForm.appendChild(newElement);
    };

    addStyleClass = function (element, styleClass) {
        // routine protection in javascript
        if (element === null || styleClass === null) {
            return false;
        }

        // handle easy case first
        if (element.className === null) {
            element.className = styleClass;
            return true;
        }

        // break out style classes into an array  
        var classes = splitStyleClasses(element);
        if (classes === null) {
            return false;
        }

        // For each styleClass, check if it's hidden and remove otherwise write 
        // it back out to the class
        for (var i = 0; i < classes.length; i++) {
            if (classes[i] !== null && classes[i] === styleClass) {
                return true;
            }
        }
        element.className = element.className + " " + styleClass;
    };

    stripStyleClass = function (element, styleClass) {
        // routine protection in javascript
        if (element === null || styleClass === null || element.className === null) {
            return false;
        }

        // break out style classes into an array  
        var classes = splitStyleClasses(element);
        if (classes === null) {
            return false;
        }

        // For each styleClass, check if it's hidden and remove otherwise write
        // it back out to the class
        for (var i = 0; i < classes.length; i++) {
            if (classes[i] !== null && classes[i] === styleClass) {
                classes.splice(i, 1);
            }
        }
        element.className = classes.join(" ");
    };

    checkStyleClasses = function (styleArray, styleClass) {
        if (styleArray === null || styleClass === null) {
            return false;
        }
        for (var i = 0; i < styleArray.length; i++) {
            if (styleArray[i] !== null && styleArray[i] === styleClass) {
                return true;
            }
        }
        return false;
    };

    createSubmittableArray = function (name, parentForm, labels, values) {
        deleteSubmittableArray(name, parentForm);

        if (values === null || values.length <= 0) {
            return;
        }

        var selections = document.createElement('select');
        selections.className = props.hiddenClassName;
        selections.name = name;
        selections.id = name;
        selections.multiple = true;

        // Start from the end of the array because
        // add puts things in at the head.
        //
        for (var i = 0; i < values.length; ++i) {
            var opt = document.createElement('option');
            opt.value = values[i];
            if (labels !== null) {
                opt.label = labels[i];
            }
            opt.defaultSelected = true;
            selections.add(opt, null);
        }
        parentForm.appendChild(selections);
        return selections;
    };

    leaveSubmitterTrace = function (form, submissionComponentId) {
        // This function only needs to be called in the onclick handler of 
        // an ActionSource component that appears within a -standard- table.
        // Under those circumstances, if this function is not called, then when
        // the component is clicked, the virtual form implementation will have 
        // no way of knowing that a virtual form was submitted.
        if (form !== null && submissionComponentId !== null
                && submissionComponentId.length > 0) {
            insertHiddenField('_submissionComponentId',
                    submissionComponentId, form);
        }
    };

    deleteSubmittableArray = function (name, parentForm) {
        try {
            var submittableArray = document.getElementById(name);
            if (submittableArray !== null) {
                parentForm.removeChild(submittableArray);
            }
        } catch (e) {
        }
    };

    isVisibleElement = function (element) {
        if (element === null) {
            return false;
        }
        // Test for the hidden style class.
        var styleClasses = splitStyleClasses(element);
        return !checkStyleClasses(styleClasses,
                props.hiddenClassName);
    };

    isVisible = function (elementId) {
        if (elementId === null) {
            return false;
        }
        // Get element.
        var element = document.getElementById(elementId);
        return isVisibleElement(element);
    };

    setVisible = function (elementId, visible) {
        if (elementId === null || visible === null) {
            return false;
        }
        // Get element.
        var element = document.getElementById(elementId);
        setVisibleElement(element, visible);
    };

    setVisibleElement = function (element, visible) {
        if (element === null || visible === null) {
            return false;
        }
        if (visible) {
            stripStyleClass(element, props.hiddenClassName);
        } else {
            addStyleClass(element, props.hiddenClassName);
        }
    };

    isFunction = function (obj) {
        return !!(obj && obj.constructor && obj.call && obj.apply);
    };

    fireInitCallBacks = function (obj) {
        if(obj=== null || obj === undefined){
            return false;
        }
        if (isInitialized(obj)) {
            // fire onInit callbacks
            if(obj.__onInitCallbacks !== undefined){
                var onInitCallback = obj.__onInitCallbacks.pop();
                while(onInitCallback !== undefined){
                    if (isFunction(onInitCallback)) {
                        onInitCallback(obj);
                    }
                    onInitCallback = obj.__onInitCallbacks.pop();
                }
            }
            return true;
        }
        return false;
    };

    isInitialized = function (elt) {
        if(elt=== null || elt === undefined){
            return false;
        }
        if (elt.__initialized === true) {
            return true;
        }
        return false;
    };

    setInitialized = function (elt) {
        if(elt === null || elt === undefined){
            return;
        }
        elt.__initialized = true;
    };

    addOnInitCallback = function (elt, cb) {
        // check argument types
        if (elt === null || elt === undefined || !isFunction(cb)) {
            return;
        }

        // add init callback if elt is not initialized
        if (!isInitialized(elt)) {
            if (elt.__onInitCallbacks === undefined) {
                elt.__onInitCallbacks = [];
            }
            elt.__onInitCallbacks.push(cb);
        } else {
            // if elt is initialized just invoke the callback
            cb(elt);
        }
    };

    return {
        // Variables needed when submitting form so timeout will work properly.
        formToSubmit: null,
        submissionComponentId: null,

        // Browser properties.
        browser: browser,

        // Place holder for body properties instantiated by BodyRenderer.
        body: null,

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // String functions
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        /**
         * Replace occurences of delimiter with the escapeChar and the
         * delimiter.
         * For example replace "," with "/," if delimiter == "," and
         * escapeChar is "/".
         */
        escapeString: escapeString,

        /**
         * Replace occurences of a sequence of 2 instances of delimiter 
         * with 1 instance of the delimiter.
         * For example replace ",," with "," if delimiter == ","
         */
        unescapeString: unescapeString,

        /**
         * Return an array of unescaped strings from escapedString
         * where the escaped character is delimiter.
         * If delimiter is "," escapedString might have the form
         *
         * XX\,XX,MM\,MM
         *
         * where "\" is the escape char.
         * 
         * and is returned as an array
         * array[0] == "XX,XX"
         * array[1] == "MM,MM"
         *
         */
        unescapeStrings: unescapeStrings,

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Style functions
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        /**
         * Use this function add any styleClass to an html tag
         *
         * @param element the dom html tag element
         * @param styleClass the name of the class to add
         * @return true if successful; otherwise, false
         */
        addStyleClass: addStyleClass,

        /**
         * Use this function to check if an array has a style class
         *
         * @param styleArray of style classes to check
         * @param styleClass the styleClass to check
         * @return array of classes
         */
        checkStyleClasses: checkStyleClasses,

        /**
         * Use this function to get array of style classes
         *
         * @param element the dom html tag element
         * @return array of classes
         */
        splitStyleClasses: splitStyleClasses,

        /**
         * Use this function remove any styleClass for an html tag
         *
         * @param element the dom html tag element
         * @param styleClass the name of the class to remove
         * @return true if successful; otherwise, false
         */
        stripStyleClass: stripStyleClass,

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Submit functions
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        /**
         * Use this function to insert a hidden field element in the page.
         *
         * @param elementId The element ID of the html tag 
         * @param elementValue The value of the html tag.
         * @param parentForm The parent form of the html tag.
         */
        insertHiddenField: insertHiddenField,

        /**
         * Use this function to submit a virtual form.
         */
        submitForm: function () {
            // "formToSubmit" is a literal (not virtual) form.
            // "submissionComponentId" is a component id (not client id).
            // the virtual form implementation uses _submissionComponentId
            // to determine which virtual form (if any) was submitted.
            if (this.formToSubmit === null) {
                return false;
            }
            if (this.submissionComponentId !== null
                    && this.submissionComponentId.length > 0) {
                insertHiddenField('_submissionComponentId',
                        this.submissionComponentId,
                        this.formToSubmit);
            }
            this.formToSubmit.submit();
            return false;
        },

        /**
         * Helper function to submit a virtual form.
         *
         * @param form The HTML form element to submit.
         * @param submissionComponentId The Id of the component submitting the form.
         */
        timeoutSubmitForm: function (form, submissionComponentId) {
            this.formToSubmit = form;
            this.submissionComponentId = submissionComponentId;
            common = this;
            setTimeout(function(){
                common.submitForm();
            }, 0);
        },

        /**
         * Helper function to submit a virtual form.
         *
         * @param form The HTML form element to submit.
         * @param submissionComponentId The Id of the component submitting the form.
         */
        leaveSubmitterTrace: leaveSubmitterTrace,

        /**
         * delete a previously created element by createSubmittableArray.
         */
        deleteSubmittableArray: deleteSubmittableArray,

        /**
         * webui.suntheme.common.createSubmittableArray(string, string, 
         * array, array);
         *
         * This method creates a hidden "select" element with id 
         * and name attributes set name, values taken from the values
         * array argument, and display labels from the labels array.
         * It adds the element to the parentForm argument.
         * 
         * The pupose of this method is to create an array of values
         * that can be decoded using "name" as the key from a FacesContext
         * external context's "getRequestParameterValuesMap" as an
         * array of Strings. This reduces the need of rendering hidden input
         * field and delimiting several strings so that a multiple selection
         * can be returned.
         * The labels array provides an additional piece of data
         * for use on the client, but it is not contained in the submit.
         * All values added to the select are selected so that the
         * values will be submitted.
         *
         * Returns the created select element.
         *
         * It relies on the webui.suntheme.props.hiddenClassName style class.
         * An attempt is made to remove a possibly previously created element
         * by this name. It always deletes an element of name from parentForm.
         */
        createSubmittableArray: createSubmittableArray,

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Visible functions
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        /**
         * Use this function to test if the specified element is visible (i.e., className
         * does not contain webui.suntheme.props.hiddenClassName).
         *
         * @param elementId The element ID of the html tag 
         * @return true if visible; otherwise, false
         */
        isVisible: isVisible,

        /**
         * Use this function to test if the given element is visible (i.e., className
         * does not contain webui.suntheme.props.hiddenClassName).
         *
         * @param element The HTML element
         * @return true if visible; otherwise, false
         */
        isVisibleElement: isVisibleElement,

        /**
         * Use this function to show or hide any html element in the page
         *
         * @param elementId The element ID of the html tag 
         * @param visible true to make the element visible, false to hide the element
         * @return true if successful; otherwise, false
         */
        setVisible: setVisible,

        /**
         * Use this function to show or hide any html element in the page
         *
         * @param element The HTML element
         * @param visible true to make the element visible, false to hide the element
         * @return true if successful; otherwise, false
         */
        setVisibleElement: setVisibleElement,

        isFunction: isFunction,
        fireInitCallBacks: fireInitCallBacks,
        isInitialized: isInitialized,
        setInitialized: setInitialized,
        addOnInitCallback: addOnInitCallback
    };
});
