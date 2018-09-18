/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

function ajaxSubmit(elem) {
    var form = getForm(elem);
    var method = form.method.toLowerCase();
    var url = form.action;

    var xmlhttp = (window.XMLHttpRequest) ?
        new XMLHttpRequest() : // code for IE7+, Firefox, Chrome, Opera, Safari
        new ActiveXObject("Microsoft.XMLHTTP"); // code for IE6, IE5

    xmlhttp.open(method, url, false);
    xmlhttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
    xmlhttp.setRequestHeader("X-Requested-By", "GlassFish REST HTML interface");
    if (method == "get") {
        xmlhttp.send();
    } else {
        xmlhttp.send(gatherFormParameters(form));
    }
    // Get the response inside the body tags and replace the document body
    var start = xmlhttp.responseText.indexOf("<body");
    var end = xmlhttp.responseText.indexOf("</body");
    document.body.innerHTML = xmlhttp.responseText.substring(start+6, end);
}

function getForm(elem) {
    while (elem.tagName.toLowerCase() != 'form') {
        elem = elem.parentNode;
    }

    return elem;
}

function gatherFormParameters(form) {
    var result = "";
    var sep = "";
    var elements = form.elements;
    var length = elements.length;
    for (var i = 0; i < length; i++) {
        var element = elements[i];
        var name = element.name;
        var type = element.type.toLowerCase();
        var value = "";
        if (type == 'select') {
            value = element.options[element.selectedIndex];
        } else {
            value = element.value;
        }

        result += sep + encodeURIComponent(name) + "=" + encodeURIComponent(value);
        sep="&";
    }

    return result;
}
