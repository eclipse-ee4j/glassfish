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
 * UuidUtil.java
 *
 * Created on October 15, 2002, 9:39 AM
 */

package com.sun.enterprise.util.uuid;

import java.net.InetAddress;
import java.rmi.server.UID;
import java.security.SecureRandom;

/**
 * Class UuidUtil
 *
 *
 */
public class UuidUtil
{

    static final String _inetAddr = initInetAddr();

    //first method (from MarketMaker Guid)
    public static String generateUuidMM() {
        return new StringBuffer(new UID().toString()).reverse().append(':').append(_inetAddr).toString();
    }

    //second method
    public static String generateUuid() {
        return generateUuid(new Object());
    }

    //this method can take in the session object
    //and insure better uniqueness guarantees
    public static String generateUuid(Object obj) {

        //low order time bits
        long presentTime = System.currentTimeMillis();
        int presentTimeLow = (int) presentTime;
        String presentTimeStringLow = formatHexString(presentTimeLow);

        StringBuilder sb = new StringBuilder(50);
        sb.append(presentTimeStringLow);
        //sb.append(":");
        sb.append(getIdentityHashCode(obj));
        //sb.append(":");
        //sb.append(_inetAddr);
        sb.append(addRandomTo(_inetAddr));
        //sb.append(":");
        sb.append(getNextRandomString());
        return sb.toString();
    }

    /**
     * Method initInetAddr
     *
     *
     * @return
     *
     * @audience
     */
    private static String initInetAddr() {

        try {
            byte[] bytes = InetAddress.getLocalHost().getAddress();
            StringBuilder b = new StringBuilder();

            for (int i = 0; i < bytes.length; i++) {
                String s = Integer.toHexString(bytes[i]);

                if (bytes[i] < 0) {
                    b.append(s.substring(s.length() - 2));
                } else {
                    b.append(s);
                }
            }

            return b.toString();
        } catch (Exception ex) {
            //must return a value
            return "a48eb993";
            //return null;
        }
    }

    private static String addRandomTo(String hexString)
    {
        long hexAsLong = convertToLong(hexString);
        int nextRandom = getNextInt();
        long resultInt = hexAsLong + nextRandom;
        String result = Long.toHexString(resultInt);
        // START PWC 6425338
        // Always return a length of 7
        int len = result.length();
        if (len < 7) {
            result = padto7(result);
        } else {
            result = result.substring(len - 7, len);
        }
        //  END PWC 6425338
        return result;
    }

    /**
     * Method getIdentityHashCode
     *
     *
     * @return
     *
     * @audience
     */
    private static String getIdentityHashCode(Object obj) {

        try {
            int hc = System.identityHashCode(obj);
            return formatHexString(hc);

        } catch (Exception ex) {
            //must return a value
            //return null;
            return "8AF5182";
        }
    }

    private static String formatHexString(int inputInt)
    {
        String result;
        String s = Integer.toHexString(inputInt);
        /* PWC 6425338
        if(s.length() < 8)
        {
            result = s;
        } else {
            result = s.substring(0, 7);
        }
        */
        // START PWC 6425338
        // Always return a length of 7
        int len = s.length();
        if (len < 7) {
            result = padto7(s);
        } else {
            result = s.substring(len - 7, len);
        }
        //  END PWC 6425338
        return result;
    }

    private static synchronized int getNextInt() {
        return _seeder.nextInt();
    }

    private static String getNextRandomString() {
        int nextInt = getNextInt();
        return formatHexString(nextInt);
    }

    private static long convertToLong(String hexString)
    {
        long result = 0;
        try
        {
            result = (Long.valueOf(hexString, 16)).longValue();
        } catch (NumberFormatException ex) {
        }
        return result;
    }

    private static SecureRandom _seeder = new SecureRandom();

    /**
     * Method main
     *
     *
     * @param args
     *
     * @audience
     */
    public static void main(String[] args) {
        System.out.println(UuidUtil.generateUuidMM());
        System.out.println(UuidUtil.generateUuid());
        System.out.println(UuidUtil.generateUuid(new Object()));
    }
    // START PWC 6425338
    /*
    * Pads the given string to a length of 7.
    */
   private static String padto7(String s) {

       int i = 0;
       char[] chars = new char[7];
       int len = s.length();
       while (i < len) {
           chars[i] = s.charAt(i);
           i++;
       }
       while (i < 7) {
           chars[i++] = '0';
       }
       return new String(chars);
   }
    // END PWC 6425338
}



