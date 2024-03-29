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

package org.glassfish.ejb.deployment.descriptor.runtime;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.runtime.BeanPoolDescriptor;
import com.sun.enterprise.deployment.runtime.RuntimeDescriptor;
import com.sun.enterprise.deployment.util.DOLUtils;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Class that contains all the extra iAS elements for an EJB that are not
 * in the RI DTD like:
 *
 * MdbConnectionFactoryDescriptor
 * jmsMaxMessagesLoad
 * isReadOnlyBean
 * refreshPeriodInSeconds
 * commitOption
 * checkpointedMethods
 * passByReference
 * BeanPoolDescriptor
 * BeanCacheDescriptor
 * FlushAtEndOfMethodDescriptor
 * CheckpointAtEndOfMethodDescriptor
 *
 * @author Ludo
 * @since JDK 1.4
 */
public class IASEjbExtraDescriptors extends RuntimeDescriptor {

    public static final String AVAILABILITY_ENABLED = "AvailabilityEnabled";

    private static final long serialVersionUID = 1L;

    /**
     * ejb - refresh period in seconds
     */
    private static final int REFRESH_PERIOD_IN_SECONDS_DEFAULT = -1;

    private static final Logger LOG = DOLUtils.getDefaultLogger();


    private boolean isReadOnlyBean;

    /**
     * A string field whose value denoted the rate at which the read-only-bean
     * must be refreshed from the data source.
     * Valid values are: negative (never refreshed), 0 (always refreshed)
     * and positive (refreshed at specified intervals).
     * Note that the value is just a hint to the container.
     */
    private int refreshPeriodInSeconds;

    /**
     * A string value specifies the maximum number of messages to
     * load into a JMS session at one time for a message-driven
     * bean to serve. If not specified, the default is 1.
     */
    private int jmsMaxMessagesLoad;

    private MdbConnectionFactoryDescriptor mdbConnectionFactory;

    /**
     * A string field whose valid values are either A, B, or C.
     * Default value is set in the server configuration (server.xml).
     */
    private String commitOption;

    /**
     * This contains the bean pool properties. Used only for stateless
     * session bean and mdb pools.
     */
    private BeanPoolDescriptor beanPool;

    /**
     * This contains the bean cache properties. Used only for entity beans.
     */
    private BeanCacheDescriptor beanCache;

    private FlushAtEndOfMethodDescriptor flushMethodDescriptor;

    private CheckpointAtEndOfMethodDescriptor checkpointMethodDescriptor;

    private String checkpointedMethods;

    /**
     * This contains the pass-by-reference property.
     */
    private Boolean passByReference;


    /*
     * This contains the EjbDescriptor - J2EE specific descriptor
     * @see com.sun.enterprise.deployment.EjbDescriptor EjbDescriptor
     */
    private EjbDescriptor ejbDescriptor;

    /**
     * This contains the timeout used for container started transactions
     * This value is used by the container only if the value is greater than 0
     */
    private int cmtTimeoutInSeconds;

    /**
     * Specifies the thread pool to be used for this ejb's invocation
     */
    private String useThreadPoolId;

    /**
     * a true/false flag that controls the per-request load balancing
     * behavior of EJB 2.x/3.x Remote client invocations on a stateless session
     * bean. If set to true, per-request load balancing is enabled for the
     * associated stateless session bean.  If set to false or not set,
     * per-request load balancing is not enabled.
     */
    private Boolean perRequestLoadBalancing;

    // contants used to parse the checkpointedMethods
    private final static String METHODS_DELIM = ";";
    private final static String PARAMS_DELIM = ",";
    private final static String LEFT_PAREN = "(";
    private final static String RIGHT_PAREN = ")";
    private final static String PARAM_DELIM = " ";

    /**
     * Default constructor.
     */
    public IASEjbExtraDescriptors() {
        jmsMaxMessagesLoad = 1;
        isReadOnlyBean = false;
        refreshPeriodInSeconds = REFRESH_PERIOD_IN_SECONDS_DEFAULT;//RO Bean never refreshed???
    }

    /**
     * Getter for property beanCache.
     * @return Value of property beanCache.
     */
    public BeanCacheDescriptor getBeanCache() {
        return beanCache;
    }

    /**
     * Setter for property beanCache.
     * @param beanCache New value of property beanCache.
     */
    public void setBeanCache(BeanCacheDescriptor beanCache) {
        this.beanCache = beanCache;
    }

     /**
      * Getter for property beanPool.
      * @return Value of property beanPool.
      */
    public BeanPoolDescriptor getBeanPool() {
        return beanPool;
    }

    /**
     * Setter for property beanPool.
     * @param beanPool New value of property beanPool.
     */
    public void setBeanPool(BeanPoolDescriptor beanPool) {
        this.beanPool = beanPool;
    }

     /**
      * Getter for flush-at-end-of-method
      * @return Value of flushMethodDescriptor
      */
    public FlushAtEndOfMethodDescriptor getFlushAtEndOfMethodDescriptor() {
        return flushMethodDescriptor;
    }

    /**
     * Setter for flush-at-end-of-method
     * @param flushMethodDescriptor New value of flushMethodDescriptor.
     */
    public void setFlushAtEndOfMethodDescriptor(
        FlushAtEndOfMethodDescriptor flushMethodDescriptor) {
        this.flushMethodDescriptor = flushMethodDescriptor;
    }

     /**
      * Getter for checkpoint-at-end-of-method
      * @return Value of checkpointMethodDescriptor
      */
    public CheckpointAtEndOfMethodDescriptor
        getCheckpointAtEndOfMethodDescriptor() {
        return checkpointMethodDescriptor;
    }

    /**
     * Setter for checkpoint-at-end-of-method
     * @param checkpointMethodDescriptor New value of
     * checkpointMethodDescriptor.
     */
    public void setCheckpointAtEndOfMethodDescriptor(
        CheckpointAtEndOfMethodDescriptor checkpointMethodDescriptor) {
        this.checkpointMethodDescriptor = checkpointMethodDescriptor;
    }

     /**
      * Getter for property checkpointedMethods
      * @return Value of property checkpointedMethods
      */
    public String getCheckpointedMethods() {
        return checkpointedMethods;
    }

    /**
     * Setter for property checkpointedMethods
     * @param checkpointedMethods New value of checkpointed methods.
     */
    public void setCheckpointedMethods(String checkpointedMethods) {
        this.checkpointedMethods = checkpointedMethods;
    }

    /**
     * Getter for property commitOption.
     * @return Value of property commitOption.
     */
    public java.lang.String getCommitOption() {
        return commitOption;
    }

    /**
     * Setter for property commitOption.
     * @param commitOption New value of property commitOption.
     */
    public void setCommitOption(java.lang.String commitOption) {
        this.commitOption = commitOption;
    }

    /**
     * Getter for property cmt-timeout-in-seconds.
     * @return Value of property cmt-timeout-in-seconds.
     */
    public int getCmtTimeoutInSeconds() {
        return this.cmtTimeoutInSeconds;
    }

    /**
     * Setter for property cmt-timeout-in-seconds.
     * @param val New value of property cmt-timeout-in-seconds.
     */
    public void setCmtTimeoutInSeconds(int val) {
        this.cmtTimeoutInSeconds = val;
    }

    /**
     * Getter for the property use-thread-pool-id
     * @return The value of use-thread-pool-id
     */
    public String getUseThreadPoolId() {
        return this.useThreadPoolId;
    }

    /**
     * Setter for the property use-thread-pool-id
     * @param val The value for use-thread-pool-id
     */
    public void setUseThreadPoolId(String val) {
        this.useThreadPoolId = val;
    }

    /**
     * Getter for property isReadOnlyBean.
     * @return Value of property isReadOnlyBean.
     */
    public boolean isIsReadOnlyBean() {
        return isReadOnlyBean;
    }

    /**
     * Setter for property isReadOnlyBean.
     * @param isReadOnlyBean New value of property isReadOnlyBean.
     */
    public void setIsReadOnlyBean(boolean isReadOnlyBean) {
        this.isReadOnlyBean = isReadOnlyBean;
    }

    /**
     * Getter for property jmsMaxMessagesLoad.
     * @return Value of property jmsMaxMessagesLoad.
     */
    public int getJmsMaxMessagesLoad() {
        return jmsMaxMessagesLoad;
    }

    /**
     * Setter for property jmsMaxMessagesLoad.
     * @param jmsMaxMessagesLoad New value of property jmsMaxMessagesLoad.
     */
    public void setJmsMaxMessagesLoad(int jmsMaxMessagesLoad) {
        this.jmsMaxMessagesLoad = jmsMaxMessagesLoad;
    }

    /**
     * Getter for property mdbConnectionFactory.
     * @return Value of property mdbConnectionFactory.
     */
    public MdbConnectionFactoryDescriptor getMdbConnectionFactory() {
        return mdbConnectionFactory;
    }

    /**
     * Setter for property mdbConnectionFactory.
     * @param mdbConnectionFactory New value of property mdbConnectionFactory.
     */
    public void setMdbConnectionFactory(
            MdbConnectionFactoryDescriptor mdbConnectionFactory) {

        this.mdbConnectionFactory = mdbConnectionFactory;
    }

    /**
     * Getter for property refreshPeriodInSeconds.
     * @return Value of property refreshPeriodInSeconds.
     */
    public int getRefreshPeriodInSeconds() {
        return refreshPeriodInSeconds;
    }

    /**
     * Setter for property refreshPeriodInSeconds.
     * @param refreshPeriodInSeconds  New value of property
     *                                refreshPeriodInSeconds.
     */
    public void setRefreshPeriodInSeconds(int refreshPeriodInSeconds) {
        this.refreshPeriodInSeconds = refreshPeriodInSeconds;
    }

    /**
     * Gets ejb pass-by-reference value.
     * @return Value of property passByReference if it is not null.  Otherwise
     *         returns value of passByReference property of Application if
     *         it is not null.  Default value is false.
     */
    public boolean getPassByReference() {
        if (this.isPassByReferenceDefined()) {
            // if pass-by-reference defined for ejb
            return this.passByReference.booleanValue();
        }
        // if pass-by-reference undefined for ejb set to
        // application's pass-by-reference value if defined
        ejbDescriptor = this.getEjbDescriptor();
        if (ejbDescriptor != null) {
            Application application = ejbDescriptor.getApplication();
            if (application != null) {
                if (application.isPassByReferenceDefined()) {
                    return application.getPassByReference();
                }
            }
        }
        return false;
    }

    /**
     * Sets ejb pass-by-reference value.
     * @param passByReference New value of property pass-by-reference.
     */
    public void setPassByReference(Boolean passByReference) {
        this.passByReference = passByReference;
    }

    /**
     * Evaluates property passByReference for null value
     * @return boolean true if property passByReference is not null
     *         boolean false if property passByReference is null
     */
    public boolean isPassByReferenceDefined() {
        boolean passByReferenceDefined = false;
        if (this.passByReference != null) {
            passByReferenceDefined = true;
        }
        return passByReferenceDefined;
    }

    /**
     * Getter for property ejbDescriptor.
     * @return EjbDescriptor object property - J2EE specific ejb descriptor
     */
    public EjbDescriptor getEjbDescriptor() {
        return this.ejbDescriptor;
    }

    /**
     * Setter for property ejbDescriptor
     * @param ejbDescriptor - EjbDescriptor object - J2EE specific ejb descriptor
     */
    public void setEjbDescriptor(EjbDescriptor ejbDescriptor) {
        this.ejbDescriptor = ejbDescriptor;
    }

    /**
      * Convenience method to check if a method is flush enabled or not
      * @param methodDesc - Method Descriptor object to check
      * @return boolean true if methodDesc is flushed enabled
      *         boolean false if methodDesc is not flushed enabled
      */
    public boolean isFlushEnabledFor(MethodDescriptor methodDesc) {
        if (flushMethodDescriptor != null) {
            return flushMethodDescriptor.isFlushEnabledFor(methodDesc);
        }
        return false;
    }

    public Boolean getPerRequestLoadBalancing() {
        return perRequestLoadBalancing;
    }

    public void setPerRequestLoadBalancing(Boolean perRequestLoadBalancing) {
        this.perRequestLoadBalancing = perRequestLoadBalancing;
    }

     /**
      * Parse checkpointed-methods element and save its values in
      * CheckpointAtEndOfMethodDescriptor
      *
      * The methods should be separated by semicolons.
      * The param list should separated by commas.
      * All method param types should be full qualified.
      * Variable name is allowed for the param type.
      * No return type or exception type.
      *
      * Example:
      * foo(java.lang.String,  a.b.c d); bar(java.lang.String s)
      *
      */
    public void parseCheckpointedMethods(EjbDescriptor ejbDesc) {
        if (checkpointedMethods == null || checkpointedMethods.isBlank()) {
            return;
        }
        if (checkpointMethodDescriptor == null) {
            checkpointMethodDescriptor = new CheckpointAtEndOfMethodDescriptor();
            setCheckpointAtEndOfMethodDescriptor(checkpointMethodDescriptor);
            checkpointMethodDescriptor.setEjbDescriptor(ejbDesc);
        }
        StringTokenizer methodsTokenizer = new StringTokenizer(checkpointedMethods, METHODS_DELIM);
        while (methodsTokenizer.hasMoreTokens()) {
            // process each method
            String method = methodsTokenizer.nextToken().trim();
            if (method.isEmpty()) {
                continue;
            }
            MethodDescriptor methodDescriptor = parseCheckpointedMethod(method);
            if (methodDescriptor != null) {
                checkpointMethodDescriptor.getMethodDescriptors().add(methodDescriptor);
            }
        }
    }


    // parse the given method string into a MethodDescriptor
    private MethodDescriptor parseCheckpointedMethod(String method) {
        String methodName, methodParams;
        ArrayList<String> paramTypeList = new ArrayList<>();
        try {
            if (method.indexOf(LEFT_PAREN) == -1 || method.indexOf(RIGHT_PAREN) == -1) {
                LOG.log(Level.WARNING, "DPL5412: Bad format: checkpointed-methods [{0}]", method);
                return null;
            }
            int pos = method.indexOf(LEFT_PAREN);
            int pos2 = method.indexOf(RIGHT_PAREN);
            // retrieve the method name
            methodName = method.substring(0, pos).trim();
            // retrieve the parameter list
            if (pos < pos2 - 1) {
                methodParams = method.substring(pos + 1, pos2).trim();
                StringTokenizer paramsTokenizer = new StringTokenizer(methodParams, PARAMS_DELIM);
                while (paramsTokenizer.hasMoreTokens()) {
                    // process each param
                    String param = paramsTokenizer.nextToken().trim();
                    if (param.isEmpty()) {
                        continue;
                    }
                    StringTokenizer paramTokenizer = new StringTokenizer(param, PARAM_DELIM);
                    while (paramTokenizer.hasMoreTokens()) {
                        String paramType = paramTokenizer.nextToken().trim();
                        if (!paramType.isEmpty()) {
                            paramTypeList.add(paramType);
                            // only interested in the first token
                            break;
                        }
                    }
                }
            }
            if (paramTypeList.isEmpty()) {
                return new MethodDescriptor(methodName, null, null, null);
            }
            String[] paramTypeArray = paramTypeList.toArray(new String[paramTypeList.size()]);
            return new MethodDescriptor(methodName, null, paramTypeArray, null);
        } catch (Exception e) {
            // any parsing exception indicates it is not a well-formed
            // string, we will just print warning and return null
            LogRecord log = new LogRecord(Level.WARNING, "DPL5412: Bad format: checkpointed-methods [{0}]");
            log.setParameters(new Object[] {method});
            log.setThrown(e);
            LOG.log(log);
            return null;
        }
    }
}
