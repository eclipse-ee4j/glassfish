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
    return function () {
        // Convert all characters to lowercase to simplify testing.
        this.agent = navigator.userAgent.toLowerCase();

        // Note: On IE5, these return 4, so use is_ie5up to detect IE5.
        this.is_major = parseInt(navigator.appVersion);
        this.is_minor = parseFloat(navigator.appVersion);

        // Navigator version
        // Note = Opera and WebTV spoof Navigator.
        this.is_nav = this.agent.indexOf('mozilla') !== -1
                && this.agent.indexOf('spoofer') === -1
                && this.agent.indexOf('compatible') === -1;
        this.is_nav4 = this.is_nav && this.is_major === 4;
        this.is_nav4up = this.is_nav && this.is_major >= 4;
        this.is_navonly = this.is_nav && this.agent.indexOf(";nav") !== -1
                || this.agent.indexOf("; nav") !== -1;
        this.is_nav6 = this.is_nav && this.is_major === 5;
        this.is_nav6up = this.is_nav && this.is_major >= 5;
        this.is_gecko = this.agent.indexOf('gecko') !== -1;

        // IE version
        this.is_ie = this.agent.indexOf("msie") !== -1
                && this.agent.indexOf("opera") === -1;
        this.is_ie3 = this.is_ie && this.is_major < 4;
        this.is_ie4 = this.is_ie && this.is_major === 4
                && this.agent.indexOf("msie 4") !== -1;
        this.is_ie4up = this.is_ie && this.is_major >= 4;
        this.is_ie5 = this.is_ie && this.is_major === 4
                && this.agent.indexOf("msie 5.0") !== -1;
        this.is_ie5_5 = this.is_ie && this.is_major === 4
                && this.agent.indexOf("msie 5.5") !== -1;
        this.is_ie5up = this.is_ie && !this.is_ie3 && !this.is_ie4;
        this.is_ie5_5up = this.is_ie && !this.is_ie3 && !this.is_ie4
                && !this.is_ie5;
        this.is_ie6 = this.is_ie && this.is_major === 4
                && this.agent.indexOf("msie 6.") !== -1;
        this.is_ie6up = this.is_ie && !this.is_ie3 && !this.is_ie4
                && !this.is_ie5 && !this.is_ie5_5;
        this.is_ie7 = this.is_ie && this.is_major === 4
                && this.agent.indexOf("msie 7.") !== -1;
        this.is_ie7up = this.is_ie && !this.is_ie3 && !this.is_ie4
                && !this.is_ie5 && !this.is_ie5_5 && !this.is_ie6;

        // Platform
        this.is_linux = this.agent.indexOf("inux") !== -1;
        this.is_sun = this.agent.indexOf("sunos") !== -1;
        this.is_win = this.agent.indexOf("win") !== -1
                || this.agent.indexOf("16bit") !== -1;
    };
});
