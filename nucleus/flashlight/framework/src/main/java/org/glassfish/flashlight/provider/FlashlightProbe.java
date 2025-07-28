/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.flashlight.provider;

import com.sun.enterprise.util.LocalStringManagerImpl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.monitoring.ProbeInfo;
import org.glassfish.flashlight.FlashlightLoggerInfo;
import org.glassfish.flashlight.client.ProbeClientInvoker;
import org.glassfish.flashlight.client.ProbeHandle;
import org.glassfish.flashlight.client.StatefulProbeClientInvoker;

public class FlashlightProbe
        implements ProbeHandle, ProbeInfo{

    public FlashlightProbe(int id, Class providerClazz, String moduleProviderName,
            String moduleName, String probeProviderName, String probeName,
            String[] probeParamNames, Class[] paramTypes, boolean self, boolean hidden,
            boolean stateful, boolean statefulReturn, boolean statefulException,
            String [] profileNames) {
        this.id = id;
        this.providerClazz = providerClazz;
        this.moduleProviderName = moduleProviderName;
        this.moduleName = moduleName;
        this.probeProviderName = probeProviderName;
        this.probeName = probeName;
        this.probeDesc = moduleProviderName + ":" + moduleName + ":" +
                probeProviderName + ":" + probeName;
        this.hasSelf = self;
        this.hidden = hidden;
        this.stateful = stateful;
        this.statefulReturn = statefulReturn;
        this.statefulException = statefulException;
        this.profileNames = profileNames;

        if (self) {
            if (isMethodStatic()) {
                String errStr = localStrings.getLocalString("cannotDefineSelfOnStatic", "Cannot define \"self\" on a static method - ", probeDesc);
                throw new RuntimeException(errStr);
            }
            // Fill in the first slot of ParamNames with @Self and paramTypes with the providerClass type
            this.probeParamNames = new String[probeParamNames.length+1];
            this.paramTypes = new Class[paramTypes.length+1];
            this.probeParamNames[0] = SELF;
            this.paramTypes[0] = providerClazz;
            for (int index = 0; index < probeParamNames.length; index++) {
                this.probeParamNames[index+1] = probeParamNames[index];
                this.paramTypes[index+1] = paramTypes[index];
            }
        } else {
            this.probeParamNames = probeParamNames;
            this.paramTypes = paramTypes;
        }

    }

    public Method getProbeMethod() {
        return probeMethod;
    }

    public void setProbeMethod(Method probeMethod) {
        this.probeMethod = probeMethod;
    }

    private boolean isMethodStatic() {
        try {
            int modifier = getProviderClazz().getDeclaredMethod(getProviderJavaMethodName(),
                                                                    getParamTypes()).getModifiers();
            return Modifier.isStatic(modifier);
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized void addInvoker(ProbeClientInvoker invoker) {
        if(invokers.putIfAbsent(invoker.getId(), invoker) != null) {
            if (logger.isLoggable(Level.FINE))
                logger.fine("Adding an invoker that already exists: " + invoker.getId() +  "  &&&&&&&&&&");
        }
        else {
            if (logger.isLoggable(Level.FINE))
                logger.fine("Adding an Invoker that does not exist: " + invoker.getId() +   " $$$$$$$$$$$$$");
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Total invokers = " + invokers.size());
        }
        listenerEnabled.set(true);

        initInvokerList();
    }

    public synchronized boolean removeInvoker(ProbeClientInvoker invoker) {
        ProbeClientInvoker pci = invokers.remove(invoker.getId());

        if(pci != null) {
            if (logger.isLoggable(Level.FINE))
                logger.fine("Removing an invoker that already exists: " + pci.getId() +  "  ##########");
        }
        else {
            if (logger.isLoggable(Level.FINE))
                logger.fine("Failed to remove an invoker that does not exist: " + invoker.getId() +  "  %%%%%%%%%");
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Total invokers = " + invokers.size());
        }

        listenerEnabled.set(!invokers.isEmpty());

        initInvokerList();
        return listenerEnabled.get();
    }

    public void fireProbe(Object[] params) {
        if(!listenerEnabled.get()) {
            return;
        }

        if (parent != null) {
            parent.fireProbe(params);
        }

        int sz = invokerList.size();

        for (int i=0; i<sz; i++) {
            ProbeClientInvoker invoker = invokerList.get(i);
            if(invoker != null) {
                invoker.invoke(params);
            }
        }
    }

    public ArrayList<ProbeInvokeState> fireProbeBefore(Object[] params) {
        if(!listenerEnabled.get()) {
            return null;
        }

        ArrayList<ProbeInvokeState> probeInvokeStates = new ArrayList<ProbeInvokeState> ();

        if (parent != null) {
            ArrayList <ProbeInvokeState> parentStates = parent.fireProbeBefore(params);
            probeInvokeStates.addAll(parentStates);
        }

        int sz = invokerList.size();

        for (int i=0; i<sz; i++) {
            StatefulProbeClientInvoker invoker = (StatefulProbeClientInvoker) invokerList.get(i);
            if(invoker != null) {
                probeInvokeStates.add(new ProbeInvokeState(invoker.getId(),
                                               invoker.invokeBefore(params)));
            }
        }

        return probeInvokeStates;
    }

    public void fireProbeAfter(Object returnValue, ArrayList<ProbeInvokeState> states) {
        if(!listenerEnabled.get()) {
            return;
        }

        if (parent != null) {
            parent.fireProbeAfter(returnValue, states);
        }

        int sz = invokerList.size();

        int stateIndex = -1;
        for (int i=0; i<sz; i++) {
            StatefulProbeClientInvoker invoker = (StatefulProbeClientInvoker)invokerList.get(i);
            if(invoker != null) {
                stateIndex = findStateIndex(invoker.getId(), states);
                if (stateIndex >= 0)
                    invoker.invokeAfter(states.get(stateIndex).getState(), returnValue);
            }
        }
    }

    public void fireProbeOnException(Object exceptionValue, ArrayList<ProbeInvokeState> states) {
        if(!listenerEnabled.get()) {
            return;
        }

        if (parent != null) {
            parent.fireProbeOnException(exceptionValue, states);
        }

        int sz = invokerList.size();

        int stateIndex = -1;
        for (int i=0; i<sz; i++) {
            StatefulProbeClientInvoker invoker = (StatefulProbeClientInvoker)invokerList.get(i);
            if(invoker != null) {
                stateIndex = findStateIndex(invoker.getId(), states);
                if (stateIndex >= 0)
                    invoker.invokeOnException(states.get(stateIndex).getState(), exceptionValue);
            }
        }
    }

    public boolean isEnabled() {
        return listenerEnabled.get();
    }

    public int getId() {
        return id;
    }

    public String getModuleProviderName() {
                return moduleProviderName;
        }

    public String getModuleName() {
        return moduleProviderName;
    }

        public String getProbeProviderName() {
        return probeProviderName;
    }

    public String getProbeName() {
        return probeName;
    }

    public String[] getProbeParamNames() {
        return probeParamNames;
    }

    public Class[] getParamTypes() {
        return paramTypes;
    }

    public String getProviderJavaMethodName() {
        return providerJavaMethodName;
    }

    public void setProviderJavaMethodName(String providerJavaMethodName) {
        this.providerJavaMethodName = providerJavaMethodName;
    }

    public String getProbeDesc() {
        return probeDesc;
    }

    public static String getProbeDesc(String moduleProviderName,
                                    String moduleName,
                                    String probeProviderName,
                                    String probeName){
        return (moduleProviderName + ":" + moduleName + ":" +
                probeProviderName + ":" + probeName);
    }

    public Class getProviderClazz() {
                return providerClazz;
        }

         public String toString() {
         StringBuilder sbldr = new StringBuilder(moduleProviderName + ":" + moduleName
                         + ":" + probeProviderName + ":" + probeName);
         String delim = " (";
         for (int i = 0; i < paramTypes.length; i++) {
             sbldr.append(delim).append((paramTypes[i] == null) ? " " : paramTypes[i].getName());
             sbldr.append(" ").append((probeParamNames[i] == null) ? " " : probeParamNames[i]);
             delim = ", ";
         }
         if (paramTypes.length == 0)
             sbldr.append(" (");
         sbldr.append(")");

         return sbldr.toString();
     }

    public void setDTraceProviderImpl(Object impl) {
        dtraceProviderImpl = impl;
    }

    public Object getDTraceProviderImpl() {
        return dtraceProviderImpl;
    }

    public Method getDTraceMethod() {
        return dtraceMethod;
    }

    public void setDTraceMethod(Method m) {
        dtraceMethod = m;
    }

    public boolean hasSelf() {
        return hasSelf;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setParent(FlashlightProbe parent) {
        // Only setting the parent here if both are stateful or both are stateless (no mixing)
        if (stateful != parent.getStateful())
            return;
        this.parent = parent;
    }

    public boolean getStateful() { return stateful; }
    public boolean getStatefulReturn() { return statefulReturn; }
    public boolean getStatefulException() { return statefulException; }
    public String [] getProfileNames() { return profileNames; }

    private void initInvokerList() {
        Set<Map.Entry<Integer, ProbeClientInvoker>> entries = invokers.entrySet();
        List<ProbeClientInvoker> invList = new ArrayList(2);
        if (stateful) {
            // If this is a stateful probe, we only want invokers in the list that actually can handle stateful
            // invokes
            for (Map.Entry<Integer, ProbeClientInvoker> entry : entries) {
                ProbeClientInvoker invoker = entry.getValue();
                if (invoker instanceof StatefulProbeClientInvoker)
                    invList.add(invoker);
            }
        } else {
            for (Map.Entry<Integer, ProbeClientInvoker> entry : entries) {
                ProbeClientInvoker invoker = entry.getValue();
                invList.add(invoker);
            }
        }

        invokerList = invList;
    }

    private int findStateIndex(int invokerId, ArrayList <ProbeInvokeState> states) {
        if (states == null)
            return -1;

        int size = states.size();
        ProbeInvokeState state = null;
        for (int stateIndex=0; stateIndex<size; stateIndex++) {
            state = states.get(stateIndex);
            if (invokerId == state.getInvokerId())
                return stateIndex;
        }
        return -1;
    }

    public static final class ProbeInvokeState {
        private int invokerId;
        private Object state = null;
        /* package */ ProbeInvokeState() {}
        /* package */ ProbeInvokeState(int invokerId, Object state) {
            this.invokerId = invokerId;
            this.state = state;
        }
        /* package */ final Object getState() { return state; }
        /* package */ final int getInvokerId() { return invokerId; }
    }

    private Method probeMethod;
    public static final String SELF = "@SELF";
    private int id;
    private Class providerClazz;
    private String moduleProviderName;
    private String moduleName;
    private String probeName;
    private String probeProviderName;
    private String[] probeParamNames;
    private Class[] paramTypes;
    private volatile List<ProbeClientInvoker> invokerList = new ArrayList(2);
    private String providerJavaMethodName;
    private AtomicBoolean listenerEnabled = new AtomicBoolean(false);
    private String probeDesc;
    private Object  dtraceProviderImpl;
    private Method  dtraceMethod;
    private boolean hasSelf;
    private boolean hidden;
    private ConcurrentMap<Integer, ProbeClientInvoker> invokers = new ConcurrentHashMap<Integer, ProbeClientInvoker>();
    private static final Logger logger = FlashlightLoggerInfo.getLogger();
    public final static LocalStringManagerImpl localStrings =
                            new LocalStringManagerImpl(FlashlightProbe.class);
    private FlashlightProbe parent = null;
    private boolean stateful = false;
    private boolean statefulReturn = false;
    private boolean statefulException = false;
    private String [] profileNames = null;
}

