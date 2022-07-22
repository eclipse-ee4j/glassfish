/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.web.accesslog;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author David Matejcek
 */
public class AccessLogPattern {

    private final DateTimeFormatter dateTimeFormatter;
    private final boolean timeTakenRequired;
    private final List<String> items;

    AccessLogPattern(DateTimeFormatter dateTimeFormatter, boolean timeTakenRequired, List<String> items) {
        this.dateTimeFormatter = dateTimeFormatter;
        this.timeTakenRequired = timeTakenRequired;
        this.items = items;
    }


    public DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatter;
    }


    /**
     * Has the time-taken token been specified in the access log pattern?
     *
     * @return true if the time-taken token has been specified in the access
     *         log pattern, false otherwise.
     */
    public boolean isTimeTakenRequired() {
        return timeTakenRequired;
    }


    public List<String> getItems() {
        return this.items;
    }
}
