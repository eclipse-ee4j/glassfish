/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

import com.sun.ejb.codegen.AsmSerializableBeanGenerator;
import com.sun.ejb.codegen.ClassGeneratorFactory;
import com.sun.ejb.codegen.Generator;
import com.sun.ejb.codegen.GenericHomeGenerator;
import com.sun.ejb.codegen.Remote30WrapperGenerator;
import com.sun.ejb.codegen.RemoteGenerator;
import com.sun.ejb.containers.BaseContainer;
import com.sun.ejb.containers.EjbContainerUtilImpl;
import com.sun.ejb.containers.GenericEJBLocalHome;
import com.sun.ejb.containers.RemoteBusinessWrapperBase;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.logging.LogDomains;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.glassfish.pfl.dynamic.codegen.spi.Wrapper;

import static com.sun.ejb.codegen.GenericHomeGenerator.GENERIC_HOME_CLASSNAME;
import static java.util.logging.Level.FINE;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper.DUMP_AFTER_SETUP_VISITOR;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper.TRACE_BYTE_CODE_GENERATION;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper.USE_ASM_VERIFIER;

/**
 * A handy class with static utility methods.
 *
 * Note that much of this code has to execute in the client so
 * it needs to be careful about which server-only resources it
 * uses and in which code paths.
 *
 */
public class EJBUtils {

    //
    private static final Logger _logger =
            LogDomains.getLogger(EJBUtils.class, LogDomains.EJB_LOGGER);


    // Internal property to force generated ejb container classes to
    // be created during deployment time instead of dynamically.  Note that
    // this property does *not* cover RMI-IIOP stub generation.
    // See IASEJBC.java for more details.
    private static final String EJB_USE_STATIC_CODEGEN_PROP =
        "com.sun.ejb.UseStaticCodegen";

    private static final String REMOTE30_HOME_JNDI_SUFFIX =
        "__3_x_Internal_RemoteBusinessHome__";

    private static Boolean ejbUseStaticCodegen_ = null;

    // Initial portion of a corba interoperable naming syntax jndi name.
    private static final String CORBA_INS_PREFIX = "corbaname:";

    // Prefix of portable global JNDI namespace
    private static final String JAVA_GLOBAL_PREFIX = "java:global/";

    // Separator between simple and fully-qualified portable ejb global JNDI names
    private static final String PORTABLE_JNDI_NAME_SEP = "!";

    // Separator between simple and fully-qualified glassfish-specific JNDI names
    private static final String GLASSFISH_JNDI_NAME_SEP = "#";

    /**
     * Utility methods for serializing EJBs, primary keys and
     * container-managed fields, all of which may include Remote EJB
     * references,
     * Local refs, JNDI Contexts etc which are not Serializable.
     * This is not used for normal RMI-IIOP serialization.
     * It has boolean replaceObject control, whether to call replaceObject
     * or not
     */
    public static final byte[] serializeObject(Object obj,
                                               boolean replaceObject)
        throws IOException
    {
        return EjbContainerUtilImpl.getInstance().getJavaEEIOUtils().serializeObject(obj, replaceObject);
    }

    public static final byte[] serializeObject(Object obj)
        throws IOException
    {
        return EjbContainerUtilImpl.getInstance().getJavaEEIOUtils().serializeObject(obj, true);
    }

    /**
     * Utility method for deserializing EJBs, primary keys and
     * container-managed fields, all of which may include Remote
     * EJB references,
     * Local refs, JNDI Contexts etc which are not Serializable.
     */
    public static final Object deserializeObject(byte[] data,
            ClassLoader loader, boolean resolveObject)
        throws Exception
    {
        return EjbContainerUtilImpl.getInstance().getJavaEEIOUtils().deserializeObject(data, resolveObject, loader);
    }

    public static final Object deserializeObject(byte[] data,
                                                 ClassLoader loader)
        throws Exception
    {
        return EjbContainerUtilImpl.getInstance().getJavaEEIOUtils().deserializeObject(data, true, loader);
    }

    public static boolean useStaticCodegen() {
        synchronized (EJBUtils.class) {
            if( ejbUseStaticCodegen_ == null ) {
                String ejbStaticCodegenProp = null;
                if(System.getSecurityManager() == null) {
                    ejbStaticCodegenProp =
                        System.getProperty(EJB_USE_STATIC_CODEGEN_PROP);
                } else {
                    ejbStaticCodegenProp = (String)
                    java.security.AccessController.doPrivileged
                            (new java.security.PrivilegedAction() {
                        @Override
                        public java.lang.Object run() {
                            return
                                System.getProperty(EJB_USE_STATIC_CODEGEN_PROP);
                        }});
                }

                boolean useStaticCodegen =
                    ( (ejbStaticCodegenProp != null) &&
                      ejbStaticCodegenProp.equalsIgnoreCase("true"));

                ejbUseStaticCodegen_ = useStaticCodegen;

                _logger.log(FINE, "EJB Static codegen is " +
                            (useStaticCodegen ? "ENABLED" : "DISABLED") +
                            " ejbUseStaticCodegenProp = " +
                            ejbStaticCodegenProp);
            }
        }

        return ejbUseStaticCodegen_.booleanValue();

    }


    /**
     * Prepends __EJB31_Generated__ and adds _Intf__ to the simple class name.
     *
     * @param ejbClassName full class name
     */
    public static String getGeneratedOptionalInterfaceName(String ejbClassName) {
        String packageName = Generator.getPackageName(ejbClassName);
        String simpleName = Generator.getBaseName(ejbClassName);
        String optionalIntfName = "__EJB31_Generated__" + simpleName + "__Intf__";
        return packageName == null ? optionalIntfName : packageName + "." + optionalIntfName;
    }


    /**
     * Actual jndi-name under which Remote ejb factory lives depends on
     * whether it's a Remote Home view or Remote Business view.  This is
     * necessary since a single session bean can expose both views and
     * the resulting factory objects are different.  These semantics are
     * not exposed to the developer-view to keep things simpler.  The
     * developer can simply deal with a single physical jndi-name.  If the
     * target bean exposes both a Remote Home view and a Remote Business
     * view, the developer can still use the single physical jndi-name
     * to resolve remote ejb-refs, and we will handle the distinction
     * internally.  Of course, this is based on the assumption that the
     * internal name is generated in a way that will not clash with a
     * separate top-level physical jndi-name chosen by the developer.
     *
     * Note that it's better to delay this final jndi name translation as
     * much as possible and do it right before the NamingManager lookup,
     * as opposed to changing the jndi-name within the descriptor objects
     * themselves.  This way, the extra indirection will not be exposed
     * if the descriptors are written out and they won't complicate any
     * jndi-name equality logic.
     *
     */
    public static String getRemoteEjbJndiName(EjbReferenceDescriptor refDesc) {

        String intf = refDesc.isEJB30ClientView() ?
                refDesc.getEjbInterface() : refDesc.getHomeClassName();

        return getRemoteEjbJndiName(refDesc.isEJB30ClientView(),
                                    intf,
                                    refDesc.getJndiName());
    }

    public static String getRemote30HomeJndiName(String jndiName) {
        return jndiName + REMOTE30_HOME_JNDI_SUFFIX;
    }

    public static String getRemoteEjbJndiName(boolean businessView,
                                              String interfaceName,
                                              String jndiName) {

        String returnValue = jndiName;

        String portableFullyQualifiedPortion = PORTABLE_JNDI_NAME_SEP + interfaceName;
        String glassfishFullyQualifiedPortion = GLASSFISH_JNDI_NAME_SEP + interfaceName;

        if( businessView ) {
            if( jndiName.startsWith(CORBA_INS_PREFIX) ) {


                // In the case of a corba interoperable naming string, we
                // need to lookup the internal remote home.  We can't rely
                // on our SerialContext Reference object (RemoteBusinessObjectFactory)
                // to do the home lookup because we have to directly access
                // the CosNaming service.

                // First, strip off any fully-qualified portion since there's only
                // one internal generic home object in CosNaming no matter how many
                // remote business interfaces there are.

                // Separate <jndi-name> portion from "corbaname:iiop:...#<jndi-name>
                // We need to do this since we also use "#" in some glassfish-specific
                // JNDI names
                int indexOfCorbaNameSep = jndiName.indexOf("#");
                String jndiNameMinusCorbaNamePortion = jndiName.substring(indexOfCorbaNameSep + 1);

                // Make sure any of the resulting jndi names still have corbaname: prefix intact
                String newJndiName = jndiName;

                if( jndiNameMinusCorbaNamePortion.startsWith(JAVA_GLOBAL_PREFIX) ){

                    newJndiName = stripFullyQualifiedJndiName(jndiName, portableFullyQualifiedPortion);

                } else if( jndiNameMinusCorbaNamePortion.endsWith(glassfishFullyQualifiedPortion ) ){

                    newJndiName = stripFullyQualifiedJndiName(jndiName, glassfishFullyQualifiedPortion);

                }

                returnValue = getRemote30HomeJndiName(newJndiName);

            } else {
                // Convert to fully-qualified names
                if( jndiName.startsWith(JAVA_GLOBAL_PREFIX)) {
                    returnValue = checkFullyQualifiedJndiName(jndiName, portableFullyQualifiedPortion);
                } else {
                    returnValue = checkFullyQualifiedJndiName(jndiName, glassfishFullyQualifiedPortion);
                }
            }
        } else {

            // EJB 2.x Remote  Home

            // Only in the portable global case, convert to a fully-qualified name
            if( jndiName.startsWith(JAVA_GLOBAL_PREFIX)) {
                returnValue = checkFullyQualifiedJndiName(jndiName, portableFullyQualifiedPortion);
            }
        }

        return returnValue;
    }

    private static String checkFullyQualifiedJndiName(String origJndiName, String fullyQualifiedPortion) {
        String returnValue = origJndiName;
        if( !origJndiName.endsWith(fullyQualifiedPortion) ) {
            returnValue = origJndiName + fullyQualifiedPortion;
        }
        return returnValue;
    }

    private static String stripFullyQualifiedJndiName(String origJndiName, String fullyQualifiedPortion) {
        String returnValue = origJndiName;
        if( origJndiName.endsWith(fullyQualifiedPortion) ) {
            int portionLength = fullyQualifiedPortion.length();
            returnValue = origJndiName.substring(0, origJndiName.length() - portionLength );
        }
        return returnValue;
    }


    public static Object resolveEjbRefObject(EjbReferenceDescriptor refDesc, Object jndiObj) throws NamingException {
        if (refDesc.isLocal()) {
            EjbDescriptor target = refDesc.getEjbDescriptor();
            BaseContainer container = EjbContainerUtilImpl.getInstance().getContainer(target.getUniqueId());
            if (refDesc.isEJB30ClientView()) {
                GenericEJBLocalHome genericLocalHome = container.getEJBLocalBusinessHome(refDesc.getEjbInterface());
                return genericLocalHome.create(refDesc.getEjbInterface());
            }
            return container.getEJBLocalHome();
        }

        if (refDesc.isEJB30ClientView() && !(jndiObj instanceof RemoteBusinessWrapperBase)) {
            return EJBUtils.lookupRemote30BusinessObject(jndiObj, refDesc.getEjbInterface());
        }
        return jndiObj;
    }


    public static Object lookupRemote30BusinessObject(Object jndiObj, String businessInterface) throws NamingException {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class<?> genericEJBHome = loadGeneratedGenericEJBHomeClass(loader);
            final Object genericHomeObj = PortableRemoteObject.narrow(jndiObj, genericEJBHome);

            // The generated remote business interface and the
            // client wrapper for the business interface are produced
            // dynamically. The following call must be made before
            // any EJB 3.0 Remote business interface runtime behavior
            // is needed in a given JVM.
            loadGeneratedRemoteBusinessClasses(businessInterface);

            String generatedRemoteIntfName = RemoteGenerator.getGeneratedRemoteIntfName(businessInterface);
            Method createMethod = genericEJBHome.getMethod("create", String.class);
            java.rmi.Remote delegate = (java.rmi.Remote) createMethod.invoke(genericHomeObj, generatedRemoteIntfName);

            // TODO Bring over appclient security exception retry logic CR 6620388
            return createRemoteBusinessObject(loader, businessInterface, delegate);
        } catch (Exception e) {
            NamingException ne = new NamingException(
                "ejb ref resolution error for remote business interface" + businessInterface);
            ne.initCause(e instanceof InvocationTargetException ? e.getCause() : e);
            throw ne;
        }
    }


    public static Class loadGeneratedSerializableClass(ClassLoader loader, String className) throws Exception {
        String generatedSerializableClassName = AsmSerializableBeanGenerator.getGeneratedSerializableClassName(className);
        Class developerClass = loader.loadClass(className);
        AsmSerializableBeanGenerator gen =
            new AsmSerializableBeanGenerator(loader, developerClass, generatedSerializableClassName);
        return gen.generateSerializableSubclass();
    }


    public static void loadGeneratedRemoteBusinessClasses(String businessInterfaceName) throws Exception {
        ClassLoader appClassLoader = getBusinessIntfClassLoader(businessInterfaceName);
        loadGeneratedRemoteBusinessClasses(appClassLoader, businessInterfaceName);
    }


    /**
     * @param appClassLoader - used to verify existence of classes and for generating too.
     * @param businessInterfaceName - this class must exist
     * @return full class name of the generated remote interface
     * @throws Exception
     */
    public static String loadGeneratedRemoteBusinessClasses(ClassLoader appClassLoader, String businessInterfaceName)
        throws Exception {
        String generatedRemoteIntfName = RemoteGenerator.getGeneratedRemoteIntfName(businessInterfaceName);
        String wrapperClassName = Remote30WrapperGenerator.getGeneratedRemoteWrapperName(businessInterfaceName);
        Class<?> generatedRemoteIntf = loadClassIgnoringExceptions(appClassLoader, generatedRemoteIntfName);
        Class<?> generatedRemoteWrapper = loadClassIgnoringExceptions(appClassLoader, wrapperClassName);
        if (generatedRemoteIntf != null && generatedRemoteWrapper != null) {
            return generatedRemoteIntfName;
        }

        Wrapper._setClassLoader(appClassLoader);
        try {
            if (generatedRemoteIntf == null) {
                RemoteGenerator generator = new RemoteGenerator(appClassLoader, businessInterfaceName);
                generateAndLoad(generator, appClassLoader);
            }
            if (generatedRemoteWrapper == null) {
                Remote30WrapperGenerator generator
                    = new Remote30WrapperGenerator(appClassLoader, businessInterfaceName, generatedRemoteIntfName);
                generateAndLoad(generator, appClassLoader);
            }
        } finally {
            // Make sure no classloader is bound to threadlocal: avoid possible classloader leak.
            Wrapper._setClassLoader(null) ;
        }
        return generatedRemoteIntfName;
    }


    public static Class<?> loadGeneratedGenericEJBHomeClass(final ClassLoader appClassLoader) throws Exception {
        final Class<?> generatedGenericEJBHomeClass = loadClassIgnoringExceptions(appClassLoader, GENERIC_HOME_CLASSNAME);
        if (generatedGenericEJBHomeClass != null) {
            return generatedGenericEJBHomeClass;
        }
        final GenericHomeGenerator generator = new GenericHomeGenerator();
        return generateAndLoad(generator, appClassLoader);
    }


    public static Class<?> generateSEI(ClassGeneratorFactory cgf, ClassLoader loader) {
        Class<?> clazz = loadClassIgnoringExceptions(loader, cgf.getGeneratedClassName());
        if (clazz != null) {
            return clazz;
        }
        return generateAndLoad(cgf, loader);
    }


    /**
     * Checks if the class wasn't already generated by another thread and if not, generates it.
     * The class name is retrieved from {@link ClassGeneratorFactory#getGeneratedClassName()}
     * and if it wasn't found, generator knows it's definition.
     */
    // made package visible just for tests
    static synchronized Class<?> generateAndLoad(final ClassGeneratorFactory generator, final ClassLoader loader) {
        Class<?> clazz = loadClassIgnoringExceptions(loader, generator.getGeneratedClassName());
        if (clazz != null) {
            return clazz;
        }

        generator.evaluate();

        final Properties props = new Properties();
        if (_logger.isLoggable(Level.FINEST)) {
            props.put(DUMP_AFTER_SETUP_VISITOR, "true");
            props.put(TRACE_BYTE_CODE_GENERATION, "true");
            props.put(USE_ASM_VERIFIER, "true");
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos);
                Wrapper._sourceCode(ps, props);
                _logger.fine(baos.toString());
            } catch (Exception e) {
                _logger.log(Level.SEVERE, "Exception generating src for logs", e);
            }
        }

        if (System.getSecurityManager() == null) {
            return Wrapper._generate(loader, generator.getAnchorClass().getProtectionDomain(), props);
        }
        PrivilegedAction<Class<?>> action = () ->
            Wrapper._generate(loader, generator.getAnchorClass().getProtectionDomain(), props);
        return AccessController.doPrivileged(action);
    }

    private static Class<?> loadClassIgnoringExceptions(ClassLoader classLoader, String className) {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            _logger.log(FINE, "Could not load class: " + className + " by classloader " + classLoader, e);
            return null;
        }
    }

    public static RemoteBusinessWrapperBase createRemoteBusinessObject
        (String businessInterface, java.rmi.Remote delegate)
        throws Exception {

        ClassLoader appClassLoader =
            getBusinessIntfClassLoader(businessInterface);

        return createRemoteBusinessObject(appClassLoader,
                                          businessInterface, delegate);
    }


    public static RemoteBusinessWrapperBase createRemoteBusinessObject(ClassLoader loader, String businessInterface,
        java.rmi.Remote delegate) throws Exception {
        String wrapperClassName = Remote30WrapperGenerator.getGeneratedRemoteWrapperName(businessInterface);
        Class clientWrapperClass = loader.loadClass(wrapperClassName);
        Constructor ctors[] = clientWrapperClass.getConstructors();
        Constructor ctor = null;
        for (Constructor next : ctors) {
            if (next.getParameterTypes().length > 0) {
                ctor = next;
                break;
            }
        }
        Object obj = null;
        if (ctor != null) {
            obj = ctor.newInstance(delegate, businessInterface);
        }
        return (RemoteBusinessWrapperBase) obj;
    }


    private static ClassLoader getBusinessIntfClassLoader
        (String businessInterface) throws Exception {

        ClassLoader contextLoader = null;
        if(System.getSecurityManager() == null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            contextLoader = (cl != null) ? cl :
                ClassLoader.getSystemClassLoader();
        } else {
            contextLoader = (ClassLoader)
            java.security.AccessController.doPrivileged
                    (new java.security.PrivilegedAction() {
                @Override
                public java.lang.Object run() {
                    // Return context class loader.  If there is none,
                    // which could happen within Appclient container,
                    // return system class loader.
                    ClassLoader cl =
                            Thread.currentThread().getContextClassLoader();
                    return (cl != null) ? cl :
                        ClassLoader.getSystemClassLoader();

                }});
        }

        final Class businessInterfaceClass =
            contextLoader.loadClass(businessInterface);

        ClassLoader appClassLoader = null;
        if(System.getSecurityManager() == null) {
            appClassLoader = businessInterfaceClass.getClassLoader();
        } else {
            appClassLoader = (ClassLoader)
            java.security.AccessController.doPrivileged
                    (new java.security.PrivilegedAction() {
                @Override
                public java.lang.Object run() {
                    return businessInterfaceClass.getClassLoader();

                }});
        }

        return appClassLoader;
    }

    public static void serializeObjectFields(
                                             Object instance,
                                             ObjectOutputStream oos)
        throws IOException {

        serializeObjectFields(instance, oos, true);
    }

    public static void serializeObjectFields(
                                             Object instance,
                                             ObjectOutputStream oos,
                                             boolean usesSuperClass)
        throws IOException {

        Class clazz = (usesSuperClass)? instance.getClass().getSuperclass() : instance.getClass();
        final ObjectOutputStream objOutStream = oos;

        // Write out list of fields eligible for serialization in sorted order.
        for(Field next : getSerializationFields(clazz)) {

            final Field nextField = next;
            final Object theInstance = instance;
                Object value = null;
            try {
                if(System.getSecurityManager() == null) {
                    if( !nextField.isAccessible() ) {
                        nextField.setAccessible(true);
                    }
                    value = nextField.get(theInstance);
                } else {
                    value = java.security.AccessController.doPrivileged(
                            new java.security.PrivilegedExceptionAction() {
                        @Override
                        public java.lang.Object run() throws Exception {
                            if( !nextField.isAccessible() ) {
                                nextField.setAccessible(true);
                            }
                            return nextField.get(theInstance);
                        }
                    });
                }
                if( _logger.isLoggable(FINE) ) {
                    _logger.log(FINE, "=====> Serializing field: " + nextField);
                }

                objOutStream.writeObject(value);
            } catch(Throwable t) {
                if( _logger.isLoggable(FINE) ) {
                    _logger.log(FINE, "=====> failed serializing field: " + nextField +
                             " =====> of class: " + clazz + " =====> using: " + oos.getClass() +
                             " =====> serializing value of type: " + ((value == null)? null : value.getClass().getName()) +
                             " ===> Error: " + t);
                    _logger.log(FINE, "", t);
                }
                IOException ioe = new IOException();
                Throwable cause = (t instanceof InvocationTargetException) ?
                    ((InvocationTargetException)t).getCause() : t;
                ioe.initCause( cause );
                throw ioe;
            }
        }
    }

    public static void deserializeObjectFields(
                                               Object instance,
                                               ObjectInputStream ois)
        throws IOException {

        deserializeObjectFields(instance, ois, null, true);

    }

    public static void deserializeObjectFields(
                                               Object instance,
                                               ObjectInputStream ois,
                                               Object replaceValue,
                                               boolean usesSuperClass)
        throws IOException {

        Class clazz = (usesSuperClass)? instance.getClass().getSuperclass() : instance.getClass();
        if( _logger.isLoggable(FINE) ) {
            _logger.log(FINE, "=====> Deserializing class: " + clazz);
            if (replaceValue != null) {
                _logger.log(FINE, "=====> Replace requested for value: " + replaceValue.getClass());
            }
        }

        // Use helper method to get sorted list of fields eligible
        // for deserialization.  This ensures that we correctly match
        // serialized state with its corresponding field.
        for(Field next : getSerializationFields(clazz)) {

            try {

                final Field nextField = next;
                if( _logger.isLoggable(FINE) ) {
                    _logger.log(FINE, "=====> Deserializing field: " + nextField);
                }

                // Read value from the stream even if it is to be replaced to adjust the pointers
                Object value = ois.readObject();
                if (replaceValue != null && nextField.getType().isAssignableFrom(replaceValue.getClass())) {
                    if( _logger.isLoggable(FINE) ) {
                        _logger.log(FINE, "=====> Replacing field: " + nextField);
                    }

                    value = replaceValue;
                }
                final Object newValue = value;
                final Object theInstance = instance;

                if(System.getSecurityManager() == null) {
                    if( !nextField.isAccessible() ) {
                        nextField.setAccessible(true);
                    }
                    nextField.set(theInstance, newValue);
                } else {
                    java.security.AccessController.doPrivileged(
                            new java.security.PrivilegedExceptionAction() {
                        @Override
                        public java.lang.Object run() throws Exception {
                            if( !nextField.isAccessible() ) {
                                nextField.setAccessible(true);
                            }
                            nextField.set(theInstance, newValue);
                            return null;
                        }
                    });
                }
            } catch(Throwable t) {
                IOException ioe = new IOException();
                Throwable cause = (t instanceof InvocationTargetException) ?
                    ((InvocationTargetException)t).getCause() : t;
                ioe.initCause( cause );
                throw ioe;
            }
        }
    }

    private static Collection<Field> getSerializationFields(Class clazz) {

        Field[] fields = clazz.getDeclaredFields();

        SortedMap<String, Field> sortedMap = new TreeMap<>();

        for(Field next : fields) {

            int modifiers = next.getModifiers();
            if( Modifier.isStatic(modifiers) ||
                Modifier.isTransient(modifiers) ) {
                continue;
            }

            // All fields come from a single class(not from any superclasses),
            // so sorting on field name is sufficient.  We use natural ordering
            // of field name java.lang.String object.
            sortedMap.put(next.getName(), next);

        }

        return sortedMap.values();
    }

}
