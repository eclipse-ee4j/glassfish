/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
 * Portions Copyright (c) 2011 Ken Paulsen
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

package com.sun.jsft.facelets;

import com.sun.jsft.util.LogUtil;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Stack;

/**
 * <p>
 * This class is responsible for the actual parsing of a template.
 * </p>
 *
 * <p>
 * This class is intended to read the template one time. Often it may be useful to cache the result as it would be
 * inefficient to reread a template multiple times. Templates that are generated from this class are intended to be
 * static and safe to share. However, this class itself is not thread safe.
 * </p>
 *
 * @author Ken Paulsen (kenapaulsen@gmail.com)
 */
public class CommandParser {

    /**
     * <p>
     * Constructor which accepts a <code>InputStream</code>.
     * </p>
     *
     * @param stream <code>InputStream</code> for the template.
     */
    public CommandParser(InputStream stream) {
        _inputStream = stream;
    }

    /**
     * <p>
     * Accessor for the <code>InputStream</code>. This comes from the supplied <code>InputStream</code>.
     * </p>
     */
    public InputStream getInputStream() throws IOException {
        return _inputStream;
    }

    /**
     * <p>
     * Creates the Reader Object.
     * </p>
     *
     * @throws IOException
     */
    public void open() throws IOException {
        if (_reader != null) {
            // Generally this should not happen, but just in case... start over
            close();
        }

// FIXME: It is possible while evaluating the file an #include may need to log a message to the screen!  Provide a callback mechanism to do this in a Template-specific way
        // Create the reader from the stream
        _reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(getInputStream())));

        // Initialize the queue we will use to push values back
        _stack = new Stack<>();
    }

    /**
     * <p>
     * This method closes the stream if it is open. It doesn't throw an exception, instead it logs any exceptions at the
     * CONFIG level.
     * </p>
     */
    public void close() {
        try {
            if (_reader != null) {
                _reader.close();
            }
        } catch (Exception ex) {
            if (LogUtil.configEnabled(this)) {
                LogUtil.config("Exception while closing stream.", ex);
            }
        }
    }

    /**
     * <p>
     * This method returns the next character.
     * </p>
     */
    public int nextChar() throws IOException {
        if (!_stack.empty()) {
            // We have values in the queue
            return _stack.pop().charValue();
        }
        return _reader.read();
    }

    /**
     * <p>
     * This method pushes a character on the read queue so that it will be read next.
     * </p>
     */
    public void unread(int ch) {
        _stack.push(new Character((char) ch));
    }

    /**
     * <p>
     * This method returns a <code>String</code> of characters from the current position in the file until the given
     * character (or end of file) is encountered. It will leave the given character in the buffer, so the next character to
     * be read will be the <code>endingChar</code> or -1.
     * </p>
     *
     * @param endingChar The character to read up to, but not including
     * @param skipComments <code>true</code> to strip comments.
     */
    public String readUntil(int endingChar, boolean skipComments) throws IOException {
        return readUntil(new int[] { endingChar }, skipComments);
    }

    /**
     *
     */
    public String readUntil(int[] endingChars, boolean skipComments) throws IOException {
        if (skipComments) {
            // In case we start on a comment and should skip it...
            skipCommentsAndWhiteSpace("");
        }
        int tmpch;
        int next = nextChar();
        StringBuffer buf = new StringBuffer();
        while (!isInArray(endingChars, next) && (next != -1)) {
            switch (next) {
                case '\'':
                case '\"':
                    if (skipComments) {
                        // In this case, we want to make sure no comments are
                        // skipped when inside a quote
                        //
                        // NOTE: Also means endingChars will not be found in
                        // a quote.
                        buf.append((char) next);
                        buf.append(readUntil(next, false));
                        buf.append((char) next);
                        nextChar(); // Throw away the last char read...
                    } else {
                        buf.append((char) next);
                    }
                    break;
                case '#':
                case '/':
                case '<':
                    // When reading we want to ignore comments, don't skip
                    // whitespace, though...
                    if (skipComments) {
                        unread(next);
                        skipCommentsAndWhiteSpace("");
                        // If same char, read next to prevent infinite loop
                        // We don't have to go through switch again b/c its
                        // not the ending char and its not escaped -- so it is
                        // safe to add.
                        tmpch = nextChar();
                        if (next == tmpch) {
                            buf.append((char) next);
                        } else {
                            // We're somewhere different, unread
                            unread(tmpch);
                        }
                    } else {
                        buf.append((char) next);
                    }
                    break;
                case '\\':
                    // Escape Character...
                    next = nextChar();
                    if (next == 'n') {
                        // Special case, insert a '\n' character.
                        buf.append('\n');
                    } else if (next == 't') {
                        // Special case, insert a '\t' character.
                        buf.append('\t');
                    } else if (next != '\n') {
                        // add the next char unless it's a return char
                        buf.append((char) next);
                    }
                    break;
                default:
                    buf.append((char) next);
                    break;
            }
            next = nextChar();
        }
        if (next != -1) {
            unread(next);
        }

        // Return the result
        return buf.toString();
    }

    /**
     * <p>
     * This method returns a <code>String</code> of characters from the current position in the file until the given String
     * (or end of file) is encountered. It will not leave the given String in the buffer, so the next character to be read
     * will be the character following the given character.
     * </p>
     *
     * @param endingStr The terminating <code>String</code>.
     * @param skipComments <code>true</code> to ignore comments.
     */
    public String readUntil(String endingStr, boolean skipComments) throws IOException {
        // Sanity Check
        if ((endingStr == null) || (endingStr.length() == 0)) {
            return "";
        }

        // Break String into characters
        char arr[] = endingStr.toCharArray();
        int arrlen = arr.length;

        StringBuffer buf = new StringBuffer("");
        int ch = nextChar(); // Read a char to unread
        int idx = 1;
        do {
            // We didn't find the end, push read values back on queue
            unread(ch);
            for (int cnt = idx - 1; cnt > 0; cnt--) {
                unread(arr[cnt]);
            }

            // Read until the beginning of the end (maybe)
            buf.append(readUntil(arr[0], skipComments));
            // buf.append(arr[0]); // readUntil reads but doesn't return this char

            // Check to see if we are at the end
            for (idx = 1; idx < arrlen; idx++) {
                ch = nextChar();
                if (ch != arr[idx]) {
                    // This is not the end!
                    break;
                }
            }
        } while ((ch != -1) && (idx < arrlen));

        // Append the remaining characters (use idx in case we hit eof)...
        for (int cnt = 1; cnt < idx; cnt++) {
            buf.append(arr[cnt]);
        }

        if (arrlen != idx) {
            // Didn't find it!
            throw new IllegalStateException("Unable to find: '" + endingStr + "'.  Read to EOF and gave up.  Read: \n" + buf.toString());
        }

        // Return the result
        return buf.toString();
    }

    /**
     * <p>
     * This method skips the given String of characters (usually used to skip white space. The contents of the String that
     * is skipped is lost. Often you may wish to skip comments as well, use
     * {@link CommandParser#skipCommentsAndWhiteSpace(String)} in this case.
     * </p>
     *
     * @param skipChars The white space characters to skip.
     *
     * @see CommandParser#skipCommentsAndWhiteSpace(String)
     */
    public void skipWhiteSpace(String skipChars) throws IOException {
        int next = nextChar();
        while ((next != -1) && (skipChars.indexOf(next) != -1)) {
            // Skip...
            next = nextChar();
        }

        // This will skip one too many
        unread(next);
    }

    /**
     * <p>
     * Normally you don't just want to skip white space, you also want to skip comments. This method allows you to do that.
     * It skips comments of the following types:
     * </p>
     *
     * <code>
     *        <ul><li>//        -   Comment extends to the rest of the line.</li>
     *        <li>#        -   Comment extends to the rest of the line.</li>
     *        <li>/*        -   Comment extends until closing '*' and '/'.</li>
     *        <li>&lt;!-- -   Comment extends until closing --&gt;.</li></ul>
     *    </code>
     *
     * @param skipChars The white space characters to skip
     *
     * @see CommandParser#skipWhiteSpace(String)
     */
    public void skipCommentsAndWhiteSpace(String skipChars) throws IOException {
        int ch = 0;
        while (ch != -1) {
            ch = nextChar();
            switch (ch) {
                case '#':
                    // Skip rest of line
                    readLine();
                    break;
                case '/':
                    ch = nextChar();
                    if (ch == '/') {
                        // Skip rest of line
                        readLine();
                    } else if (ch == '*') {
                        // Throw away everything until '*' & '/'.
                        readUntil("*/", false);
                        nextChar(); // Throw away the last char read...
                    } else {
                        // Not a comment, don't read
                        unread(ch);
                        unread('/');
                        ch = -1; // Exit loop
                    }
                    break;
                case '<':
                    ch = nextChar(); // !
                    if (ch == '!') {
                        ch = nextChar(); // -
                        if (ch == '-') {
                            ch = nextChar(); // -
                            if (ch == '-') {
                                // Ignore HTML-style comment
                                readUntil("-->", false);
                                nextChar(); // Throw away the last char read...
                            } else {
                                // Not a comment, probably a mistake... lets
                                // throw an exception
                                unread(ch);
                                unread('-');
                                unread('!');
                                unread('<');
                                throw new IllegalArgumentException(
                                        "Invalid " + "comment!  Expected comment to begin " + "with \"<!--\", but found: " + readLine());
                            }
                        } else {
                            // Not a comment, probably an event.. back out
                            unread(ch);
                            unread('!');
                            unread('<');
                            ch = -1; // Cause loop to end
                        }
                    } else {
                        // '!' not found, not a comment... we shouldn't be here
                        // skipping this back out
                        unread(ch);
                        unread('<');
                        ch = -1; // Cause loop to end
                    }
                    break;
                case -1: // Ignore this case
                    break;
                default:
                    // See if this is white space...
                    if (skipChars.indexOf(ch) == -1) {
                        // Nope... we're done skipping (undo last read)
                        unread(ch);
                        ch = -1; // Exit loop
                    }
                    break;
            }
        }
    }

    /**
     * <p>
     * This method reads the rest of the line. This can be used to read entire lines (obviously), or as a means of skipping
     * the remainder of a line (i.e. to ignore line comments).
     * </p>
     */
    public String readLine() throws IOException {
        StringBuffer buf = new StringBuffer();
        int ch = -1;
        while (!_stack.empty()) {
            // We have values in the queue
            ch = _stack.pop().charValue();
            if ((ch == '\r') || (ch == '\n')) {
                // We hit the EOL...
                // Check to see if there are 2...
                if (!_stack.empty()) {
                    ch = _stack.peek().charValue();
                    if ((ch == '\r') || (ch == '\n')) {
                        // Remove this one too...
                        _stack.pop().charValue();
                    }
                }
                return buf.toString();
            }
            buf.append((char) ch);
        }

        // Read the rest of the line
        buf.append(_reader.readLine());

        int idx = buf.indexOf("\\n");
        while (idx != -1) {
            // Replace '\\n' with '\n'
            buf.replace(idx, idx + 2, "\n");
            idx = buf.indexOf("\\n", idx + 1);
        }

        // Check to see if '\' character is at eol, if so read next line too
        int lastChar = buf.length() - 1;
        if ((lastChar >= 0) && (buf.charAt(lastChar) == '\\')) {
            buf.deleteCharAt(lastChar);
            buf.append(readLine());
        }

        return buf.toString();
    }

    /**
     * <p>
     * Simple check to see if the given <code>val</code> exists in <code>arr</code>.
     * </p>
     */
    private boolean isInArray(int[] arr, int val) {
        int len = arr.length;
        for (int idx = 0; idx < len; idx++) {
            if (arr[idx] == val) {
                return true;
            }
        }
        return false;
    }

    //////////////////////////////////////////////////////////////////////
    // Constants
    //////////////////////////////////////////////////////////////////////

    /**
     * <p>
     * This String constant defines the characters that are interpretted to be basic white space characters. The value of
     * this is:
     * </p>
     *
     * <p>
     * <ul>
     * <li><code>" \t\r\n"</code></li>
     * </ul>
     * </p>
     */
    public static final String SIMPLE_WHITE_SPACE = " \t\r\n";

    private InputStream _inputStream = null;
    private transient BufferedReader _reader = null;
    private transient Stack<Character> _stack = null;
}
