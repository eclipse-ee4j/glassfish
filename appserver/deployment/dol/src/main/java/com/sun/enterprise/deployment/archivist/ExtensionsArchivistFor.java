/*
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

package com.sun.enterprise.deployment.archivist;

import jakarta.inject.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.glassfish.hk2.api.Metadata;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Normally goes with {@link org.jvnet.hk2.annotations.Service} annotation,
 * and this annotation must be placed on a class that extends
 * {@link com.sun.enterprise.deployment.archivist.ExtensionsArchivist}.
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
@Qualifier
public @interface ExtensionsArchivistFor {
    /**
     * see {@link org.glassfish.api.container.Sniffer.getModuleType} and its
     * implementation classes for valid string values.
     */
    @Metadata(ArchivistFactory.EXTENSION_ARCHIVE_TYPE)
    String value();
}
