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

package org.glassfish.admin.monitor.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.StringStatistic;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.external.statistics.impl.StringStatisticImpl;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

/* jvm.runtime */
// v2 mbean: com.sun.appserv:name=runtime,type=runtime,category=monitor,server=server
// v3 mbean:
@AMXMetadata(type = "runtime-mon", group = "monitoring")
@ManagedObject
@Description("JVM Runtime Statistics")
public class JVMRuntimeStatsProvider {

    private RuntimeMXBean rtBean = ManagementFactory.getRuntimeMXBean();

    private StringStatisticImpl bootClassPath = new StringStatisticImpl("BootClassPath", "String",
            "Boot class path that is used by the bootstrap class loader to search for class files");
    private StringStatisticImpl classPath = new StringStatisticImpl("ClassPath", "String",
            "Java class path that is used by the system class loader to search for class files");
    private StringStatisticImpl inputArguments = new StringStatisticImpl("InputArguments", "String",
            "Input arguments passed to the Java virtual machine which does not include the arguments to the main method");
    private StringStatisticImpl libraryPath = new StringStatisticImpl("LibraryPath", "String", "Java library path");
    private StringStatisticImpl mgmtSpecVersion = new StringStatisticImpl("ManagementSpecVersion", "String",
            "Version of the specification for the management interface implemented by the running Java virtual machine");
    private StringStatisticImpl runtimeName = new StringStatisticImpl("Name", "String",
            "Name representing the running Java virtual machine");
    private StringStatisticImpl specName = new StringStatisticImpl("SpecName", "String", "Java virtual machine specification name");
    private StringStatisticImpl specVendor = new StringStatisticImpl("SpecVendor", "String", "Java virtual machine specification vendor");
    private StringStatisticImpl specVersion = new StringStatisticImpl("SpecVersion", "String",
            "Java virtual machine specification version");
    private CountStatisticImpl uptime = new CountStatisticImpl("Uptime", CountStatisticImpl.UNIT_MILLISECOND,
            "Uptime of the Java virtual machine in milliseconds");
    private StringStatisticImpl vmName = new StringStatisticImpl("VmName", "String", "Java virtual machine implementation name");
    private StringStatisticImpl vmVendor = new StringStatisticImpl("VmVendor", "String", "Java virtual machine implementation vendor");
    private StringStatisticImpl vmVersion = new StringStatisticImpl("VmVersion", "String", "Java virtual machine implementation version");

    @ManagedAttribute(id = "bootclasspath-current")
    @Description("boot class path that is used by the bootstrap class loader to search for class files")
    public StringStatistic getBootClassPath() {
        bootClassPath.setCurrent(rtBean.getBootClassPath());
        return bootClassPath;
    }

    @ManagedAttribute(id = "classpath-current")
    @Description("Java class path that is used by the system class loader to search for class files")
    public StringStatistic getClassPath() {
        classPath.setCurrent(rtBean.getClassPath());
        return classPath;
    }

    @ManagedAttribute(id = "inputarguments-current")
    @Description("input arguments passed to the Java virtual machine which does not include the arguments to the main method")
    public StringStatistic getInputArguments() {
        List<String> inputList = rtBean.getInputArguments();
        StringBuffer sb = new StringBuffer();
        for (String arg : inputList) {
            sb.append(arg);
            sb.append(", ");
        }
        String finalString = sb.substring(0, sb.lastIndexOf(","));
        inputArguments.setCurrent(finalString);
        return inputArguments;
    }

    @ManagedAttribute(id = "librarypath-current")
    @Description("Java library path")
    public StringStatistic getLibraryPath() {
        libraryPath.setCurrent(rtBean.getLibraryPath());
        return libraryPath;
    }

    @ManagedAttribute(id = "managementspecversion-current")
    @Description("version of the specification for the management interface implemented by the running Java virtual machine")
    public StringStatistic getManagementSpecVersion() {
        mgmtSpecVersion.setCurrent(rtBean.getManagementSpecVersion());
        return mgmtSpecVersion;
    }

    @ManagedAttribute(id = "name-current")
    @Description("name representing the running Java virtual machine")
    public StringStatistic getRuntimeName() {
        runtimeName.setCurrent(rtBean.getName());
        return runtimeName;
    }

    @ManagedAttribute(id = "specname-current")
    @Description("Java virtual machine specification name")
    public StringStatistic getSpecName() {
        specName.setCurrent(rtBean.getSpecName());
        return specName;
    }

    @ManagedAttribute(id = "specvendor-current")
    @Description("Java virtual machine specification vendor")
    public StringStatistic getSpecVendor() {
        specVendor.setCurrent(rtBean.getSpecVendor());
        return specVendor;
    }

    @ManagedAttribute(id = "specversion-current")
    @Description("Java virtual machine specification version")
    public StringStatistic getSpecVersion() {
        specVersion.setCurrent(rtBean.getSpecVersion());
        return specVersion;
    }

    @ManagedAttribute(id = "uptime-count")
    @Description("uptime of the Java virtual machine in milliseconds")
    public CountStatistic getUptime() {
        uptime.setCount(rtBean.getUptime());
        return uptime;
    }

    @ManagedAttribute(id = "vmname-current")
    @Description("Java virtual machine implementation name")
    public StringStatistic getVmName() {
        vmName.setCurrent(rtBean.getVmName());
        return vmName;
    }

    @ManagedAttribute(id = "vmvendor-current")
    @Description("Java virtual machine implementation vendor")
    public StringStatistic getVmVendor() {
        vmVendor.setCurrent(rtBean.getVmVendor());
        return vmVendor;
    }

    @ManagedAttribute(id = "vmversion-current")
    @Description("Java virtual machine implementation version")
    public StringStatistic getVmVersion() {
        vmVersion.setCurrent(rtBean.getVmVersion());
        return vmVersion;
    }
}
