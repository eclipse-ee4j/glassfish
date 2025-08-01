/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package org.glassfish.admin.amx.util;

import com.sun.enterprise.util.io.FileUtils;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.glassfish.embeddable.GlassFishVariable.INSTANCE_ROOT;

/**
Internal debug facility.  For development use only.
<b>Do not use this class; it is subject to arbitrary change.</b>
<p>
AMX has some unique issues that make a separate debug
facility highly useful.  These include:

<ul>
<li>The Logging MBean cannot use System.out/err or logging
mechanisms to emit debugging information because it can
potentially cause infinite recursion (and thus stack overflow).
Without anothermechanism, debugging the Logging MBean would
be very difficult
</li>
<li>
AMX code has both client and server aspects, and the
client code runs in both environments.  The client code
in particular can't necessarily assume there is any
logging infrastructure in place
</li>
<li>Debugging complex interactions of MBean invocations
over a remote connection which may also involve asynchronous
Notifications is complicated.  This logging facility makes
it possible to selectively view specific entities in the
interactions that are occuring, on a fine-grained level,
something not possible with the server logging mechanism</li>
</li>
<li>
There is reason and justification for considering debugging
code a separate issue than the log file that customers are
expected to use.  Comingling the two has already led to
the log file becoming useless when logging is set to FINE[R][EST].
This issue becomes a serious problem when trying to diagnose
an issue; one is either stuck with inserting INFO or higher
logging messages, or using FINE and dealing with the vast
quantify of log messages emitted at that level. It simply
is unproductive, and discourages thorough testing.
</li>
</ul>

<p>
<b>Usage notes</b>
AMXDebug associates a file with each identifier (typically a classname).
These files are located within the {@link #AMX_DEBUG_SUBDIR}
subdirectory within the directory specified by
System.getProperty( "user.home" ) unless the system property
{@link #AMX_DEBUG_DIR_SPROP} is specified.
All resulting AMXDebug output files
end in the suffix {@link #AMX_DEBUG_SUFFIX}.
<p>
This fine-grained approach makes it possible to "tail" just the
output from just the classes of interest,
something that is difficult or impossible otherwise.
<p>
AMXDebug is designed as a singleton. However, arbitrary identifiers
may be used to associate debugging output with a particular
output file.  This allows fine-grained and selective debugging
of just the items of interest.
<p>
When debugging is off, overhead is minimal, because all debugging
calls are routed to "dev/null".  The caller can also wrap
such calls such that they don't make it to AMXDebug at all.
<p>
The debug flag may be set via the system property
{@link #AMX_DEBUG_ENABLED_SPROP}.  Debugging will be enabled if that
property has value "true".  Otherwise, it is disabled.
Debugging may also be programmatically enabled, on a per-ID
basis.
<p>
The expected usage is per-class and the classname can generally
be used as the identifier.  However, usage include other
patterns; anything that the emitting code can agree on, regardless
of whether it is in the same class, or spread across many.  One
possibility would be to place the Output into the thread context.
There are other possibilities.
<p>
Output may be marked using the {@link #mark} and {@link #markAll} routines.
This aids in visually organizing the output.
<p>
<b>For more information, see the javadoc on individual routines.</b>

 */
public final class AMXDebug
{
    private final ConcurrentMap<String, WrapOutput> mOutputs;

    private static final AMXDebug INSTANCE = new AMXDebug();

    private final File mDir;

    private boolean mMadeDebugDir;

    private boolean mDefaultDebug;

    private final boolean mAppend;

    /** the key for the system property to enable AMX debug facility */
    public static final String AMX_DEBUG_ENABLED_SPROP = "AMX-DEBUG.enabled";

    /** the key for the system property to append to debug files.
    Otherwise they are overwritten each time
     */
    public static final String AMX_DEBUG_APPEND_SPROP = "AMX-DEBUG.append";

    /**
    The key for the system property to specify a different AMX_DEBUG_DIR.
    This value is uninterpreted--the result from
    new File( System.getProperty( {@link #AMX_DEBUG_SUBDIR} ) is used directly.
    <p>
    If the sytem property {@link #AMX_DEBUG_SUBDIR} is not specified,
    then AMXDebug looks for the system property
    "com.sun.aas.instanceRoot". If that system property
    is not found, then "user.home" is used.  The result of this is
    the "parent dir".  The resulting output
    directory is then <parent-dir>/{@link #AMX_DEBUG_SUBDIR}.
     */
    public static final String AMX_DEBUG_DIR_SPROP = "AMX-DEBUG.dir";

    /**
    The name of the default subdirectory which contains
    the ".debug" files created by AMXDebug.  This is the directory
    used if {@link #AMX_DEBUG_DIR_SPROP} is not specified.
     */
    public static final String AMX_DEBUG_SUBDIR = "AMX-DEBUG";

    /**
    Suffix used on all Output files.
     */
    public static final String AMX_DEBUG_SUFFIX = ".debug";

    // Output for AMXDebug itself
    private final WrapOutput mDebug;

    private final String NEWLINE;

    private final Set<Character> ILLEGAL_CHARS;

    private final char[] ILLEGAL_CHARS_ARRAY =
    {
        '\u0000',
        '?', '*', '|', '\'', '|', '\\', '/', ':',
    };

    private AMXDebug()
    {
        ILLEGAL_CHARS = new HashSet<>();
        for (final char c : ILLEGAL_CHARS_ARRAY)
        {
            ILLEGAL_CHARS.add(c);
        }

        NEWLINE = System.getProperty("line.separator");
        assert (NEWLINE != null);

        String value = System.getProperty(AMX_DEBUG_ENABLED_SPROP);
        if (value == null)
        {
            // not the right one, but a common mistake.
            value = System.getProperty("AMX-DEBUG");
            if (value != null && value.equals(""))
            {
                value = "true";
            }
        }
        mDefaultDebug = (value != null) && Boolean.parseBoolean(value);

        value = System.getProperty(AMX_DEBUG_APPEND_SPROP);
        mAppend = (value != null) && Boolean.parseBoolean(value);

        mOutputs = new ConcurrentHashMap<>();

        mDir = getDir();
        mMadeDebugDir = false;

        if (mDefaultDebug)
        {
            makeDebugDir();
        }

        mDebug = _getOutput(this.getClass().getName());
        mark(mDebug, getStdMarker("AMXDebug started "));
        mDebug.println("*** System Properties ***");
        dumpSystemProps(mDebug);

        mark(mDebug, getStdMarker("AMXDebug initialization done"));
    }

    private void dumpSystemProps(final Output output)
    {
        final java.util.Properties props = System.getProperties();

        Set<Object> keyset = props.keySet();
        final String[] keys = keyset.toArray(new String[keyset.size()]);
        java.util.Arrays.sort(keys);
        for (final String key : keys)
        {
            debug(key + "=" + props.getProperty(key));
        }

    }

    private void makeDebugDir()
    {
        if (!mMadeDebugDir)
        {
            if (mDir.mkdirs()) {
                mMadeDebugDir = true;
            }
        }
    }

    private void debug(final String s)
    {
        // we don't use debug() because we don't want/need the "DEBUG:" prefix

        if (mDefaultDebug && mDebug != null)
        {
            mDebug.println("" + s);
        }
    }

    private static String parens(final String s)
    {
        return "(" + s + ")";
    }

    private File getDir()
    {
        final String value = System.getProperty(AMX_DEBUG_DIR_SPROP);

        File debugDir;

        if (value == null)
        {
            final String instanceRoot = System.getProperty(INSTANCE_ROOT.getSystemPropertyName());
            File parentDir = instanceRoot == null ? FileUtils.USER_HOME : new File(instanceRoot);
            debugDir = new File(parentDir, AMX_DEBUG_SUBDIR);
        }
        else
        {
            debugDir = new File(value);
        }

        return debugDir;
    }

    public String[] getOutputIDs()
    {
        return SetUtil.toStringArray(mOutputs.keySet());
    }

    /**
    Get the current default debug state used when any
    new Outputs are created.
     */
    public boolean getDefaultDebug()
    {
        return mDefaultDebug;
    }

    /**
    Set the current default debug state.  Existing outputs
    are not affected.
    @see #setAll
     */
    public void setDefaultDebug(final boolean debug)
    {
        mDefaultDebug = debug;
        if (mDefaultDebug)
        {
            makeDebugDir();
        }
        mDebug.setDebug(debug);
        debug("setDefaultDebug" + parens("" + debug));
    }

    /**
    Get the debug state of a particular Output for the
    specified ID.
     */
    public boolean getDebug(final String id)
    {
        return _getOutput(id).getDebug();
    }

    /**
    Set the debug state of a particular Output for the
    specified ID.  If the Output currently maintains
    an open file, and debug is false, the file is closed.
     */
    public void setDebug(final String id, final boolean debug)
    {
        if (debug)
        {
            makeDebugDir();
        }
        _getOutput(id).setDebug(debug);
        debug("setDebug" + parens(id + ", " + debug));
    }

    /**
    Set the debug state of all Outputs.
    @see #setDebug
     */
    public void setAll(final boolean debug)
    {
        debug("setAll" + parens("" + debug));

        setDefaultDebug(debug);
        for (final WrapOutput w : mOutputs.values())
        {
            w.setDebug(debug);
        }
    }

    /**
    Turn off all debugging and close all files.
     */
    public void cleanup()
    {
        debug("cleanup()");

        setDefaultDebug(false);
        setAll(false);
    }

    /**
    Turn off debugging any close the associated file (if any)
    for the Output specified by 'id'.
     */
    public void reset(final String id)
    {
        debug("reset" + parens(id));
        _getOutput(id).reset();
    }

    private static final String DASHES = "----------";

    /**
    The standard marker line emitted by {@link #mark}.
     */
    public String getStdMarker()
    {
        return getStdMarker("");
    }

    /**
    The standard marker line emitted by {@link #mark} with a message
    inserted.
     */
    public String getStdMarker(final String msg)
    {
        return (NEWLINE + NEWLINE +
                DASHES + " " + new java.util.Date() + " " + msg + DASHES + NEWLINE);
    }

    /**
    Output a marker into the Output. If 'marker' is null, then
    the std marker is emitted.
     */
    public void mark(final Output output, final String marker)
    {
        output.println(marker == null ? getStdMarker() : marker);
    }

    /**
    Output a marker into the Output associated with 'id'.
     */
    public void mark(final String id, final String marker)
    {
        mark(getOutput(id), marker);
    }

    /**
    Output a standard marker into the Output.
     */
    public void mark(final String id)
    {
        mark(id, null);
    }

    /**
    Output a standard marker into the Output.
     */
    public void markAll(final String marker)
    {
        for (final WrapOutput w : mOutputs.values())
        {
            if (w.getDebug()) // optimization for debug=false
            {
                mark(w, marker);
            }
        }
    }

    /**
    Output a standard marker into the Output.
     */
    public void markAll()
    {
        markAll(null);
    }

    /**
    Get the Singletone AMXDebug instance.
     */
    public static AMXDebug getInstance()
    {
        return INSTANCE;
    }

    /**
    Get the File associated with 'id'.  The file may or
    may not exist, and may or may not be open, depending
    on whether debug is enabled and whether anything was
    written to the file.
     */
    public File getOutputFile(final String id)
    {
        final String filename = makeSafeForFile(id) + AMX_DEBUG_SUFFIX;

        return new java.io.File(mDir, filename);
    }

    private WrapOutput _getOutput(final String id)
    {
        WrapOutput output = mOutputs.get(id);
        /*
        A "fast path" is important here not for speed, but to avoid the side-effects
        of taking a shared lock while debugging (which serializes threads), thus changing
        the very behavior that might be being debugged.  So we want to take a lock
        only if the WrapOutput must be created.
         */
        if (output == null)
        {
            // Did not exist at the time of the 'if' test above.
            // It might now exist; create a new WrapOutput optimistically.
            output = new WrapOutput(this, getOutputFile(id), mDefaultDebug);

            // retain existing output if already present
            // Note that ConcurrentHashMap guarantees "happens before" for put/get
            // so all fields of the WrapOutput will be visible to other threads
            final WrapOutput prev = mOutputs.putIfAbsent(id, output);
            if (prev != null)
            {
                output = prev;
            }
            else
            {
                debug("AMXDebug: Created output for " + StringUtil.quote(id));
            }
        }

        return output;
    }

    public Output getShared()
    {
        return getOutput("AMXDebug-Shared");
    }

    /**
    ID is typically a classname, but may be
    anything which can be used for a filename.  The
    id will be used to create file <id>.debug in the
    {@link #AMX_DEBUG_SUBDIR} directory.
     */
    public Output getOutput(final String id)
    {
        return _getOutput(id);
    }

    /**
    Get a form of the ID that is safe to for a filename.
     */
    private String makeSafeForFile(final String id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("id is null");
        }

        final StringBuilder s = new StringBuilder();

        final char[] chars = id.toCharArray();
        for (final char c : chars)
        {
            if (ILLEGAL_CHARS.contains(c))
            {
                s.append("_");
            }
            else
            {
                s.append(c);
            }
        }

        return s.toString();
    }

    /**
    Internal class which wraps the Output so that
    debug may be dynamically enabled or disabled without any
    users of the Output having to be aware of it.
     */
    public final static class WrapOutput implements Output
    {
        private volatile Output mWrapped;

        private final File mFile;

        // protected by synchronized in checkStatus() and reset();
        private Output mFileOutput;

        // can be changed at any time
        private volatile boolean mDebug;

        private WrapOutput(final AMXDebug adebug, final File file, final boolean debug)
        {
            mDebug = debug;
            mWrapped = OutputIgnore.INSTANCE;
            mFile = file;
            mFileOutput = new FileOutput(file, adebug.mAppend);
            checkStatus();
        }

        public boolean getDebug()
        {
            return mDebug;
        }

        /**
        Change debug status.  If debug is <i>enabled</i> any
        subsequent debugging messages will be written to their outputs,
        creating files if necessary.
        If debug is <i>disabled</i>, all output to files ceases, and
        the files are closed.
         */
        public void setDebug(final boolean debug)
        {
            mDebug = debug;

            checkStatus();
        }

        @Override
        public void print(final Object o)
        {
            mWrapped.print(o);
        }

        @Override
        public void println(final Object o)
        {
            mWrapped.println(o);
        }

        @Override
        public void printError(final Object o)
        {
            mWrapped.printError(o);
        }

        @Override
        public void printDebug(final Object o)
        {
            mWrapped.printDebug(o);
        }

        public synchronized void reset()
        {
            // change output first!
            mWrapped = OutputIgnore.INSTANCE;

            // OK, now mFileOutput is not used
            mFileOutput.close();

            // the new one is lazy-opened...
            mFileOutput = new FileOutput(mFile);

            checkStatus();
        }

        @Override
        public void close()
        {
            reset();
        }

        /**
        Switch between FileOutput and OutputIgnore
        if there is a mismatch.
         */
        private synchronized void checkStatus()
        {
            if (getDebug())
            {
                mWrapped = mFileOutput;
            }
            else
            {
                mWrapped.println("turning DEBUG OFF");
                mWrapped = OutputIgnore.INSTANCE;
            }
        }

    }

    public static String methodString(final String name, final Object... args)
    {
        return DebugOutImpl.methodString(name, args);
    }

}












