/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.webui.jsf.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This <code>InputStream</code> looks for lines beginning with "#include
 * '<em>filename</em>'" where filename is the name of a file to include. It
 * replaces the "#include" line with contents of the specified file. Any other
 * line beginning with '#' is illegal.
 */
public final class IncludeInputStream extends FilterInputStream {

    /**
     * EOL flag.
     */
    private boolean eol = true;

    /**
     * Redirected stream.
     */
    private IncludeInputStream redirStream = null;

    /**
     * Get real path parameter classes.
     */
    private static final Class[] GET_REAL_PATH_ARGS = new Class[]{
        String.class
    };

    /**
     * Constant for {@code include}/.
     */
    private static final String INCLUDE = "include";

    /**
     * Constant for {@code "include".length}.
     */
    private static final int INCLUDE_LEN = INCLUDE.length();

    /**
     * Faces context class.
     */
    private static final Class FACES_CONTEXT = getFacesContextClass();

    /**
     * Create a new instance.
     * @param input raw input stream
     */
    public IncludeInputStream(final InputStream input) {
        super(input);
    }

    /**
     * This overridden method implements the include feature.
     *
     * @return The next character.
     * @throws java.io.IOException if an IO error occurs
     */
    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public int read() throws IOException {
        int intChar;
        if (redirStream != null) {
            // We are already redirecting, delegate
            intChar = redirStream.read();
            if (intChar != -1) {
                return intChar;
            }

            // Found end of redirect file, stop delegating
            redirStream = null;
        }

        // Read next character
        intChar = super.read();
        char ch = (char) intChar;

        // If we were at the end of the line, check for new line w/ #
        if (eol) {
            // Check to see if we have a '#'
            if (ch == '#') {
                intChar = startInclude();
            } else {
                eol = false;
            }
        }

        // Flag EOL if we're at the end of a line
        if ((ch == 0x0A) || (ch == 0x0D)) {
            eol = true;
        }

        return intChar;
    }

    @Override
    public int available() throws IOException {
        return 0;
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public int read(final byte[] bytes, final int off, final int len)
            throws IOException {

        if (bytes == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > bytes.length) || (len < 0)
                || ((off + len) > bytes.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int c = read();
        if (c == -1) {
            return -1;
        }
        bytes[off] = (byte) c;

        int i = 1;
        try {
            for (; i < len; i++) {
                c = read();
                if (c == -1) {
                    break;
                }
                bytes[off + i] = (byte) c;
            }
        } catch (IOException ex) {
            LogUtil.severe(null, ex);
        }
        return i;
    }

    /**
     * Process start include.
     * @return the next character
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private int startInclude() throws IOException {
        // We have a line beginning w/ '#', verify we have "#include"
        char ch;
        for (int count = 0; count < INCLUDE_LEN; count++) {
            // look for include
            ch = (char) super.read();
            if (Character.toLowerCase(ch) != INCLUDE.charAt(count)) {
                throw new RuntimeException(
                        "\"#include\" expected in " + "IncludeInputStream.");
            }
        }

        // Skip whitespace...
        ch = (char) super.read();
        while ((ch == ' ') || (ch == '\t')) {
            ch = (char) super.read();
        }

        // Skip '"' or '\''
        if ((ch == '"') || (ch == '\'')) {
            ch = (char) super.read();
        }

        // Read the file name
        StringBuilder buf = new StringBuilder("");
        while ((ch != '"') && (ch != '\'')
                && (ch != 0x0A)
                && (ch != 0x0D) && (ch != -1)) {
            buf.append(ch);
            ch = (char) super.read();
        }

        // Skip ending '"' or '\'', if any
        if ((ch == '"') || (ch == '\'')) {
            ch = (char) super.read();
        }

        // Get the file name...
        String filename = buf.toString();

        // Determine if we're in a JSF environment...
        if (FACES_CONTEXT != null) {
            // We are... get a context root relative path...
            filename = convertRelativePath(filename);
        }
        File file = new File(filename);
        // Make sure file exists (don't check read, let it throw an exception)
        if (file.exists()) {
            // Open the included file
            redirStream = new IncludeInputStream(
                    new BufferedInputStream(new FileInputStream(file)));
        } else {
            // Check Classpath?
            InputStream stream = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream(filename);
            if (stream == null) {
                stream = Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream("/" + filename);
            }
            if (stream == null) {
                throw new FileNotFoundException(filename);
            }
            redirStream = new IncludeInputStream(
                    new BufferedInputStream(stream));
        }

        // Read the first character from the file to return
        return redirStream.read();
    }

    /**
     * <p>
     * This method converts a context-root relative path to the actual path
     * using the ServletContext or PortletContext. This requires the application
     * to be running in a Servlet or Portlet environment... and further requires
     * that it be running in JSF environment (which is used to access the
     * Servlet or Portlet Context).</p>
     *
     * @param filename The relative filename to convert to a full path.
     *
     * @return The full path based on the app's context root.
     */
    @SuppressWarnings("unchecked")
    protected String convertRelativePath(final String filename) {
        // NOTE: This method uses reflection to avoid build/runtime
        // NOTE: dependencies on JSF, this method is only used if the
        // NOTE: FacesContext class is found in the classpath.

        // Check for the file in docroot
        Method method;
        Object ctx;
        String newFilename = null;
        try {
            // The following should work w/ a ServletContext or PortletContext
            // Get the FacesContext...
            method = FACES_CONTEXT.getMethod(
                    "getCurrentInstance", (Class[]) null);
            ctx = method.invoke((Object) null, (Object[]) null);

            // Get the ExternalContext...
            method = ctx.getClass().getMethod(
                    "getExternalContext", (Class[]) null);
            ctx = method.invoke(ctx, (Object[]) null);

            // Get actual underlying external context...
            method = ctx.getClass().getMethod(
                    "getContext", (Class[]) null);
            ctx = method.invoke(ctx, (Object[]) null);

            // Get the real path using the ServletContext/PortletContext
            method = ctx.getClass().getMethod(
                    "getRealPath", GET_REAL_PATH_ARGS);
            newFilename = (String) method.invoke(ctx, new Object[]{filename});
            if (!(new File(newFilename)).exists()) {
                // The file doesn't exist, fall back to absolute path
                newFilename = filename;
            }
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
        return newFilename;
    }

    /**
     * Simple test case (requires a test file).
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        try {
            IncludeInputStream stream
                    = new IncludeInputStream(new FileInputStream(args[0]));
            int ch = '\n';
            while (ch != -1) {
                System.out.print((char) ch);
                ch = stream.read();
            }
        } catch (IOException ex) {
            LogUtil.severe(null, ex);
        }
    }

    /**
     * Get the {@code FacesContext} class.
     * @return Class or {@code null} if not in a JSF environment.
     */
    private static Class getFacesContextClass() {
        try {
            return Class.forName("jakarta.faces.context.FacesContext");
        } catch (ClassNotFoundException ex) {
            // Ignore, this just means we're not in a JSF environment
            return null;
        }
    }
}
