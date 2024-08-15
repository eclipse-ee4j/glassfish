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

package com.sun.enterprise.connectors.util;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.enterprise.deployment.ConnectorConfigProperty;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.MessageListener;
import com.sun.logging.LogDomains;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is message listener configuration parser. It parses the
 * ra.xml file for the message listener specific configurations
 * like activationSpec javabean properties, message listener types .
 *
 * @author Srikanth P
 */
public class MessageListenerConfigParserImpl implements MessageListenerConfigParser {

    private static final Logger LOG = LogDomains.getLogger(MessageListenerConfigParserImpl.class, LogDomains.RSR_LOGGER);

    /**
     *  Default constructor.
     */
    public MessageListenerConfigParserImpl() {

    }

    /**
     * Return the ActivationSpecClass name for given rar and messageListenerType
     * @param desc ConnectorDescriptor pertaining to rar.
     * @param messageListenerType MessageListener type
     * @throws  ConnectorRuntimeException If moduleDir is null.
     *          If corresponding rar is not deployed.
     */

    @Override
    public String getActivationSpecClass(ConnectorDescriptor desc, String messageListenerType)
        throws ConnectorRuntimeException {
        if (desc == null) {
            throw new ConnectorRuntimeException("Invalid arguments");
        }

        MessageListener messageListeners[] = ddTransformUtil.getMessageListeners(desc);
        if (messageListeners != null) {
            for (MessageListener messageListener : messageListeners) {
                if (messageListenerType.equals(messageListener.getMessageListenerType())) {
                    return messageListener.getActivationSpecClass();
                }
            }
        }
        return null;
    }


    /**
     * Parses the ra.xml and returns all the Message listener types.
     *
     * @param desc ConnectorDescriptor pertaining to rar.
     * @return Array of message listener types as strings.
     * @throws ConnectorRuntimeException If moduleDir is null. If corresponding rar is not deployed.
     */
    @Override
    public String[] getMessageListenerTypes(ConnectorDescriptor desc) throws ConnectorRuntimeException {
        if (desc == null) {
            throw new ConnectorRuntimeException("Invalid arguments");
        }

        MessageListener messageListeners[] = ddTransformUtil.getMessageListeners(desc);
        String[] messageListenerTypes = null;
        if (messageListeners != null) {
            messageListenerTypes = new String[messageListeners.length];
            for (int i = 0; i < messageListeners.length; ++i) {
                messageListenerTypes[i] = messageListeners[i].getMessageListenerType();
            }
        }
        return messageListenerTypes;
    }


    /**
     * Parses the ra.xml for the ActivationSpec javabean
     * properties. The ActivationSpec to be parsed is
     * identified by the moduleDir where ra.xml is present and the
     * message listener type.
     * message listener type will be unique in a given ra.xml.
     * It throws ConnectorRuntimeException if either or both the
     * parameters are null, if corresponding rar is not deployed,
     * if message listener type mentioned as parameter is not found in ra.xml.
     * If rar is deployed and message listener (type mentioned) is present
     * but no properties are present for the corresponding message listener,
     * null is returned.
     *
     * @param desc ConnectorDescriptor pertaining to rar.
     * @param messageListenerType message listener type.It is uniqie
     *            across all <messagelistener> sub-elements in <messageadapter>
     *            element in a given rar.
     * @return Javabean properties with the property names and values
     *         of properties. The property values will be the values
     *         mentioned in ra.xml if present. Otherwise it will be the
     *         default values obtained by introspecting the javabean.
     *         In both the case if no value is present, empty String is
     *         returned as the value.
     * @throws ConnectorRuntimeException if either of the parameters are null.
     *             If corresponding rar is not deployed i.e moduleDir is invalid.
     *             If messagelistener type is not found in ra.xml
     */
    @Override
    public Properties getJavaBeanProps(ConnectorDescriptor desc, String messageListenerType, String rarName)
        throws ConnectorRuntimeException {
        MessageListener messageListener = getMessageListener(desc, messageListenerType);

        /* ddVals           -> Properties present in ra.xml
        *  introspectedVals -> All properties with values
        *                      obtained by introspection of resource
        *                      adapter javabean
        *  mergedVals       -> merged props of raConfigPros and
        *                      allraConfigPropsWithDefVals
        */

        Properties mergedVals = null;
        Set<ConnectorConfigProperty> ddVals = messageListener.getConfigProperties();
        String className = messageListener.getActivationSpecClass();
        if (className != null && !className.isEmpty()) {
            Properties introspectedVals = configParserUtil.introspectJavaBean(className, ddVals, false, rarName);
            mergedVals = configParserUtil.mergeProps(ddVals, introspectedVals);
        }
        return mergedVals;
    }


    private MessageListener getMessageListener(ConnectorDescriptor desc, String messageListenerType)
        throws ConnectorRuntimeException {
        if (desc == null || messageListenerType == null) {
            throw new ConnectorRuntimeException("Invalid arguments");
        }

        MessageListener[] allMessageListeners = ddTransformUtil.getMessageListeners(desc);
        MessageListener messageListener = null;
        for (MessageListener messageListener2 : allMessageListeners) {
            if (messageListenerType.equals(messageListener2.getMessageListenerType())) {
                messageListener = messageListener2;
            }
        }

        if (messageListener == null) {
            LOG.log(Level.FINE, "No such MessageListener found in ra.xml, type: {0}", messageListenerType);
            throw new ConnectorRuntimeException("No such MessageListener found in ra.xml : " + messageListenerType);
        }
        return messageListener;
    }


    @Override
    public List<String> getConfidentialProperties(ConnectorDescriptor desc, String rarName, String... keyFields)
        throws ConnectorRuntimeException {
        if (keyFields == null || keyFields.length == 0 || keyFields[0] == null) {
            throw new ConnectorRuntimeException("MessageListenerType must be specified");
        }
        MessageListener messageListener = getMessageListener(desc, keyFields[0]);
        List<String> confidentialProperties = new ArrayList<>();
        Set<ConnectorConfigProperty> configProperties = messageListener.getConfigProperties();
        if (configProperties != null) {
            for (ConnectorConfigProperty ccp : configProperties) {
                if (ccp.isConfidential()) {
                    confidentialProperties.add(ccp.getName());
                }
            }
        }
        return confidentialProperties;
    }


    /**
     * Returns the Properties object consisting of propertyname as the
     * key and datatype as the value.
     *
     * @param messageListenerType message listener type.It is uniqie
     *            across all <messagelistener> sub-elements in <messageadapter>
     *            element in a given rar.
     * @return Properties object with the property names(key) and datatype
     *         of property(as value).
     * @throws ConnectorRuntimeException if either of the parameters are null.
     *             If corresponding rar is not deployed i.e moduleDir is invalid.
     *             If messagelistener type is not found in ra.xml
     */
    @Override
    public Properties getJavaBeanReturnTypes(ConnectorDescriptor desc, String messageListenerType, String rarName)
        throws ConnectorRuntimeException {
        MessageListener messageListener = getMessageListener(desc, messageListenerType);

        /* ddVals           -> Properties present in ra.xml
        *  introspectedVals -> All properties with values
        *                      obtained by introspection of resource
        *                      adapter javabean
        *  mergedVals       -> merged props of raConfigPros and
        *                      allraConfigPropsWithDefVals
        */

        Properties mergedVals = null;
        Set<ConnectorConfigProperty> ddVals = messageListener.getConfigProperties();
        String className = messageListener.getActivationSpecClass();
        if (className != null && className.length() != 0) {
            Properties introspectedVals = configParserUtil.introspectJavaBeanReturnTypes(className, ddVals, rarName);
            mergedVals = configParserUtil.mergePropsReturnTypes(ddVals, introspectedVals);
        }
        return mergedVals;
    }
}
