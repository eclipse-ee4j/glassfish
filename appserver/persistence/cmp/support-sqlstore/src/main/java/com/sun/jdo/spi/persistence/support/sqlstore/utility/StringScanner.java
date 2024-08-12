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
 * StringScanner.java
 *
 * Created on March 3, 2000
 */

package com.sun.jdo.spi.persistence.support.sqlstore.utility;

import java.util.Vector;

/**
 * Helper class for breaking up a string.
 * <P>
 *<P>
 */
//
public class StringScanner
{
    private static String           intStrTable[]           = new String[2048];

    static int skipWhite(String s, int offset)
    {
        int    end = s.length();

        if (offset < 0)
            return end;
        while (offset < end && Character.isWhitespace(s.charAt(offset)))
            offset++;
        return offset;
    }

    static int skipTo(String s, int offset, String delim)
    {
        int end = s.length();

        if (offset < 0)
            return end;
        while (offset < end && delim.indexOf(s.charAt(offset)) == -1)
            offset++;
        return offset;
    }

    static int skipTo(String s, int offset, char delim)
    {
        int end = s.length();

        if (offset < 0)
            return end;
        while (offset < end && delim != s.charAt(offset))
            offset++;
        return offset;
    }

    static char charAt(String s, int offset)
    {
        if (offset < 0 || offset >= s.length())
            return '\0';
        return s.charAt(offset);
    }

    static int skipInt(String s, int offset)
    {
        int end = s.length();

        if (offset < 0 || !Character.isDigit(s.charAt(offset)))
            return end;
        //
        // Unlike the others, this can return the offset past the end
        // of the string.  The idea is that the caller wants to
        // do a substring to get the integer.
        //
        while (offset < end && Character.isDigit(s.charAt(offset)))
            offset++;
        return offset;
    }

    public static String format(StringBuffer buf, String fmt, Object o1)
    {
        return format(buf, fmt, 1, o1, null, null, null, null, null, null,
                null, null);
    }

    public static String format(StringBuffer buf, String fmt,
                        Object o1,
                        Object o2)
    {
        return format(buf, fmt, 2, o1, o2, null, null, null, null, null,
                null, null);
    }

    public static String format(StringBuffer buf, String fmt,
                         Object o1,
                         Object o2,
                         Object o3)
    {
        return format(buf, fmt, 3, o1, o2, o3, null, null, null, null,
                    null, null);
    }

    public static String format(StringBuffer buf, String fmt,
                         Object o1,
                         Object o2,
                         Object o3,
                         Object o4)
    {
        return format(buf, fmt, 4, o1, o2, o3, o4, null, null, null,
                    null, null);
    }

    public static String format(StringBuffer buf, String fmt,
                         Object o1,
                         Object o2,
                         Object o3,
                         Object o4,
                         Object o5)
    {
        return format(buf, fmt, 5, o1, o2, o3, o4, o5, null, null, null, null);
    }

    public static String format(StringBuffer buf, String fmt,
                         Object o1,
                         Object o2,
                         Object o3,
                         Object o4,
                         Object o5,
                         Object o6)
    {
        return format(buf, fmt, 6, o1, o2, o3, o4, o5, o6, null, null, null);
    }

    public static String format(StringBuffer buf, String fmt,
                         Object o1,
                         Object o2,
                         Object o3,
                         Object o4,
                         Object o5,
                         Object o6,
                         Object o7)
    {
        return format(buf, fmt, 7, o1, o2, o3, o4, o5, o6, o7, null, null);
    }

    public static String format(StringBuffer buf, String fmt,
                         Object o1,
                         Object o2,
                         Object o3,
                         Object o4,
                         Object o5,
                         Object o6,
                         Object o7,
                         Object o8)
    {
        return format(buf, fmt, 8, o1, o2, o3, o4, o5, o6, o7, o8, null);
    }

    public static String format(StringBuffer buf, String fmt,
                         Object o1,
                         Object o2,
                         Object o3,
                         Object o4,
                         Object o5,
                         Object o6,
                         Object o7,
                         Object o8,
                         Object o9)
    {
        return format(buf, fmt, 9, o1, o2, o3, o4, o5, o6, o7, o8, o9);
    }

    public static String format(StringBuffer buf, String fmt,
                         int argcnt,
                         Object o1,
                         Object o2,
                         Object o3,
                         Object o4,
                         Object o5,
                         Object o6,
                         Object o7,
                         Object o8,
                         Object o9)
    {
        Object[] params = {o1, o2, o3, o4, o5, o6, o7, o8, o9};

        int i = 0;
        int begSubstr = 0;
        int percent;
        int nextParam = 0;
        StringBuffer msg;
        int fmtLen;

        if (buf == null)
            msg = new StringBuffer();
        else
            msg = buf;

        fmtLen = fmt.length();

        while ((percent = fmt.indexOf('%', i)) >= 0 && percent < (fmtLen - 1))
        {
            char     c = fmt.charAt(percent + 1);
            boolean leftJustify = false;
            boolean raw = false;
            int     nextChar = percent;

            if (c == '-')
            {
                nextChar++;
                leftJustify = true;
                if (nextChar + 1 < fmtLen)
                    c = fmt.charAt(nextChar + 1);
            }

            if (Character.isDigit(c))
            {
                int endInt = StringScanner.skipInt(fmt, nextChar + 1);
                int size = Integer.parseInt(fmt.substring(nextChar + 1,endInt));

                if (size == 0)
                    raw = true;

                msg.append(fmt.substring(begSubstr, percent));
                if (nextParam < argcnt)
                {
                    Object    p = params[nextParam++];
                    String    val = p.toString();
                    int        len = val.length();

                    if (!raw)
                    {
                        if (!leftJustify && len < size)
                        {
                            for (int j = 0; j < size - len; j++)
                                msg.append(" "); // NOI18N
                        }
                        else if (len > size)
                        {
                            val = val.substring(0, size);
                        }
                    }
                    msg.append(val);
                    if (leftJustify && len < size)
                    {
                        for (int j = 0; j < size - len; j++)
                            msg.append(" "); // NOI18N
                    }
                }
                else
                {
                    /*                    throw (StringIndexOutOfBoundsException)
                        ErrorManager.createFormatAdd(
                            java.lang.StringIndexOutOfBoundsException.class,
                            ErrorManager.USER,
                            UtilMsgCat.SP_ERR_FMT_OUT_OF_RANGE,
                            (new Integer(nextParam)),
                            (new Integer(1)), (new Integer(argcnt)), fmt);
                    */
                }
                i = endInt;
                begSubstr = i;
            }
            else
            {
                i = nextChar + 1;
            }
        }
        msg.append(fmt.substring(begSubstr, fmtLen));
        return msg.toString();
    }

    static String createParamString(String message,
                                    int argcnt,
                                    Object param1,
                                    Object param2,
                                    Object param3,
                                    Object param4,
                                    Object param5,
                                    Object param6,
                                    Object param7,
                                    Object param8,
                                    Object param9)
    {
        if (argcnt < 1 || argcnt > 9)
            return message;

        Object[] p = null;
        if (argcnt == 1)
        {
            Object[] params = {param1};
            p = params;
        }
        else if (argcnt == 2)
        {
            Object[] params = {param1, param2};
            p = params;
        }
        else if (argcnt == 3)
        {
            Object[] params = {param1, param2, param3};
            p = params;
        }
        else if (argcnt == 4)
        {
            Object[] params = {param1, param2, param3, param4};
            p = params;
        }
        else if (argcnt == 5)
        {
            Object[] params = {param1, param2, param3, param4, param5};
            p = params;
        }
        else if (argcnt == 6)
        {
            Object[] params = {param1, param2, param3, param4, param5,
                                   param6};
            p = params;
        }
        else if (argcnt == 7)
        {
            Object[] params = {param1, param2, param3, param4, param5,
                                   param6, param7};
            p = params;
        }
        else if (argcnt == 8)
        {
            Object[] params = {param1, param2, param3, param4, param5,
                                   param6, param7, param8};
            p = params;
        }
        else if (argcnt == 9)
        {
            Object[] params = {param1, param2, param3, param4, param5,
                                   param6, param7, param8, param9};
            p = params;
        }

        //
        // Decide if we should use our own formatting algorithm or
        // call Java's MessageFormat.format().  Our own algorithm is
        // about 20 times faster but cannot deal with templates.
        // So, use our own if no template is involved. i.e. only
        // positional parameters.
        // e.g. {0} is OK but {0,ddd-mmm-yyyy} is not.
        //
        boolean useJava = false;
        int lbrace, rbrace = 0;
        int i = 0;

        // look for the start of next positional parameter
        while ((lbrace = message.indexOf('{', i)) >= 0)
        {
            // if the parameter doesn't end with a right brace, get
            // out and use the Java formatter. Or if there are more
            // than 1 character between the braces (e.g. {0,ddd-mmm-yyyy})
            if ((rbrace = message.indexOf('}', lbrace + 1)) < 0 ||
                (rbrace - lbrace) > 2)
            {
                useJava = true;
                break;
            }

            i = rbrace + 1;
        }

        if (useJava)
        {
            return java.text.MessageFormat.format(message, p);
        }

        // Since we are using our own algorithm, we need to get rid of
        // the pesty single quote escape character
        int esc;
        boolean foundEsc = false;
        StringBuffer msg = new StringBuffer();

        i = 0;
        while ((esc = message.indexOf('\'', i)) >= 0)
        {
            msg.append(message.substring(i, esc));
            msg.append(message.substring(esc + 1, esc + 2));
            i = esc + 2;
            foundEsc = true;
        }

        if (foundEsc)
        {
            msg.append(message.substring(i, message.length()));
            message = msg.toString();
        }

        char c;
        i = 0;
        int msglen = message.length();
        msg = new StringBuffer();
        while (   (lbrace = message.indexOf('{', i)) >= 0
               && lbrace < msglen-1)
        {
            c = message.charAt(lbrace + 1);

            if (Character.isDigit(c))
            {
                int pnum = c - '0';

                if (0 <= pnum && pnum < argcnt)
                {
                    Object pp = p[pnum];

                    msg.append(message.substring(i, lbrace));

                    if (pp == null)
                    {
                        msg.append("<null>"); // NOI18N
                    }
                    else
                    {
                        msg.append(pp.toString());
                    }
                }
                else
                {
                    if (argcnt > 0)
                    {
                        /*                        throw (StringIndexOutOfBoundsException)
                            ErrorManager.createFormatAdd(
                                java.lang.StringIndexOutOfBoundsException.class,
                                ErrorManager.USER,
                                UtilMsgCat.SP_ERR_FMT_OUT_OF_RANGE,
                                (new Integer(pnum)),
                                (new Integer(1)), (new Integer(argcnt)), msg);
                        */
                    }
                    else
                    {
                        /*                        throw (StringIndexOutOfBoundsException)
                            ErrorManager.createFormatAdd(
                                java.lang.StringIndexOutOfBoundsException.class,
                                ErrorManager.USER,
                                UtilMsgCat.SP_ERR_FMT_MISSING_PARAM,
                                (new Integer(pnum)), msg);
                        */
                    }
                }
                i = lbrace + 3;
            }
            else
            {
                i = lbrace + 1;
            }
        }
        msg.append(message.substring(i, msglen));
        return msg.toString();
    }

    /**
     * Return a string containing a "level" number of spaces.
     * <P>
     *     Used for formatted print, this will return a string containing 4
     *  spaces for each specified level.
     * @return The requested string.
     * @param level The level to fill to.
     */
    //
    // CHANGES
    //     6-aug-1997
    //         Created (jak)
    //
    //
    public static String levelString(int level)
    {
        int        i;
        String    str;

        if (level <= 0)
            return ""; // NOI18N

        switch (level)
        {
        case 1:
            return "    "; // NOI18N
        case 2:
            return "        "; // NOI18N
        case 3:
            return "            "; // NOI18N
        case 4:
            return "                "; // NOI18N
        case 5:
            return "                    "; // NOI18N
        case 6:
            return "                       "; // NOI18N
        default:
            str = new String("                       "); // NOI18N
            for (i = 6; i < level; i++)
                str = str.concat("    "); // NOI18N
            return str;
        }
    }

    /**
     * Split a string into sub-strings based on a given delimeter.
     * <P>
     *  Split str based on the delimeter and return as an array of Strings.
     * @return An array of sub-strings
     * @param delimeter The string representing the delimeters.
     * @param str The string to process.
     */
    //
    // CHANGES
    //     12-jun-1997
    //         Created (jak)
    //     6-aug-1997
    //         Moved from CommandShell (jak)
    //
    //
    public static Vector splitString(String str, String delimeter)
    {
        int        mark;
        int        start;
        int        delLen;
        Vector    list;

        list = new Vector();

        if (str != null)
        {
            mark = 0;
            start = 0;

            delLen = delimeter.length();
            while ((start = str.indexOf(delimeter, mark)) != -1)
            {
                if (start != 0)
                    list.addElement((Object) str.substring(mark, start));
                mark = start + delLen;
            }

            // Put in the last bit.
            if (mark < str.length())
                list.addElement((Object) str.substring(mark));
          }
        return list;
    }

    /**
     * Fills a string with blanks to a given size.
     * <P>
     *  Left or right fills the given string with spaces (' ').
     * @return A new filled string.
     * false.
     * @param left true when the text should be left justified, otherwise
     * @param len The desired length of the formatted string.
     * @param str The string to process.
     */
    //
    // CHANGES
    //     19-aug-1997
    //         Created (jak)
    //
    //
    public static String fill(String str, int len, boolean left)
    {
        String    nstr;
        int        startLen;
        int        diff;

        if (str == null)
            str = "null"; // NOI18N

        startLen = str.length();
        nstr = str;
        while (startLen < len)
        {
            diff = len - startLen;
            if (diff >= 10)
            {
                startLen += 10;
                if (left)
                    nstr = nstr + "          "; // NOI18N
                else
                    nstr = "          " + nstr; // NOI18N
                continue;
            }
            else if (diff >= 8)
            {
                startLen += 8;
                if (left)
                    nstr = nstr + "        "; // NOI18N
                else
                    nstr = "        " + nstr; // NOI18N
                continue;
            }
            else if (diff >= 4)
            {
                startLen += 4;
                if (left)
                    nstr = nstr + "    "; // NOI18N
                else
                    nstr = "    " + nstr; // NOI18N
                continue;
            }
            else if (diff >= 2)
            {
                startLen += 2;
                if (left)
                    nstr = nstr + "  "; // NOI18N
                else
                    nstr = "  " + nstr; // NOI18N
                continue;
            }
            else
            {
                startLen++;
                if (left)
                    nstr = nstr + " "; // NOI18N
                else
                    nstr = " " + nstr; // NOI18N
                continue;
            }
        }
        return nstr;
    }

    public static String createParamString(String fmt)
    {
        return fmt;
    }
    public static String createParamString(String fmt, Object obj1)
    {
        return StringScanner.createParamString(fmt, 1, obj1, null, null, null,
                                null, null, null, null, null);
    }
    public static String createParamString(String fmt, Object obj1, Object obj2)
    {
        return StringScanner.createParamString(fmt, 2, obj1, obj2, null, null,
                                null, null, null, null, null);
    }
    public static String createParamString(String fmt, Object obj1, Object obj2,
                                           Object obj3)
    {
        return StringScanner.createParamString(fmt, 3, obj1, obj2, obj3, null,
                                null, null, null, null, null);
    }
    public static String createParamString(String fmt, Object obj1, Object obj2,
                                           Object obj3, Object obj4)
    {
        return StringScanner.createParamString(fmt, 4, obj1, obj2, obj3, obj4,
                                null, null, null, null, null);
    }
    public static String createParamString(String fmt, Object obj1, Object obj2,
                                           Object obj3, Object obj4,
                                           Object obj5)
    {
        return StringScanner.createParamString(fmt, 5, obj1, obj2, obj3, obj4,
                                obj5, null, null, null, null);
    }
    public static String createParamString(String fmt, Object obj1, Object obj2,
                                           Object obj3, Object obj4,
                                           Object obj5, Object obj6)
    {
        return StringScanner.createParamString(fmt, 6, obj1, obj2, obj3, obj4,
                                obj5, obj6, null, null, null);
    }
    public static String createParamString(String fmt, Object obj1, Object obj2,
                                           Object obj3, Object obj4,
                                           Object obj5, Object obj6,
                                           Object obj7)
    {
        return StringScanner.createParamString(fmt, 7, obj1, obj2, obj3, obj4,
                                obj5, obj6, obj7, null, null);
    }
    public static String createParamString(String fmt, Object obj1, Object obj2,
                                           Object obj3, Object obj4,
                                           Object obj5, Object obj6,
                                           Object obj7, Object obj8)
    {
        return StringScanner.createParamString(fmt, 8, obj1, obj2, obj3, obj4,
                                obj5, obj6, obj7, obj8, null);
    }
    public static String createParamString(String fmt, Object obj1, Object obj2,
                                           Object obj3, Object obj4,
                                           Object obj5, Object obj6,
                                           Object obj7, Object obj8,
                                           Object obj9)
    {
        return StringScanner.createParamString(fmt, 9, obj1, obj2, obj3, obj4,
                                obj5, obj6, obj7, obj8, obj9);
    }


    public static String getIntStr(int num)
    {
        String    str;
        boolean    big;

        try
        {
            str = (String) StringScanner.intStrTable[num];
            big = false;
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            str = null;
            big = true;
        }

        if (str == null)
        {
            str = Integer.toString(num);
            if (!big)
                StringScanner.intStrTable[num] = str;
        }
        return str;
    }
}
