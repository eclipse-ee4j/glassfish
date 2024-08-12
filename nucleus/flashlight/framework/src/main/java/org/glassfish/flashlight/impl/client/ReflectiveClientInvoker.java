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

package org.glassfish.flashlight.impl.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

import org.glassfish.flashlight.FlashlightUtils;
import org.glassfish.flashlight.client.ProbeClientInvoker;
import org.glassfish.flashlight.impl.core.ComputedParamsHandlerManager;
import org.glassfish.flashlight.provider.FlashlightProbe;

public class ReflectiveClientInvoker
        implements ProbeClientInvoker {
    private int id;
    private Object target;
    private Method method;
    private String[] paramNames;
    boolean hasComputedParams;
    int[] probeIndices;
    boolean useProbeArgs;
    Class[] methodParamTypes;
    boolean emittedOneMessage = false;

    public ReflectiveClientInvoker(int id, Object target, Method method,
            String[] clientParamNames, FlashlightProbe probe) {
        this.id = id;
        this.target = target;
        this.method = method;
        this.paramNames = clientParamNames;
        methodParamTypes = method.getParameterTypes();
        int size = clientParamNames.length;
        probeIndices = new int[size];

        String[] probeParamNames = probe.getProbeParamNames();
        HashMap<String, Integer> probeParamIndexMap =
                new HashMap<String, Integer>();
        for (int index = 0; index < probeParamNames.length; index++) {
            probeParamIndexMap.put(probeParamNames[index], index);
        }

        for (int index = 0; index < size; index++) {
            if (clientParamNames[index].startsWith("$")) {
                hasComputedParams = true;
                probeIndices[index] = -1;
            }
            else {
                int actualIndex = probeParamIndexMap.get(clientParamNames[index]);
                probeIndices[index] = actualIndex;
            }
        }

        if (!hasComputedParams) {
            useProbeArgs = true;
            for (int index = 0; index < size; index++) {
                int probeParamIndex = probeParamIndexMap.get(paramNames[index]);
                if (index != probeParamIndex) {
                    useProbeArgs = false;
                    break;
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id=").append(id).append('\n');
        sb.append("target=").append(target).append('\n');
        sb.append("method=").append(method).append('\n');
        sb.append("paramNames=").append(Arrays.toString(paramNames)).append('\n');
        sb.append("probeIndices=").append(Arrays.toString(probeIndices)).append('\n');
        sb.append("useProbeArgs=").append(useProbeArgs).append('\n');
        sb.append("hasComputedParams=").append(hasComputedParams).append('\n');
        return sb.toString();
    }

    public int getId() {
        return id;
    }

    public void invoke(Object[] args) {
        if (!FlashlightUtils.isMonitoringEnabled())
            return;

        try {
            if (useProbeArgs) {
                //We can use the args as it is
            }
            else if (hasComputedParams) {
                ComputedParamsHandlerManager cphm = ComputedParamsHandlerManager.getInstance();
                int size = paramNames.length;
                Object[] tempArgs = args;
                args = new Object[size];
                for (int i = 0; i < size; i++) {
                    if (probeIndices[i] == -1) {
                        args[i] = cphm.computeValue(paramNames[i]);
                    }
                    else {
                        args[i] = tempArgs[probeIndices[i]];
                    }
                }
            }
            else {
                int size = paramNames.length;
                Object[] tempArgs = args;
                args = new Object[size];
                for (int i = 0; i < size; i++) {
                    args[i] = tempArgs[probeIndices[i]];
                }
            }

            if (method.isVarArgs())
                method.invoke(target, (Object) args);
            else
                methodInvoke(args);
        }
        catch (Exception ex) {
            if (!emittedOneMessage) {
                // Only do this one time!
                emittedOneMessage = true;
                StringBuilder sb = new StringBuilder();
                sb.append(getClass().getName()).append('\n').append(ex).append('\n');
                sb.append("CAUSE:  ").append(ex.getCause()).append('\n');
                sb.append(this);
                System.out.println(sb.toString());
            }
        }
    }

    private void methodInvoke(Object[] args) throws IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        try {
            method.invoke(target, args);
        }
        catch (Exception e1) {
            matchupArgs(args);
            method.invoke(target, args);
        }
    }

    private void matchupArgs(Object[] args) {
        // if any error -- just return quietly
        // if it can be fixed -- then change the contents of args

        if (args == null || args.length == 0 || methodParamTypes == null || methodParamTypes.length == 0)
            return;
        if (args.length != methodParamTypes.length)
            return;

        // it may look odd because I'm trying to be as efficient as possible...
        for (int i = 0; i < args.length; i++) {
            // weird.  Should not happen
            if (args[i] == null || methodParamTypes[i] == null)
                continue;

            Class argClass = args[i].getClass();

            // normal
            if (argClass.equals(methodParamTypes[i]))
                continue;

            // not an exact match.  Is it a sub-class?
            if (methodParamTypes[i].isAssignableFrom(argClass))
                continue;

            // is the only difference boxing, e.g. Short and short ??
            if (FlashlightUtils.compareIntegralOrFloat(argClass, methodParamTypes[i]))
                continue;

            // mismatch!!
            if (methodParamTypes[i].isAssignableFrom(String.class)) {
                args[i] = args[i].toString();
                if (!emittedOneMessage) {
                    System.out.println("FIXED MISMATCH!!!!\n" + toString());
                    emittedOneMessage = true;
                }
            }
            else {
                if (!emittedOneMessage) {
                    System.out.printf("ERROR!  Mismatched params  Expected " + methodParamTypes[i].toString()
                            + " but got " + args[i].getClass().toString() + "\n" + toString());
                    emittedOneMessage = true;
                }
            }
        }
    }
}
