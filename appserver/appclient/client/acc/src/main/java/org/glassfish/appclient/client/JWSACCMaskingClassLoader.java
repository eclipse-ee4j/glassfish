/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2011, 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.appclient.client;

import java.util.Collection;
import java.util.Collections;

/**
 * Masks classes or resources that are listed in the "endorsed" packages that GlassFish provides.
 * <p>
 * During Java Web Start launches we cannot set java.endorsed.dirs to have the bootstrap class loader look in the
 * GlassFish-provided JARs for classes and resources that would otherwise be found in the system JARs (rt.jar for
 * example). We need some other way of making sure the GlassFish-provided JARs are used. Those JARs are listed in the
 * JNLP documents for the app, so the JNLPClassLoader which Java Web Start provides will find them if we give it a
 * chance.
 * <p>
 * This loader knows what packages are in the GlassFish-provided endorsed JARs and will report any matching class or
 * resource as not found. When an instance of this loader is inserted as the parent of the JNLPClassLoader then the
 * JNLPClassLoader will delegate to it first. The masking loader will report GlassFish-provided content as not found, so
 * the JNLPClassLoader will try to resolve them itself. That resolution will use the downloaded GlassFish JARs, thereby
 * making sure the Java Web Start launch uses the GlassFish-provided endorsed JARs instead of whatever happens to be in
 * the Java system JARs.
 *
 * @author Tim Quinn
 */
class JWSACCMaskingClassLoader extends MaskingClassLoader {

    private final Collection<String> endorsedPackagesToMask;

    JWSACCMaskingClassLoader(ClassLoader parent, Collection<String> endorsedPackagesToMask) {
        super(parent, Collections.emptySet() /* punchins */, Collections.emptySet() /* multiples */,
                false /* useExplicitCallsToFindSystemClass */);
        this.endorsedPackagesToMask = endorsedPackagesToMask;
    }

    @Override
    protected boolean isDottedNameLoadableByParent(final String name) {
        /*
         * Currently the only masked packages start with javax. or org. Check the prefix as an optimization to avoid searching
         * the collection.
         */
        if (!(name.startsWith("javax.") || name.startsWith("org."))) {
            return true;
        }

        final String packageName = name.substring(0, name.lastIndexOf("."));
        if (endorsedPackagesToMask.contains(packageName)) {
            /*
             * The requested name is one to be masked, so do not let the caller delegate to its parent.
             */
            return false;
        }
        /*
         * The requested name should not be masked. Allow the caller to delegate to its parent first.
         */
        return true;
    }
}
