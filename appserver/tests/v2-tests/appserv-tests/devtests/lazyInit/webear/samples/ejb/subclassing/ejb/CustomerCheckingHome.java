/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package samples.ejb.subclassing.ejb;

import java.rmi.RemoteException;

public interface CustomerCheckingHome extends CustomerHome {
    public CustomerChecking create(String SSN, String lastName, String firstName, String address1, String address2, String city, String state, String zipCode) throws RemoteException,jakarta.ejb.CreateException;

    public CustomerChecking findByPrimaryKey(String SSN) throws RemoteException,jakarta.ejb.FinderException;
}

