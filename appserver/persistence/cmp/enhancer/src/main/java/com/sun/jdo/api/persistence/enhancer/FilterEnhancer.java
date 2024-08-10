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

/*
 * FilterEnhancer.java
 */

package com.sun.jdo.api.persistence.enhancer;

import com.sun.jdo.api.persistence.enhancer.classfile.ClassFile;
import com.sun.jdo.api.persistence.enhancer.impl.ClassControl;
import com.sun.jdo.api.persistence.enhancer.impl.EnhancerControl;
import com.sun.jdo.api.persistence.enhancer.impl.Environment;
import com.sun.jdo.api.persistence.enhancer.meta.JDOMetaData;
import com.sun.jdo.api.persistence.enhancer.meta.JDOMetaDataModelImpl;
import com.sun.jdo.api.persistence.enhancer.meta.JDOMetaDataPropertyImpl;
import com.sun.jdo.api.persistence.enhancer.util.ClassFileSource;
//@olsen: added: support for I18N
import com.sun.jdo.api.persistence.enhancer.util.Support;
import com.sun.jdo.api.persistence.enhancer.util.UserException;
import com.sun.jdo.api.persistence.model.Model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Properties;


//@lars: the output stream is always written with the class - even if it hasn't been enhanced
//@lars: added an error-PrintWriter to all constructors
//@lars: changes to reflect the new ByteCodeEnhancer interface


/**
 * Implements a JDO enhancer as a byte-code filtering tool.
 */
//@olsen: added class
public class FilterEnhancer
    extends Support
    implements ByteCodeEnhancer
{
    static public final String DO_SIMPLE_TIMING
    = "ByteCodeEnhancer.doSimpleTiming";//NOI18N
    static public final String VERBOSE_LEVEL
    = "ByteCodeEnhancer.verboseLevel";//NOI18N
    static public final String VERBOSE_LEVEL_QUIET
    = "quiet";//NOI18N
    static public final String VERBOSE_LEVEL_WARN
    = "warn";//NOI18N
    static public final String VERBOSE_LEVEL_VERBOSE
    = "verbose";//NOI18N
    static public final String VERBOSE_LEVEL_DEBUG
    = "debug";//NOI18N

    /* Central repository for the options selected by
     * the user and the current state of the Filter execution */
    private Environment env = new Environment();

    private EnhancerControl econtrol = new EnhancerControl(env);

//    private StringWriter errString = new StringWriter();
//    private PrintWriter err = new PrintWriter(errString, true);

    /**
     * Initializes an instance of a JDO enhancer.
     * @param metaData the JDO meta-data object
     * @param settings enhancement properties
     * @param out standard ouput stream for the enhancer
     */
    protected void init(JDOMetaData metaData,
                        Properties  settings,
                        PrintWriter out,
                        PrintWriter err)
        throws EnhancerUserException, EnhancerFatalError
    {
        if (metaData == null) {
            //@olsen: support for I18N
            throw new EnhancerFatalError(
                getI18N("enhancer.internal_error",//NOI18N
                        "Illegal argument: metaData == null"));//NOI18N
        }

        env.setJDOMetaData(metaData);

        // set verbose level
        if  (err != null)
        {
            env.setErrorWriter(err);
        }
        if  (out != null)
        {
            env.setOutputWriter(out);
        }
        final String verboseLevel
            = (settings == null ? null : settings.getProperty(VERBOSE_LEVEL));
        if (VERBOSE_LEVEL_QUIET.equals(verboseLevel)) {
            env.setVerbose(false);
            env.setQuiet(true);
        } else if (VERBOSE_LEVEL_WARN.equals(verboseLevel)) {
            env.setVerbose(false);
            env.setQuiet(false);
        } else if (VERBOSE_LEVEL_VERBOSE.equals(verboseLevel)) {
            env.setVerbose(true);
            env.setQuiet(false);
        } else if (VERBOSE_LEVEL_DEBUG.equals(verboseLevel)) {
            env.setVerbose(true);
            env.setQuiet(false);
        } else {
            env.setVerbose(false);
            env.setQuiet(false);
        }

        //@olsen: force settings
        env.setNoOptimization(true);
        env.messageNL("FilterEnhancer: forced settings: -noopt");//NOI18N
    }

    /**
     * Creates an instance of a JDO enhancer.
     * @param metaData the JDO meta-data object
     * @param settings enhancement properties
     * @param out standard ouput stream for the enhancer
     */
    public FilterEnhancer(JDOMetaData metaData,
                          Properties  settings,
                          PrintWriter out,
                          PrintWriter err)
        throws EnhancerUserException, EnhancerFatalError
    {
        init(metaData, settings, out, err);
    }

    /**
     * Creates an instance of a JDO enhancer.
     * @param metaData the JDO meta-data properties
     * @param settings enhancement properties
     * @param out standard ouput stream for the enhancer
     */
    public FilterEnhancer(Properties  metaData,
                          Properties  settings,
                          PrintWriter out,
                          PrintWriter err)
        throws EnhancerUserException, EnhancerFatalError
    {
        if (metaData == null) {
            //@olsen: support for I18N
            throw new EnhancerFatalError(
                getI18N("enhancer.internal_error",//NOI18N
                        "Illegal argument: metaData == null"));//NOI18N
        }

        final JDOMetaData meta
            = new JDOMetaDataPropertyImpl(metaData, out);
        init(meta, settings, out, err);
    }

    /**
     * Creates an instance of a JDO enhancer.
     * @param metaData the JDO model
     * @param settings enhancement properties
     * @param out standard ouput stream for the enhancer
     */
    public FilterEnhancer(Model       metaData,
                          Properties  settings,
                          PrintWriter out,
                          PrintWriter err)
        throws EnhancerUserException, EnhancerFatalError
    {
        if (metaData == null) {
            //@olsen: support for I18N
            throw new EnhancerFatalError(
                getI18N("enhancer.internal_error",//NOI18N
                        "Illegal argument: metaData == null"));//NOI18N
        }

        final JDOMetaData meta
            = new JDOMetaDataModelImpl(metaData,
                                       env.getOutputWriter());
        init(meta, settings, out, err);
    }


    /**
     * Enhances a given class according to the JDO meta-data.
     */
    public boolean enhanceClassFile(InputStream         inByteCode,
                                    OutputStreamWrapper outByteCode)
        throws EnhancerUserException, EnhancerFatalError
    {
        env.messageNL("FilterEnhancer: enhancing classfile ...");//NOI18N

        // reset environment to clear class map etc.
        env.reset();

        // enhance class file; check Exceptions
        final boolean changed;
        try {
            changed = enhanceClassFile1(inByteCode, outByteCode);
        } catch (UserException ex) {
            // note: catch UserException before RuntimeException

            // reset environment to clear class map etc.
            env.reset();
            //@olsen: support for I18N
            throw new EnhancerUserException(
                getI18N("enhancer.error",//NOI18N
                        ex.getMessage()),
                ex);
        } catch (RuntimeException ex) {
            // note: catch UserException before RuntimeException

            // reset environment to clear class map etc.
            env.reset();
            //@olsen: support for I18N
            ex.printStackTrace ();
            throw new EnhancerFatalError(
                getI18N("enhancer.internal_error",//NOI18N
                        ex.getMessage()),
                ex);
        }

        env.messageNL(changed
                      ? "FilterEnhancer: classfile enhanced successfully."//NOI18N
                      : "FilterEnhancer: classfile not changed.");//NOI18N
        return changed;
    }

    /**
     * Enhances a given class according to the JDO meta-data.
     */
    private boolean enhanceClassFile1(InputStream         inByteCode,
                                      OutputStreamWrapper outByteCode)
    {
        // check arguments
        affirm(inByteCode, "Illegal argument: inByteCode == null.");//NOI18N
        affirm(outByteCode, "Illegal argument: outByteCode == null.");//NOI18N

        // parse class
        final ClassFileSource cfs;
        final ClassFile cf;
        final ClassControl cc;
        try {
            // create class file source
            cfs = new ClassFileSource(null, inByteCode);

            // create class file
            final DataInputStream dis = cfs.classFileContents();
            cf = new ClassFile(dis);
//@lars: do not close the input stream
//            dis.close();

            // create class control
            cc = new ClassControl(cfs, cf, env);
            env.addClass(cc);

            // get real class name
            final String className = cc.className();
            cfs.setExpectedClassName(className);
        } catch (IOException ex) {
            //@olsen: support for I18N
            throw new UserException(
                getI18N("enhancer.io_error_while_reading_stream"),//NOI18N
                ex);
        } catch (ClassFormatError ex) {
            //@olsen: support for I18N
            throw new UserException(
                getI18N("enhancer.class_format_error"),//NOI18N
                ex);
        }

        // enhance class
        econtrol.modifyClasses();
        if (env.errorCount() > 0) {
            // retrieve error messages
            env.getErrorWriter ().flush ();
            /*
            final String str = errString.getBuffer().toString();

            // reset env's error writer
            errString = new StringWriter();
            err = new PrintWriter(errString, true);
            env.setErrorWriter(err);
            */

            //@olsen: support for I18N
            throw new UserException(env.getLastErrorMessage ());
        }

        // write class
        boolean changed = (cc.updated() && cc.filterRequired());
        try {
            if (changed)
            {
                env.message("writing enhanced class " + cc.userClassName()//NOI18N
                            + " to output stream");//NOI18N
            }
            else
            {
                env.message("no changes on class " + cc.userClassName());
            }
            outByteCode.setClassName (cc.userClassName ());
            final DataOutputStream dos = new DataOutputStream(outByteCode.getStream ());
            cf.write(dos);
            dos.flush();
        } catch (IOException ex) {
            //@olsen: support for I18N
            throw new UserException(
                getI18N("enhancer.io_error_while_writing_stream"),//NOI18N
                ex);
        }
        return changed;
    }


    /**********************************************************************
     *
     *********************************************************************/

    public boolean enhanceClassFile (InputStream  in,
                                     OutputStream out)
                   throws EnhancerUserException,
                          EnhancerFatalError
    {

        return enhanceClassFile (in, new OutputStreamWrapper (out));

    }  //FilterEnhancer.enhanceClassFile()


}  //FilterEnhancer
