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

package org.glassfish.admin.amxtest.config;

import com.sun.appserv.management.config.JavaConfig;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.StringUtil;
import org.glassfish.admin.amxtest.AMXTestBase;

import java.util.HashSet;
import java.util.Set;


import com.sun.appserv.management.helper.AttributeResolverHelper;

/**
 */
public final class JavaConfigTest
        extends AMXTestBase {
    public JavaConfigTest() {
    }

    public void
    testGetJVMOptions() {
        final JavaConfig jc = getConfigConfig().getJavaConfig();

        final String[] jvmOptions = jc.getJVMOptions();

        if (jvmOptions.length < 2) {
            warning("Fewer than 2 JVM options, is this right: " +
                    StringUtil.toString(jvmOptions));

        }

        /*
        Arrays.sort( jvmOptions );
        trace("length = " + jvmOptions.length);
        for (int ii=0; ii<jvmOptions.length; ii++)
        {
            trace("jvmOptions[" + ii + "] = " + jvmOptions[ii]);
        }
        */
    }

    public void
    testSetJVMOptions() {
        final String newOption1 = "-DJavaConfigTest.OK=true";
        final String newOption2 = "-XJavaConfigTest.OK=true";

        final JavaConfig jc = getConfigConfig().getJavaConfig();

        final Set<String> beforeSet = GSetUtil.newUnmodifiableStringSet(jc.getJVMOptions());

        // add our new options
        final Set<String> requestSet = new HashSet<String>(beforeSet);
        requestSet.add(newOption1);
        requestSet.add(newOption2);
        jc.setJVMOptions(GSetUtil.toStringArray(requestSet));

        Set<String> afterSet = GSetUtil.newUnmodifiableStringSet(jc.getJVMOptions());

        // make sure our new options are present
        assert (afterSet.contains(newOption1));
        assert (afterSet.contains(newOption2));

        // make sure all prior options are still present
        for (final String beforeOption : beforeSet) {
            assert (afterSet.contains(beforeOption));
        }

        // now remove our two options
        requestSet.remove(newOption1);
        requestSet.remove(newOption2);
        jc.setJVMOptions(GSetUtil.toStringArray(requestSet));

        // verify our two options are gone
        afterSet = GSetUtil.newUnmodifiableStringSet(jc.getJVMOptions());
        assert (!afterSet.contains(newOption1));
        assert (!afterSet.contains(newOption2));

        // make sure all prior options are still present
        assert (afterSet.equals(beforeSet));
    }

    public void
    testGetters()
            throws Exception {
        final JavaConfig jc = getConfigConfig().getJavaConfig();

        String s;

        s = jc.getBytecodePreprocessors();
        if (s != null) {
            jc.setBytecodePreprocessors(s);
        }

        s = jc.getClasspathPrefix();
        if (s != null) {
            jc.setClasspathPrefix(s);
        }

        s = jc.getClasspathSuffix();
        if (s != null) {
            jc.setClasspathSuffix(s);
        }

        s = jc.getSystemClasspath();
        if (s != null) {
            jc.setSystemClasspath(s);
        }

        final String debugEnabledStr = jc.getDebugEnabled();
        final boolean debugEnabled = AttributeResolverHelper.resolveBoolean( jc, debugEnabledStr);
        jc.setDebugEnabled( debugEnabledStr );

        s = jc.getDebugOptions();
        if (s != null) {
            jc.setDebugOptions(s);
        }

        final String existingValue = jc.getEnvClasspathIgnored();
        final boolean envClasspathIgnored = AttributeResolverHelper.resolveBoolean( jc, existingValue);
        jc.setEnvClasspathIgnored( existingValue);

        s = jc.getJavaHome();
        if (s != null) {
            jc.setJavaHome(s);
        }

        s = jc.getJavacOptions();
        if (s != null) {
            jc.setJavacOptions(s);
        }

        final String[] options = jc.getJVMOptions();
        if (options != null) {
            jc.setJVMOptions(options);
        }

        s = jc.getNativeLibraryPathPrefix();
        if (s != null) {
            jc.setNativeLibraryPathPrefix(s);
        }

        s = jc.getNativeLibraryPathSuffix();
        if (s != null) {
            jc.setNativeLibraryPathSuffix(s);
        }

        s = jc.getRMICOptions();
        if (s != null) {
            jc.setRMICOptions(s);
        }

        s = jc.getServerClasspath();
        if (s != null) {
            jc.setServerClasspath(s);
        }
    }
}

















