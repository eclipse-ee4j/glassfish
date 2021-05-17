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

package org.glassfish.apf.factory;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;

import org.glassfish.apf.Scanner;
import org.glassfish.apf.AnnotationProcessor;
import org.glassfish.apf.AnnotationHandler;
import org.glassfish.apf.impl.AnnotationProcessorImpl;
import org.glassfish.apf.impl.AnnotationUtils;
/**
 * The Factory is responsible for initializing a ready to use AnnotationProcessor.
 *
 * @author Jerome Dochez
 */
public abstract class Factory {

    private static Set<String> skipAnnotationClassList = null;
    private static final String SKIP_ANNOTATION_CLASS_LIST_URL =
        "skip-annotation-class-list";

    /** we do no Create new instances of Factory */
    protected Factory() {
    }

    /**
     * Return a empty AnnotationProcessor with no annotation handlers registered
     * @return initialized AnnotationProcessor instance
     */
    public static AnnotationProcessorImpl getDefaultAnnotationProcessor() {
        return new AnnotationProcessorImpl();
    }

    // initialize the list of class files we should skip annotation processing
    private synchronized static void initSkipAnnotationClassList() {
        if (skipAnnotationClassList == null) {
            skipAnnotationClassList = new HashSet<String>();
            InputStream is = null;
            BufferedReader bf = null;
            try {
                is = AnnotationProcessorImpl.class.getClassLoader().getResourceAsStream(SKIP_ANNOTATION_CLASS_LIST_URL);
                if (is==null) {
                    AnnotationUtils.getLogger().log(Level.FINE, "no annotation skipping class list found");
                    return;
                }
                bf = new BufferedReader(new InputStreamReader(is));
                String className;
                while ( (className = bf.readLine()) != null ) {
                    skipAnnotationClassList.add(className.trim());
                }
            } catch (IOException ioe) {
                AnnotationUtils.getLogger().log(Level.WARNING,
                    ioe.getMessage(), ioe);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ioe2) {
                        // ignore
                    }
                }
                if (bf != null) {
                    try {
                        bf.close();
                    } catch (IOException ioe2) {
                        // ignore
                    }
                }
            }
        }
    }

    // check whether a certain class can skip annotation processing
    public static boolean isSkipAnnotationProcessing(String cName) {
        if (skipAnnotationClassList == null) {
            initSkipAnnotationClassList();
        }
        return skipAnnotationClassList.contains(cName);
    }

}
