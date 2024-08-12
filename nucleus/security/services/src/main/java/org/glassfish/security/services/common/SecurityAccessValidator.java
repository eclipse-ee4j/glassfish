/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.security.services.common;

import com.sun.enterprise.util.LocalStringManagerImpl;

import java.security.AccessControlException;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.ValidationInformation;
import org.glassfish.hk2.api.Validator;


public class SecurityAccessValidator implements Validator {

    private static final Logger LOG = SecurityAccessValidationService._theLog;
    private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(SecurityAccessValidator.class);


    @Override
    public boolean validate(ValidationInformation info) {

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("ValidationInformation info= " + info);
        }

        switch (info.getOperation()) {
        case BIND:
        case UNBIND:
            return validateBindAndUnbind(info);
        case LOOKUP:
            return validateLookup(info.getCandidate(), info.getInjectee());
        default:
            return false;
        }

    }

    private boolean isSecureAnnotated(ValidationInformation info) {

        Descriptor d = info.getCandidate();

        Set<String> qualifiers = d.getQualifiers();
        if (qualifiers != null && qualifiers.size() != 0) {
            for (String s : qualifiers) {
                if (Secure.class.getCanonicalName().equals(s)) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("The instance is annotated with \'Secure\': "
                                + info);
                    }
                    return true;
                }
            }
        }

        return false;

    }

    private boolean validateBindAndUnbind(ValidationInformation info) {

        // do nothing if the instance is not annotated with 'Secure'
        if (!isSecureAnnotated(info))
            return true;

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("validateBindAndUnbind," + " injectee= "
                    + info.getInjectee());
        }

        return validateLookup(info.getCandidate(), info.getInjectee());
    }

    private boolean validateLookup(ActiveDescriptor<?> candidate,
            Injectee injectee) {

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Lookup candiate =" + candidate + ", injectee= "
                    + injectee);
        }

        if (!candidate.isReified()) {
            // not yet really injected yet, so not to check perm
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Lookup candiate is not reified yet");
            }
            return true;
        } else {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Lookup candiate is reified, candidate = " + candidate);
            }
        }

        Set<String> contracts = candidate.getAdvertisedContracts();

        if (contracts == null)
            return true;

        Map<String, List<String>> md = candidate.getMetadata();

        if (LOG.isLoggable(Level.FINE)) {
            Iterator<Map.Entry<String, List<String>>> itr = md.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<String, List<String>> entry = itr.next();
                String k = entry.getKey();
                for (String v : entry.getValue()) {
                    LOG.fine("$$ key= " + k + ", value= " + v);
                }
            }
        }

        Permission perm = null;
        List<String> names = md.get(Secure.NAME);
        if (names == null || names.isEmpty()) {

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Perm name is empty, will use default value");
            }

            //the 'Secure' annotation did not specify a accessPermissionName, use default accessPermissionName name
            perm = getAccessPermision(Secure.DEFAULT_PERM_NAME, null);

        } else {
            String permName = names.get(0);
            perm = getAccessPermision(permName, null);
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("The permission to be protected = " + perm);
        }

        boolean check_result = false;
        if (injectee == null) {
            // lookup style check

            Class caller = getServiceLookupCaller();

            check_result = checkPerm(perm, caller);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Lookup, checked perm for = " + perm + ", result= "
                        + check_result);
            }
        } else {
            // injection style check
            check_result = validateInjection(candidate, injectee, perm);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Injection, checked perm for = " + perm + ", result= "
                        + check_result);
            }
        }

        return check_result;
    }


    //temporary alternative fixing to JIRA-HK2:116
    private Class getServiceLookupCaller() {
/*
        StackTraceElement[] steArr = new Exception().getStackTrace();

        for (int i = 0; i < steArr.length; i++ ) {
            StackTraceElement elm = steArr[i];

            if (elm.getClassName().equals("org.jvnet.hk2.internal.ServiceLocatorImpl") &&
                    (elm.getMethodName().equals("getService") ||
                     elm.getMethodName().equals("getAllServices") ||
                     elm.getMethodName().equals("getServiceHandle") ||
                     elm.getMethodName().equals("getAllServiceHandles") ||
                     elm.getMethodName().equals("create") ||
                     elm.getMethodName().equals("createAndInitialize") ||
                     elm.getMethodName().equals("shutdown")

               )) {

                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Found the service locator, classname= "
                            + steArr[i+1].getClassName() + ", ste =" + steArr[i+1]);
                }

                //after finding the 1st service locator, then look for the class which is not ServiceLocatorImpl
                System.out.println("%%%%found the locator");
                for (int j = i+1; j < steArr.length; j++ ) {
                    StackTraceElement elmj = steArr[j];
                    if (elmj.getClassName().startsWith("org.jvnet.hk2.internal."))  //by pass all hk2 classes to find the caller
                        continue;
                    else {
                        StackTraceElement caller = elmj;
                        //found the caller class which is not ServiceLocatorImpl
                        System.out.println("%%%caller Class name= " + caller.getClassName() + ", caller ste =" + caller +
                                ", method=" + caller.getMethodName());
                        try {
                            return Class.forName(caller.getClassName(), true, Thread.currentThread().getContextClassLoader());
                        } catch (ClassNotFoundException e) {

                            try {
                                return Class.forName(caller.getClassName());
                            } catch (ClassNotFoundException e1) {
                                // TODO Auto-generated catch block
                                //e1.printStackTrace();
                                LOG.warning(localStrings.getLocalString("sec.validate.lookup.noclass",
                                        "Lookup Class not found in classpath: {0}", caller.getClassName()));
                                throw new RuntimeException(e);
                            }

                        }
                    }
                }
            }
        }

        System.out.println("%%%caller Class= null");
        LOG.warning(localStrings.getLocalString("sec.validate.lookup.fail", "Cannot find the looup caller class"));
*/
        return null;

    }

    private boolean checkPerm(Permission p, Class caller) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Checked perm for = " + p);
        }


        try {
            if (caller != null) {
                ProtectionDomain pd = this.getCallerProtDomain(caller);
                pd.implies(p);
            } else
                AccessController.checkPermission(p);

        } catch (SecurityException e) {

            LOG.warning(localStrings.getLocalString(
                    "sec.validate.lookup.deny", "Check Permission failed in lookup for permission = {0}",  p));

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Check Permission failed, perm= " + p + ", message = "
                        + e.getMessage());
            }
            throw e;
        }

        return true;
    }


    private boolean validateInjection(ActiveDescriptor<?> candidate,
            Injectee injectee, Permission p) {

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Injectee =" + injectee + ", permission= " + p);
        }

        // If this is an Inject, get the protection domain of the injectee
        Class<?> injecteeClass = injectee.getInjecteeClass();

        ProtectionDomain pd = getCallerProtDomain(injecteeClass);

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Protection domain code src= " + pd.getCodeSource());
        }

        if (!pd.implies(p)) {

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("permission check failed for " + injectee + ", to get perm " + p + ", for candidate "
                        + candidate);
            }

            throw new AccessControlException(localStrings.getLocalString("sec.validate.injection.deny",
                    "Access denied for injectee {0} to get permission {1}.", injectee, p));

        } else {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("permission check success for " + injectee
                        + " to get " + candidate);
            }
        }

        return true;
    }

    private ProtectionDomain getCallerProtDomain(final Class caller) {
        ProtectionDomain pd = AccessController
                .doPrivileged(new PrivilegedAction<ProtectionDomain>() {
                    @Override
                    public ProtectionDomain run() {
                        return caller.getProtectionDomain();
                    }
                });

        return pd;
    }


    /**
     * The permission to be checked
     *
     * @return permission to be checked
     */
    private Permission getAccessPermision(String protectName, String action) {
        return new SecureServiceAccessPermission(protectName, action);
    }

}
