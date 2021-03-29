/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.provider;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Meta-data store for resource method. Holds meta-data for message and query paramters of the method.
 *
 * @author Rajeshwar Patil
 */
public class MethodMetaData {

    public MethodMetaData() {
        __parameterMetaData = new TreeMap<String, ParameterMetaData>();
    }

    public ParameterMetaData getParameterMetaData(String parameter) {
        return __parameterMetaData.get(parameter);
    }

    public ParameterMetaData putParameterMetaData(String parameter, ParameterMetaData parameterMetaData) {
        return __parameterMetaData.put(parameter, parameterMetaData);
    }

    public ParameterMetaData removeParamMetaData(String param) {
        return __parameterMetaData.remove(param);
    }

    public int sizeParameterMetaData() {
        return __parameterMetaData.size();
    }

    public Set<String> parameters() {
        return __parameterMetaData.keySet();
    }

    public boolean isFileUploadOperation() {
        return __isFileUploadOperation;
    }

    public void setIsFileUploadOperation(boolean isFileUploadOperation) {
        __isFileUploadOperation = isFileUploadOperation;
    }

    Map<String, ParameterMetaData> __parameterMetaData;
    boolean __isFileUploadOperation = false;
}
