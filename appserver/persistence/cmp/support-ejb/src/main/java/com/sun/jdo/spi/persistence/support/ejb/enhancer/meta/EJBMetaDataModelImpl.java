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

/******************************************************************************
 *
 *  Author:  Shing Wai Chan (shingwai@iplanet.com)
 *
 *****************************************************************************/

package com.sun.jdo.spi.persistence.support.ejb.enhancer.meta;

import com.sun.jdo.api.persistence.enhancer.meta.ExtendedJDOMetaData;
import com.sun.jdo.api.persistence.enhancer.meta.JDOMetaDataFatalError;
import com.sun.jdo.api.persistence.enhancer.meta.JDOMetaDataModelImpl;
import com.sun.jdo.api.persistence.enhancer.meta.JDOMetaDataUserException;
import com.sun.jdo.api.persistence.model.Model;
import com.sun.jdo.api.persistence.model.jdo.PersistenceFieldElement;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;


/**
 * Provide MetaDataModel Class used by CMP code generation during
 * EJB deployment.
 * Note that classPath is used for I/O of MetaData and
 * className is used for I/O of Model.
 * @author Shing Wai Chan
 */
public class EJBMetaDataModelImpl extends JDOMetaDataModelImpl
        implements ExtendedJDOMetaData {


    public EJBMetaDataModelImpl(Model model)
            throws JDOMetaDataUserException, JDOMetaDataFatalError {
        super(model);
    }


    //methods from ExtendedJDOMetaData, not in JDOMetaData
    public String[] getKnownClasses()
            throws JDOMetaDataUserException, JDOMetaDataFatalError {
        throw new UnsupportedOperationException();
    }

    public String[] getKnownFields(String classPath)
            throws JDOMetaDataUserException, JDOMetaDataFatalError {
        return getManagedFields(classPath);
    }

    public String getFieldType(String classPath, String fieldName)
            throws JDOMetaDataUserException, JDOMetaDataFatalError {
        final String className = pathToName(classPath);
        String ftype = model.getFieldType(className, fieldName);

        return nameToPath(ftype);
    }

    public int getClassModifiers(String classPath)
            throws JDOMetaDataUserException, JDOMetaDataFatalError {
        return Modifier.PUBLIC;
    }

    public int getFieldModifiers(String classPath, String fieldName)
            throws JDOMetaDataUserException, JDOMetaDataFatalError {
        final String className = pathToName(classPath);
        return model.getModifiers(model.getField(className, fieldName));
    }

    public String getKeyClass(String classPath)
            throws JDOMetaDataUserException, JDOMetaDataFatalError {
        final String className = pathToName(classPath);
        String keyClass = model.getPersistenceClass(className).getKeyClass();
        if (keyClass.toLowerCase().endsWith(".oid")) {
            int ind = keyClass.lastIndexOf('.');
            keyClass = keyClass.substring(0, ind) + "$Oid";
        }
        return nameToPath(keyClass);
    }

    public boolean isKnownNonManagedField(String classPath,
            String fieldName, String fieldSig)
            throws JDOMetaDataUserException, JDOMetaDataFatalError {
        return !isPersistentField(classPath, fieldName);
    }

    public boolean isManagedField(String classPath, String fieldName)
            throws JDOMetaDataUserException, JDOMetaDataFatalError {
        return (isPersistentField(classPath, fieldName)
                || isTransactionalField(classPath, fieldName));
    }

    public boolean isKeyField(String classPath, String fieldName)
            throws JDOMetaDataUserException, JDOMetaDataFatalError {
        return isPrimaryKeyField(classPath, fieldName);
    }

    public boolean isPrimaryKeyField(String classPath, String fieldName)
        throws JDOMetaDataUserException, JDOMetaDataFatalError
    {
        final String className = pathToName(classPath);
        final PersistenceFieldElement pfe
            = model.getPersistenceField(className, fieldName);
        if (pfe != null) {
            return pfe.isKey();
        } else {
            return false;
        }
    }

    public int getFieldFlags(String classPath, String fieldName)
            throws JDOMetaDataUserException, JDOMetaDataFatalError {
        if (!isManagedField(classPath, fieldName)) {
            affirm(!isTransactionalField(classPath, fieldName));
            affirm(!isPersistentField(classPath, fieldName));
            affirm(!isKeyField(classPath, fieldName));
            affirm(!isDefaultFetchGroupField(classPath, fieldName));
            return 0;
        }
        //affirm(isManagedField(classPath, fieldName));

        if (isTransactionalField(classPath, fieldName)) {
            affirm(!isPersistentField(classPath, fieldName));
            affirm(!isKeyField(classPath, fieldName));
            // ignore any dfg membership of transactional fields
            //affirm(!isDefaultFetchGroupField(classPath, fieldName));
            return CHECK_WRITE;
        }
        //affirm(!isTransactionalField(classPath, fieldName));
        affirm(isPersistentField(classPath, fieldName));

        if (isKeyField(classPath, fieldName)) {
            // ignore any dfg membership of key fields
            //affirm(!isDefaultFetchGroupField(classPath, fieldName));
            return MEDIATE_WRITE;
        }
        //affirm(!isKeyField(classPath, fieldName));

        if (isDefaultFetchGroupField(classPath, fieldName)) {
            if (Boolean.getBoolean("AllowMediatedWriteInDefaultFetchGroup")) {
                 return CHECK_READ | MEDIATE_WRITE;
            }
            return CHECK_READ | CHECK_WRITE;
        }
        //affirm(!isDefaultFetchGroupField(classPath, fieldName));

        return MEDIATE_READ | MEDIATE_WRITE;
    }

    public int[] getFieldFlags(String classPath, String[] fieldNames)
           throws JDOMetaDataUserException, JDOMetaDataFatalError {
        final int n = (fieldNames != null ? fieldNames.length : 0);
        final int[] flags = new int[n];
        for (int i = 0; i < n; i++) {
            flags[i] = getFieldFlags(classPath, fieldNames[i]);
        }
        return flags;
    }

    public String[] getFieldType(String className, String[] fieldNames)
            throws JDOMetaDataUserException, JDOMetaDataFatalError {
        final int n = (fieldNames != null ? fieldNames.length : 0);
        final String[] types = new String[n];
        for (int i = 0; i < n; i++) {
            types[i] = getFieldType(className, fieldNames[i]);
        }
        return types;
    }

    public int[] getFieldNo(String classPath, String[] fieldNames)
            throws JDOMetaDataUserException, JDOMetaDataFatalError {
        final int n = (fieldNames != null ? fieldNames.length : 0);
        final int[] flags = new int[n];
        for (int i = 0; i < n; i++) {
            flags[i] = getFieldNo(classPath, fieldNames[i]);
        }
        return flags;
    }

    public String[] getKeyFields(String classPath)
            throws JDOMetaDataUserException, JDOMetaDataFatalError {
        final List keys = new ArrayList();
        final String[] fieldNames = getManagedFields(classPath);
        final int n = fieldNames.length;
        for (int i = 0; i < n; i++) {
            if (isKeyField(classPath, fieldNames[i])) {
                keys.add(fieldNames[i]);
            }
        }
        return (String[])keys.toArray(new String[keys.size()]);
    }

    public String getPersistenceCapableSuperClass(String classPath)
            throws JDOMetaDataUserException, JDOMetaDataFatalError {
        return null;
    }

    public String getSuperKeyClass(String classPath)
            throws JDOMetaDataUserException, JDOMetaDataFatalError {
        return null;
    }
}
