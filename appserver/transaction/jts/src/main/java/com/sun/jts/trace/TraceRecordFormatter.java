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

package com.sun.jts.trace;

import java.util.Enumeration;
import java.util.Properties;

import javax.transaction.xa.Xid;

import org.omg.CosTransactions.otid_t;

/**
 * This class is used to format the trace record.
 *
 * @author <a href="mailto:kannan.srinivasan@sun.com">Kannan Srinivasan</a>
 * @version 1.0
 */
public class TraceRecordFormatter
{
  /**
   * Returns the formatted record, by accepting the simple string
   * message, tid and originator, which can be written to OutputStream
   * @param tid an <code>Object</code> value
   * @param origin an <code>Object</code> value
   * @param message a <code>String</code> value
   * @return a <code>String</code> value
   */
    public static String createTraceRecord(Object tid, Object origin, String message)
    {
        StringBuffer strBuf = new StringBuffer(TraceUtil.getTraceRecordTag());
        strBuf.append(TraceUtil.getCurrentTraceLevel())
              .append(TraceUtil.getFieldDelimiter());
        if(tid == null)
        {
            strBuf.append("<unknown-tid>");
        }
        else
        {
            if(tid instanceof String)
            {
                strBuf.append(tid);
            }
            else if(tid instanceof otid_t)
            {
                        strBuf.append(convertToString(((otid_t)tid).tid));
            }
        }
        strBuf.append(TraceUtil.getFieldDelimiter())
              .append(System.currentTimeMillis())
              .append(TraceUtil.getFieldDelimiter());
        if(origin == null)
        {
            strBuf.append("<unknown-origin>");
        }
        else
        {
            strBuf.append(origin);
        }
        strBuf.append(TraceUtil.getFieldDelimiter()).append(message).append("\n");
        return strBuf.toString();
    }

  /**
   * Returns the scheme used to format the record. This prints the different components (fields) of
   * the trace and record and their order of occurrence.
   *
   * @return a <code>String</code> value
   */
    public static String getTraceRecordScheme()
    {
        String traceRecordScheme = "<trace-record-tag><level><separator><omg-tid><separator><current-time-in-milliseconds><separator><originator><separator><message>";
        return traceRecordScheme;
    }

  /**
   * Helper method to convert a byte arror to string. This is typically used for printing Xids.
   *
   * @param byteArray a <code>byte[]</code> value
   * @return a <code>String</code> value
   */
    public static String convertToString(byte[] byteArray)
    {
        int i;
        StringBuffer strBuf=new StringBuffer();
        for(i = 0; i < byteArray.length; i++)
         {
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
    public static String convertXidArrayToString(Xid[] xidArray)
    {
    if(xidArray.length != 0)
    {
            int i;
            StringBuffer strBuf = new StringBuffer("[ ");
            for(i = 0; i < xidArray.length - 1; i++)
            {
                strBuf.append(convertToString(xidArray[i].getGlobalTransactionId()))
                        .append(", ");
            }
            strBuf.append(xidArray[xidArray.length - 1]).append(" ]");
            return strBuf.toString();
    }
    else
        return " null ";
    }

  /**
   * Helper method to convert properties to string.
   *
   * @param prop a <code>Properties</code> value
   * @return a <code>String</code> value
   */
    public static String convertPropsToString(Properties prop)
    {
        if(prop==null){
        return "{null}";
        }
        StringBuffer strBuf =  new StringBuffer("{ ");
        for(Enumeration e = prop.propertyNames(); e.hasMoreElements(); )
        {
            Object obj = e.nextElement();
            strBuf.append("[ ").append(obj).append("->");
            Object val=prop.getProperty((String)obj);
            if(val==null)
                strBuf.append("null");
            else
                strBuf.append((String)val);
            strBuf.append(" ] ");
        }
        strBuf.append("}");
        return strBuf.toString();
    }
}
