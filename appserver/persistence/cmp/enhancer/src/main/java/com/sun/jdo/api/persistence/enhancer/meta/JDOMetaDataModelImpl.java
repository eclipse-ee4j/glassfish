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

package com.sun.jdo.api.persistence.enhancer.meta;

import com.sun.jdo.api.persistence.enhancer.util.Support;
import com.sun.jdo.api.persistence.model.Model;
import com.sun.jdo.api.persistence.model.jdo.PersistenceClassElement;
import com.sun.jdo.api.persistence.model.jdo.PersistenceFieldElement;
import com.sun.jdo.api.persistence.model.jdo.RelationshipElement;

import java.io.PrintWriter;


/**
 * Provides the JDO meta information based on a JDO meta model.
 */
//@olsen: new class
public class JDOMetaDataModelImpl extends Support implements JDOMetaData {

    // misc
    protected final PrintWriter out;

    // model
    protected Model model;

    /**
     * Creates an instance.
     */
    public JDOMetaDataModelImpl(Model model) {
        this(model, null);
    }


    public JDOMetaDataModelImpl(Model model, PrintWriter out) throws JDOMetaDataUserException, JDOMetaDataFatalError {
        // check arguments
        if (model == null) {
            final String msg = "Initializing meta data: model == null";// NOI18N
            throw new JDOMetaDataFatalError(msg);
        }
        this.model = model;
        this.out = out;
    }


    /**
     * Tests whether a class is known to be transient.
     */
    @Override
    public boolean isTransientClass(String classPath) throws JDOMetaDataUserException, JDOMetaDataFatalError {
        // ^olsen: delegate to Model once supported
        if (classPath.startsWith("java/")) {
            return true;
        }
        if (classPath.startsWith("javax/")) {
            return true;
        }
        if (classPath.startsWith("com/sun/jdo/")) {
            return true;
        }
        return false;
    }


    /**
     * Tests whether a class is known to be persistence-capable.
     */
    @Override
    public boolean isPersistenceCapableClass(String classPath) throws JDOMetaDataUserException, JDOMetaDataFatalError {
        // ^olsen: delegate to Model once supported
        if (isTransientClass(classPath)) {
            return false;
        }
        final String className = pathToName(classPath);
        return model.isPersistent(className);
    }


    /**
     * Tests whether a class is known as a persistence-capable root class.
     */
    @Override
    public boolean isPersistenceCapableRootClass(String classPath)
        throws JDOMetaDataUserException, JDOMetaDataFatalError {
        // @olsen: 4631388 - do not attempt to support inheritance right now
        final String className = pathToName(classPath);
        return model.isPersistent(className);
        // return (model.isPersistent(className)
        // && !model.hasPersistentSuperclass(className));
    }


    /**
     * Returns the name of the persistence-capable root class of a class.
     */
    @Override
    public String getPersistenceCapableRootClass(String classPath)
        throws JDOMetaDataUserException, JDOMetaDataFatalError {
        // ^olsen: exchange dummy implementation once supported by Model
        return (isPersistenceCapableClass(classPath) ? classPath : null);
    }


    /**
     * Returns the name of the superclass of a class.
     * <P>
     *
     * @param classPath the JVM-qualified name of the class
     * @return the name of the superclass.
     */
    @Override
    public String getSuperClass(String classPath) throws JDOMetaDataUserException, JDOMetaDataFatalError {
        // throw new UnsupportedOperationException ("not implemented yet");
        return null;
    }


    /**
     * Tests whether a type is known for Second Class Objects.
     */
    @Override
    public boolean isSecondClassObjectType(String classPath) throws JDOMetaDataUserException, JDOMetaDataFatalError {
        final String className = pathToName(classPath);
        return model.isSecondClassObject(className);
    }


    /**
     * Tests whether a type is known for Mutable Second Class Objects.
     */
    @Override
    public boolean isMutableSecondClassObjectType(String classPath)
        throws JDOMetaDataUserException, JDOMetaDataFatalError {
        final String className = pathToName(classPath);
        return model.isMutableSecondClassObject(className);
    }


    /**
     * Tests whether a field of a class is known to be persistent.
     */
    @Override
    public boolean isPersistentField(String classPath, String fieldName)
        throws JDOMetaDataUserException, JDOMetaDataFatalError {
        final String className = pathToName(classPath);
        return model.isPersistent(className, fieldName);
    }


    /**
     * Tests whether a field of a class is known to be transactional.
     */
    @Override
    public boolean isTransactionalField(String classPath, String fieldName)
        throws JDOMetaDataUserException, JDOMetaDataFatalError {
        // throw new UnsupportedOperationException ("not implemented yet");
        return false;
    }


    /**
     * Tests whether a field of a class is known to be Primary Key.
     */
    @Override
    public boolean isPrimaryKeyField(String classPath, String fieldName)
        throws JDOMetaDataUserException, JDOMetaDataFatalError {
        final String className = pathToName(classPath);
        return model.isKey(className, fieldName);
    }


    /**
     * Tests whether a field of a class is known to be part of the
     * Default Fetch Group. Please note that for a relationship field, this
     * method always returns false.
     */
    @Override
    public boolean isDefaultFetchGroupField(String classPath, String fieldName)
        throws JDOMetaDataUserException, JDOMetaDataFatalError {
        final String className = pathToName(classPath);
        boolean isdfgField = model.isDefaultFetchGroup(className, fieldName);
        if(isdfgField) {
            final PersistenceFieldElement pfe = model.getPersistenceField(className, fieldName);
            if (pfe instanceof RelationshipElement) {
                // This is a relationship field. Flag it as not belonging
                // to dfg.
                // Relationship fields are always flaged as not belonging to dfg
                // This assures that access to a relationship fields is always
                // mediated.
                // Please see call to this method from following for more details.
                // 1. EJBMetaDataModelImpl#getFieldFlags()
                // 2. MethodAnnotater#notePutFieldAnnotation()
                // 3. MethodAnnotater#noteGetFieldAnnotation()
                isdfgField = false;
            }
        }
        return isdfgField;
    }

    /**
     * Returns the unique field index of a declared, persistent field of a
     * class.
     */
    @Override
    public int getFieldNo(String classPath, String fieldName) throws JDOMetaDataUserException, JDOMetaDataFatalError {
        final String className = pathToName(classPath);
        final PersistenceFieldElement pfe = model.getPersistenceField(className, fieldName);
        if (pfe == null || pfe.getPersistenceType() != PersistenceFieldElement.PERSISTENT) {
            return -1;
        }

        return pfe.getFieldNumber();
    }

    /**
     * Returns an array of field names of all declared, persistent fields
     * of a class.
     */
    private final String[] getPersistentFields(String classPath)
        throws JDOMetaDataUserException, JDOMetaDataFatalError {
        final String className = pathToName(classPath);
        final PersistenceClassElement pce = model.getPersistenceClass(className);
        if (pce == null) {
            return new String[] {};
        }

        // exctract field names into result array
        final PersistenceFieldElement[] pfes = pce.getFields();
        final int nofFields = (pfes != null  ?  pfes.length  :  0);
        final String[] names = new String[nofFields];
        for (int i = 0; i < nofFields; i++) {
            final PersistenceFieldElement pfe = pfes[i];
            names[i] = pfe.getName();

            //@olsen: debugging check
            if (false) {
                if (pfe.getPersistenceType()
                    != PersistenceFieldElement.PERSISTENT) {
                    final String msg
                        = ("Getting persistent field names: " //NOI18N
                           + "Encountered non-persistent field '"//NOI18N
                           + names[i] + "' for class " + classPath);//NOI18N
                    throw new JDOMetaDataFatalError(msg);
                    //out.println(msg);
                    //names[i] = null;
                }
            }
        }
        return names;
    }


    /**
     * Returns an array of field names of all declared persistent and
     * transactional fields of a class.
     */
    @Override
    public String[] getManagedFields(String classPath) throws JDOMetaDataUserException, JDOMetaDataFatalError {
        return getPersistentFields(classPath);
    }


    static protected String pathToName(String classPath) {
        if (classPath != null) {
            return classPath.replace('/', '.');
        } else {
            return null;
        }
    }


    static protected String nameToPath(String className) {
        if (className != null) {
            return className.replace('.', '/');
        } else {
            return null;
        }
    }
}
