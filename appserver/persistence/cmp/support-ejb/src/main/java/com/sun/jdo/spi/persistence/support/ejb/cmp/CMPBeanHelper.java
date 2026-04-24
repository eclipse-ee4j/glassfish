/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.jdo.spi.persistence.support.ejb.cmp;

import com.sun.jdo.api.persistence.support.JDOException;
import com.sun.jdo.api.persistence.support.JDOHelper;
import com.sun.jdo.api.persistence.support.PersistenceCapable;
import com.sun.jdo.api.persistence.support.PersistenceManager;
import com.sun.jdo.spi.persistence.support.sqlstore.ejb.EJBHelper;

import jakarta.ejb.DuplicateKeyException;
import jakarta.ejb.EJBException;
import jakarta.ejb.ObjectNotFoundException;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Collection;
import java.util.ResourceBundle;

import org.glassfish.persistence.common.I18NHelper;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.WARNING;

/** Provides static helper methods for CMP bean implementation to simplify
 * the generated code.
 */
public class CMPBeanHelper {

    /** I18N message handlers */
    private final static ResourceBundle cmpMessages = I18NHelper.loadBundle(
        "com.sun.jdo.spi.persistence.support.ejb.ejbc.Bundle",
        CMPBeanHelper.class.getClassLoader());

    private static final Logger LOG = System.getLogger(CMPBeanHelper.class.getName());

    /**
     * Called from a CMP bean to log JDOException message
     * with the LifecycleLogger.
     *
     * @param key the key for the corresponding Bundle.
     * @param beanName the name of the calling bean.
     * @param ex the JDOException.
     */
    public static void logJDOExceptionWithLifecycleLogger(String key, String beanName, JDOException ex) {
        LOG.log(WARNING, () -> I18NHelper.getMessage(cmpMessages, key, beanName, findCallingMethodName()), ex);
    }

    /**
     * Called from a CMP bean to log JDOException message
     * with the LifecycleLogger.
     *
     * @param key the key for the corresponding Bundle.
     * @param beanName the name of the calling bean.
     * @param paramList the list of the concatenated parameters.
     * @param ex the JDOException.
     */
    public static void logJDOExceptionWithLifecycleLogger(String key, String beanName, String paramList,
        JDOException ex) {
        LOG.log(WARNING, () -> I18NHelper.getMessage(cmpMessages, key, beanName, findCallingMethodName(), paramList),
            ex);
    }


    /**
     * Called from a CMP bean to log JDOException message thrown
     * from a any getter or setter method, with the InternalLogger.
     *
     * @param beanName the name of the calling bean.
     * @param ex the JDOException.
     */
    public static void logJDOExceptionWithInternalLogger(String beanName, JDOException ex) {
        LOG.log(WARNING,
            () -> I18NHelper.getMessage(cmpMessages, "GEN.generic_method_exception", beanName, findCallingMethodName()),
            ex);
    }


    /**
     * Called from a CMP bean to log JDOException message thrown
     * from a any finder or selector method, with the FinderLogger.
     *
     * @param beanName the name of the calling bean.
     * @param params the Object[] of the parameter values for the
     * finder or selector method.
     * @param ex the JDOException.
     */
    public static void logJDOExceptionWithFinderLogger(
            String beanName, Object[] params, JDOException ex) {

        String msg;
        if (params != null) {
            msg = I18NHelper.getMessage(cmpMessages, "GEN.ejbSSReturnBody_exception", beanName, findCallingMethodName(),
                java.util.Arrays.asList(params).toString());
        } else {
            msg = I18NHelper.getMessage(cmpMessages, "GEN.ejbSSReturnBody_exception_woparams", beanName,
                findCallingMethodName());
        }
        LOG.log(WARNING, msg, ex);
    }

    /**
     * Called from a CMP bean to log JDOException message thrown
     * from a any finder or selector method, with the FinderLogger.
     *
     * @param level the logging level as int.
     * @param beanName the name of the calling bean.
     * @param ex the Exception.
     */
    public static void logFinderException(Level level, String beanName, Exception ex) {
        LOG.log(level,
            () -> I18NHelper.getMessage(cmpMessages, "GEN.generic_method_exception", beanName, findCallingMethodName()),
            ex);
    }


    /**
     * Called from a CMP bean to log JDOException message thrown
     * from a PK setter method, with the InternalLogger.
     * Returns generated message to the caller to be used for a
     * IllegalStateException.
     *
     * @param beanName the name of the calling bean.
     * @param ex the JDOException.
     * @return logged message as String.
     */
    public static String logJDOExceptionFromPKSetter(String beanName, JDOException ex) {
        String msg = I18NHelper.getMessage(cmpMessages, "EXC_PKUpdate", beanName, findCallingMethodName());
        LOG.log(DEBUG, msg, ex);
        return msg;
    }


    /**
     * Called from a CMP bean to verify that the PersistenceCapable
     * instance is already persistent. Throws IllegalStateException
     * otherwise.
     *
     * @param pc the PersistenceCapable instance to be checked.
     * @param beanName the name of the caller bean.
     * @throws IllegalStateException if the instance is not persistent.
     */
    public static void assertPersistent(PersistenceCapable pc, String beanName) {
        if (!JDOHelper.isPersistent(pc)) {
            String msg = I18NHelper.getMessage(cmpMessages, "GEN.cmrgettersetter_exception", beanName,
                findCallingMethodName());

            LOG.log(ERROR, msg);
            throw new IllegalStateException(msg);
        }
    }


    /**
     * Called from a CMP bean to verify that the argument for
     * a Collection set method is not null. Throws IllegalArgumentException
     * if the argument is null.
     *
     * @param c the Collection to check.
     * @param beanName the name of the caller bean.
     * @throws IllegalArgumentException if the argument is null.
     */
    public static void assertCollectionNotNull(Collection c, String beanName) {
        if (c == null) {
            String msg = I18NHelper.getMessage(cmpMessages, "GEN.cmrsettercol_nullexception", beanName,
                findCallingMethodName());

            LOG.log(ERROR, msg);
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Called from a CMP bean to verify that the PersistenceManager
     * is not null. Throws IllegalStateException if the argument is null.
     *
     * @param pm the PersistenceManager to check.
     * @param bean the calling bean instance.
     * @throws IllegalStateException if the PersistenceManager is null.
     */
    public static void assertPersistenceManagerNotNull(PersistenceManager pm,
        Object bean) {
        if (pm == null) {
            String msg = I18NHelper.getMessage(cmpMessages,
                "JDO.beannotloaded_exception", bean);

            LOG.log(ERROR, msg);
            throw new IllegalStateException(msg);
        }
    }

    /**
     * Called from a CMP bean to verify that the PersistenceManager
     * is null. Throws IllegalStateException if the argument is not null.
     *
     * @param pm the PersistenceManager to check.
     * @param bean the calling bean instance.
     * @throws IllegalStateException if the PersistenceManager is not null.
     */
    public static void assertPersistenceManagerIsNull(PersistenceManager pm,
        Object bean, StringBuffer buf) {
        if (pm != null) {
            String msg = I18NHelper.getMessage(cmpMessages,
                "JDO.beaninuse_exception", bean);

            // Excption to use only short message
            IllegalStateException e = new IllegalStateException(msg);

            if (buf != null && buf.length() > 0) {
                msg = (new StringBuffer(msg)).append(" ...Last Instance Usage: ").
                        append(buf).toString();
            }
            LOG.log(ERROR, msg);
            throw e;
        }
    }

    /**
     * Called from a 1.1 CMP bean to verify that the bean method is not called
     * in a container transaction. Throws IllegalStateException otherwise.
     *
     * @param bean the calling bean instance.
     * @throws IllegalStateException if the bean method is called in a container transaction.
     */
    public static void assertNotContainerTransaction(Object bean) {
        if (EJBHelper.getTransaction() != null) {
            String msg = I18NHelper.getMessage(cmpMessages,
                "JDO.containertransaction_exception", bean);

            LOG.log(ERROR, msg);
            throw new IllegalStateException(msg);
        }
    }

    /**
     * Called from a CMP bean to process JDODuplicateObjectIdException.
     * Logs the message and throws DuplicateKeyException.
     *
     * @param beanName the name of the calling bean.
     * @param paramList the list of the concatenated parameters.
     * @param ex the JDOException.
     * @throws DuplicateKeyException.
     */
    public static void handleJDODuplicateObjectIdAsDuplicateKeyException(
        String beanName, String paramList, JDOException ex)
        throws DuplicateKeyException {

        String msg = I18NHelper.getMessage(cmpMessages,
            "GEN.ejbcreate_exception_dup", beanName,
            findCallingMethodName(), paramList);

        LOG.log(DEBUG, msg, ex);
        throw new DuplicateKeyException(msg);
    }

    /**
     * Called from a CMP bean to process JDODuplicateObjectIdException.
     * Logs the message and throws EJBException.
     *
     * @param beanName the name of the calling bean.
     * @param paramList the list of the concatenated parameters.
     * @param ex the JDOException.
     * @throws EJBException.
     */
    public static void handleJDODuplicateObjectIdAsEJBException(
        String beanName, String paramList, JDOException ex) {

        String msg = I18NHelper.getMessage(cmpMessages,
            "GEN.ejbcreate_exception_dup", beanName,
            findCallingMethodName(), paramList);

        LOG.log(DEBUG, msg, ex);
        throw new EJBException(msg);
    }

   /**
     * Called from a CMP bean to process JDOObjectNotFoundException.
     * Logs the message and throws ObjectNotFoundException
     *
     * @param primaryKey the PrimaryKey instance.
     * @param beanName the name of the calling bean.
     * @param ex the JDOException.
     * @throws ObjectNotFoundException.
     */
    public static void handleJDOObjectNotFoundException(
        Object primaryKey, String beanName, JDOException ex)
        throws ObjectNotFoundException {

        String msg = I18NHelper.getMessage(cmpMessages,
            "GEN.findbypk_exception_notfound", beanName,
            primaryKey.toString());

        LOG.log(DEBUG, msg, ex);
        throw new ObjectNotFoundException(msg);
    }

   /**
     * Throws EJBException on attempted updates to the
     * calling bean.
     * @param beanName the name of the calling bean.
     * @throws EJBException.
     */
    public static void handleUpdateNotAllowedException(
        String beanName) {
        String msg = I18NHelper.getMessage(cmpMessages,
            "GEN.update_not_allowed", beanName,
            findCallingMethodName());

        LOG.log(ERROR, msg);
        throw new EJBException(msg);
    }

   /**
     * Throws EJBException on failed clone of persistence state
     * in read-only beans.
     *
     * @param primaryKey the PrimaryKey instance.
     * @param beanName the name of the calling bean.
     * @param ex the Exception.
     * @throws EJBException.
     */
    public static void handleCloneException(
        Object primaryKey, String beanName, Exception ex) {

        String msg = I18NHelper.getMessage(cmpMessages,
            "GEN.clone_exception", beanName,
            primaryKey.toString());

        LOG.log(ERROR, msg, ex);
        throw new EJBException(msg);
    }

    /**
     * Calculates the method name of the calling method.
     *
     * @return method name as String.
     */
    private static String findCallingMethodName() {
        StackTraceElement[] ste = (new Throwable()).getStackTrace();
        return ste[2].getMethodName();
    }
}
