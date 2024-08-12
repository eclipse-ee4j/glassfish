/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.internal.embedded.admin;

import java.util.ArrayList;
import java.util.List;

import org.glassfish.api.admin.ParameterMap;

/**
 * Command Parameters, needs to be refined...
 *
 * @author Jerome Dochez
 */
public class CommandParameters {

    private ParameterMap params = new ParameterMap();
    private List<String> operands = new ArrayList<String>();;

    /**
     * Sets the command primary (operand) parameter.
     * @param operand the command operand
     */
    public void setOperand(String operand) {
        operands.clear();
        operands.add(operand);
    }

    /**
     * Adds a command primary (operand) parameter.
     * @param operand the command operand
     */
    public void addOperand(String operand) {
        operands.add(operand);
    }

    /**
     * Get the first operand.
     */
    public String getOperand() {
        return operands.get(0);
    }

    /**
     * Get the operands.
     */
    public List<String> getOperands() {
        return new ArrayList<String>(operands);
    }

    /**
     * Sets a command option as the user would specify it using the
     * CLI command for instance
     *
     * @param optionName option name (without leading -- chars)
     * @param optionValue option value
     */
    public void setOption(String optionName, String optionValue) {
        params.set(optionName, optionValue);
    }

    /**
     * Adds a command option as the user would specify it using the
     * CLI command for instance
     *
     * @param optionName option name (without leading -- chars)
     * @param optionValue option value
     */
    public void addOption(String optionName, String optionValue) {
        params.add(optionName, optionValue);
    }

    public ParameterMap getOptions() {
        return new ParameterMap(params);
    }
}
