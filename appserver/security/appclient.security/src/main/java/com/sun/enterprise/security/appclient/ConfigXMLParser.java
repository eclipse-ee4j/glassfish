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

package com.sun.enterprise.security.appclient;

import com.sun.enterprise.security.common.Util;
import com.sun.enterprise.security.jmac.config.*;
import com.sun.enterprise.security.jmac.config.GFServerConfigProvider;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;

import com.sun.logging.LogDomains;

import java.util.List;
import jakarta.xml.bind.JAXBException;
import org.glassfish.appclient.client.acc.config.*;
import sun.security.util.PropertyExpander;
import com.sun.enterprise.security.jmac.AuthMessagePolicy;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.security.auth.message.MessagePolicy;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import org.glassfish.internal.api.Globals;


/**
 * Parser for message-security-config in  glassfish-acc.xml
 */
public class ConfigXMLParser implements ConfigParser { 
    private static Logger _logger=null;
    static {
        _logger = LogDomains.getLogger(ConfigXMLParser.class, LogDomains.SECURITY_LOGGER);
    }

    // configuration info
    private Map configMap = new HashMap();
    private Set<String> layersWithDefault = new HashSet<String>();
    private List<MessageSecurityConfig> msgSecConfigs = null;
    private static final String ACC_XML = "glassfish-acc.xml.url";

    public ConfigXMLParser() throws IOException {
    }

    public void initialize(List<MessageSecurityConfig> msgConfigs) throws IOException {
        this.msgSecConfigs = msgConfigs;
        if (this.msgSecConfigs != null) {
            processClientConfigContext(configMap);
        }
    }


    private void processClientConfigContext(Map newConfig) throws IOException {
        // auth-layer
        String intercept = null;

        List<MessageSecurityConfig> msgConfigs = this.msgSecConfigs;
        assert(msgConfigs != null);
        for (MessageSecurityConfig config : msgConfigs) {
            intercept = parseInterceptEntry(config, newConfig);
            List<ProviderConfig> pConfigs = config.getProviderConfig();
            for (ProviderConfig pConfig : pConfigs) {
                parseIDEntry(pConfig, newConfig, intercept);
            }
        }

    }

    public Map getConfigMap() {
        return configMap;
    }

    public Set<String> getLayersWithDefault() {
        return layersWithDefault;
    }
    
    private String parseInterceptEntry(
            MessageSecurityConfig msgConfig, Map newConfig) throws IOException {

        String intercept = null;
        String defaultServerID = null;
        String defaultClientID = null;
        MessageSecurityConfig clientMsgSecConfig = msgConfig;
        intercept = clientMsgSecConfig.getAuthLayer();
        defaultServerID = clientMsgSecConfig.getDefaultProvider();
        defaultClientID = clientMsgSecConfig.getDefaultClientProvider();
        
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("Intercept Entry: " +
                        "\n    intercept: " + intercept +
                        "\n    defaultServerID: " + defaultServerID +
                        "\n    defaultClientID:  " + defaultClientID);
        }

        if (defaultServerID != null || defaultClientID != null) {
            layersWithDefault.add(intercept);
        }

        GFServerConfigProvider.InterceptEntry intEntry =
            (GFServerConfigProvider.InterceptEntry)newConfig.get(intercept);
        if (intEntry != null) {
            throw new IOException("found multiple MessageSecurityConfig " +
                                "entries with the same auth-layer");
        }

        // create new intercept entry
        intEntry = new GFServerConfigProvider.InterceptEntry(defaultClientID,
                defaultServerID, null);
        newConfig.put(intercept, intEntry);
        return intercept;
    }

    // duplicate implementation for clientbeans config
    private void parseIDEntry(
            ProviderConfig pConfig,
            Map newConfig, String intercept)
            throws IOException {

        String id = pConfig.getProviderId();
        String type = pConfig.getProviderType();
        String moduleClass = pConfig.getClassName();
        MessagePolicy requestPolicy = parsePolicy(pConfig.getRequestPolicy());
        MessagePolicy responsePolicy = parsePolicy(pConfig.getResponsePolicy());

        // get the module options

        Map options = new HashMap();
        List<Property> props = pConfig.getProperty();
        for (Property prop : props) {
            try {
                options.put(prop.getName(),
                            PropertyExpander.expand
                            (prop.getValue(),
                             false));
            } catch (sun.security.util.PropertyExpander.ExpandException ee) {
                // log warning and give the provider a chance to 
                // interpret value itself.
                if (_logger.isLoggable(Level.WARNING)) {
                    _logger.warning("jmac.unexpandedproperty");
                }
                options.put(prop.getName(),
                            prop.getValue());
            }
        }

        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("ID Entry: " +
                        "\n    module class: " + moduleClass +
                        "\n    id: " + id +
                        "\n    type: " + type +
                        "\n    request policy: " + requestPolicy +
                        "\n    response policy: " + responsePolicy +
                        "\n    options: " + options);
        }

        // create ID entry

        GFServerConfigProvider.IDEntry idEntry =
                new GFServerConfigProvider.IDEntry(type, moduleClass,
                requestPolicy, responsePolicy, options);

        GFServerConfigProvider.InterceptEntry intEntry =
                (GFServerConfigProvider.InterceptEntry)newConfig.get(intercept);
        if (intEntry == null) {
            throw new IOException
                ("intercept entry for " + intercept +
                " must be specified before ID entries");
        }

        if (intEntry.getIdMap() == null) {
            intEntry.setIdMap(new HashMap());
        }

        // map id to Intercept
        intEntry.getIdMap().put(id, idEntry);
    }

    
    private MessagePolicy parsePolicy(Object policy) {

        if (policy == null) {
            return null;
        }
        String authSource = null;
        String authRecipient = null;

       if (policy instanceof RequestPolicy) {
            RequestPolicy clientRequestPolicy = (RequestPolicy)policy;
            authSource = clientRequestPolicy.getAuthSource();
            authRecipient = clientRequestPolicy.getAuthRecipient();
        } else if (policy instanceof ResponsePolicy) {
            ResponsePolicy clientResponsePolicy = (ResponsePolicy)policy;
            authSource = clientResponsePolicy.getAuthSource();
            authRecipient = clientResponsePolicy.getAuthRecipient();
        }
        return AuthMessagePolicy.getMessagePolicy(authSource, authRecipient);
    }

    public void initialize(Object config) throws IOException {
        String sun_acc = System.getProperty(ACC_XML, "glassfish-acc.xml");
        List<MessageSecurityConfig> msgconfigs = null;
        if (Globals.getDefaultHabitat() == null && sun_acc != null) {
            InputStream is = null;
            try {
                is = new FileInputStream(sun_acc);
                JAXBContext jc = JAXBContext.newInstance(ClientContainer.class);
                Unmarshaller u = jc.createUnmarshaller();
                ClientContainer cc = (ClientContainer) u.unmarshal(is);
                msgconfigs = cc.getMessageSecurityConfig();
            } catch (JAXBException ex) {
                _logger.log(Level.SEVERE, null, ex);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        } else {
            Util util = Util.getInstance();
            assert (util != null);
            msgconfigs = (List<MessageSecurityConfig>) util.getAppClientMsgSecConfigs();
        }
        this.initialize(msgconfigs);
        //this.initialize(config);
    }
}
