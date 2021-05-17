/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.ejb30.clientview.adapted;

import jakarta.ejb.FinderException;

// Remote business interface

public interface Hello
{
    void warmup(int type);
    void shutdown();

    float notSupported(int type, boolean tx);
    float required(int type, boolean tx);
    float requiresNew(int type, boolean tx);
    float mandatory(int type, boolean tx);
    float never(int type, boolean tx);
    float supports(int type, boolean tx);

    boolean hasBeenPassivatedActivated();


    boolean checkSlessLocalReferences();

    boolean checkSfulLocalReferences();

    public boolean checkSlessRemoteReferences();

    public boolean checkSfulRemoteReferences();

    public DummyRemote getSfulRemoteBusiness(int num);

    public DummyRemote2 getSfulRemoteBusiness2(int num);

    public boolean compareRemoteRefs(Object ref1, Object ref2);

}
