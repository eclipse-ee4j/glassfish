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

package com.sun.enterprise.backup;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 *
 * @author  bnevins
 */
class StringHelper
{
    private StringHelper()
    {
    }

    /**
     * @return the String from LocalStrings or the supplied String if it doesn't exist
     */

    static String get(String s)
    {
        try
        {
            return bundle.getString(s);
        }
        catch (Exception e)
        {
            // it is not an error to have no key...
            return s;
        }
    }

    /**
     * Convenience method which calls get(String, Object[])
     * @return the String from LocalStrings or the supplied String if it doesn't exist --
     * using the one supplied argument
     * @see get(String, Object[])
     */
    static String get(String s, Object o)
    {
        return get(s, new Object[] { o });
    }

    /**
     * Convenience method which calls get(String, Object[])
     * @return the String from LocalStrings or the supplied String if it doesn't exist --
     * using the two supplied arguments
     * @see get(String, Object[])
     */
    static String get(String s, Object o1, Object o2)
    {
        return get(s, new Object[] { o1, o2 });
    }

    /**
     * Convenience method which calls get(String, Object[])
     * @return the String from LocalStrings or the supplied String if it doesn't exist --
     * using the three supplied arguments
     * @see get(String, Object[])
     */
    static String get(String s, Object o1, Object o2, Object o3)
    {
        return get(s, new Object[] { o1, o2, o3 });
    }

    /**
     * Get and format a String from LocalStrings.properties
     * @return the String from LocalStrings or the supplied String if it doesn't exist --
     * using the array of supplied Object arguments
     */
    static String get(String s, Object[] objects)
    {
        s = get(s);

        try
        {
            MessageFormat mf = new MessageFormat(s);
            return mf.format(objects);
        }
        catch(Exception e)
        {
            return s;
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    private static ResourceBundle bundle;

    static
    {
        try
        {
            String props = StringHelper.class.getPackage().getName() + ".LocalStrings";
            bundle = ResourceBundle.getBundle(props);
        }
        catch (Exception e)
        {
            LoggerHelper.warning("No resource bundle found: " + Constants.exceptionResourceBundle, e);
            bundle = null;
        }
    }

    static void main(String[] notUsed)
    {
        System.out.println("key=backup-res.BadProjectBackupDir, value =" + get("backup-res.BadProjectBackupDir"));
    }
}


