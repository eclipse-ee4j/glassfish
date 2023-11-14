/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

function showHide(id) {
    var element = document.getElementById(id);
    if (element.style.display == 'block') {
        element.style.display = 'none';
    } else {
        element.style.display = 'block';
    }
}
var statii = new Array();
statii[0] = "pass";
statii[1] = "fail";
statii[2] = "didnotrun";
function toggleResults() {
    var allHTMLTags = document.getElementsByTagName('tr');
    for (var i in allHTMLTags) {
        var show = false;
        var styled = false;
        for (var index in statii) {
            var theClass = statii[index]
            var input = document.getElementById('summary').getElementsByTagName('input')[index];
            if (allHTMLTags[i].className.indexOf(theClass) != -1) {
                show = show || input.checked
                styled = true
            }
        }
        if (styled) {
            if (show) {
                allHTMLTags[i].style.display = 'table-row';
            } else {
                allHTMLTags[i].style.display = 'none';
            }
        }
    }
}
