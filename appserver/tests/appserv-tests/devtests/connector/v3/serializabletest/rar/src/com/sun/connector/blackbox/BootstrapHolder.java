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

package com.sun.connector.blackbox;

import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.XATerminator;
import jakarta.resource.spi.work.WorkManager;
import java.io.*;

public class BootstrapHolder {

    private BootstrapContext context;
    private XATerminator xat;
    private WorkManager wm;

    public BootstrapHolder(BootstrapContext context) {
        this.context = context;
        this.wm = (WorkManager) makeCopyOfObject(context.getWorkManager());
        checkEquality(wm, context.getWorkManager());
        this.xat = (XATerminator) makeCopyOfObject(context.getXATerminator());
    }

    private boolean checkEquality(Object obj_1, Object obj_2) {
        boolean equal = obj_1.equals(obj_2);
        if(equal){
            System.out.println("checkEquality : objects of "+obj_1.getClass().getName() +" , "+obj_2.getClass().getName()+" are equal ");
        }else{
            System.out.println("checkEquality : objects of "+obj_1.getClass().getName() +" , "+obj_2.getClass().getName()+" are not equal ");
        }
        return equal;
    }

    public Object makeCopyOfObject(Object obj) {
        if (obj instanceof Serializable) {
            try {
                // first serialize the object
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(obj);
                oos.flush();
                byte[] data = bos.toByteArray();
                oos.close();
                bos.close();

                // now deserialize it
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                ObjectInputStream ois = new ObjectInputStream(bis);


                return ois.readObject();
            } catch (Exception ex) {
                RuntimeException re =
                        new RuntimeException("Cant copy Serializable object of : " + obj.getClass().getName());
                re.initCause(ex);
                throw re;
            }
        } else {
            throw new RuntimeException("Cant copy Serializable object of  : " + obj.getClass().getName());
        }
    }
}
