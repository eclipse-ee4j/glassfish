/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.layout.template;

import com.sun.jsftemplating.layout.SyntaxException;
import com.sun.jsftemplating.layout.descriptors.handler.OutputTypeManager;
import com.sun.jsftemplating.util.IncludeInputStream;
import com.sun.jsftemplating.util.LogUtil;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class TemplateParser {

    /**
     * <p>
     * Constructor.
     * </p>
     *
     * @param url <code>URL</code> pointing to the template.
     */
    public TemplateParser(URL url) {
        _url = url;
    }

    /**
     * <p>
     * Constructor which accepts a <code>InputStream</code>.
     * </p>
     *
     * @param stream <code>InputStream</code> for the template.
     */
    public TemplateParser(InputStream stream) {
        _inputStream = stream;
    }

    /**
     * <p>
     * Accessor for the URL.
     * </p>
     */
    public URL getURL() {
        return _url;
    }

    /**
     * <p>
     * Accessor for the <code>InputStream</code>. This either comes from the supplied <code>URL</code>, or simply from the
     * supplied <code>InputStream</code>.
     * </p>
     */
    public InputStream getInputStream() throws IOException {
        if (_inputStream == null && _url != null) {
            _inputStream = getURL().openStream();
        }
        return _inputStream;
    }

    /**
     * <p>
     * The init method opens the given <code>URL</code> pointing to a template and prepares to parses it.
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
        _reader = new BufferedReader(new InputStreamReader(new IncludeInputStream(new BufferedInputStream(getInputStream()))));

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
                LogUtil.config("Exception while closing stream for url: '" + getURL() + "'.", ex);
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
     * This method reads a "Name Value Pair" from the stream. For the purposes of this method, a "Name Value Pair" may look
     * like like one of these formats:
     * </p>
     *
     * <code>
     *    <ul><li>keyName="keyValue"</li>
     *        <li>keyName='keyValue'</li>
     *        <li>"keyValue" (only if defName is supplied)</li>
     *        <li>'keyValue' (only if defName is supplied)</li>
     *        <li>keyName=&gt;$attribute{attributeKey}</li>
     *        <li>keyName=&gt;$session{sessionKey}</li>
     *        <li>keyName=&gt;$page{pageSessionKey}</li>
     *        <li>keyName=&gt;$pageSession{pageSessionKey}</li></ul>
     *    </code>
     *
     * <p>
     * In the first two formats, <code>keyName</code> must consist of letters, numbers, or the underscore '_' character.
     * <code>keyValue</code> must be wrapped in single or double quotes. The backslash '\' character may be used to escape
     * characters, this may be useful if a backslash, single, or double quote exists in the string.
     * </p>
     *
     * <p>
     * The last four formats are only used for mapping return values. This is necessary when a handler returns a value so
     * that the value can be stored somewhere. <code>keyName</code> in these cases is the name of the return value to map.
     * The value after the dollar '$' character (which is either "attribute", "page", "pageSession", or "session") specifies
     * the type of storage the value should be saved. The value inside the curly braces "{}" specifies the key that should
     * be used when saving the value as a request, page, or session attribute.
     * </p>
     *
     * <p>
     * The return value is of type {@link NameValuePair}. This object contains the necessary information to interpret this
     * NVP.
     * </p>
     *
     * @param defName The default name to use if ommitted. If <code>null</code>, no default will be used -- a
     * {@link SyntaxException} will be generated.
     *
     * @return A {@link NameValuePair} object containing the NVP info.
     */
    public NameValuePair getNVP(String defName) throws IOException {
        return getNVP(defName, true);
    }

    /**
     * <p>
     * This method behaves the same as {@link #getNVP(String)}, however, it adds the ability to make quotes around the value
     * optional. This is done by passing in <code>false</code> for <code>requireQuotes</code>. This is used by some special
     * commands which only take a single argument with no property name. In this case, the value will be read until a '&gt;'
     * is encountered (if "/&gt;" is encountered, it will stop before the '/').
     * </p>
     *
     * <p>
     * Also, in cases where quotes are optional, output NVPs will not be allowed. The rationale is that the "=&gt;$...{...}"
     * syntax did not require quotes already, and use cases which allow for omitting quotes do not use output mappings.
     * </p>
     *
     * @param defName The default name to use if ommitted. If <code>null</code>, no default will be used -- a
     * {@link SyntaxException} will be generated.
     *
     * @param requireQuotes Flag indicating whether enforce the use of quotes or not.
     *
     * @return A {@link NameValuePair} object containing the NVP info.
     *
     * @throws SyntaxException if the syntax is not correct.
     */
    public NameValuePair getNVP(String defName, boolean requireQuotes) throws IOException {
        return getNVP(defName, requireQuotes, "_.");
    }

    /**
     * <p>
     * This method behaves the same as {@link #getNVP(String, boolean)}, however, it adds the ability to specify the valid
     * characters which may appear in the parameter name (via <code>otherChars</code>).
     * </p>
     *
     * @param defName The default name to use if ommitted. If <code>null</code>, no default will be used -- a
     * {@link SyntaxException} will be generated.
     *
     * @param requireQuotes Flag indicating whether enforce the use of quotes or not.
     *
     * @param otherChars Other valid characters.
     *
     * @return A {@link NameValuePair} object containing the NVP info.
     *
     * @throws SyntaxException if the syntax is not correct.
     */
    public NameValuePair getNVP(String defName, boolean requireQuotes, String otherChars) throws IOException {
        // Read the name
        String name = readToken(otherChars);
        Object value = null;

        // Check for empty name
        if (name.length() == 0 && defName != null) {
            name = defName; // Use default name
            unread('='); // Add '=' character
        }

        // Skip White Space
        skipCommentsAndWhiteSpace(SIMPLE_WHITE_SPACE);

        // Ensure next character is '='
        int next = nextChar();
        if (next != '=' && next != ':') {
            if (!requireQuotes && !name.equals(defName)) {
                // This is the case where there is no property name and no
                // quotes, the whole string is the value.
                value = name;
                name = defName;
                // Add a flag to ensure the next switch goes to the "default" case
                unread(next);
                unread('f');
            } else {
                throw new SyntaxException("'=' or ':' missing for Name Value Pair: '" + name + "'!");
            }
        }

        // Skip whitespace...
        skipCommentsAndWhiteSpace(SIMPLE_WHITE_SPACE);

        // Check for '>' character (means we're mapping an output value)
        String target = null;
        int endingChar = -1;
        next = nextChar();
        switch (next) {
        case '>':
            if (!requireQuotes) {
                // This means output mappings are not allowed, this must
                // be the end of the input (meaning there was no input
                // since we're at the beginning also)
                unread(next);
                value = "";
                break;
            }

            // We are mapping an output value, this should look like:
            // keyName => $attribute{attKey}
            // keyName => $application{appKey}
            // keyName => $session{sessionKey}
            // keyName => $pageSession{pageSessionKey}

            // First skip any whitespace after the '>'
            skipCommentsAndWhiteSpace(SIMPLE_WHITE_SPACE);

            // Next Make sure we have a '$' character
            next = nextChar();
            if (next != '$') {
                throw new SyntaxException("'$' missing for Name Value Pair named: '" + name + "=>'!  This NVP appears to be a mapping expression, "
                        + "therefor requires a format similar to:\n\t" + name + " => $attribute{attKey}\nor:\n\t" + name
                        + " => $application{applicationKey}\nor:\n\t" + name + " => $session{sessionKey}\nor:\n\t" + name + " => $pageSession{pageSessionKey}");
            }

            // Next look for valid type...
            target = readToken();
            OutputTypeManager otm = OutputTypeManager.getInstance();
            if (otm.getOutputType(null, target) == null) {
                throw new SyntaxException("Invalid OutputType ('" + target + "') for Name Value " + "Pair named: '" + name + "=>$" + target + "{...}'!  "
                        + "This NVP appears to be a mapping expression, " + "therefor requires a format similar to:\n\t" + name
                        + " => $attribute{attKey}\nor:\n\t" + name + " => $application{applicationKey}\nor:\n\t" + name + " => $session{sessionKey}\nor:\n\t"
                        + name + " => $pageSession{pageSessionKey}");
            }

            // Skip whitespace again...
            skipCommentsAndWhiteSpace(SIMPLE_WHITE_SPACE);

            // Now look for '{'
            next = nextChar();
            if (next != '{') {
                throw new SyntaxException("'{' missing for Name Value Pair: '" + name + "=>$" + target + "'!  The format must resemble the following:\n\t"
                        + name + " => $" + target + "{key}");
            }
            endingChar = '}';
            break;
        case '{':
            // NVP w/ a List as its value
            value = parseList('}');
            break;
        case '[':
            // NVP w/ an array as its value
            value = parseList(']').toArray();
            break;
        case '"':
        case '\'':
            // Regular NVP...
            // Set the ending character to the same type of quote
            endingChar = next;
            break;
        case 'f':
            if (value != null && value.toString().length() > 0) {
                // We have the case where the whole string is the value
                // Get the next character so we can fall through w/ it
                // to the default case
                next = nextChar();
            }
            // Don't break here, fall through...
        default:
            // See if we require quotes around the value...
            if (!requireQuotes) {
                unread(next); // Include "next" when getting the value
                // Read the value until '>'
                String strVal = readUntil('>', true);

                // Unread the '>'
                unread('>');

                // See if we also need put back a '/'...
                if (strVal.endsWith("/")) {
                    // Remove the '/' and place back in the read buffer
                    strVal = strVal.substring(0, strVal.length() - 1).trim();
                    unread('/');
                }
                value = value == null ? strVal : value.toString() + strVal;
                break;
            }

            // This isn't legal, throw an exception
            throw new SyntaxException("Name Value Pair named '" + name + "' is missing single or double quotes enclosing "
                    + "its value.  It must follow one of these formats:\n\t" + name + "=\"value\"\nor:\n\t" + name + "='value'");
        }

        // Read the value
        if (endingChar != -1) {
            value = readUntil(endingChar, false);
        }

        // Create the NVP object and return it
        return new NameValuePair(name, value, target);
    }

    /**
     * <p>
     * This method processes lists of String values in the format:
     * </p>
     *
     * <p>
     * <code>"value1", "value2", ...}</code>
     * </p>
     *
     * <p>
     * The content inside the Strings can be anything. The double quotes can also be single quotes. The separators can be
     * spaces, tabs, new lines, commas, semi-colons, or colons. The terminating character is whatever is passed in for
     * <code>endChar</code> (shown as '}' above).
     * </p>
     */
    protected List<String> parseList(int endChar) throws IOException {
        List<String> list = new ArrayList<>();
        skipCommentsAndWhiteSpace(SIMPLE_WHITE_SPACE);
        int next = nextChar();
        while (next != endChar) {
            // We should start w/ a single or double quote
            if (next != '\'' && next != '"') {
                throw new IllegalArgumentException("A List or array is missing a single or double quotes " + "enclosing one or more of its values.  It must "
                        + "follow:\n\tname={\"value\", ...}\nor:\n\tname={'value'," + "...}\n\n[]'s may be used in place of {}'s to specify "
                        + "an array instead of a List.");
            }

            // Read everything inside the quotes
            list.add(readUntil(next, false));

            // Skip white space (including the seperators ",:;");
            skipCommentsAndWhiteSpace(SIMPLE_WHITE_SPACE + ",:;");
            next = nextChar();
        }
        return list;
    }

    /**
     * <p>
     * This method reads while the stream contains letters, numbers, the colon character ':', a dot '.', or the underscore
     * '_' character, and returns the result.
     * </p>
     */
    public String readToken() throws IOException {
        return readToken("_:.");
    }

    /**
     * <p>
     * This method reads while the stream contains letters or numbers and returns the result.
     * </p>
     *
     * <p>
     * It also allows any charcters specified by <code>otherChars</code> to be considered as part of the token. This allows
     * tokens with additional valid characters to be read. <code>otherChars</code> may be null if no additional chars are
     * valid.
     * </p>
     *
     * @param otherChars Other valid characters.
     */
    public String readToken(String otherChars) throws IOException {
        if (otherChars == null) {
            otherChars = "";
        }

        StringBuffer buf = new StringBuffer();
        int next = nextChar();
        while (Character.isLetterOrDigit(next) || otherChars.indexOf(next) != -1) {
            buf.append((char) next);
            next = nextChar();
        }
        unread(next);

        // Return the result
        return buf.toString();
    }

    /**
     * <p>
     * This method returns a <code>String</code> of characters from the current position in the file until the given
     * character (or end of file) is encountered. It will not leave the given character in the buffer, so the next character
     * to be read will be the character following the given character.
     * </p>
     *
     * @param skipComments <code>true</code> to strip comments.
     */
    public String readUntil(int endingChar, boolean skipComments) throws IOException {
        if (skipComments) {
            // In case we start on a comment and should skip it...
            skipCommentsAndWhiteSpace("");
        }
        int tmpch;
        int next = nextChar();
        StringBuffer buf = new StringBuffer();
        while (next != endingChar && next != -1) {
            switch (next) {
            case '\'':
            case '\"':
                if (skipComments && next != endingChar) {
                    // In this case, we want to make sure no comments are
                    // skipped when inside a quote
                    //
                    // NOTE: Also means endingChar will not be found in
                    // a quote.
                    buf.append((char) next);
                    buf.append(readUntil(next, false));
                    buf.append((char) next);
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
        if (endingStr == null || endingStr.length() == 0) {
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
            buf.append(arr[0]); // readUntil reads but doesn't return this char

            // Check to see if we are at the end
            for (idx = 1; idx < arrlen; idx++) {
                ch = nextChar();
                if (ch != arr[idx]) {
                    // This is not the end!
                    break;
                }
            }
        } while (ch != -1 && idx < arrlen);

        // Append the remaining characters (use idx in case we hit eof)...
        for (int cnt = 1; cnt < idx; cnt++) {
            buf.append(arr[cnt]);
        }

        if (arrlen != idx) {
            // Didn't find it!
            throw new SyntaxException("Unable to find: '" + endingStr + "'.  Read to EOF and gave up.  Read: \n" + buf.toString());
        }

        // Return the result
        return buf.toString();
    }

    /**
     * <p>
     * This method skips the given String of characters (usually used to skip white space. The contents of the String that
     * is skipped is lost. Often you may wish to skip comments as well, use
     * {@link TemplateParser#skipCommentsAndWhiteSpace(String)} in this case.
     * </p>
     *
     * @param skipChars The white space characters to skip.
     *
     * @see TemplateParser#skipCommentsAndWhiteSpace(String)
     */
    public void skipWhiteSpace(String skipChars) throws IOException {
        int next = nextChar();
        while (next != -1 && skipChars.indexOf(next) != -1) {
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
     * @see TemplateParser#skipWhiteSpace(String)
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
                        } else {
                            // Not a comment, probably a mistake... lets
                            // throw an exception
                            unread(ch);
                            unread('-');
                            unread('!');
                            unread('<');
                            throw new IllegalArgumentException("Invalid " + "comment!  Expected comment to begin " + "with \"<!--\", but found: " + readLine());
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
            if (ch == '\r' || ch == '\n') {
                // We hit the EOL...
                // Check to see if there are 2...
                if (!_stack.empty()) {
                    ch = _stack.peek().charValue();
                    if (ch == '\r' || ch == '\n') {
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
        if (lastChar >= 0 && buf.charAt(lastChar) == '\\') {
            buf.deleteCharAt(lastChar);
            buf.append(readLine());
        }

        return buf.toString();
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

    private URL _url = null;
    private InputStream _inputStream = null;
    private transient BufferedReader _reader = null;
    private transient Stack<Character> _stack = null;
}
