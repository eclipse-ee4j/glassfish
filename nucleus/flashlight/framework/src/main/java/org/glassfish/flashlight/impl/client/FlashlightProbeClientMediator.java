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

/*
 * BOOBY TRAP SITTING RIGHT HERE!!! There is a ProbeListener in
 * org.glassfish.flashlight.client -- don't use that one or everything will
 * fail! Do not use this import --> import org.glassfish.flashlight.client.* -->
 * import individually instead!!
 */
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.reflect.ReflectUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.flashlight.FlashlightLoggerInfo;
import org.glassfish.flashlight.FlashlightUtils;
import org.glassfish.flashlight.client.ProbeClientInvoker;
import org.glassfish.flashlight.client.ProbeClientInvokerFactory;
import org.glassfish.flashlight.client.ProbeClientMediator;
import org.glassfish.flashlight.client.ProbeClientMethodHandle;
import org.glassfish.flashlight.impl.core.FlashlightProbeProvider;
import org.glassfish.flashlight.provider.FlashlightProbe;
import org.glassfish.flashlight.provider.ProbeRegistry;
import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.annotations.Service;

import static org.glassfish.flashlight.FlashlightLoggerInfo.BAD_TRANSFORM;

/**
 * @author Mahesh Kannan Date: Jan 27, 2008
 * @author Byron Nevins, significant rewrite/refactor August 2009
 */
@Service
public class FlashlightProbeClientMediator
        implements ProbeClientMediator, PostConstruct {

    private static final ProbeRegistry probeRegistry = ProbeRegistry.getInstance();
    private static final Logger logger =
            FlashlightLoggerInfo.getLogger();
    public final static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(FlashlightProbeClientMediator.class);
    private static FlashlightProbeClientMediator _me = new FlashlightProbeClientMediator();
    private AtomicInteger clientIdGenerator =
            new AtomicInteger(0);
    private static ConcurrentHashMap<Integer, Object> clients =
            new ConcurrentHashMap<Integer, Object>();

    @Override
    public void postConstruct() {
        FlashlightProbeClientMediator.initMe(this);
    }

    private static void initMe(FlashlightProbeClientMediator me) {
        _me = me;
    }

    public static FlashlightProbeClientMediator getInstance() {
        return _me;
    }

    public static Object getClient(int id) {
        return clients.get(id);
    }

    @Override
    public Collection<ProbeClientMethodHandle> registerListener(Object listener) {
        return (registerListener(listener, null));
    }

    @Override
    public Collection<ProbeClientMethodHandle> registerListener(Object listener, String invokerId) {

        List<ProbeClientMethodHandle> pcms = new ArrayList<ProbeClientMethodHandle>();
        List<FlashlightProbe> probesRequiringClassTransformation = new ArrayList<FlashlightProbe>();
        if (invokerId != null) {
            invokerId = FlashlightUtils.getUniqueInvokerId(invokerId);
        }
        registerJavaListener(listener, pcms, probesRequiringClassTransformation, invokerId);
        transformProbes(listener, probesRequiringClassTransformation);

        return pcms;
    }

    public Collection<ProbeClientMethodHandle> registerDTraceListener(FlashlightProbeProvider propro) {

        List<ProbeClientMethodHandle> pcms = new ArrayList<ProbeClientMethodHandle>();
        List<FlashlightProbe> probesRequiringClassTransformation = new ArrayList<FlashlightProbe>();

        Object listener = registerDTraceListener(propro, pcms, probesRequiringClassTransformation);
        transformProbes(listener, probesRequiringClassTransformation);

        return pcms;
    }

    private void registerJavaListener(
            Object listener,
            List<ProbeClientMethodHandle> pcms,
            List<FlashlightProbe> probesRequiringClassTransformation,
            String invokerId) {

        List<MethodProbe> methodProbePairs =
                handleListenerAnnotations(listener.getClass(), invokerId);

        if (methodProbePairs.isEmpty()) {
            return;
        }

        for (MethodProbe mp : methodProbePairs) {
            FlashlightProbe probe = mp.probe;
            ProbeClientInvoker invoker = ProbeClientInvokerFactory.createInvoker(listener, mp.method, probe);
            ProbeClientMethodHandleImpl hi = new ProbeClientMethodHandleImpl(invoker.getId(), invoker, probe);
            pcms.add(hi);

            if (probe.addInvoker(invoker))
                probesRequiringClassTransformation.add(probe);
        }
    }

    private Object registerDTraceListener(
            FlashlightProbeProvider propro,
            List<ProbeClientMethodHandle> pcms,
            List<FlashlightProbe> probesRequiringClassTransformation) {

        // The "listener" needs to be registered against every Probe in propro...

        Collection<FlashlightProbe> probes = propro.getProbes();
        Object listener = null;

        for (FlashlightProbe probe : probes) {
            ProbeClientInvoker invoker = ProbeClientInvokerFactory.createDTraceInvoker(probe);
            ProbeClientMethodHandleImpl hi = new ProbeClientMethodHandleImpl(invoker.getId(), invoker, probe);
            pcms.add(hi);

            if (probe.addInvoker(invoker))
                probesRequiringClassTransformation.add(probe);

            if (listener == null)
                listener = probe.getDTraceProviderImpl();    // all the probes in propro have the same "listener"
        }

        return listener;
    }

    @Override
    public void transformProbes(Object listener, List<FlashlightProbe> probes) {
        if (probes.isEmpty())
            return;

        int clientID = clientIdGenerator.incrementAndGet();
        clients.put(clientID, listener);

        for (FlashlightProbe probe : probes) {
            Class clz = probe.getProviderClazz();
            ProbeProviderClassFileTransformer transformer =
                    ProbeProviderClassFileTransformer.getInstance(clz);
            try {
                transformer.addProbe(probe);
            }
            catch (Exception ex) {
                logger.log(Level.SEVERE, BAD_TRANSFORM, ex);
            }
        }
        ProbeProviderClassFileTransformer.transformAll();
    }

    /**
     * Pick out all methods in the listener with the correct annotation, look up
     * the referenced Probe and return a list of all such pairs. Validate that
     * the methods really do matchup properly.
     *
     * @throws RuntimeException if there is any serious problem.
     * @param listenerClass
     * @return
     */
    private List<MethodProbe> handleListenerAnnotations(Class listenerClass, String invokerId) {

        List<MethodProbe> mp = new LinkedList<MethodProbe>();

        for (Method method : listenerClass.getMethods()) {
            ProbeListener probeAnn = method.getAnnotation(ProbeListener.class);

            if (probeAnn == null)
                continue;

            String probeString = probeAnn.value();
            if (probeString == null)
                continue;

            if (invokerId != null) {
                String[] strArr = probeString.split(":");
                probeString = strArr[0] + ":"
                        + strArr[1] + ":"
                        + strArr[2] + invokerId + ":"
                        + strArr[3];
            }

            FlashlightProbe probe = probeRegistry.getProbe(probeString);
            if (probe == null) {
                String errStr = localStrings.getLocalString("probeNotRegistered",
                        "Probe is not registered: {0}", probeString);
                throw new RuntimeException(errStr);
            }
            mp.add(new MethodProbe(method, probe));
        }

        return mp;
    }

    // this is just used internally for cleanly organizing the code.
    // It throws RuntimeException because this module was specifically architected
    // to *not* use the Java Exception mechanism for errors.
    private static class MethodProbe {

        MethodProbe(Method m, FlashlightProbe p) {
            method = m;
            probe = p;
            String err = ReflectUtils.equalSignatures(method, p.getProbeMethod());

            if (err != null)
                throw new RuntimeException(Strings.get("method_mismatch", err));
        }
        Method method;
        FlashlightProbe probe;
    }
}
