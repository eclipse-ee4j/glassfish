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

    /**
     * The functions of this closure are used to manipulate cookies.
     */
    cookie = {
        badCookieChars: ["(", ")", "<", ">", "@", ",", ";", ":", "\\", "\"", "/", "[", "]", "?", "=", "{", "}", " ", "\t"],

        /**
         * Ensure we use a RFC 2109 compliant cookie name.
         */
        getValidCookieName: function (name) {
            for (var idx = 0; idx < this.badCookieChars.length; idx++) {
                name = name.replace(this.badCookieChars[idx], "_");
            }
            return name;
        },

        /**
         * This function will get the cookie value.
         */
        get: function () {
            // Get document cookie.
            var cookie = document.cookie;
            var cName = this.getValidCookieName(this.$cookieName);
            // Parse webui_ScrollCookie value.
            var pos = cookie.indexOf(cName + "=");
            if (pos === -1) {
                return null;
            }

            var start = pos + cName.length + 1;
            var end = cookie.indexOf(";", start);
            if (end === -1) {
                end = cookie.length;
            }

            // return cookie value
            return cookie.substring(start, end);
        },

        /**
         * This function will load the cookie value.
         */
        load: function () {
            // Get document cookie.
            var cookieVal = this.get();
            if (cookieVal === null) {
                return false;
            }

            // Break cookie into names and values.
            var a = cookieVal.split('&');

            // Break each pair into an array.
            for (var i = 0; i < a.length; i++) {
                a[i] = a[i].split(':');
            }

            // Set name and values for this object.
            for (i = 0; i < a.length; i++) {
                this[a[i][0]] = unescape(a[i][1]);
            }
            return true;
        },

        /**
         * This function will reset the cookie value.
         */
        reset: function () {
            // Clear cookie value.
            document.cookie = this.getValidCookieName(this.$cookieName) + "=";
            return true;
        },

        /**
         * This function will store the cookie value.
         */
        store: function () {
            // Create cookie value by looping through object properties
            var cookieVal = "";

            // Since cookies use the equals and semicolon signs as separators,
            // we'll use colons and ampersands for each variable we store.
            for (var prop in this) {
                // Ignore properties that begin with '$' and methods.
                if (prop.charAt(0) === '$' || typeof this[prop] === 'function') {
                    continue;
                }
                if (cookieVal !== "") {
                    cookieVal += '&';
                }
                cookieVal += prop + ':' + escape(this[prop]);
            }
            var cookieString = this.getValidCookieName(this.$cookieName) + "=" + cookieVal;
            if (this.$path !== null) {
                cookieString += ";path=" + this.$path;
            }
            // Store cookie value.
            document.cookie = cookieString;
            return true;
        }
    };

    /**
     * This function is used to construct a javascript object for
     * maintaining scroll position via cookie.
     */

    scrollCookie = function (viewId, path) {
        // All predefined properties of this object begin with '$' because
        // we don't want to store these values in the cookie.
        this.$cookieName = viewId;
        this.$path = path;

        // Default properties.
        this.left = "0";
        this.top = "0";

        // This function will load the cookie and restore scroll position.
        this.restore = function () {
            // Load cookie value.
            this.load();
            scrollTo(this.left, this.top);
            return true;
        };

        // This function will set the cookie value.
        this.set = function () {
            var documentElement = window.document.documentElement;
            if (documentElement && documentElement.scrollTop) {
                this.left = documentElement.scrollLeft;
                this.top = documentElement.scrollTop;
            } else {
                this.left = window.document.body.scrollLeft;
                this.top = window.document.body.scrollTop;
            }
            // if the left and top scroll values are still null
            // try to extract it assuming the browser is IE
            if (this.left === null && this.top === null) {
                this.left = window.pageXOffset;
                this.top = window.pageYOffset;
            }
            // Store cookie value.
            this.store();
            return true;
        };
    };

    // Inherit cookie properties.
    scrollCookie.prototype = this.cookie;

    return {
        cookie: cookie,
        scrollCookie: scrollCookie
    };

});
