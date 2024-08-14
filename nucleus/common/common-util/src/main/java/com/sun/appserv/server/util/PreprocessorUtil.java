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

package com.sun.appserv.server.util;

import com.sun.enterprise.util.CULoggerInfo;

import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.BytecodePreprocessor;

/**
 * PreprocessorUtil is a utility class for managing the bytecode
 * preprocessor(s). The list of preprocessors are passed in as a string array
 * to the initialize method.  If there is a problem initialize any of the
 * preprocessors, all preprocessing is disabled.
 */
public class PreprocessorUtil {

    private static boolean _preprocessorEnabled = false;
    private static BytecodePreprocessor[] _preprocessor;

    /**
     * Initializes the preprocessor utility with the associated class names
     * array arugment.
     * @param ppClassNames - the String array of preprocessor class names.
     * @return - true if successful, otherwise false.  All preprocessors must
     * successfully initialize for true to be returned.
     */
    public static boolean init (String[] ppClassNames) {
        if (ppClassNames != null) {
            setupPreprocessor(ppClassNames);
        }
        return _preprocessorEnabled;
    }

    /**
     * Processes a class through the preprocessor.
     * @param className - the class name.
     * @param classBytes - the class byte array.
     * @return - the processed class byte array.
     */
    public static byte[] processClass (String className, byte[] classBytes) {
        Logger _logger = CULoggerInfo.getLogger();
        byte[] goodBytes = classBytes;
        if (_preprocessorEnabled) {
            if (_preprocessor != null) {
                // Loop through all of the defined preprocessors...
                for (int i=0; i < _preprocessor.length; i++) {
                    classBytes =
                        _preprocessor[i].preprocess(className, classBytes);
                    _logger.log(Level.FINE,
                            "[PreprocessorUtil.processClass] Preprocessor {0} Processed Class: {1}",
                            new Object[]{i, className});
                    // Verify the preprocessor returned some bytes
                    if (classBytes != null){
                        goodBytes = classBytes;
                    }
                    else{
                        _logger.log(Level.SEVERE, CULoggerInfo.preprocessFailed,
                            new String[] {className,
                                          _preprocessor[i].getClass().getName()});

                        // If were on the 1st preprocessor
                        if (i == 0){
                            _logger.log(Level.SEVERE, CULoggerInfo.resettingOriginal,
                                className);
                        }
                        // We're on the 2nd or nth preprocessor.
                        else {
                            _logger.log(Level.SEVERE, CULoggerInfo.resettingLastGood,
                                className);
                        }
                    }
                }
            }
        }
        return goodBytes;
    }

    private synchronized static void setupPreprocessor(String[] ppClassNames) {
        Logger _logger = CULoggerInfo.getLogger();

        if (_preprocessor != null) {
            // The preprocessors have already been set up.
            return;
        }

        try {
            _preprocessor = new BytecodePreprocessor[ppClassNames.length];
            for (int i = 0; i < ppClassNames.length; i++) {
                String ppClassName = ppClassNames[i].trim();
                Class ppClass = Class.forName(ppClassName);
                if (ppClass != null){
                    _preprocessor[i] = (BytecodePreprocessor)
                                                        ppClass.newInstance();
                        _preprocessorEnabled = true;
                }
                if (_preprocessor[i] != null){
                    if (!_preprocessor[i].initialize(new Hashtable())) {
                        _logger.log(Level.SEVERE, CULoggerInfo.failedInit,
                            ppClassName);
                        _logger.log(Level.SEVERE, CULoggerInfo.disabled);
                        _preprocessorEnabled = false;
                    }
                } else {
                    _logger.log(Level.SEVERE, CULoggerInfo.failedInit,
                        ppClassName);
                    _logger.log(Level.SEVERE, CULoggerInfo.disabled);
                    _preprocessorEnabled = false;
                }
            }
        } catch (Throwable t) {
            _logger.log(Level.SEVERE, CULoggerInfo.setupEx, t);
            _logger.log(Level.SEVERE, CULoggerInfo.disabled);
            _preprocessorEnabled = false;
        }
    }

    /**
     * Indicates whether or not the preprocessor is enabled
     * @return - true of the preprocessor is enabled, otherwise false.
     */
    public static boolean isPreprocessorEnabled() {
        return _preprocessorEnabled;
    }
}
