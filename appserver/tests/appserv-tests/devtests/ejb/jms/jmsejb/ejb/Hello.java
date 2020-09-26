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

package com.sun.s1asdev.ejb.jms.jmsejb;

import jakarta.ejb.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.Serializable;

public interface Hello extends EJBObject {
    String sendMessage1(String msg) throws RemoteException;
    String sendMessage2(String msg) throws RemoteException;
    String sendMessage3(String msg) throws RemoteException;
    void receiveMessage1() throws RemoteException;
    void receiveMessage2() throws RemoteException;
    void receiveMessage3() throws RemoteException;
    String sendMessage4Part1(String msg) throws RemoteException;
    String sendMessage4Part2(String msg) throws RemoteException;
    void receiveMessage4Part1() throws RemoteException;
    void receiveMessage4Part2() throws RemoteException;
    void sendAndReceiveMessage() throws RemoteException;
    void sendAndReceiveRollback() throws RemoteException;
    String sendMessageRollback(String msg) throws RemoteException;
    void receiveMessageRollback() throws RemoteException;
}
