/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.util;

import com.sun.enterprise.deployment.annotation.introspection.AnnotationScanner;
import com.sun.enterprise.deployment.annotation.introspection.ClassFile;
import com.sun.enterprise.deployment.annotation.introspection.ConstantPoolInfo;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.hk2.classmodel.reflect.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.Collection;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.net.URI;

/**
 * Abstract superclass for specific types of annotation detectors.
 *
 * @author Jerome Dochez
 */
public class AnnotationDetector {

    protected final ClassFile classFile;
    protected final AnnotationScanner scanner;

    public AnnotationDetector(AnnotationScanner scanner) {
        this.scanner = scanner;
        ConstantPoolInfo poolInfo = new ConstantPoolInfo(scanner);
        classFile = new ClassFile(poolInfo);
    }

    public boolean hasAnnotationInArchiveWithNoScanning(ReadableArchive archive) throws IOException {
        Types types = null;
        if (archive.getParentArchive() != null) {
            types = archive.getParentArchive().getExtraData(Types.class);
        } else {
            types = archive.getExtraData(Types.class);
        }

        // we are on the client side so we need to scan annotations
        if (types == null) {
            return hasAnnotationInArchive(archive);
        }

        List<URI> uris = new ArrayList<URI>();
        uris.add(archive.getURI());
        try {
            uris.addAll(DOLUtils.getLibraryJarURIs(null, archive));
        } catch (Exception e) {
            DOLUtils.getDefaultLogger().log(Level.WARNING, e.getMessage(), e);
        }


        // force populating the annotations field in the scanner
        scanner.isAnnotation("foo");

        Set<String> annotations = scanner.getAnnotations();
        if (annotations == null) {
            return false;
        }

        for (String annotationType : annotations)  {
            Type type = types.getBy(annotationType);
             // we never found anyone using that type
            if (type==null) continue;
            if (type instanceof AnnotationType) {
                Collection<AnnotatedElement> elements = ((AnnotationType) type).allAnnotatedTypes();
                for (AnnotatedElement element : elements) {
                    Type t = (element instanceof Member?((Member) element).getDeclaringType():(Type) element);
                    if (t.wasDefinedIn(uris)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean hasAnnotationInArchive(ReadableArchive archive) throws IOException {

        Enumeration<String> entries = archive.entries();
        while (entries.hasMoreElements()) {
            String entryName = entries.nextElement();
            if (entryName.endsWith(".class")) {
                if (containsAnnotation(archive, entryName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsAnnotation(ReadableArchive archive, String entryName) throws IOException {
        return containsAnnotation(archive.getEntry(entryName), archive.getEntrySize(entryName));
    }

    protected boolean containsAnnotation(InputStream is, long size)
        throws IOException {
        boolean result = false;
        // check if it contains top level annotations...
        ReadableByteChannel channel = null;
        try {
            channel = Channels.newChannel(is);
            if (channel!=null) {
                result = classFile.containsAnnotation(channel, size);
             }
             return result;
        } finally {
            if (channel != null) {
                channel.close();
            }
        }
    }
}
