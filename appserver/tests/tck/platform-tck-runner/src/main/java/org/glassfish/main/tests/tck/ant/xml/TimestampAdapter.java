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

package org.glassfish.main.tests.tck.ant.xml;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.regex.Pattern;

/**
 * @author David Matejcek
 */
public class TimestampAdapter extends XmlAdapter<String, LocalDateTime> {
    private static final Pattern P_LDT = Pattern.compile("[0-9T:\\-.]+");

    @Override
    public LocalDateTime unmarshal(final String value) {
        if (value == null) {
            return null;
        }
        if (value.length() < 11) {
            return LocalDate.parse(value).atStartOfDay();
        }
        if (P_LDT.matcher(value).matches()) {
            return LocalDateTime.parse(value);
        }
        return ZonedDateTime.parse(value).withZoneSameLocal(ZoneId.systemDefault()).toLocalDateTime();
    }


    @Override
    public String marshal(final LocalDateTime value) {
        return value == null ? null : value.toString();
    }
}
