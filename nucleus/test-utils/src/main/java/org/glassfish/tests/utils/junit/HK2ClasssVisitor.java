/*
 * Copyright (c) 2021, 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.utils.junit;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;

import static org.objectweb.asm.Opcodes.ASM9;


/**
 * @author jwells
 * @author David Matejcek
 */
// This class was taken from org.jvnet.hk2.testing.junit.internal.ClassVisitorImpl and updated.
class HK2ClasssVisitor extends ClassVisitor {
    private static final Logger LOG = Logger.getLogger(HK2ClasssVisitor.class.getName());
    private final static String SERVICE_CLASS_FORM = "Lorg/jvnet/hk2/annotations/Service;";

    private final ServiceLocator locator;
    private final Set<String> excludedClasses;
    private String implName;
    private boolean isAService;

    /**
     * @param locator
     * @param excludedClasses
     */
    HK2ClasssVisitor(final ServiceLocator locator, final Set<Class<?>> excludedClasses) {
        super(ASM9);
        this.locator = locator;
        this.excludedClasses = excludedClasses.stream().map(Class::getName).collect(Collectors.toSet());
    }


    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        implName = name.replace("/", ".");
    }


    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (!visible) {
            return null;
        }
        if (SERVICE_CLASS_FORM.equals(desc)) {
            isAService = true;
        }
        return null;
    }


    @Override
    public void visitEnd() {
        if (!isAService) {
            return;
        }
        if (excludedClasses.contains(implName)) {
            return;
        }

        Class<?> implClass = null;
        try {
            implClass = Class.forName(implName, true, getClass().getClassLoader());
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            LOG.log(Level.WARNING, "Classloading failed for service {0}, skipped. Reason: {1}",
                new Object[] {implName, e});
            return;
        }

        final List<ActiveDescriptor<?>> added = ServiceLocatorUtilities.addClasses(locator, implClass);
        LOG.log(Level.CONFIG, "Added HK2 services: {0}", added);
    }
}
