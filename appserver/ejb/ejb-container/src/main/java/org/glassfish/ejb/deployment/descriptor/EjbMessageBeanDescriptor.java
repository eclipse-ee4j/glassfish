/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package org.glassfish.ejb.deployment.descriptor;

import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.MessageDestinationDescriptor;
import com.sun.enterprise.deployment.MessageDestinationReferenceDescriptor;
import com.sun.enterprise.deployment.MessageDestinationReferencerImpl;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.ScheduledTimerDescriptor;
import com.sun.enterprise.deployment.types.MessageDestinationReferencer;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.glassfish.deployment.common.Descriptor;
import org.glassfish.ejb.deployment.EjbTagNames;

/**
 * Objects of this kind represent the deployment information describing a single message driven Ejb.
 */
public final class EjbMessageBeanDescriptor extends EjbDescriptor
    implements MessageDestinationReferencer, com.sun.enterprise.deployment.EjbMessageBeanDescriptor {

    private static final long serialVersionUID = 1L;
    private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(EjbMessageBeanDescriptor.class);

    // The following properties are used for processing of EJB 2.0
    // JMS-specific deployment descriptor elements.
    private static final String DURABLE_SUBSCRIPTION_PROPERTY = "subscriptionDurability";
    private static final String DURABLE = EjbTagNames.JMS_SUBSCRIPTION_IS_DURABLE;
    private static final String NON_DURABLE = EjbTagNames.JMS_SUBSCRIPTION_NOT_DURABLE;

    private static final String ACK_MODE_PROPERTY = "acknowledgeMode";
    private static final String AUTO_ACK = EjbTagNames.JMS_AUTO_ACK_MODE;
    private static final String DUPS_OK_ACK = EjbTagNames.JMS_DUPS_OK_ACK_MODE;

    private static final String MESSAGE_SELECTOR_PROPERTY = "messageSelector";

    private static final int AUTO_ACKNOWLEDGE = 1;
    private static final int DUPS_OK_ACKNOWLEDGE = 3;


    private String messageListenerType = "jakarta.jms.MessageListener";

    // These are the method objects from the
    // *message-bean implementation class* that implement the
    // Message Listener interface methods or ejbTimeout method.
    private transient Collection<MethodDescriptor> beanClassTxMethods;

    // *Optional* type of destination from which message bean consumes.
    private String destinationType;
    private String durableSubscriptionName;
    private String connectionFactoryName;
    private String resourceAdapterMid;

    // Holds *optional* information about the destination to which
    // we are linked.
    private final MessageDestinationReferencerImpl msgDestReferencer;

    // activationConfig represents name/value pairs that are
    // set by the assembler of an MDB application; those properties
    // are not resource adapter vendor dependent.
    private ActivationConfigDescriptor activationConfig;

    // runtimeActivationConfig represents name/value pairs that are
    // set by the deployer of an MDB application; those properties
    // are resource adapter vendor dependent.
    private ActivationConfigDescriptor runtimeActivationConfig;

    /**
     * Default constructor.
     */
    public EjbMessageBeanDescriptor() {
        msgDestReferencer = new MessageDestinationReferencerImpl(this);
        this.activationConfig = new ActivationConfigDescriptor();
        this.runtimeActivationConfig = new ActivationConfigDescriptor();
    }


    @Override
    public String getEjbTypeForDisplay() {
        return "MessageDrivenBean";
    }


    /**
     * Returns the type of this bean - always "Message-driven".
     */
    @Override
    public String getType() {
        return TYPE;
    }


    @Override
    public void setContainerTransactionFor(MethodDescriptor methodDescriptor, ContainerTransaction containerTransaction) {
        String txType = containerTransaction.getTransactionAttribute();
        if (ContainerTransaction.REQUIRED.equals(txType) || ContainerTransaction.NOT_SUPPORTED.equals(txType)) {
            super.setContainerTransactionFor(methodDescriptor, containerTransaction);
            return;
        }
        if (isTimedObject() && ContainerTransaction.REQUIRES_NEW.equals(txType)) {
            super.setContainerTransactionFor(methodDescriptor, containerTransaction);
            return;
        }
        throw new IllegalArgumentException("Invalid transaction attribute for message-driven bean: " + txType);
    }


    public void setMessageListenerType(String messagingType) {
        messageListenerType = messagingType;

        // Clear message listener methods so transaction methods will be
        // recomputed using new message listener type;
        beanClassTxMethods = null;
    }


    @Override
    public String getMessageListenerType() {
        return messageListenerType;
    }


    @Override
    public Set<MethodDescriptor> getTxBusinessMethodDescriptors() {
        ClassLoader classLoader = getEjbBundleDescriptor().getClassLoader();
        try {
            Set<MethodDescriptor> methods = new HashSet<>();
            addAllInterfaceMethodsIn(methods, classLoader.loadClass(messageListenerType), MethodDescriptor.EJB_BEAN);
            addAllInterfaceMethodsIn(methods, classLoader.loadClass(getEjbClassName()), MethodDescriptor.EJB_BEAN);
            if (isTimedObject()) {
                if (getEjbTimeoutMethod() != null) {
                    methods.add(getEjbTimeoutMethod());
                }
                for (ScheduledTimerDescriptor schd : getScheduledTimerDescriptors()) {
                    methods.add(schd.getTimeoutMethod());
                }
            }
            return methods;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("getTxBusinessMethodDescriptors failed", e);
        }
    }


    @Override
    public Set<MethodDescriptor> getSecurityBusinessMethodDescriptors() {
        throw new IllegalArgumentException(
            localStrings.getLocalString("enterprise.deployment.exceptioncannotgetsecbusmethodsinmsgbean",
                "Cannot get business method for security for message-driven bean."));
    }


    /**
     * This returns the message listener onMessage method from the message listener interface
     * itself, as opposed to the method from the ejb class that implements it.
     *
     * @param classLoader
     * @return array of methods
     * @throws NoSuchMethodException
     */
    public Method[] getMessageListenerInterfaceMethods(ClassLoader classLoader) throws NoSuchMethodException {
        List<Method> methods = new ArrayList<>();
        try {
            Class<?> messageListenerClass = classLoader.loadClass(messageListenerType);
            for (Method method : messageListenerClass.getDeclaredMethods()) {
                methods.add(method);
            }
            final Class<?> ejbClass = classLoader.loadClass(getEjbClassName());
            for (Method method : ejbClass.getMethods()) {
                methods.add(method);
            }
        } catch (Exception e) {
            NoSuchMethodException nsme = new NoSuchMethodException();
            nsme.initCause(e);
            throw nsme;
        }
        return methods.toArray(Method[]::new);
    }


    public boolean hasMessageDestinationLinkName() {
        return msgDestReferencer.getMessageDestinationLinkName() != null;
    }


    @Override
    public boolean isLinkedToMessageDestination() {
        return msgDestReferencer.isLinkedToMessageDestination();
    }


    /**
     * @return the name of the message destination to which I refer
     */
    @Override
    public String getMessageDestinationLinkName() {
        return msgDestReferencer.getMessageDestinationLinkName();
    }


    /**
     * Sets the name of the message destination to which I refer.
     */
    @Override
    public void setMessageDestinationLinkName(String linkName) {
        msgDestReferencer.setMessageDestinationLinkName(linkName);
    }


    @Override
    public MessageDestinationDescriptor setMessageDestinationLinkName(String linkName, boolean resolveLink) {
        return msgDestReferencer.setMessageDestinationLinkName(linkName, resolveLink);
    }


    @Override
    public MessageDestinationDescriptor resolveLinkName() {
        return msgDestReferencer.resolveLinkName();
    }


    @Override
    public boolean ownedByMessageDestinationRef() {
        return false;
    }


    @Override
    public MessageDestinationReferenceDescriptor getMessageDestinationRefOwner() {
        return null;
    }


    /**
     * True if the owner is a message-driven bean.
     */
    @Override
    public boolean ownedByMessageBean() {
        return true;
    }


    /**
     * Get the descriptor for the message-driven bean owner.
     */
    @Override
    public EjbMessageBeanDescriptor getMessageBeanOwner() {
        return this;
    }


    /**
     * @return the message destination to which I refer. Can be NULL.
     */
    @Override
    public MessageDestinationDescriptor getMessageDestination() {
        return msgDestReferencer.getMessageDestination();
    }


    /**
     * @param newMsgDest the message destination to which I refer.
     */
    @Override
    public void setMessageDestination(MessageDestinationDescriptor newMsgDest) {
        msgDestReferencer.setMessageDestination(newMsgDest);
    }

    //
    // ActivationConfig
    //


    /**
     * @return Set of EnvironmentProperty elements.
     */
    @Override
    public Set<EnvironmentProperty> getActivationConfigProperties() {
        return activationConfig.getActivationConfig();
    }


    @Override
    public String getActivationConfigValue(String name) {
        for (EnvironmentProperty next : activationConfig.getActivationConfig()) {
            if (next.getName().equals(name)) {
                return next.getValue();
            }
        }
        return null;
    }


    public void putActivationConfigProperty(EnvironmentProperty prop) {
        // remove first an existing property with the same name
        removeActivationConfigPropertyByName(prop.getName());
        activationConfig.getActivationConfig().add(prop);
    }


    public void removeActivationConfigProperty(EnvironmentProperty prop) {
        for (Iterator<EnvironmentProperty> iter = activationConfig.getActivationConfig().iterator(); iter.hasNext();) {
            EnvironmentProperty next = iter.next();
            if (next.getName().equals(prop.getName()) && next.getValue().equals(prop.getValue())) {
                iter.remove();
                break;
            }
        }
    }


    public void removeActivationConfigPropertyByName(String name) {
        for (Iterator<EnvironmentProperty> iter = activationConfig.getActivationConfig().iterator(); iter.hasNext();) {
            EnvironmentProperty next = iter.next();
            if (next.getName().equals(name)) {
                iter.remove();
                break;
            }
        }
    }


    @Override
    public Set<EnvironmentProperty> getRuntimeActivationConfigProperties() {
        return runtimeActivationConfig.getActivationConfig();
    }


    public String getRuntimeActivationConfigValue(String name) {
        for (EnvironmentProperty next : runtimeActivationConfig.getActivationConfig()) {
            if (next.getName().equals(name)) {
                return next.getValue();
            }
        }
        return null;
    }


    @Override
    public void putRuntimeActivationConfigProperty(EnvironmentProperty prop) {
        runtimeActivationConfig.getActivationConfig().add(prop);
    }


    public void removeRuntimeActivationConfigProperty(EnvironmentProperty prop) {
        for (Iterator<EnvironmentProperty> iter = runtimeActivationConfig.getActivationConfig().iterator(); iter.hasNext();) {
            EnvironmentProperty next = iter.next();
            if (next.getName().equals(prop.getName()) && next.getValue().equals(prop.getValue())) {
                iter.remove();
                break;
            }
        }

    }


    public void removeRuntimeActivationConfigPropertyByName(String name) {
        for (Iterator<EnvironmentProperty> iter = runtimeActivationConfig.getActivationConfig().iterator(); iter.hasNext();) {
            EnvironmentProperty next = iter.next();
            if (next.getName().equals(name)) {
                iter.remove();
                break;
            }
        }
    }


    @Override
    public boolean hasQueueDest() {
        return "jakarta.jms.Queue".equals(destinationType);
    }


    public boolean hasTopicDest() {
        return "jakarta.jms.Topic".equals(destinationType);
    }


    public boolean hasDestinationType() {
        return destinationType != null;
    }


    @Override
    public String getDestinationType() {
        return destinationType;
    }


    public void setDestinationType(String destType) {
        destinationType = destType;
    }


    public boolean hasDurableSubscription() {
        String value = getActivationConfigValue(DURABLE_SUBSCRIPTION_PROPERTY);
        return DURABLE.equals(value);
    }


    public void setHasDurableSubscription(boolean durable) {
        if (durable) {
            EnvironmentProperty durableProp = new EnvironmentProperty(DURABLE_SUBSCRIPTION_PROPERTY, DURABLE, "");
            putActivationConfigProperty(durableProp);
        } else {
            removeActivationConfigPropertyByName(DURABLE_SUBSCRIPTION_PROPERTY);
        }
    }


    public void setHasQueueDest() {
        destinationType = "jakarta.jms.Queue";
        setHasDurableSubscription(false);
    }


    public void setHasTopicDest() {
        destinationType = "jakarta.jms.Topic";
    }


    public void setSubscriptionDurability(String subscription) {
        if (DURABLE.equals(subscription)) {
            setHasDurableSubscription(true);
        } else if (NON_DURABLE.equals(subscription)) {
            setHasDurableSubscription(false);
        } else {
            throw new IllegalArgumentException("Invalid subscription durability string : " + subscription);
        }
    }


    public boolean hasJmsMessageSelector() {
        return (getActivationConfigValue(MESSAGE_SELECTOR_PROPERTY) != null);
    }


    public void setJmsMessageSelector(String selector) {
        if (selector == null || selector.isBlank()) {
            removeActivationConfigPropertyByName(MESSAGE_SELECTOR_PROPERTY);
        } else {
            EnvironmentProperty msgSelectorProp = new EnvironmentProperty(MESSAGE_SELECTOR_PROPERTY, selector, "");
            putActivationConfigProperty(msgSelectorProp);
        }
    }


    public String getJmsMessageSelector() {
        return getActivationConfigValue(MESSAGE_SELECTOR_PROPERTY);
    }


    public int getJmsAcknowledgeMode() {
        String ackModeStr = getActivationConfigValue(ACK_MODE_PROPERTY);
        return (ackModeStr != null && ackModeStr.equals(DUPS_OK_ACK)) ? DUPS_OK_ACKNOWLEDGE : AUTO_ACKNOWLEDGE;
    }


    public String getJmsAcknowledgeModeAsString() {
        return getActivationConfigValue(ACK_MODE_PROPERTY);
    }


    public void setJmsAcknowledgeMode(int acknowledgeMode) {
        String ackModeValue = (acknowledgeMode == AUTO_ACKNOWLEDGE) ? AUTO_ACK : DUPS_OK_ACK;
        EnvironmentProperty ackModeProp = new EnvironmentProperty(ACK_MODE_PROPERTY, ackModeValue, "");
        putActivationConfigProperty(ackModeProp);
    }


    public void setJmsAcknowledgeMode(String acknowledgeMode) {
        if (AUTO_ACK.equals(acknowledgeMode)) {
            setJmsAcknowledgeMode(AUTO_ACKNOWLEDGE);
        } else {
            if (DUPS_OK_ACK.equals(acknowledgeMode)) {
                setJmsAcknowledgeMode(DUPS_OK_ACKNOWLEDGE);
            } else {
                throw new IllegalArgumentException("Invalid jms acknowledge mode : " + acknowledgeMode);
            }
        }
    }


    @Override
    public String getDurableSubscriptionName() {
        return durableSubscriptionName;
    }


    // Reflection in EjbNode
    public void setDurableSubscriptionName(String durableSubscriptionName) {
        this.durableSubscriptionName = durableSubscriptionName;
    }


    public String getConnectionFactoryName() {
        return connectionFactoryName;
    }


    /**
     * Connection factory is optional. If set to null,
     * hasConnectionFactory will return false.
     */
    // Reflection in EjbNode
    public void setConnectionFactoryName(String connectionFactory) {
        connectionFactoryName = connectionFactory;
    }


    public boolean hasConnectionFactory() {
        return connectionFactoryName != null;
    }


    @Override
    public String getResourceAdapterMid() {
        return resourceAdapterMid;
    }


    @Override
    public String getMdbConnectionFactoryJndiName() {
        return getIASEjbExtraDescriptors().getMdbConnectionFactory().getJndiName();
    }


    @Override
    public void setResourceAdapterMid(String resourceAdapterMid) {
        this.resourceAdapterMid = resourceAdapterMid;
    }


    public boolean hasResourceAdapterMid() {
        return resourceAdapterMid != null;
    }


    @Override
    public List<Method> getMethods() {
        return Collections.emptyList();
    }


    /**
     * @return a collection of MethodDescriptor for methods which
     *         may have a assigned security attribute.
     */
    @Override
    protected Collection<MethodDescriptor> getTransactionMethods(ClassLoader classLoader) {
        Vector<MethodDescriptor> txMethods = new Vector<>();

        if (beanClassTxMethods == null) {
            try {
                beanClassTxMethods = new HashSet<>();
                Class<?> ejbClass = classLoader.loadClass(this.getEjbClassName());
                Method interfaceMessageListenerMethods[] = getMessageListenerInterfaceMethods(classLoader);
                for (Method next : interfaceMessageListenerMethods) {
                    // Convert method objects from MessageListener interface
                    // to method objects from ejb class
                    Method nextBeanMethod = ejbClass.getMethod(next.getName(), next.getParameterTypes());
                    beanClassTxMethods.add(new MethodDescriptor(nextBeanMethod, MethodDescriptor.EJB_BEAN));
                }
                if (isTimedObject()) {
                    beanClassTxMethods.add(getEjbTimeoutMethod());
                }
            } catch (Exception e) {
                NoSuchMethodError nsme = new NoSuchMethodError(
                    localStrings.getLocalString("enterprise.deployment.noonmessagemethod", "",
                        new Object[] {getEjbClassName(), getMessageListenerType()}));
                nsme.initCause(e);
                throw nsme;
            }
        }
        txMethods.addAll(beanClassTxMethods);
        return txMethods;
    }


    @Override
    public String getContainerFactoryQualifier() {
        return "MessageBeanContainerFactory";
    }


    /**
     * Sets the transaction type for this bean.
     * Must be either BEAN_TRANSACTION_TYPE or CONTAINER_TRANSACTION_TYPE.
     */
    @Override
    public void setTransactionType(String transactionType) {
        boolean isValidType = BEAN_TRANSACTION_TYPE.equals(transactionType)
            || CONTAINER_TRANSACTION_TYPE.equals(transactionType);

        if (!isValidType && Descriptor.isBoundsChecking()) {
            throw new IllegalArgumentException(
                localStrings.getLocalString("enterprise.deployment.exceptionmsgbeantxtypenotlegaltype",
                    "{0} is not a legal transaction type for a message-driven bean", new Object[] {transactionType}));
        }
        super.setTransactionType(transactionType);
        super.setMethodContainerTransactions(new Hashtable<>());
    }


    public void setActivationConfigDescriptor(ActivationConfigDescriptor desc) {
        activationConfig = desc;
    }


    // NOTE : This method should only be used by the XML processing logic.
    // All access to activation config properties should be done
    // through the other accessors on the message bean descriptor.
    public ActivationConfigDescriptor getActivationConfigDescriptor() {
        return activationConfig;
    }


    public void setRuntimeActivationConfigDescriptor(ActivationConfigDescriptor desc) {
        runtimeActivationConfig = desc;
    }


    // NOTE : This method should only be used by the XML processing logic.
    // All access to activation config properties should be done
    // through the other accessors on the message bean descriptor.
    public ActivationConfigDescriptor getRuntimeActivationConfigDescriptor() {
        return runtimeActivationConfig;
    }


    /**
     * Appends a formatted String of the attributes of this object.
     */
    @Override
    public void print(StringBuffer toStringBuffer) {
        super.print(toStringBuffer);
        toStringBuffer.append("Message-driven descriptor : ").append(activationConfig.getActivationConfig())
            .append(runtimeActivationConfig.getActivationConfig());
    }
}
