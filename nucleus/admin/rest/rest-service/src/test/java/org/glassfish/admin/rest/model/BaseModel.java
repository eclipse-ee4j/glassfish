/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

import org.glassfish.admin.rest.composite.RestModel;
import org.glassfish.admin.rest.composite.metadata.AttributeReference;
import org.glassfish.admin.rest.composite.metadata.CreateOnly;

public interface BaseModel extends RestModel {
    @NotNull
    String getName();
    void setName(String name);

    int getCount();
    void setCount(int count);

    @Min(10)
    @Max(15)
    int getSize();
    void setSize(int size);

    List<RelatedModel> getRelated();
    void setRelated(List<RelatedModel> related);

    @AttributeReference(bean="com.sun.enterprise.config.serverbeans.Cluster", attribute="ConfigRef")
    String getConfigRef();
    void setConfigRef(String value);

    String[] getStringArray();
    void setStringArray(String[] array);

    @CreateOnly
    String getCreateOnly();
    void setCreateOnly(String value);
}
