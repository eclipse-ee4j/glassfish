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

package com.sun.enterprise.admin.util;

import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Iterator;

class IllegalEscapeSequenceException extends TokenizerException {

    public IllegalEscapeSequenceException(String msg) {
        super(msg);
    }
}

class UnterminatedLiteralStringException extends TokenizerException {

    public UnterminatedLiteralStringException(String msg) {
        super(msg);
    }
}

class MalformedUnicodeSequenceException extends IllegalEscapeSequenceException {

    public MalformedUnicodeSequenceException(String msg) {
        super(msg);
    }
}

/**
 */
public final class TokenizerImpl implements Tokenizer {

    final String[] mTokens;

    public TokenizerImpl(String input, String delimiters, char escapeChar, String escapableChars) throws TokenizerException {
        this(input, delimiters, true, escapeChar, escapableChars);
    }

    public TokenizerImpl(String input, String delimiters, boolean multipleDelimsCountAsOne, char escapeChar, String escapableChars)
            throws TokenizerException {
        final TokenizerInternal worker = new TokenizerInternal(input, delimiters, escapeChar, escapableChars);

        ArrayList allTokens = worker.parseTokens();

        if (multipleDelimsCountAsOne) {
            allTokens = removeMultipleDelims(allTokens);
        }

        mTokens = interpretTokenList(allTokens);
    }

    static ArrayList removeMultipleDelims(ArrayList list) {
        final ArrayList resultList = new ArrayList();

        boolean lastWasDelim = false;
        final Iterator iter = list.iterator();
        while (iter.hasNext()) {
            final Object value = iter.next();

            if (value instanceof String) {
                resultList.add(value);
                lastWasDelim = false;
            } else if (!lastWasDelim) {
                // add the delimiter
                resultList.add(value);
                lastWasDelim = true;
            }
        }

        return (resultList);
    }

    /**
     * Interpret the parsed token list, which consists of a series of strings and tokens. We need to handle the special
     * cases where the list starts with a delimiter and/or ends with a delimiter. Examples:
     *
     * "" => {} "." => { "", "" } "..." => { "", "", "", "" } "x." => { "x", "" } ".x" => { "", "x" } "y.x" => { "y", "x" }
     */
    static String[] interpretTokenList(ArrayList list) {
        final ArrayList resultList = new ArrayList();

        boolean lastWasDelim = true;

        final Iterator iter = list.iterator();
        while (iter.hasNext()) {
            final Object value = iter.next();
            if (value instanceof String) {
                resultList.add(value);
                lastWasDelim = false;
            } else {
                if (lastWasDelim) {
                    // this one's a delimiter, and so was the last one
                    // insert the implicit empty string
                    resultList.add("");
                } else {
                    lastWasDelim = true;
                }
            }
        }

        // a trailing delimiter implies an empty string after it
        if (lastWasDelim && !list.isEmpty()) {
            resultList.add("");
        }

        return ((String[]) resultList.toArray(new String[resultList.size()]));
    }

    @Override
    public String[] getTokens() {
        return (mTokens);
    }
}

final class TokenizerInternal {

    final String mDelimiters;
    final char mEscapeChar;
    final String mEscapableChars;
    final StringCharacterIterator mIter;

    // a distinct object used to denote a delimiter
    private static class Delim {

        private Delim() {
        }

        public static Delim getInstance() {
            return (new Delim());
        }

        @Override
        public String toString() {
            return ("<DELIM>");
        }
    }

    final static Delim DELIM = Delim.getInstance();

    public TokenizerInternal(String input, String delimiters, char escapeChar, String escapableChars) {
        mDelimiters = delimiters;
        mEscapeChar = escapeChar;
        mEscapableChars = escapableChars;
        mIter = new StringCharacterIterator(input);
    }

    static boolean isSpecialEscapeChar(char theChar) {
        // carriage return or newline
        return (theChar == 'n' || theChar == 'r' || theChar == 't' || theChar == QUOTE_CHAR);
    }

    boolean isCallerProvidedEscapableChar(char theChar) {
        return (mEscapableChars.indexOf(theChar) >= 0 || theChar == mEscapeChar);
    }

    boolean isEscapableChar(char theChar) {
        return (isCallerProvidedEscapableChar(theChar) || isSpecialEscapeChar(theChar));
    }

    boolean isDelim(String delims, char theChar) {
        return (delims.indexOf(theChar) >= 0 || theChar == StringCharacterIterator.DONE);
    }

    static boolean isDigit(char theChar) {
        return ((theChar >= '0' && theChar <= '9'));
    }

    static boolean isHexDigit(char theChar) {
        return (isDigit(theChar) || (theChar >= 'a' && theChar <= 'f') || isUpper(theChar));
    }

    static boolean isUpper(char c) {
        return ((c >= 'A' && c <= 'F'));
    }

    boolean hasMoreChars() {
        return (mIter.current() != StringCharacterIterator.DONE);
    }

    char nextChar() {
        final char theChar = mIter.current();
        mIter.next();

        return (theChar);
    }

    private static final char QUOTE_CHAR = '\"';

    char decodeUnicodeSequence() throws MalformedUnicodeSequenceException {
        int value = 0;

        try {
            for (int i = 0; i < 4; ++i) {
                value = (value << 4) | hexValue(nextChar());
            }
        } catch (Exception e) {
            throw new MalformedUnicodeSequenceException("");
        }

        return ((char) value);
    }

    static int hexValue(char c) {
        if (!isHexDigit(c)) {
            throw new IllegalArgumentException();
        }

        int value;

        if (isDigit(c)) {
            value = (int) c - (int) '0';
        } else if (isUpper(c)) {
            value = (int) c - (int) 'A';
        } else {
            value = (int) c - (int) 'a';
        }
        return value;
    }

    char getEscapedChar(final char inputChar) throws MalformedUnicodeSequenceException, IllegalEscapeSequenceException {
        char outChar;

        if (isCallerProvidedEscapableChar(inputChar)) {
            outChar = inputChar;
        } else {
            switch (inputChar) {
            default:
                throw new IllegalEscapeSequenceException("" + inputChar);
            case 'n':
                outChar = '\n';
                break;
            case 'r':
                outChar = '\r';
                break;
            case 't':
                outChar = '\t';
                break;
            case QUOTE_CHAR:
                outChar = QUOTE_CHAR;
                break;
            case 'u':
                outChar = decodeUnicodeSequence();
                break;
            }
        }

        return (outChar);
    }

    ArrayList parseTokens() throws UnterminatedLiteralStringException, MalformedUnicodeSequenceException, IllegalEscapeSequenceException {
        final StringBuffer tok = new StringBuffer();
        final ArrayList tokens = new ArrayList();
        boolean insideStringLiteral = false;

        /**
         * Escape sequences are always processed regardless of whether we're inside a quoted string or not. A quote string
         * really only alters whether delimiters are treated as literal characters, or not.
         */
        while (hasMoreChars()) {
            final char theChar = nextChar();

            if (theChar == mEscapeChar) {
                tok.append(getEscapedChar(nextChar()));
            } else if (theChar == Tokenizer.LITERAL_STRING_DELIM) {
                // special cases of "", """", """""", etc require forcing an empty string out
                // these case have no delimiter or regular characters to cause a string to
                // be emitted
                if (insideStringLiteral && tok.length() == 0 && tokens.isEmpty()) {
                    tokens.add("");
                }

                insideStringLiteral = !insideStringLiteral;
            } else if (insideStringLiteral) {
                tok.append(theChar);
            } else if (isDelim(mDelimiters, theChar)) {
                // we've hit a delimiter...if characters have accumulated, spit them out
                // then spit out the delimiter token.
                if (tok.length() != 0) {
                    tokens.add(tok.toString());
                    tok.setLength(0);
                }
                tokens.add(DELIM);
            } else {
                tok.append(theChar);
            }
        }

        if (tok.length() != 0) {
            tokens.add(tok.toString());
        }

        if (insideStringLiteral) {
            throw new UnterminatedLiteralStringException(tok.toString());
        }

        return (tokens);
    }
}
