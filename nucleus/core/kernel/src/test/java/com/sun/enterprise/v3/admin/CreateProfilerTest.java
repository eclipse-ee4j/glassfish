/*
 * Copyright (c) 2021, 2025 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.config.serverbeans.Profiler;
import com.sun.enterprise.v3.common.PlainTextActionReporter;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.security.auth.Subject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandContextImpl;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.main.core.kernel.test.KernelJUnitExtension;
import org.glassfish.tests.utils.mock.MockGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.TransactionFailure;

import static org.glassfish.api.admin.ServerEnvironment.DEFAULT_INSTANCE_NAME;
import static org.glassfish.hk2.utilities.BuilderHelper.createConstantDescriptor;
import static org.glassfish.hk2.utilities.ServiceLocatorUtilities.addOneDescriptor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Prashanth
 * @author David Matejcek
 */
@ExtendWith(KernelJUnitExtension.class)
public class CreateProfilerTest {

    @Inject
    private ServiceLocator locator;

    @Inject
    private MockGenerator mockGenerator;

    @Inject
    private JavaConfig javaConfig;

    @Inject
    private CommandRunnerImpl commandRunner;

    private CreateProfiler command;
    private AdminCommandContext context;
    private Subject adminSubject;


    @BeforeEach
    public void init() {
        assertNotNull(javaConfig);
        assertNotNull(commandRunner);

        final Config config = locator.getService(Config.class);
        assertNotNull(config, "config");
        addOneDescriptor(locator, createConstantDescriptor(config, DEFAULT_INSTANCE_NAME, Config.class));

        command = locator.getService(CreateProfiler.class);
        assertNotNull(command);

        adminSubject = mockGenerator.createAsadminSubject();
        context = new AdminCommandContextImpl(
            LogDomains.getLogger(CreateProfilerTest.class, LogDomains.ADMIN_LOGGER),
            locator.<ActionReport>getService(PlainTextActionReporter.class));
    }


    @AfterEach
    public void deleteProfiler() throws TransactionFailure {
        ConfigSupport.apply(param -> {
            if (param.getProfiler() != null) {
                param.setProfiler(null);
            }
            return null;
        }, javaConfig);
    }


    /**
     * Test of execute method, of class CreateProfiler.
     * asadmin create-profiler --nativelibrarypath "myNativeLibraryPath"
     *          --enabled=true --classpath "myProfilerClasspath" testProfiler
     */
    @Test
    public void properties() {
        ParameterMap parameters = new ParameterMap();
        parameters.set("classpath", "myProfilerClasspath");
        parameters.set("enabled", "true");
        parameters.set("nativelibrarypath", "myNativeLibraryPath");
        parameters.set("property","a=x:b=y:c=z");
        parameters.set("DEFAULT", "testProfiler");

        commandRunner.getCommandInvocation("create-profiler", context.getActionReport(), adminSubject)
            .parameters(parameters).execute(command);

        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        Profiler profiler = javaConfig.getProfiler();
        assertAll(
            () -> assertEquals("testProfiler", profiler.getName()),
            () -> assertEquals("myProfilerClasspath", profiler.getClasspath()),
            () -> assertEquals("true", profiler.getEnabled()),
            () -> assertEquals("myNativeLibraryPath", profiler.getNativeLibraryPath()),
            () -> assertThat(profiler.getProperty(), hasSize(3)),
            () -> assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode()),
            () -> assertEquals("", context.getActionReport().getMessage(), "message")
        );

        Map<String, String> properties = profiler.getProperty().stream().map(p -> Map.entry(p.getName(), p.getValue()))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        assertAll(
            () -> assertThat(properties, hasEntry("a", "x")),
            () -> assertThat(properties, hasEntry("b", "y")),
            () -> assertThat(properties, hasEntry("c", "z"))
        );

    }

    /**
     * Test of execute method, of class CreateProfiler with default values.
     * asadmin create-profiler --nativelibrarypath "myNativeLibraryPath"
     *          --enabled=true --classpath "myProfilerClasspath" testProfiler
     */
    @Test
    public void defaults() {
        ParameterMap parameters = new ParameterMap();
        parameters.set("DEFAULT", "myProfilerAllDefaults");

        commandRunner.getCommandInvocation("create-profiler", context.getActionReport(), adminSubject)
            .parameters(parameters).execute(command);
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        Profiler profiler = javaConfig.getProfiler();
        assertAll(
            () -> assertEquals("myProfilerAllDefaults", profiler.getName()),
            () -> assertNull(profiler.getClasspath()),
            () -> assertEquals("true", profiler.getEnabled()),
            () -> assertNull(profiler.getNativeLibraryPath()),
            () -> assertEquals("", context.getActionReport().getMessage(), "message")
        );
    }

    /**
     * Test of execute method, creating a new when there is already one.
     * asadmin create-profiler --nativelibrarypath "myNativeLibraryPath"
     *          --enabled=true --classpath "myProfilerClasspath" testProfiler
     */
    @Test
    public void twoProfilersForbidden() {
        ParameterMap parameters = new ParameterMap();
        parameters.set("DEFAULT", "testProfiler");

        commandRunner.getCommandInvocation("create-profiler", context.getActionReport(), adminSubject)
            .parameters(parameters).execute(command);
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        // try to create another profile
        parameters = new ParameterMap();
        parameters.set("DEFAULT", "testProfilerNew");

        commandRunner.getCommandInvocation("create-profiler", context.getActionReport(), adminSubject)
            .parameters(parameters).execute(command);
        assertAll(
            () -> assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode()),
            () -> assertEquals("profiler exists. Please delete it first", context.getActionReport().getMessage())
        );

        Profiler profiler = javaConfig.getProfiler();
        assertAll(
            () -> assertEquals("testProfiler", profiler.getName()),
            () -> assertNull(profiler.getClasspath(), "classpath"),
            () -> assertEquals("true", profiler.getEnabled()),
            () -> assertNull(profiler.getNativeLibraryPath(), "nativelibrarypath")
        );
    }

    /**
     * Test of execute method, of class CreateProfiler when enabled set to junk
     * asadmin create-profiler --nativelibrarypath "myNativeLibraryPath"
     *          --enabled=true --classpath "myProfilerClasspath" testProfiler
     */
    @Test
    public void invalidOption() {
        ParameterMap parameters = new ParameterMap();
        parameters.set("enabled", "junk");
        parameters.set("DEFAULT", "myProfiler");

        commandRunner.getCommandInvocation("create-profiler", context.getActionReport(), adminSubject)
            .parameters(parameters).execute(command);

        assertAll(
            () -> assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode()),
            () -> assertEquals(
                "Invalid parameter: enabled."
                    + "  This boolean option must be set (case insensitive) to true or false."
                    + "  Its value was set to junk",
                context.getActionReport().getMessage()));
    }

    /**
     * Test of execute method, of class CreateProfiler when enabled has no value
     * asadmin create-profiler --nativelibrarypath "myNativeLibraryPath"
     *          --enabled=true --classpath "myProfilerClasspath" testProfiler
     */
    @Test
    public void noValueOptionEnabled() {
        ParameterMap parameters = new ParameterMap();
        parameters.set("enabled", "");
        parameters.set("DEFAULT", "testProfiler");

        commandRunner.getCommandInvocation("create-profiler", context.getActionReport(), adminSubject)
            .parameters(parameters).execute(command);

        Profiler profiler = javaConfig.getProfiler();
        assertAll(
            () -> assertEquals("testProfiler", profiler.getName()),
            () -> assertEquals("true", profiler.getEnabled()),
            () -> assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode()),
            () -> assertEquals("", context.getActionReport().getMessage())
        );
    }
}
