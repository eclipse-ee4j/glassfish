/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.ejb.deployment.descriptor;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.ejb.deployment.descriptor.runtime.IASEjbCMPFinder;
import org.glassfish.ejb.deployment.descriptor.runtime.PrefetchDisabledDescriptor;

/**
 * This class contains information about EJB1.1 and EJB2.0 CMP EntityBeans.
 */
public  class IASEjbCMPEntityDescriptor extends EjbCMPEntityDescriptor {

    private static final long serialVersionUID = 1L;
    private transient Class<?> ejbClass;
    private String pcImplClassName;
    private String concreteImplClassName;
    private String ejbImplClassName;
    private String mappingProperties;
    private transient ClassLoader jcl;
    private String uniqueName;

    private String moduleDir;

    // for i18N
    private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(IASEjbCMPEntityDescriptor.class);
    private static final Logger _logger = DOLUtils.getDefaultLogger();

    // Standard String and Character variables.
    private static final char DOT                  = '.';   // NOI18N
    private static final char LIST_START           = '(';// NOI18N
    private static final char LIST_END             = ')';   // NOI18N
    private static final char LIST_SEPARATOR       = ',';   // NOI18N
    private static final char NAME_PART_SEPARATOR  = '_';   // NOI18N
    private static final char NAME_CONCATENATOR    = ' ';   // NOI18N
    private static final String FIND               = "find"; // NOI18N
    private static final String EJB_SELECT         = "ejbSelect"; // NOI18N
    private static final String JDOSTATE           = "_JDOState"; // NOI18N
    private static final String CONCRETE_IMPL      = "_ConcreteImpl"; // NOI18N
    private static final String MAPPINGEXT         = DOT + "mapping"; // NOI18N

    private transient Collection<Method> finders;
    private transient Collection<Method> selectors;
    private transient QueryParser queryParser;
    private PrefetchDisabledDescriptor prefetchDisabledDescriptor;
    private static final Map<String, String> conversionTable = createConversionTable();
    private final Map<String, IASEjbCMPFinder> oneOneFinders = new HashMap<>();
    private final List<IASEjbCMPFinder> arrOneOneFinders = new ArrayList<>();

    private void addAllInterfaceMethodsIn(Collection<Method> methodDescriptors, Class<?> c) {
        Method[] methods = c.getMethods();
        for (Method method : methods) {
            methodDescriptors.add(method);
        }
    }

    private void addAllUniqueInterfaceMethodsIn(Collection<Method> methodDescriptors, Class<?> c) {
        Method[] methods = c.getMethods();
        for (Method method : methods) {
            if (findEquivalentMethod(methodDescriptors, method) == null) {
                methodDescriptors.add(method);
            }
        }
    }

    public Collection<Method> getAllUniqueMethods() {
        HashSet<Method> methods = new HashSet<>();
        try {
            if (isRemoteInterfacesSupported()) {
                addAllUniqueInterfaceMethodsIn(methods, jcl.loadClass(getHomeClassName()));
                addAllUniqueInterfaceMethodsIn(methods, jcl.loadClass(getRemoteClassName()));
            }
            if (isLocalInterfacesSupported()) {
                addAllUniqueInterfaceMethodsIn(methods, jcl.loadClass(getLocalHomeClassName()));
                addAllUniqueInterfaceMethodsIn(methods, jcl.loadClass(getLocalClassName()));
            }
        } catch (Throwable t) {
            _logger.log(Level.WARNING, "enterprise.deployment_error_loading_class_excp", t);
            throw new RuntimeException(t.getMessage());
        }
        return methods;

    }

    public Collection<Method> getAllMethods() {
        HashSet<Method> methods = new HashSet<>();
        try {
            if (isRemoteInterfacesSupported()) {
                addAllInterfaceMethodsIn(methods, jcl.loadClass(getHomeClassName()));
                addAllInterfaceMethodsIn(methods, jcl.loadClass(getRemoteClassName()));
            }

            if (isLocalInterfacesSupported()) {
                addAllInterfaceMethodsIn(methods, jcl.loadClass(getLocalHomeClassName()));
                addAllInterfaceMethodsIn(methods, jcl.loadClass(getLocalClassName()));
            }
        } catch (Throwable t) {
            _logger.log(Level.WARNING, "enterprise.deployment_error_loading_class_excp", t);
            throw new RuntimeException(t.getMessage());
        }
        return methods;
    }


    private Method findEquivalentMethod(Collection<Method> methods, Method methodToMatch) {
        if (methods == null) {
            return null;
        }

        for (Method method : methods) {
            // Compare methods, ignoring declaring class.
            if (methodsEqual(method, methodToMatch, false)) {
                return method;
            }
        }
        return null;
    }


    /**
     * Checks whether two methods that might have been loaded by
     * different class loaders are equal.
     *
     * @param compareDeclaringClass if true, declaring class will
     *            be considered as part of equality test.
     */
    private boolean methodsEqual(Method m1, Method m2, boolean compareDeclaringClass) {
        boolean equal = false;

        do {
            String m1Name = m1.getName();
            String m2Name = m2.getName();

            if (!m1Name.equals(m2Name)) {
                break;
            }

            String m1DeclaringClass = m1.getDeclaringClass().getName();
            String m2DeclaringClass = m2.getDeclaringClass().getName();

            if (compareDeclaringClass) {
                if (!m1DeclaringClass.equals(m2DeclaringClass)) {
                    break;
                }
            }

            Class<?>[] m1ParamTypes = m1.getParameterTypes();
            Class<?>[] m2ParamTypes = m2.getParameterTypes();

            if (m1ParamTypes.length != m2ParamTypes.length) {
                break;
            }

            equal = true;
            for (int pIndex = 0; pIndex < m1ParamTypes.length; pIndex++) {
                String m1ParamClass = m1ParamTypes[pIndex].getName();
                String m2ParamClass = m2ParamTypes[pIndex].getName();
                if (!m1ParamClass.equals(m2ParamClass)) {
                    equal = false;
                    break;
                }
            }

        } while (false);

        return equal;
    }

    /**
     * The method returns the class instance for the ejb class.
     * @return ejb class
     */
    private Class<?> getEjbClass() {
        if (ejbClass == null) {
            String ejbClassName = getEjbClassName();
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("@@@@@@ Ejb name is  " + ejbClassName);
            }
            if (jcl == null) {
                String msg = localStrings.getLocalString("enterprise.deployment.error_missing_classloader",
                    "IASEjbCMPEntityDescriptor.getEjbClass");
                _logger.log(Level.WARNING, msg);
                throw new RuntimeException(msg);
            }

            try {
                ejbClass = Class.forName(ejbClassName, true, jcl);
            } catch (ClassNotFoundException e) {
                String msg = localStrings.getLocalString("enterprise.deployment.error_cannot_find_ejbclass", // NOI18N
                    ejbClassName);
                _logger.log(Level.WARNING, msg);
                throw new RuntimeException(msg);
            }
        }
        return ejbClass;
    }

    /**
     * @return collection of finder method instances.
     */
    public Collection<Method> getFinders() {
        if (finders == null) {
            String ejbClassName = getEjbClassName();
            Class<?> ejbClass = getEjbClass();

            if (super.isRemoteInterfacesSupported()) {
                Class<?> remoteHomeIntf = null;
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.fine("@@@@@@ " + ejbClassName + // NOI18N
                        " : Remote Interface is supported "); // NOI18N
                }

                try {
                    remoteHomeIntf = ejbClass.getClassLoader().loadClass(super.getHomeClassName());
                } catch (ClassNotFoundException ex) {
                    _logger.log(Level.WARNING, "enterprise.deployment_class_not_found", ex); // NOI18N

                    return null;
                }

                finders = getFinders(remoteHomeIntf);
                if (_logger.isLoggable(Level.FINE)) {
                    for (Object finder : finders) {
                        Method remoteHomeMethod = (Method) finder;
                        _logger.fine("@@@@ adding Remote interface method " + // NOI18N
                            remoteHomeMethod.getName());
                    }
                }
            } // end of isRemoteInterfaceSupported

            if (super.isLocalInterfacesSupported()) {
                Class<?> localHomeIntf = null;

                if (_logger.isLoggable(Level.FINE)) {
                    _logger.fine("@@@@@@ " + ejbClassName + ":  Local Interface is supported "); // NOI18N
                }

                try {
                    localHomeIntf = ejbClass.getClassLoader().loadClass(
                        super.getLocalHomeClassName());
                } catch (ClassNotFoundException ex) {
                    _logger.log( Level.WARNING,
                         "enterprise.deployment_class_not_found", ex ); //NOI18N
                    return null;
                }

                Collection<Method> localFinders = getFinders(localHomeIntf);
                if(finders == null) {
                    // if there were no finders specified in the remote
                    // home, the local finders are the finders
                    finders = localFinders;
                } else if (localFinders != null) {
                    // Remove the Common Elements from the collections
                    // and keep only unique methods
                    if (_logger.isLoggable(Level.FINE)) {
                        _logger.fine("@@@@@@ Trying to remove the Common Elements from HashSet....... "); // NOI18N
                    }

                    for (Object localFinder : localFinders) {
                        Method localHomeMethod = (Method) localFinder;
                        if (findEquivalentMethod(finders, localHomeMethod) == null) {
                            if (_logger.isLoggable(Level.FINE)) {
                                _logger.fine("@@@@ adding local interface method " + // NOI18N
                                    localHomeMethod.getName());
                            }
                            finders.add(localHomeMethod);
                        }
                    }
                }
            } //end of isLocalInterfaceSupported

            if (finders == null) {
                // still not initialized => empty set
                finders = new HashSet<>();
            }
        }

        return finders;
    }

    /**
     * Returns a collection of finder methods declared by the home
     * interface given by a class object.
     */
    public Collection<Method> getFinders(Class<?> homeIntf) {
        Method[] methods = homeIntf.getMethods();
        Collection<Method> finders = new HashSet<>();
        for (Method method : methods) {
            if (method.getName().startsWith(FIND)) {
                finders.add(method);
            }
        }

        return finders;
    }

    public void setClassLoader(ClassLoader jcl) {
        this.jcl = jcl;
    }

    public ClassLoader getClassLoader() {
        return jcl;
    }

    public Collection<PersistentFieldInfo> getAllPersistentFields() {
        PersistenceDescriptor pers = getPersistenceDescriptor();
        PersistentFieldInfo[] persFields = pers.getPersistentFieldInfo();
        PersistentFieldInfo[] pkeyFields = pers.getPkeyFieldInfo();
        HashMap<String, PersistentFieldInfo> fields = new HashMap<>();

        for (PersistentFieldInfo persField : persFields) {
            fields.put(persField.name, persField);
        }

        for (PersistentFieldInfo pkeyField : pkeyFields) {
            fields.put(pkeyField.name, pkeyField);
        }

        return fields.values();
    }

    public Collection<PersistentFieldInfo> getPersistentFields() {

        PersistenceDescriptor pers = getPersistenceDescriptor();
        PersistentFieldInfo[] persFields = pers.getPersistentFieldInfo();

        HashMap<String, PersistentFieldInfo> fields = new HashMap<>();

        for (PersistentFieldInfo persField : persFields) {
            fields.put(persField.name, persField);
        }

        return fields.values();
    }


    public Collection<PersistentFieldInfo> getPrimaryKeyFields() {

        PersistenceDescriptor pers = getPersistenceDescriptor();
        PersistentFieldInfo[] pkeyFields = pers.getPkeyFieldInfo();

        HashMap<String, PersistentFieldInfo> pkey = new HashMap<>();
        for (PersistentFieldInfo pkeyField : pkeyFields) {
            pkey.put(pkeyField.name, pkeyField);
        }

        return pkey.values();

    }

    /**
     * Returns a collection of selector methods.
     */
    public Collection<Method> getSelectors() {
        if (selectors == null) {
            selectors = new HashSet<>();
            Class<?> ejbClass = getEjbClass();
            Method[] methods = ejbClass.getMethods();
            for (Method method : methods) {
                if(method.getName().startsWith(EJB_SELECT)) { //NOI18N
                    selectors.add(method);
                }
            }
        }

        return selectors;
    }



    public String getBaseName(String className) {
        if (className == null) {
            return null;
        }

        int dot = className.lastIndexOf(DOT);
        if (dot == -1) {
            return className;
        }
        return className.substring(dot+1);
    }

    public IASEjbCMPEntityDescriptor() {
    }

    /**
     * The copy constructor.Hopefully we wont need it;)
     */
    public IASEjbCMPEntityDescriptor(EjbDescriptor other) {
        super(other);
        setPersistenceType(CONTAINER_PERSISTENCE);
    }


    /**
     * Sets the State class implementation classname.
     */
    public void setPcImplClassName(String name) {
        pcImplClassName = name;
    }

    public String getUniqueName() {
        if (uniqueName == null) {
            BundleDescriptor bundle = getEjbBundleDescriptor();
            Application application = bundle.getApplication();

            // Add ejb name and application name.
            StringBuilder rc = new StringBuilder().append(getName()).append(NAME_CONCATENATOR)
                .append(application.getRegistrationName());

            // If it's not just a module, add a module name.
            if (!application.isVirtual()) {
                rc.append(NAME_CONCATENATOR).append(bundle.getModuleDescriptor().getArchiveUri());
            }

            uniqueName = getBaseName(getEjbClassName()) + getUniqueNumber(rc.toString());
        }

        return uniqueName;
    }


    public String getUniqueNumber(String num) {
        // Modified to decrease the possibility of collision
        String newNum = "" + num.hashCode(); // NOI18N
        newNum = newNum.replace('-', NAME_PART_SEPARATOR); // NOI18N
        return newNum;
    }


    public String getPcImplClassName() {
        if (pcImplClassName == null) {
            // Check for Null added
            pcImplClassName = getUniqueName() + JDOSTATE;
            String packageName = getPackageName(getEjbClassName());
            if (packageName != null) {
                pcImplClassName = packageName + DOT + pcImplClassName;
            }

            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("##### PCImplClass Name is " + pcImplClassName); // NOI18N
            }
        }
        return pcImplClassName;
    }


    /**
     * Sets the State class implementation classname.
     */
    public void setConcreteImplClassName(String name) {
        concreteImplClassName = name;
    }


    public String getPackageName(String className) {
        int dot = className.lastIndexOf(DOT);
        if (dot == -1) {
            return null;
        }
        return className.substring(0, dot);
    }


    /** IASRI 4725194
     * Returns the Execution class, which is sam as the user-specified class
     * in case of Message, Session and Bean Managed Persistence Entity Beans
     * but is different for Container Mananged Persistence Entity Bean
     * Therefore, the implementation in the base class is to return
     * getEjbClassName() and the method is redefined in IASEjbCMPDescriptor.
     *
     */
    @Override
    public String getEjbImplClassName() {
        if (ejbImplClassName == null) {
            String packageName = getPackageName(getEjbClassName());
            ejbImplClassName = getConcreteImplClassName();
            if (packageName != null) {
                ejbImplClassName = packageName + DOT + ejbImplClassName;
            }
        }
        return ejbImplClassName;
    }


    /**
     * Returns the classname of the State class impl.
     */

    public String getConcreteImplClassName() {
        if (concreteImplClassName == null) {
            // The Ear may contain two jar files with beans with same ejb names
            concreteImplClassName = getUniqueName() + CONCRETE_IMPL;
        }

        return concreteImplClassName;
    }


    public void setModuleDir(String moduleRootDir) {
        moduleDir = moduleRootDir;
    }

    /**
    * Returns the Module root of this module.
    */
    public String getModuleDir() {
        // FIXME:this needs to be changed when the API is available.
        if (moduleDir == null) {
            return null;
        }
        return moduleDir;
    }

    public void setMappingProperties(String mappingProperties) {
         this.mappingProperties = mappingProperties;
    }


    /**
     * Returns the classname of the State class impl.
     */
    public String  getMappingProperties() {
         return mappingProperties;
    }


    /**
     * This method sets the parser which would be used to parse the query
     * parameter declaration given in sun-ejb-jar.xml.
     * This method is called from JDOCodenerator class 's generate() method.
     */
    public void setQueryParser(QueryParser inParser) {
        queryParser = inParser;
    }

    /**
     * Returns the query parser object
     */
    public QueryParser getQueryParser() {
        return queryParser;
    }

    /**
     * This method returns the conversion table which maps the unqualified
     * name (e.g., String) of the java.lang classes to their fully qualified
     * name (e.g., java.lang.String)
     */
    private static Map<String, String> createConversionTable () {
        HashMap<String, String> conversionTable = new HashMap<>();
        conversionTable.put("Boolean", "java.lang.Boolean"); //NOI18N
        conversionTable.put("Byte", "java.lang.Byte"); //NOI18N
        conversionTable.put("Character", "java.lang.Character"); //NOI18N
        conversionTable.put("Double", "java.lang.Double"); //NOI18N
        conversionTable.put("Float", "java.lang.Float"); //NOI18N
        conversionTable.put("Integer", "java.lang.Integer"); //NOI18N
        conversionTable.put("Long", "java.lang.Long"); //NOI18N
        conversionTable.put("Number", "java.lang.Number"); //NOI18N
        conversionTable.put("Short", "java.lang.Short"); //NOI18N
        conversionTable.put("String", "java.lang.String"); //NOI18N
        conversionTable.put("Object", "java.lang.Object"); //NOI18N
        return conversionTable;
    }

    private String getFullyQualifiedType(String type) {
        String knownType=conversionTable.get(type);
        return knownType == null ? type : knownType;
    }

     /**
      * Getter for prefetch-disabled
      * @return Value of prefetchDisabledDescriptor
      */
    public PrefetchDisabledDescriptor getPrefetchDisabledDescriptor() {
        return prefetchDisabledDescriptor;
    }

    /**
     * Setter for prefetch-disabled
     * @param prefetchDisabledDescriptor
     * New value of prefetchDisabledDescriptor.
     */
    public void setPrefetchDisabledDescriptor(
        PrefetchDisabledDescriptor prefetchDisabledDescriptor) {
        this.prefetchDisabledDescriptor = prefetchDisabledDescriptor;
    }


    /**
     * Adds the given OneOneFinder to the HashMap
     * @Param finder represents the EJB 1.1 Finder
     */
    public void addOneOneFinder(IASEjbCMPFinder finder) {
        arrOneOneFinders.add(finder);
    }

    /**
     * Returns a Map which maps between a method signature and the
     * corresponding IASEjbCMPFinder instance. The key is the method
     * signature as a string and consists of methodName(type1, type2.....).
     */
    public Map<String, IASEjbCMPFinder> getOneOneFinders() {
        // update the oneOneFinders map if there are any entries pending in
        // the array arrOneOneFinders.
        if (!arrOneOneFinders.isEmpty()) {
            if (queryParser == null) {
                String msg = localStrings.getLocalString("enterprise.deployment.error_missing_queryparser", // NOI18N
                    "IASEjbCMPEntityDescriptor.getOneOneFinders"); // NOI18N
                _logger.log(Level.WARNING, msg);
                throw new RuntimeException(msg);
            }

            // parse the query declaration parameter and store the query object
            for (IASEjbCMPFinder arrOneOneFinder : arrOneOneFinders) {
                IASEjbCMPFinder finder = arrOneOneFinder;
                String key = generateKey(finder, queryParser);
                oneOneFinders.put(key, finder);
            }
            arrOneOneFinders.clear();
        }
        return oneOneFinders;
    }


    /**
     * @return the key used to store 1.1 Finder Object.
     * the key is methodName(param0, param1.....)
     * @param finder is the object which represents the EJB 1.1 Finder
     */
    private String generateKey(IASEjbCMPFinder finder, QueryParser parser) {
        StringBuilder key = new StringBuilder();
        key.append(finder.getMethodName()).append(LIST_START);

        String queryParams = finder.getQueryParameterDeclaration();
        Iterator<String> iter = parser.parameterTypeIterator(queryParams);
        while (iter.hasNext()) {
            String type = iter.next();
            key.append(getFullyQualifiedType(type));
            if (iter.hasNext()) {
                key.append(LIST_SEPARATOR);
            }
        }
        key.append(LIST_END);

        return key.toString().intern();
    }


    /**
     * @param method object for which the Finder Object needs to be found
     * @return The finder object for the particular Method object.
     */
    public IASEjbCMPFinder getIASEjbCMPFinder(Method method) {
        // Checks if the given method is present in the interfaces.
        if (findEquivalentMethod(getFinders(), method) == null) {
            return null;
        }
        String methodName = method.getName();

        // key is of the form methodName(param0, param1, ....)
        StringBuffer key = new StringBuffer();
        key.append(methodName);
        key.append(LIST_START);
        Class<?> paramList[] = method.getParameterTypes();
        for (int index = 0; index < paramList.length; index++) {
            if (index > 0) {
                key.append(LIST_SEPARATOR);
            }
            key.append(paramList[index].getName());
        }
        key.append(LIST_END);
        return getOneOneFinders().get(key.toString());
    }
}
