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
 * UuidUtilPadded.java
 *
 * Created on May 26, 2005, 11:15 AM
 */

package com.sun.enterprise.util.uuid;

/**
 *
 * @author  lwhite
 */
public class UuidUtilPadded extends UuidUtil {

    static final int DESIRED_UUID_LENGTH = 40;

    //this method can take in the session object
    //and insure better uniqueness guarantees
    //needed length must be greater than not less than
    //expected returned lengths - i.e. at least 40
    public static String generateUuid(Object obj, int inputLength) {
        int desiredLength =
            DESIRED_UUID_LENGTH >= inputLength ? DESIRED_UUID_LENGTH:inputLength;
        String unpaddedUuid = UuidUtil.generateUuid(obj);
        StringBuffer sb = new StringBuffer(unpaddedUuid);
        int neededPadding = desiredLength - unpaddedUuid.length();
        //int neededPadding = DESIRED_UUID_LENGTH - unpaddedUuid.length();
        if(neededPadding > 0) {
            for(int i=0; i<neededPadding; i++) {
                sb.append("F");
            }
        }
        return sb.toString();
    }

    /**
     * Method main
     *
     *
     * @param args
     *
     * @audience
     */
    public static void main(String[] args) {
        System.out.println(UuidUtilPadded.generateUuidMM());
        System.out.println(UuidUtilPadded.generateUuid());
        System.out.println(UuidUtilPadded.generateUuid(new Object()));
        System.out.println(UuidUtilPadded.generateUuid(new Object(), 40));
    }

}
