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


// @olsen: new class
public final class JDOMetaDataTimer extends Support implements JDOMetaData {

    // delegate
    final protected JDOMetaData delegate;

    /**
     * Creates an instance.
     */
    public JDOMetaDataTimer(JDOMetaData delegate) throws JDOMetaDataUserException, JDOMetaDataFatalError {
        affirm(delegate);
        this.delegate = delegate;
    }


    @Override
    public boolean isPersistenceCapableClass(String classPath) throws JDOMetaDataUserException, JDOMetaDataFatalError {
        try {
            timer.push("JDOMetaData.isPersistenceCapableClass(String)",//NOI18N
                       "JDOMetaData.isPersistenceCapableClass(" + classPath + ")");//NOI18N
            return delegate.isPersistenceCapableClass(classPath);
        } finally {
            timer.pop();
        }
    }


    @Override
    public boolean isTransientClass(String classPath) throws JDOMetaDataUserException, JDOMetaDataFatalError {
        try {
            timer.push("JDOMetaData.isTransientClass(String)",//NOI18N
                       "JDOMetaData.isTransientClass(" + classPath + ")");//NOI18N
            return delegate.isTransientClass(classPath);
        } finally {
            timer.pop();
        }
    }


    @Override
    public boolean isPersistenceCapableRootClass(String classPath)
        throws JDOMetaDataUserException, JDOMetaDataFatalError {
        try {
            timer.push("JDOMetaData.isPersistenceCapableRootClass(String)",//NOI18N
                       "JDOMetaData.isPersistenceCapableRootClass(" + classPath + ")");//NOI18N
            return delegate.isPersistenceCapableRootClass(classPath);
        } finally {
            timer.pop();
        }
    }


    @Override
    public String getSuperClass(String classPath) throws JDOMetaDataUserException, JDOMetaDataFatalError {
        try {
            timer.push("JDOMetaData.getSuperClass(String)",//NOI18N
                       "JDOMetaData.getSuperClass(" + classPath + ")");//NOI18N
            return delegate.getSuperClass(classPath);
        } finally {
            timer.pop();
        }
    }


    @Override
    public String getPersistenceCapableRootClass(String classPath)
        throws JDOMetaDataUserException, JDOMetaDataFatalError {
        try {
            timer.push("JDOMetaData.getPersistenceCapableRootClass(String)",//NOI18N
                       "JDOMetaData.getPersistenceCapableRootClass(" + classPath + ")");//NOI18N
            return delegate.getPersistenceCapableRootClass(classPath);
        } finally {
            timer.pop();
        }
    }


    @Override
    public boolean isSecondClassObjectType(String classPath) throws JDOMetaDataUserException, JDOMetaDataFatalError {
        try {
            timer.push("JDOMetaData.isSecondClassObjectType(String)",//NOI18N
                       "JDOMetaData.isSecondClassObjectType(" + classPath + ")");//NOI18N
            return delegate.isSecondClassObjectType(classPath);
        } finally {
            timer.pop();
        }
    }


    @Override
    public boolean isMutableSecondClassObjectType(String classPath)
        throws JDOMetaDataUserException, JDOMetaDataFatalError {
        try {
            timer.push("JDOMetaData.isMutableSecondClassObjectType(String)",//NOI18N
                       "JDOMetaData.isMutableSecondClassObjectType(" + classPath + ")");//NOI18N
            return delegate.isMutableSecondClassObjectType(classPath);
        } finally {
            timer.pop();
        }
    }


    @Override
    public boolean isPersistentField(String classPath, String fieldName)
        throws JDOMetaDataUserException, JDOMetaDataFatalError {
        try {
            timer.push("JDOMetaData.isPersistentField(String,String)",//NOI18N
                       "JDOMetaData.isPersistentField(" + classPath//NOI18N
                       + ", " + fieldName + ")");//NOI18N
            return delegate.isPersistentField(classPath, fieldName);
        } finally {
            timer.pop();
        }
    }


    @Override
    public boolean isTransactionalField(String classPath, String fieldName)
        throws JDOMetaDataUserException, JDOMetaDataFatalError {
        try {
            timer.push("JDOMetaData.isTransactionalField(String,String)",//NOI18N
                       "JDOMetaData.isTransactionalField(" + classPath//NOI18N
                       + ", " + fieldName + ")");//NOI18N
            return delegate.isTransactionalField(classPath, fieldName);
        } finally {
            timer.pop();
        }
    }


    @Override
    public boolean isPrimaryKeyField(String classPath, String fieldName)
        throws JDOMetaDataUserException, JDOMetaDataFatalError {
        try {
            timer.push("JDOMetaData.isPrimaryKeyField(String,String)",//NOI18N
                       "JDOMetaData.isPrimaryKeyField(" + classPath//NOI18N
                       + ", " + fieldName + ")");//NOI18N
            return delegate.isPrimaryKeyField(classPath, fieldName);
        } finally {
            timer.pop();
        }
    }


    @Override
    public boolean isDefaultFetchGroupField(String classPath, String fieldName)
        throws JDOMetaDataUserException, JDOMetaDataFatalError {
        try {
            timer.push("JDOMetaData.isDefaultFetchGroupField(String,fieldName)",//NOI18N
                       "JDOMetaData.isDefaultFetchGroupField(" + classPath//NOI18N
                       + ", " + fieldName + ")");//NOI18N
            return delegate.isDefaultFetchGroupField(classPath, fieldName);
        } finally {
            timer.pop();
        }
    }


    @Override
    public int getFieldNo(String classPath, String fieldName) throws JDOMetaDataUserException, JDOMetaDataFatalError {
        try {
            timer.push("JDOMetaData.getFieldNo(String, String)",//NOI18N
                       "JDOMetaData.getFieldNo(" + classPath//NOI18N
                       + ", " + fieldName + ")");//NOI18N
            return delegate.getFieldNo(classPath, fieldName);
        } finally {
            timer.pop();
        }
    }


    @Override
    public String[] getManagedFields(String classPath) throws JDOMetaDataUserException, JDOMetaDataFatalError {
        try {
            timer.push("JDOMetaData.getPersistentFields(String)",//NOI18N
                       "JDOMetaData.getPersistentFields(" + classPath + ")");//NOI18N
            return delegate.getManagedFields(classPath);
        } finally {
            timer.pop();
        }
    }
}
