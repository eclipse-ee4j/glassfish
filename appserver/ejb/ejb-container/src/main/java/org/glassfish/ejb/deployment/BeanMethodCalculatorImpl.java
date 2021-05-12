/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.ejb.deployment;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.logging.LogDomains;
import java.util.Locale;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;
import org.glassfish.ejb.deployment.descriptor.FieldDescriptor;
import org.glassfish.ejb.deployment.descriptor.ScheduledTimerDescriptor;

/**
 * Utility class to calculate the list of methods required  to have transaction
 * attributes
 *
 * @author  Jerome Dochez
 */
final public class BeanMethodCalculatorImpl {

    // TODO - change logger if/when other EJB deployment classes are changed
    static final private Logger _logger = LogDomains.getLogger(BeanMethodCalculatorImpl.class, LogDomains.DPL_LOGGER);

    public Vector getPossibleCmpCmrFields(ClassLoader cl, String className) throws ClassNotFoundException {
        Vector fieldDescriptors = new Vector();
        Class theClass = cl.loadClass(className);

        // Start with all *public* methods
        Method[] methods = theClass.getMethods();

        // Find all accessors that could be cmp fields. This list
        // will contain all cmr field accessors as well, since there
        // is no good way to distinguish between the two purely based
        // on method signature.
        for(int mIndex = 0; mIndex < methods.length; mIndex++) {
            Method next = methods[mIndex];
            String nextName = next.getName();
            int nextModifiers = next.getModifiers();
            if( Modifier.isAbstract(nextModifiers) ) {
                if( nextName.startsWith("get") &&
                    nextName.length() > 3 ) {
                    String field =
                        nextName.substring(3,4).toLowerCase(Locale.US) +
                        nextName.substring(4);
                    fieldDescriptors.add(new FieldDescriptor(field));
                }
            }
        }
        return fieldDescriptors;
    }

    public Vector getMethodsFor(com.sun.enterprise.deployment.EjbDescriptor ejbDescriptor, ClassLoader classLoader)
            throws ClassNotFoundException
    {
        Vector methods = new Vector();

        if (ejbDescriptor.isRemoteInterfacesSupported()) {
            addAllInterfaceMethodsIn(methods, classLoader.loadClass(ejbDescriptor.getHomeClassName()));
            addAllInterfaceMethodsIn(methods, classLoader.loadClass(ejbDescriptor.getRemoteClassName()));
        }

        if (ejbDescriptor.isRemoteBusinessInterfacesSupported()) {
            for(String intf : ejbDescriptor.getRemoteBusinessClassNames()) {
                addAllInterfaceMethodsIn(methods, classLoader.loadClass(intf));
            }
        }

        if (ejbDescriptor.isLocalInterfacesSupported()) {
            addAllInterfaceMethodsIn(methods, classLoader.loadClass(ejbDescriptor.getLocalHomeClassName()));
            addAllInterfaceMethodsIn(methods, classLoader.loadClass(ejbDescriptor.getLocalClassName()));
        }

        if (ejbDescriptor.isLocalBusinessInterfacesSupported()) {
            for(String intf : ejbDescriptor.getLocalBusinessClassNames()) {
                addAllInterfaceMethodsIn(methods, classLoader.loadClass(intf));
            }
        }

        if (ejbDescriptor.isLocalBean()) {
            addAllInterfaceMethodsIn(methods, classLoader.loadClass(ejbDescriptor.getEjbClassName()));
        }

        if (ejbDescriptor.hasWebServiceEndpointInterface()) {
            addAllInterfaceMethodsIn(methods, classLoader.loadClass(ejbDescriptor.getWebServiceEndpointInterfaceName()));

        }
        return methods;
    }

    private static void addAllInterfaceMethodsIn(Collection methods, Class c) {

        methods.addAll(Arrays.asList(c.getMethods()));
    }

    /**
     * @return a collection of MethodDescriptor for all the methods of my
     * ejb which are elligible to have a particular transaction setting.
     */
    public Collection getTransactionalMethodsFor(com.sun.enterprise.deployment.EjbDescriptor desc, ClassLoader loader)
        throws ClassNotFoundException, NoSuchMethodException
    {
        EjbDescriptor ejbDescriptor = (EjbDescriptor) desc;
        // only set if desc is a stateful session bean.  NOTE that
        // !statefulSessionBean does not imply stateless session bean
        boolean statefulSessionBean = false;

        Vector methods = new Vector();
        if (ejbDescriptor instanceof EjbSessionDescriptor) {
            statefulSessionBean = ((EjbSessionDescriptor) ejbDescriptor).isStateful();

            boolean singletonSessionBean = ((EjbSessionDescriptor) ejbDescriptor).isSingleton();

            // Session Beans
            if (ejbDescriptor.isRemoteInterfacesSupported()) {
                Collection disallowedMethods = extractDisallowedMethodsFor(jakarta.ejb.EJBObject.class, sessionBeanMethodsDisallowed);
                Collection potentials = getTransactionMethodsFor(loader, ejbDescriptor.getRemoteClassName() , disallowedMethods);
                transformAndAdd(potentials, MethodDescriptor.EJB_REMOTE, methods);
            }

            if( ejbDescriptor.isRemoteBusinessInterfacesSupported() ) {

                for(String intfName :
                        ejbDescriptor.getRemoteBusinessClassNames() ) {

                    Class businessIntf = loader.loadClass(intfName);
                    Method[] busIntfMethods = businessIntf.getMethods();
                    for (Method next : busIntfMethods ) {
                        methods.add(new MethodDescriptor
                                    (next, MethodDescriptor.EJB_REMOTE));
                    }
                }
            }

            if (ejbDescriptor.isLocalInterfacesSupported()) {
                Collection disallowedMethods = extractDisallowedMethodsFor(jakarta.ejb.EJBLocalObject.class, sessionLocalBeanMethodsDisallowed);
                Collection potentials = getTransactionMethodsFor(loader, ejbDescriptor.getLocalClassName() , disallowedMethods);
                transformAndAdd(potentials, MethodDescriptor.EJB_LOCAL, methods);

            }

            if( ejbDescriptor.isLocalBusinessInterfacesSupported() ) {

                for(String intfName :
                        ejbDescriptor.getLocalBusinessClassNames() ) {

                    Class businessIntf = loader.loadClass(intfName);
                    Method[] busIntfMethods = businessIntf.getMethods();
                    for (Method next : busIntfMethods ) {
                        methods.add(new MethodDescriptor
                                    (next, MethodDescriptor.EJB_LOCAL));
                    }
                }
            }

            if( ejbDescriptor.isLocalBean() ) {
                String intfName = ejbDescriptor.getEjbClassName();
                Class businessIntf = loader.loadClass(intfName);
                Method[] busIntfMethods = businessIntf.getMethods();
                for (Method next : busIntfMethods ) {
                    methods.add(new MethodDescriptor
                                (next, MethodDescriptor.EJB_LOCAL));
                }
            }

            if (ejbDescriptor.hasWebServiceEndpointInterface()) {
                Class webServiceClass = loader.loadClass
                    (ejbDescriptor.getWebServiceEndpointInterfaceName());

                Method[] webMethods = webServiceClass.getMethods();
                for (int i=0;i<webMethods.length;i++) {
                    methods.add(new MethodDescriptor(webMethods[i],
                                MethodDescriptor.EJB_WEB_SERVICE));

                }
            }

            // SFSB and Singleton can have lifecycle callbacks transactional
            if (statefulSessionBean || singletonSessionBean) {
                Set<LifecycleCallbackDescriptor> lcds = ejbDescriptor.getLifecycleCallbackDescriptors();
                for(LifecycleCallbackDescriptor lcd : lcds) {
                    try {
                        Method m = lcd.getLifecycleCallbackMethodObject(loader);
                        MethodDescriptor md = new MethodDescriptor(m, MethodDescriptor.LIFECYCLE_CALLBACK);
                        methods.add(md);
                    } catch(Exception e) {
                        if (_logger.isLoggable(Level.FINE)) {
                            _logger.log(Level.FINE,
                            "Lifecycle callback processing error", e);
                        }
                    }
                }
            }


        } else {
            // entity beans local interfaces
            String homeIntf = ejbDescriptor.getHomeClassName();
            if (homeIntf!=null) {

                Class home = loader.loadClass(homeIntf);
                Collection potentials = getTransactionMethodsFor(jakarta.ejb.EJBHome.class, home);
                transformAndAdd(potentials, MethodDescriptor.EJB_HOME, methods);

                String remoteIntf = ejbDescriptor.getRemoteClassName();
                Class remote = loader.loadClass(remoteIntf);
                potentials = getTransactionMethodsFor(jakarta.ejb.EJBObject.class, remote);
                transformAndAdd(potentials, MethodDescriptor.EJB_REMOTE, methods);
            }

            // enity beans remote interfaces
            String localHomeIntf = ejbDescriptor.getLocalHomeClassName();
            if (localHomeIntf!=null) {
                Class home = loader.loadClass(localHomeIntf);
                Collection potentials = getTransactionMethodsFor(jakarta.ejb.EJBLocalHome.class, home);
                transformAndAdd(potentials, MethodDescriptor.EJB_LOCALHOME, methods);

                String remoteIntf = ejbDescriptor.getLocalClassName();
                Class remote = loader.loadClass(remoteIntf);
                potentials = getTransactionMethodsFor(jakarta.ejb.EJBLocalObject.class, remote);
                transformAndAdd(potentials, MethodDescriptor.EJB_LOCAL, methods);
            }
        }

        if( !statefulSessionBean ) {
            if( ejbDescriptor.isTimedObject() ) {
                if( ejbDescriptor.getEjbTimeoutMethod() != null) {
                    methods.add(ejbDescriptor.getEjbTimeoutMethod());
                }
                for (ScheduledTimerDescriptor schd : ejbDescriptor.getScheduledTimerDescriptors()) {
                    methods.add(schd.getTimeoutMethod());
                }
            }
        }

        return methods;
     }

     private Collection getTransactionMethodsFor(ClassLoader loader, String interfaceName, Collection disallowedMethods)
        throws ClassNotFoundException
     {
         Class clazz = loader.loadClass(interfaceName);
         return getTransactionMethodsFor(clazz, disallowedMethods);
     }

     private Collection getTransactionMethodsFor(Class interfaceImpl, Collection disallowedMethods) {
         Vector v = new Vector(Arrays.asList(interfaceImpl.getMethods()));
         v.removeAll(disallowedMethods);
         return v;
     }

     private Collection getTransactionMethodsFor(Class interfaceType, Class interfaceImpl) {
         Collection disallowedTransactionMethods = getDisallowedTransactionMethodsFor(interfaceType);
         return getTransactionMethodsFor(interfaceImpl, disallowedTransactionMethods);
     }

     private Collection getDisallowedTransactionMethodsFor(Class interfaceType) {
         return extractDisallowedMethodsFor(interfaceType, getDisallowedMethodsNamesFor(interfaceType));
     }

     // from EJB 2.0 spec section 17.4.1
     private Collection extractDisallowedMethodsFor(Class interfaceType, String[] methodNames) {

         Vector v = new Vector();
         // no disallowed methods for this interface
         if (methodNames.length==0)
             return v;

         Method methods[] = interfaceType.getMethods();

         for (int i=0; i<methods.length; i++) {
            // all methods of the interface are disallowed
            if (methodNames[0].equals("*"))
                 v.addElement(methods[i]);
            else if (Arrays.binarySearch(methodNames, methods[i].getName())>=0)
                 v.addElement(methods[i]);
         }
         return v;
     }

     /**
      * utiliy method to transform our collection of Method objects into
      * MethodDescriptor objects and add them to our global list of
      * elligible methods
      * @param the collection of acceptable method objects
      * @param the method-intf identifier for those methods
      * @param the global list of MethodDescriptors objects
      */
     private void transformAndAdd(Collection methods, String methodIntf, Vector globalList) {

         for (Iterator itr = methods.iterator();itr.hasNext();) {
             Method m = (Method) itr.next();
             MethodDescriptor md = new MethodDescriptor(m, methodIntf);
             globalList.add(md);
         }
     }

     /**
      * @return the list of disallowed methods for a particular interface
      */
     private String[] getDisallowedMethodsNamesFor(Class interfaceType) {
         return (String[]) getDisallowedMethodsNames().get(interfaceType);
     }

     /**
      * @return a Map of disallowed methods per interface type. The key to the
      * map is the interface type (e.g. EJBHome, EJBObject), the value
      * is an array of methods names disallowed to have transaction attributes
      */
     protected Map getDisallowedMethodsNames() {
         if (disallowedMethodsPerInterface==null) {
            disallowedMethodsPerInterface = new Hashtable();
            disallowedMethodsPerInterface.put(jakarta.ejb.EJBHome.class, entityBeanHomeMethodsDisallowed);
            disallowedMethodsPerInterface.put(jakarta.ejb.EJBObject.class, entityBeanRemoteMethodsDisallowed);
            disallowedMethodsPerInterface.put(jakarta.ejb.EJBLocalHome.class, entityBeanLocalHomeMethodsDisallowed);
            disallowedMethodsPerInterface.put(jakarta.ejb.EJBLocalObject.class, entityBeanLocalInterfaceMethodsDisallowed);
         }
         return disallowedMethodsPerInterface;
     }

     private final String entityBeanHomeMethodsDisallowed[] = {
         "getEJBMetaData", "getHomeHandle"
     };
     private final String entityBeanRemoteMethodsDisallowed[] = {
         "getEJBHome", "getHandle", "getPrimaryKey", "isIdentical"
     };
     private final String entityBeanLocalHomeMethodsDisallowed[] = {};
     private final String entityBeanLocalInterfaceMethodsDisallowed[] = {
        "getEJBLocalHome", "getPrimaryKey", "isIdentical"
     };

     private final String sessionBeanMethodsDisallowed[] = {
         "*"
     };

     private final String sessionLocalBeanMethodsDisallowed[] = {
         "*"
     };

     private Map disallowedMethodsPerInterface;

}
