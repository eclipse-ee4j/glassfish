/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.stringsubs.impl;

import com.sun.enterprise.admin.servermgmt.xml.stringsubs.ChangePairRef;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.Group;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.ModeType;

import java.io.File;
import java.lang.System.Logger;

import static java.lang.System.Logger.Level.WARNING;

/**
 * This class provides method to process {@link ModeType}. The ModeType is predefined set of values use to alter the
 * substitution containing forward or backward slash in the after value. This attribute can be applied to {@link Group}
 * or {@link ChangePairRef}.
 *
 * @see ModeType
 * @see Group
 */
public class ModeProcessor {

    private static final Logger LOG = System.getLogger(ModeProcessor.class.getName());

    /**
     * Process the {@link ModeType} for a given string.
     * <li>{@link ModeType#FORWARD} : Replaces all backward slashes to forward slash.</li>
     * <li>{@link ModeType#DOUBLE} : Append a slash to all backward slash and also add a backward slash before each colon.
     * </li>
     * <li>{@link ModeType#POLICY} : Replaces {@link File#separator} by ${/} for java policy files.</li>
     *
     * @param modeType The mode type to be applied on the given input string.
     * @param input Input string for mode processing.
     * @return Processed string
     */
    static String processModeType(ModeType modeType, String input) {
        if (modeType == null || input == null || input.isEmpty()) {
            return input;
        }
        switch (modeType) {
        case FORWARD:
            // Change all backward slashes to forward slash.
            input = input.replace("\\", "/");
            break;
        case DOUBLE:
            // Add a slash to all back slashes.
            input = input.replace("\\", "\\\\");
            // Add a backslash before each colon.
            input = input.replace(":", "\\:");
            break;
        case POLICY:
            // Replace File.separator by ${/} for java.policy files
            input = input.replace(File.separator, "${/}");
            break;
        default:
            LOG.log(WARNING, "No processing defined for {0} mode", modeType);
            break;
        }
        return input;
    }
}
