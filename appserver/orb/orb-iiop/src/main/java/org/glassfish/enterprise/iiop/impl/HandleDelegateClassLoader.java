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

package org.glassfish.enterprise.iiop.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class HandleDelegateClassLoader
    extends ClassLoader
{

    public HandleDelegateClassLoader() {
        super();
    }

    protected Class findClass(String name)
        throws ClassNotFoundException
    {
        // This is called only if the class could not be loaded by
        // the parent class loader (see javadoc for loadClass methods).
        // Load the class from the current thread's context class loader.

        Class c = Thread.currentThread().getContextClassLoader().loadClass(name);

        return c;
    }

    protected Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
        if (!name.equals("com.sun.enterprise.iiop.IIOPHandleDelegate")) {
            return super.loadClass(name, resolve);
        }

        Class handleDelClass = findLoadedClass(name);
        if (handleDelClass != null) {
            return handleDelClass;
        }

        InputStream is = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // read the bytes for IIOPHandleDelegate.class
            ClassLoader resCl = Thread.currentThread().getContextClassLoader();
            if (Thread.currentThread().getContextClassLoader() == null)  {
                resCl = getSystemClassLoader();
            }
            is = resCl.getResourceAsStream("org/glassfish/enterprise/iiop/impl/IIOPHandleDelegate.class");

            byte[] buf = new byte[4096]; // currently IIOPHandleDelegate is < 4k
            int nread = 0;
            while ( (nread = is.read(buf, 0, buf.length)) != -1 ) {
                baos.write(buf, 0, nread);
            }

            byte[] buf2 = baos.toByteArray();

            handleDelClass = defineClass(
            "org.glassfish.enterprise.iiop.impl.IIOPHandleDelegate",
            buf2, 0, buf2.length);

        } catch ( Exception ex ) {
            throw (ClassNotFoundException)new ClassNotFoundException(ex.getMessage()).initCause(ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ioe) {
                    //ignore
                }
            }
        }

        if (resolve) {
            resolveClass(handleDelClass);
        }

        return handleDelClass;
    }
}
