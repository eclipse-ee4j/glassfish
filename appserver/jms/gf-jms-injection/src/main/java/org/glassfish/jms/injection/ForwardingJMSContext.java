/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jms.injection;

import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.jms.BytesMessage;
import jakarta.jms.ConnectionMetaData;
import jakarta.jms.Destination;
import jakarta.jms.ExceptionListener;
import jakarta.jms.IllegalStateRuntimeException;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSProducer;
import jakarta.jms.MapMessage;
import jakarta.jms.Message;
import jakarta.jms.ObjectMessage;
import jakarta.jms.Queue;
import jakarta.jms.QueueBrowser;
import jakarta.jms.StreamMessage;
import jakarta.jms.TemporaryQueue;
import jakarta.jms.TemporaryTopic;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;

import java.io.Serializable;

// Delegate all business methods to JMSContext API
public abstract class ForwardingJMSContext implements JMSContext {
    private final static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ForwardingJMSContext.class);

    protected abstract JMSContext delegate();

    @Override
    public JMSContext createContext(int sessionMode) {
        return delegate().createContext(sessionMode);
    }

    @Override
    public JMSProducer createProducer() {
        return delegate().createProducer();
    }

    @Override
    public String getClientID() {
        return delegate().getClientID();
    }

    @Override
    public void setClientID(String clientID) {
        throw getUnsupportedException();
    }

    @Override
    public ConnectionMetaData getMetaData() {
        return delegate().getMetaData();
    }

    @Override
    public ExceptionListener getExceptionListener() {
        return null;
    }

    @Override
    public void setExceptionListener(ExceptionListener listener) {
        throw getUnsupportedException();
    }

    @Override
    public void start() {
        throw getUnsupportedException();
    }

    @Override
    public void stop() {
        throw getUnsupportedException();
    }

    @Override
    public void setAutoStart(boolean autoStart) {
        throw getUnsupportedException();
    }

    @Override
    public boolean getAutoStart() {
        return true;
    }

    @Override
    public void close() {
        throw getUnsupportedException();
    }

    @Override
    public BytesMessage createBytesMessage() {
        return delegate().createBytesMessage();
    }

    @Override
    public MapMessage createMapMessage() {
        return delegate().createMapMessage();
    }

    @Override
    public Message createMessage() {
        return delegate().createMessage();
    }

    @Override
    public ObjectMessage createObjectMessage() {
        return delegate().createObjectMessage();
    }

    @Override
    public ObjectMessage createObjectMessage(Serializable object) {
        return delegate().createObjectMessage(object);
    }

    @Override
    public StreamMessage createStreamMessage() {
        return delegate().createStreamMessage();
    }

    @Override
    public TextMessage createTextMessage() {
        return delegate().createTextMessage();
    }

    @Override
    public TextMessage createTextMessage(String text) {
        return delegate().createTextMessage(text);
    }

    @Override
    public boolean getTransacted() {
        return delegate().getTransacted();
    }

    @Override
    public int getSessionMode() {
        return delegate().getSessionMode();
    }

    @Override
    public void commit() {
        throw getUnsupportedException();
    }

    @Override
    public void rollback() {
        throw getUnsupportedException();
    }

    @Override
    public void recover() {
        throw getUnsupportedException();
    }

    @Override
    public JMSConsumer createConsumer(Destination destination) {
        return delegate().createConsumer(destination);
    }

    @Override
    public JMSConsumer createConsumer(Destination destination, String messageSelector) {
        return delegate().createConsumer(destination, messageSelector);
    }

    @Override
    public JMSConsumer createConsumer(Destination destination, String messageSelector, boolean noLocal) {
        return delegate().createConsumer(destination, messageSelector, noLocal);
    }

    @Override
    public Queue createQueue(String queueName) {
        return delegate().createQueue(queueName);
    }

    @Override
    public Topic createTopic(String topicName) {
        return delegate().createTopic(topicName);
    }

    @Override
    public JMSConsumer createDurableConsumer(Topic topic, String name) {
        return delegate().createDurableConsumer(topic, name);
    }

    @Override
    public JMSConsumer createDurableConsumer(Topic topic, String name, String messageSelector, boolean noLocal) {
        return delegate().createDurableConsumer(topic, name, messageSelector, noLocal);
    }

    @Override
    public JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName) {
        return delegate().createSharedConsumer(topic, sharedSubscriptionName);
    }

    @Override
    public JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName, String messageSelector) {
        return delegate().createSharedConsumer(topic, sharedSubscriptionName, messageSelector);
    }

    @Override
    public JMSConsumer createSharedDurableConsumer(Topic topic, String name) {
        return delegate().createSharedDurableConsumer(topic, name);
    }

    @Override
    public JMSConsumer createSharedDurableConsumer(Topic topic, String name, String messageSelector) {
        return delegate().createSharedDurableConsumer(topic, name, messageSelector);
    }

    @Override
    public QueueBrowser createBrowser(Queue queue) {
        return delegate().createBrowser(queue);
    }

    @Override
    public QueueBrowser createBrowser(Queue queue, String messageSelector) {
        return delegate().createBrowser(queue, messageSelector);
    }

    @Override
    public TemporaryQueue createTemporaryQueue() {
        return delegate().createTemporaryQueue();
    }

    @Override
    public TemporaryTopic createTemporaryTopic() {
        return delegate().createTemporaryTopic();
    }

    @Override
    public void unsubscribe(String name) {
        delegate().unsubscribe(name);
    }

    @Override
    public void acknowledge() {
        throw getUnsupportedException();
    }

    private IllegalStateRuntimeException getUnsupportedException() {
        return new IllegalStateRuntimeException(localStrings.getLocalString("JMSContext.injection.not.supported",
                "This method is not permitted on a container-managed (injected) JMSContext."));
    }
}
