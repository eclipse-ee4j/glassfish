/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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

package com.sun.ejb;

import com.sun.ejb.containers.EJBContextImpl;
import com.sun.ejb.containers.EJBTimerService;
import com.sun.ejb.containers.EJBTimerServiceWrapper;
import com.sun.enterprise.container.common.spi.EjbNamingReferenceManager;
import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.util.Utility;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import javax.naming.Context;
import javax.naming.NamingException;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.internal.api.ORBLocator;
import org.jvnet.hk2.annotations.Service;
import org.omg.CORBA.ORB;

import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_APP;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_APP_ENV;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_GLOBAL;

/**
 * @author Mahesh Kannan
 */
@Service
public class EjbNamingReferenceManagerImpl implements EjbNamingReferenceManager {

    @Inject
    private InvocationManager invMgr;

    @Inject
    private Provider<ORBLocator> orbLocatorProvider;

    @Override
    public Object resolveEjbReference(EjbReferenceDescriptor ejbRefDesc, Context context)
        throws NamingException {

        Object jndiObj = null;
        boolean resolved = false;


        if( ejbRefDesc.isLocal() ) {
            // local ejb dependencies if there's a lookup string, use that.
            // Otherwise, the ejb will be resolved by EJBUtils.
            if( ejbRefDesc.hasLookupName()) {
                jndiObj = context.lookup(ejbRefDesc.getLookupName().toString());
                resolved = true;
            }
        } else if (!ejbRefDesc.hasJndiName() && ejbRefDesc.hasLookupName()) {
            // For a remote reference, only do a context lookup if there is no
            // jndi name.
            // The thread context class loader usually is the EAR class loader,
            // which is able to load business interfaces.  But if the lookup request
            // originated from appclient, and the ejb-ref is under java:app/env,
            // and it also has a lookup-name, then we need to set the EAR class
            // loader to the thread.  Issue 17376.
            try {
                jndiObj = context.lookup(ejbRefDesc.getLookupName().toString());
            } catch (NamingException e) {
                ClassLoader oldLoader = null;
                try {
                    oldLoader = Utility
                        .setContextClassLoader(ejbRefDesc.getReferringBundleDescriptor().getClassLoader());
                    jndiObj = context.lookup(ejbRefDesc.getLookupName().toString());
                } finally {
                    Utility.setContextClassLoader(oldLoader);
                }
            }
            resolved = true;
        } else if (ejbRefDesc.hasJndiName() && ejbRefDesc.getJndiName().isJavaApp()
            && !ejbRefDesc.getJndiName().hasPrefix(JNDI_CTX_JAVA_APP_ENV)) {

            // This could be an @EJB dependency in an appclient whose target name
            // is a portable java:app ejb name.  Try the global version.  If that
            // doesn't work, the javaURLContext logic should be able to figure it
            // out.
            SimpleJndiName remoteJndiName = ejbRefDesc.getJndiName();

            String appName = (String) context.lookup(JNDI_CTX_JAVA_APP + "AppName");
            String globalLookup = remoteJndiName.changePrefix(JNDI_CTX_JAVA_GLOBAL + appName + '/').toString();
            jndiObj = context.lookup(globalLookup);
            resolved = true;
        } else {

            // Get actual jndi-name from ejb module.
            SimpleJndiName remoteJndiName = EJBUtils.getRemoteEjbJndiName(ejbRefDesc);

            // We could be resolving an ejb-ref as part of a remote lookup thread.  In that
            // case the context class loader won't be set appropriately on the thread
            // being used to process the remote naming request.   We can't just always
            // set the context class loader to the class loader of the application module
            // that defined the ejb reference.  That would cause ClassCastExceptions
            // when the returned object is assigned within a cross-application intra-server
            // lookup. So, just try to lookup the interface associated with the ejb-ref
            // using the context class loader.  If that doesn't work, explicitly use the
            // defining application's class loader.

            ClassLoader origClassLoader = Utility.getClassLoader();
            boolean setCL = false;
            try {
                try {
                    String refInterface = ejbRefDesc.isEJB30ClientView()
                        ? ejbRefDesc.getEjbInterface()
                        : ejbRefDesc.getHomeClassName();
                    origClassLoader.loadClass(refInterface);

                } catch (ClassNotFoundException e) {
                    ClassLoader referringBundleClassLoader = ejbRefDesc.getReferringBundleDescriptor().getClassLoader();
                    Utility.setContextClassLoader(referringBundleClassLoader);
                    setCL = true;
                }

                /* For remote ejb refs, first lookup the target remote object
                 * and pass it to the next stage of ejb ref resolution.
                 * If the string is a "corbaname:...." URL
                 * the lookup happens thru the corbanameURL context,
                 * else it happens thru the context provided by the NamingManager.
                 *
                 * NOTE : we might need some additional logic to handle cross-server
                 * MEJB resolution for cluster support post V3 FCS.
                 */
                if (remoteJndiName.hasCorbaPrefix()) {
                    ORB orb = orbLocatorProvider.get().getORB();
                    jndiObj = orb.string_to_object(remoteJndiName.toString());
                } else {
                    jndiObj = context.lookup(remoteJndiName.toString());
                }

            } catch(Exception e) {
                // Important to make the real underlying lookup name part of the exception.
                NamingException ne = new NamingException("Exception resolving Ejb for '" +
                    ejbRefDesc + "' .  Actual (possibly internal) Remote JNDI name used for lookup is '" +
                    remoteJndiName + "'");
                ne.initCause(e);
                throw ne;
            } finally {
                if( setCL ) {
                    Utility.setContextClassLoader(origClassLoader);
                }
            }
        }

        return resolved ? jndiObj : EJBUtils.resolveEjbRefObject(ejbRefDesc, jndiObj);
    }

    @Override
    public boolean isEjbReferenceCacheable(EjbReferenceDescriptor ejbRefDesc) {
        // Ejb-ref is only eligible for caching if it refers to the legacy
        // Home view and it is resolved to an ejb within the same application.
        return ( (!ejbRefDesc.isEJB30ClientView()) &&
                 (ejbRefDesc.getEjbDescriptor() != null) );

        // caching not enabled.
        //return false;
    }


    @Override
    public Object getEJBContextObject(String contextType) {

        ComponentInvocation currentInv = invMgr.getCurrentInvocation();

        if(currentInv == null) {
            throw new IllegalStateException("no current invocation");
        } else if (currentInv.getInvocationType() != ComponentInvocation.ComponentInvocationType.EJB_INVOCATION) {
            throw new IllegalStateException(
                "Illegal invocation type for EJB Context : " + currentInv.getInvocationType());
        }

        EjbInvocation ejbInv = (EjbInvocation) currentInv;
        Object returnObject = ejbInv.context;
        if (contextType.equals("jakarta.ejb.TimerService")) {
            if (EJBTimerService.getEJBTimerService() == null) {
                throw new IllegalStateException("EJB Timer Service not available");
            }
            returnObject = new EJBTimerServiceWrapper(EJBTimerService.getEJBTimerService(),
                (EJBContextImpl) ejbInv.context);
        }
        return returnObject;
    }
}
