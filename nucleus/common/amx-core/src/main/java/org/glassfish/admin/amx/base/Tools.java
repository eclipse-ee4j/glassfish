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

package org.glassfish.admin.amx.base;

import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;

import org.glassfish.admin.amx.annotation.Description;
import org.glassfish.admin.amx.annotation.ManagedAttribute;
import org.glassfish.admin.amx.annotation.ManagedOperation;
import org.glassfish.admin.amx.annotation.Param;
import org.glassfish.admin.amx.core.AMXMBeanMetadata;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;

/**
Useful informational tools.

@since GlassFish V3
 */
@Taxonomy(stability = Stability.UNCOMMITTED)
@AMXMBeanMetadata(singleton = true, globalSingleton = true, leaf = true)
public interface Tools extends AMXProxy, Utility, Singleton
{
    /** emit information about all MBeans */
    @ManagedAttribute
    @Description("emit information about all MBeans")
    String getInfo();

    /** emit information about all MBeans of the specified type, or path */
    @Description("emit information about all MBeans of the specified type, or path")
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    String infoType(
            @Param(name = "type")
            String type);

    /** emit information about all MBeans of the specified type, or path */
    @Description("emit information about all MBeans of the specified type, or path")
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    String infoPath(
            @Param(name = "path")
            String path);

    /** emit information about all MBeans having the specified parent path (PP), recursively or not */
    @Description("emit information about all MBeans having the specified parent path (PP), recursively or not")
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    String infoPP(
            @Description("path of the *parent* MBean")
            @Param(name = "parentPath")
            String parentPath,
            @Param(name = "recursive")
            boolean recursive);

    /** emit information about MBeans, loosey-goosey seach string eg type alone, pattern, etc */
    @Description("emit information about MBeans, loosey-goosey seach string eg type alone")
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    String info(
            @Description("loosey-goosey seach string")
            @Param(name = "searchString")
            String searchString);

    /** Get a compilable java interface for the specified MBean */
    @Description("Get a compilable java interface for the specified MBean")
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    String java(
            @Description("Get a compilable java interface for the specified MBean")
            @Param(name = "objectName")
            ObjectName objectName);

    /** Validate all AMX MBeans. */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("Validate all AMX MBeans and return status")
    String validate();

    /** Validate a single MBean or MBeans specified by a pattern. */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("Validate a single MBean or MBeans specified by a pattern")
    String validate(
            @Param(name = "objectNameOrPattern")
            ObjectName objectNameOrPattern);

    /** Validate MBeans: specific ObjectNames and/or patterns. */
    @Description("Validate MBeans: specific ObjectNames and/or patterns")
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    String validate(
            @Param(name = "mbeans")
            ObjectName[] mbeans);

    @Description("Dump the hierarchy of AMX MBeans by recursive descent")
    @ManagedAttribute
    String getHierarchy();

}
















