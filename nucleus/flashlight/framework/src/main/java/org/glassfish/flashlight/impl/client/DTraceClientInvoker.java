/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.impl.client;

import java.lang.reflect.Method;
import java.util.logging.*;
import org.glassfish.flashlight.FlashlightUtils;
import org.glassfish.flashlight.client.ProbeClientInvoker;
import org.glassfish.flashlight.provider.FlashlightProbe;
import org.glassfish.flashlight.FlashlightLoggerInfo;

/**
 * bnevins Aug 15, 2009
 * DTraceClientInvoker is only public because an internal class is using it from a
 * different package.  If this were C++ we would have a "friend" relationship between
 * the 2 classes.  In Java we are stuck with making it public.
 * Notes:
 * DTrace has a fairly serious limitation.  It only allows parameters that are
 * integral primitives (all primitives except float and double) and java.lang.String
 * So what we do is automagically convert other class objects to String via Object.toString()
 * This brings in a new rub with overloaded methods.  E.g. we can't tell apart these 2 methods:
 * foo(Date) and foo(String)
 *
 * TODO:I believe we should disallow such overloads when the DTrace object is being produced rather than
 * worrying about it in this class.
 *
 * @author bnevins
 */

public class DTraceClientInvoker implements ProbeClientInvoker{
    private static final Logger logger = FlashlightLoggerInfo.getLogger();

    public DTraceClientInvoker(int ID, FlashlightProbe p) {
        id          = ID;
        method      = p.getDTraceMethod();
        targetObj   = p.getDTraceProviderImpl();
    }

    public void invoke(Object[] args) {
        if(FlashlightUtils.isDtraceAvailable()) {
            try {
                method.invoke(targetObj, fixArgs(args));
            }
            catch(Exception e) {
                logger.log(Level.WARNING, FlashlightLoggerInfo.DTRACE_UNEXPECTED_EXCEPTION, e.getMessage());
            }
        }
    }

    public int getId() {
        return id;
    }

    private Object[] fixArgs(Object[] args) {
        // Logic:  DTrace only allows integral primitives and java.lang.String.
        // convert anything else to String.
        // be careful to not unbox!!
        // Very important to send back a COPY -- other listeners are sharing these args!!

        Object[] fixedArgs = new Object[args.length];

        for(int i = 0; i < args.length; i++) {
            if(args[i] == null) {
                fixedArgs[i] = null;
                continue;
            }

            Class clazz = args[i].getClass();

            if(!FlashlightUtils.isLegalDtraceParam(clazz))
                fixedArgs[i] = args[i].toString();
            else
                fixedArgs[i] = args[i];
        }

        return fixedArgs;
    }

    private final   int             id;
    private final   Method          method;
    private final   Object          targetObj;
}

