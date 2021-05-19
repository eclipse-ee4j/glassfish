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

package com.sun.jts.utils;


import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.xa.Xid;

/**
 * This class is used to format the trace record.
 *
 * @author <a href="mailto:k.venugopal@sun.comi,kannan.srinivasan@sun.com">K Venugopal</a>
 * @version 1.0
 */
public class LogFormatter {

    /**
     * Helper method to convert a byte arror to string. This is typically used for printing Xids.
     *
     * @param byteArray a <code>byte[]</code> value
     * @return a <code>String</code> value
     */
    public static String convertToString(byte[] byteArray) {
        int i;
        StringBuffer strBuf = new StringBuffer();
        for (i = 0; i < byteArray.length; i++) {
            strBuf.append(byteArray[i]);
        }
        return strBuf.toString();
    }


    /**
     * Converts an array of xids to string that can be printed. Its a helper method.
     *
     * @param xidArray a <code>Xid[]</code> value
     * @return a <code>String</code> value
     */
    public static String convertXidArrayToString(Xid[] xidArray) {
        if (xidArray.length != 0) {
            int i;
            StringBuffer strBuf = new StringBuffer(
                "Xid class name is " + xidArray[0].getClass().getName()
                + " Number of Xids are " + xidArray.length + " [ ");
            for (i = 0; i < xidArray.length - 1; i++) {
                strBuf.append(xidArray[i]).append("\n");
            }
            strBuf.append(xidArray[xidArray.length - 1]).append(" ]");
            return strBuf.toString();
        } else {
            return " null ";
        }
    }


    /**
     * Helper method to convert properties to string.
     *
     * @param prop a <code>Properties</code> value
     * @return a <code>String</code> value
     */
    public static String convertPropsToString(Properties prop) {
        if (prop == null) {
            return "{null}";
        }
        StringBuffer strBuf = new StringBuffer("{ ");
        for (Enumeration enum1 = prop.propertyNames(); enum1.hasMoreElements();) {
            Object obj = enum1.nextElement();
            strBuf.append("[ ").append(obj).append("->");
            Object val = prop.getProperty((String) obj);
            if (val == null) {
                strBuf.append("null");
            } else {
                strBuf.append((String) val);
            }
            strBuf.append(" ] ");
        }
        strBuf.append("}");
        return strBuf.toString();
    }


    /**
     * getLocalizedMessage is used to localize the messages being used in
     * exceptions
     **/

    public static String getLocalizedMessage(Logger logger, String key) {
        try {
            ResourceBundle rb = logger.getResourceBundle();
            String message = rb.getString(key);
            return message;
        } catch (Exception ex) {
            logger.log(Level.FINE, "JTS:Error while localizing the log message");
            return key;
        }
    }


    /**
     * getLocalizedMessage is used to localize the messages being used in
     * exceptions with arguments inserted appropriately.
     **/

    public static String getLocalizedMessage(Logger logger, String key, Object[] args) {
        try {
            ResourceBundle rb = logger.getResourceBundle();
            String message = rb.getString(key);
            return MessageFormat.format(message, args);
        } catch (Exception ex) {
            logger.log(Level.FINE, "JTS:Error while localizing the log message");
            return key;
        }
    }
}
