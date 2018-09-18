/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package signatureejb;

import java.util.Date;

public interface SignatureTest extends java.rmi.Remote {

/*
    public void SetTestDate(java.util.Date testDate) throws java.rmi.RemoteException;
    public java.util.Date GetTestDate() throws java.rmi.RemoteException;
*/


/*
    public void setMyDateValueType(MyDateValueType myDate) throws java.rmi.RemoteException;
    public MyDateValueType getMyDateValueType() throws java.rmi.RemoteException;
    public void setMyDateValueTypes(MyDateValueType[] myDate) throws java.rmi.RemoteException;
    public MyDateValueType[] getMyDateValueTypes() throws java.rmi.RemoteException;
*/

    public String SayHello(String hello) throws java.rmi.RemoteException;
}
