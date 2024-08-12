/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.naming;

import jakarta.inject.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.glassfish.hk2.api.Metadata;
import org.jvnet.hk2.annotations.Service;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Goes with {@link Service} annotation on a {@link NamedNamingObjectProxy}. Using this annotation a
 * {@link NamedNamingObjectProxy} indicates what namespace prefixes are handled by a proxy. This extra metadata enables
 * lazy instantiation of {@link NamedNamingObjectProxy} objects.
 *
 * @author sanjeeb.sahoo@oracle.com
 */
@Qualifier
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface NamespacePrefixes {
    @Metadata(GlassfishNamingManager.NAMESPACE_METADATA_KEY)
    String[] value();
}
