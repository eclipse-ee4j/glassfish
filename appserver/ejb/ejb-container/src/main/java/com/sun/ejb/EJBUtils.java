/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation.
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

import static com.sun.ejb.codegen.Generator.getBaseName;
import static com.sun.ejb.codegen.Generator.getFullClassName;
import static com.sun.ejb.codegen.Generator.getPackageName;
import static java.util.logging.Level.FINE;

import com.sun.ejb.codegen.AsmSerializableBeanGenerator;
import com.sun.ejb.codegen.EjbClassGeneratorFactory;
import com.sun.ejb.codegen.GeneratorException;
import com.sun.ejb.codegen.Remote30WrapperGenerator;
import com.sun.ejb.codegen.RemoteGenerator;
import com.sun.ejb.containers.BaseContainer;
import com.sun.ejb.containers.EjbContainerUtilImpl;
import com.sun.ejb.containers.GenericEJBLocalHome;
import com.sun.ejb.containers.RemoteBusinessWrapperBase;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.logging.LogDomains;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.Remote;
import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import org.glassfish.api.naming.SimpleJndiName;

/**
 * A handy class with static utility methods.
 * <p>
 * Note that much of this code has to execute in the client, so it needs to be careful about which server-only resources
 * it uses and in which code paths.
 *
 */
public class EJBUtils {

    private static final Logger _logger = LogDomains.getLogger(EJBUtils.class, LogDomains.EJB_LOGGER);

    // Internal property to force generated ejb container classes to
    // be created during deployment time instead of dynamically.
    // Note that this property does *not* cover RMI-IIOP stub generation.
    // See IASEJBC.java for more details.
    private static final String EJB_USE_STATIC_CODEGEN_PROP = "com.sun.ejb.UseStaticCodegen";

    private static final String REMOTE30_HOME_JNDI_SUFFIX = "__3_x_Internal_RemoteBusinessHome__";

    // Separator between simple and fully-qualified portable ejb global JNDI names
    private static final String PORTABLE_JNDI_NAME_SEP = "!";

    // Separator between simple and fully-qualified glassfish-specific JNDI names
    private static final String GLASSFISH_JNDI_NAME_SEP = "#";

    private static Boolean ejbUseStaticCodegen_;

    /**
     * Utility methods for serializing EJBs, primary keys and container-managed fields, all of which may include Remote EJB
     * references, Local refs, JNDI Contexts etc. which are not Serializable. This is not used for normal RMI-IIOP
     * serialization. It has boolean replaceObject control, whether to call replaceObject or not.
     */
    public static final byte[] serializeObject(Object obj, boolean replaceObject) throws IOException {
        return EjbContainerUtilImpl.getInstance().getJavaEEIOUtils().serializeObject(obj, replaceObject);
    }

    public static final byte[] serializeObject(Object obj) throws IOException {
        return EjbContainerUtilImpl.getInstance().getJavaEEIOUtils().serializeObject(obj, true);
    }

    /**
     * Utility method for deserializing EJBs, primary keys and container-managed fields, all of which may include Remote EJB
     * references, Local refs, JNDI Contexts etc. which are not Serializable.
     */
    public static final Object deserializeObject(byte[] data, ClassLoader loader, boolean resolveObject) throws Exception {
        return EjbContainerUtilImpl.getInstance().getJavaEEIOUtils().deserializeObject(data, resolveObject, loader);
    }

    public static final Object deserializeObject(byte[] data, ClassLoader loader) throws Exception {
        return EjbContainerUtilImpl.getInstance().getJavaEEIOUtils().deserializeObject(data, true, loader);
    }

    public static boolean useStaticCodegen() {
        synchronized (EJBUtils.class) {
            if (ejbUseStaticCodegen_ == null) {
                String ejbStaticCodegenProp;
                ejbStaticCodegenProp = System.getProperty(EJB_USE_STATIC_CODEGEN_PROP);

                boolean useStaticCodegen = ((ejbStaticCodegenProp != null) && ejbStaticCodegenProp.equalsIgnoreCase("true"));

                ejbUseStaticCodegen_ = useStaticCodegen;

                _logger.log(FINE, "EJB Static codegen is " + (useStaticCodegen ? "ENABLED" : "DISABLED") + " ejbUseStaticCodegenProp = "
                        + ejbStaticCodegenProp);
            }
        }

        return ejbUseStaticCodegen_;
    }

    /**
     * Prepends __EJB31_Generated__ and adds _Intf__ to the simple class name.
     *
     * @param ejbClassName full class name
     */
    public static String getGeneratedOptionalInterfaceName(String ejbClassName) {
        String packageName = getPackageName(ejbClassName);
        String simpleName = getBaseName(ejbClassName);
        String optionalIntfName = "__EJB31_Generated__" + simpleName + "__Intf__";
        return getFullClassName(packageName, optionalIntfName);
    }

    /**
     * Actual jndi-name under which Remote ejb factory lives depends on whether it's a Remote Home view or Remote Business
     * view. This is necessary since a single session bean can expose both views and the resulting factory objects are
     * different. These semantics are not exposed to the developer-view to keep things simpler. The developer can simply
     * deal with a single physical jndi-name. If the target bean exposes both a Remote Home view and a Remote Business view,
     * the developer can still use the single physical jndi-name to resolve remote ejb-refs, and we will handle the
     * distinction internally. Of course, this is based on the assumption that the internal name is generated in a way that
     * will not clash with a separate top-level physical jndi-name chosen by the developer.
     * <p>
     * Note that it's better to delay this final jndi name translation as much as possible and do it right before the
     * NamingManager lookup, as opposed to changing the jndi-name within the descriptor objects themselves. This way, the
     * extra indirection will not be exposed if the descriptors are written out and they won't complicate any jndi-name
     * equality logic.
     *
     */
    public static SimpleJndiName getRemoteEjbJndiName(EjbReferenceDescriptor refDesc) {
        String intf = refDesc.isEJB30ClientView() ? refDesc.getEjbInterface() : refDesc.getHomeClassName();
        return getRemoteEjbJndiName(refDesc.isEJB30ClientView(), intf, refDesc.getJndiName());
    }

    public static SimpleJndiName getRemote30HomeJndiName(SimpleJndiName jndiName) {
        return new SimpleJndiName(jndiName + REMOTE30_HOME_JNDI_SUFFIX);
    }

    public static SimpleJndiName getRemoteEjbJndiName(boolean businessView, String interfaceName, SimpleJndiName jndiName) {
        SimpleJndiName returnValue = jndiName;

        String portableFullyQualifiedPortion = PORTABLE_JNDI_NAME_SEP + interfaceName;
        String glassfishFullyQualifiedPortion = GLASSFISH_JNDI_NAME_SEP + interfaceName;

        if (businessView) {
            if (jndiName.hasCorbaPrefix()) {

                // In the case of a corba interoperable naming string, we
                // need to lookup the internal remote home. We can't rely
                // on our SerialContext Reference object (RemoteBusinessObjectFactory)
                // to do the home lookup because we have to directly access
                // the CosNaming service.

                // First, strip off any fully-qualified portion since there's only
                // one internal generic home object in CosNaming no matter how many
                // remote business interfaces there are.

                // Separate <jndi-name> portion from "corbaname:iiop:...#<jndi-name>
                // We need to do this since we also use "#" in some glassfish-specific
                // JNDI names
                int indexOfCorbaNameSep = jndiName.toString().indexOf("#");
                SimpleJndiName jndiNameMinusCorbaNamePortion = new SimpleJndiName(jndiName.toString().substring(indexOfCorbaNameSep + 1));

                // Make sure any of the resulting jndi names still have corbaname: prefix intact
                SimpleJndiName newJndiName = jndiName;

                if (jndiNameMinusCorbaNamePortion.isJavaGlobal()) {
                    newJndiName = stripFullyQualifiedJndiName(jndiName, portableFullyQualifiedPortion);
                } else if (jndiNameMinusCorbaNamePortion.hasSuffix(glassfishFullyQualifiedPortion)) {
                    newJndiName = stripFullyQualifiedJndiName(jndiName, glassfishFullyQualifiedPortion);
                }

                returnValue = getRemote30HomeJndiName(newJndiName);

            } else {
                // Convert to fully-qualified names
                if (jndiName.isJavaGlobal()) {
                    returnValue = checkFullyQualifiedJndiName(jndiName, portableFullyQualifiedPortion);
                } else {
                    returnValue = checkFullyQualifiedJndiName(jndiName, glassfishFullyQualifiedPortion);
                }
            }
        } else {
            // EJB 2.x Remote Home
            // Only in the portable global case, convert to a fully-qualified name
            if (jndiName.isJavaGlobal()) {
                returnValue = checkFullyQualifiedJndiName(jndiName, portableFullyQualifiedPortion);
            }
        }

        return returnValue;
    }

    private static SimpleJndiName checkFullyQualifiedJndiName(SimpleJndiName origJndiName, String fullyQualifiedPortion) {
        if (origJndiName.hasSuffix(fullyQualifiedPortion)) {
            return origJndiName;
        }
        return new SimpleJndiName(origJndiName + fullyQualifiedPortion);
    }

    private static SimpleJndiName stripFullyQualifiedJndiName(SimpleJndiName origJndiName, String fullyQualifiedPortion) {
        return origJndiName.removeSuffix(fullyQualifiedPortion);
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
            Class<?> genericEJBHome = loadGeneratedGenericEJBHomeClass(loader, jndiObj.getClass());
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
            NamingException ne = new NamingException("ejb ref resolution error for remote business interface" + businessInterface);
            ne.initCause(e instanceof InvocationTargetException ? e.getCause() : e);
            throw ne;
        }
    }

    public static Class<?> loadGeneratedSerializableClass(final ClassLoader loader, final Class<?> originalClass) {
        final String generatedClassName = AsmSerializableBeanGenerator.getGeneratedSerializableClassName(originalClass.getName());
        try {
            return loader.loadClass(generatedClassName);
        } catch (ClassNotFoundException e) {
            // Not loaded yet. Just continue
        }
        AsmSerializableBeanGenerator gen = new AsmSerializableBeanGenerator(loader, originalClass, generatedClassName);
        return gen.generateSerializableSubclass();
    }

    public static void loadGeneratedRemoteBusinessClasses(String businessInterfaceName) throws Exception {
        ClassLoader appClassLoader = getBusinessIntfClassLoader(businessInterfaceName);
        loadGeneratedRemoteBusinessClasses(appClassLoader, businessInterfaceName);
    }

    /**
     * @param appClassLoader used to verify existence of classes and for generating too.
     * @param businessInterfaceName this class must exist
     * @return full class name of the generated remote interface
     * @throws Exception if an error occurred while loading class
     */
    public static Class<?> loadGeneratedRemoteBusinessClasses(ClassLoader appClassLoader, String businessInterfaceName) throws Exception {
        try (EjbClassGeneratorFactory factory = new EjbClassGeneratorFactory(appClassLoader)) {
            return factory.ensureRemote(businessInterfaceName);
        }
    }

    public static Class<?> loadGeneratedGenericEJBHomeClass(ClassLoader appClassLoader, Class<?> anchorClass) throws GeneratorException {
        try (EjbClassGeneratorFactory factory = new EjbClassGeneratorFactory(appClassLoader)) {
            return factory.ensureGenericHome(anchorClass);
        }
    }

    public static Class<?> generateSEI(ClassLoader loader, final Class<?> ejbClass) throws GeneratorException {
        try (EjbClassGeneratorFactory factory = new EjbClassGeneratorFactory(loader)) {
            return factory.ensureServiceInterface(ejbClass);
        }
    }

    public static RemoteBusinessWrapperBase createRemoteBusinessObject(String businessInterface, Remote delegate) throws Exception {
        ClassLoader appClassLoader = getBusinessIntfClassLoader(businessInterface);
        return createRemoteBusinessObject(appClassLoader, businessInterface, delegate);
    }

    public static RemoteBusinessWrapperBase createRemoteBusinessObject(ClassLoader loader, String businessInterface, Remote delegate)
            throws Exception {
        String wrapperClassName = Remote30WrapperGenerator.getGeneratedRemoteWrapperName(businessInterface);
        Class<?> clientWrapperClass = loader.loadClass(wrapperClassName);
        Constructor<?>[] ctors = clientWrapperClass.getConstructors();
        Constructor<?> ctor = null;
        for (Constructor<?> next : ctors) {
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

    private static ClassLoader getBusinessIntfClassLoader(String businessInterface) throws Exception {
        final ClassLoader contextLoader;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        contextLoader = cl == null ? ClassLoader.getSystemClassLoader() : cl;

        final Class<?> businessInterfaceClass = contextLoader.loadClass(businessInterface);
        return businessInterfaceClass.getClassLoader();
    }

    // Warning: accessed by reflection (AsmSerializableBeanGenerator)
    public static void serializeObjectFields(Object instance, ObjectOutputStream oos) throws IOException {
        serializeObjectFields(instance, oos, true);
    }

    // warning: accessed by reflection (AsmSerializableBeanGenerator)
    public static void serializeObjectFields(Object instance, ObjectOutputStream oos, boolean usesSuperClass) throws IOException {

        Class<?> clazz = (usesSuperClass) ? instance.getClass().getSuperclass() : instance.getClass();

        // Write out list of fields eligible for serialization in sorted order.
        for (Field next : getSerializationFields(clazz)) {

            final Field nextField = next;
            final Object theInstance = instance;
            Object value = null;
            try {
                if (!nextField.trySetAccessible()) {
                    throw new InaccessibleObjectException("Unable to make accessible: " + nextField);
                }
                value = nextField.get(theInstance);

                if (_logger.isLoggable(FINE)) {
                    _logger.log(FINE, "=====> Serializing field: " + nextField);
                }

                oos.writeObject(value);
            } catch (Throwable t) {
                if (_logger.isLoggable(FINE)) {
                    _logger.log(FINE, "Failed serializing field: " + nextField + " of " + clazz + " using: " + oos.getClass()
                            + " serializing value of type: " + (value == null ? null : value.getClass().getName()) + ", cause: " + t);
                }
                throw new IOException(t instanceof InvocationTargetException ? t.getCause() : t);
            }
        }
    }

    // Note: accessed by reflection!
    public static void deserializeObjectFields(Object instance, ObjectInputStream ois) throws IOException {
        deserializeObjectFields(instance, ois, null, true);
    }

    // Note: accessed by reflection!
    public static void deserializeObjectFields(Object instance, ObjectInputStream ois, Object replaceValue, boolean usesSuperClass)
            throws IOException {
        Class<?> clazz = (usesSuperClass) ? instance.getClass().getSuperclass() : instance.getClass();
        if (_logger.isLoggable(FINE)) {
            _logger.log(FINE, "=====> Deserializing class: " + clazz);
            if (replaceValue != null) {
                _logger.log(FINE, "=====> Replace requested for value: " + replaceValue.getClass());
            }
        }

        // Use helper method to get sorted list of fields eligible
        // for deserialization. This ensures that we correctly match
        // serialized state with its corresponding field.
        for (Field next : getSerializationFields(clazz)) {

            try {

                final Field nextField = next;
                if (_logger.isLoggable(FINE)) {
                    _logger.log(FINE, "=====> Deserializing field: " + nextField);
                }

                // Read value from the stream even if it is to be replaced to adjust the pointers
                Object value = ois.readObject();
                if (replaceValue != null && nextField.getType().isAssignableFrom(replaceValue.getClass())) {
                    if (_logger.isLoggable(FINE)) {
                        _logger.log(FINE, "=====> Replacing field: " + nextField);
                    }

                    value = replaceValue;
                }
                final Object newValue = value;
                final Object theInstance = instance;

                if (!nextField.trySetAccessible()) {
                    throw new InaccessibleObjectException("Unable to make accessible: " + nextField);
                }
                nextField.set(theInstance, newValue);
            } catch (Throwable t) {
                throw new IOException(t instanceof InvocationTargetException ? t.getCause() : t);
            }
        }
    }

    private static Collection<Field> getSerializationFields(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();

        SortedMap<String, Field> sortedMap = new TreeMap<>();

        for (Field next : fields) {
            int modifiers = next.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
                continue;
            }

            // All fields come from a single class(not from any superclasses),
            // so sorting on field name is sufficient. We use natural ordering
            // of field name java.lang.String object.
            sortedMap.put(next.getName(), next);
        }

        return sortedMap.values();
    }
}