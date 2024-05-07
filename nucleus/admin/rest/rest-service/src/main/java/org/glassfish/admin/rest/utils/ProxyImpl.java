/*
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.utils;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.SecureAdmin;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.security.ssl.SSLUtils;
import jakarta.ws.rs.ProcessingException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.glassfish.admin.rest.client.utils.MarshallingUtils;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * @author Mitesh Meswani
 */
public abstract class ProxyImpl implements Proxy {

    @Override
    public Properties proxyRequest(UriInfo sourceUriInfo, Client client, ServiceLocator habitat) {
        Properties proxiedResponse = new Properties();
        try {
            Domain domain = habitat.getService(Domain.class);
            String forwardInstanceName = extractTargetInstanceName(sourceUriInfo);
            Server forwardInstance = domain.getServerNamed(forwardInstanceName);
            if (forwardInstance != null) {
                UriBuilder forwardUriBuilder = constructForwardURLPath(sourceUriInfo);
                URI forwardURI = forwardUriBuilder.scheme("https").host(forwardInstance.getAdminHost()).port(forwardInstance.getAdminPort())
                        .build(); //Host and Port are replaced to that of forwardInstanceName
                client = addAuthenticationInfo(client, forwardInstance, habitat);
                WebTarget resourceBuilder = client.target(forwardURI);
                SecureAdmin secureAdmin = habitat.getService(SecureAdmin.class);
                final String indicatorValue = SecureAdmin.configuredAdminIndicator(secureAdmin);
                Invocation.Builder builder;
                Response response;
                if (indicatorValue != null) {
                    builder = resourceBuilder.request(MediaType.APPLICATION_JSON).header(SecureAdmin.ADMIN_INDICATOR_HEADER_NAME,
                            indicatorValue);
                    response = builder.get(Response.class);
                } else {
                    response = resourceBuilder.request(MediaType.APPLICATION_JSON).get(Response.class);
                }
                Response.Status status = Response.Status.fromStatusCode(response.getStatus());
                if (status.getFamily() == jakarta.ws.rs.core.Response.Status.Family.SUCCESSFUL) {
                    String jsonDoc = response.readEntity(String.class);
                    Map responseMap = MarshallingUtils.buildMapFromDocument(jsonDoc);
                    Map resultExtraProperties = (Map) responseMap.get("extraProperties");
                    if (resultExtraProperties != null) {
                        Object entity = resultExtraProperties.get("entity");
                        if (entity != null) {
                            proxiedResponse.put("entity", entity);
                        }

                        @SuppressWarnings({ "unchecked" })
                        Map<String, String> childResources = (Map<String, String>) resultExtraProperties.get("childResources");
                        for (Map.Entry<String, String> entry : childResources.entrySet()) {
                            String targetURL = null;
                            try {
                                URL originalURL = new URL(entry.getValue());
                                //Construct targetURL which has host+port of DAS and path from originalURL
                                targetURL = constructTargetURLPath(sourceUriInfo, originalURL).build().toASCIIString();
                            } catch (MalformedURLException e) {
                                //TODO There was an exception while parsing URL. Need to decide what to do. For now ignore the child entry
                            }
                            entry.setValue(targetURL);
                        }
                        proxiedResponse.put("childResources", childResources);
                    }
                    Object message = responseMap.get("message");
                    if (message != null) {
                        proxiedResponse.put("message", message);
                    }
                    Object properties = responseMap.get("properties");
                    if (properties != null) {
                        proxiedResponse.put("properties", properties);
                    }
                } else {
                    throw new WebApplicationException(response.readEntity(String.class), status);
                }
            } else { // server == null
                // TODO error to user. Can not locate server for whom data is being looked for

            }
        } catch (ProcessingException ex) {
            // couldn't contact remote instance
            throw new WebApplicationException(ex, Response.Status.GONE);
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return proxiedResponse;

    }

    /**
     * Use SSL to authenticate
     */
    private Client addAuthenticationInfo(Client client, Server server, ServiceLocator habitat) {
        SecureAdmin secureAdmin = habitat.getService(SecureAdmin.class);

        // TODO need to get hardcoded "TLS" from corresponding ServerRemoteAdminCommand constant);
        final SSLContext sslContext = habitat.<SSLUtils>getService(SSLUtils.class)
                .getAdminSSLContext(SecureAdmin.DASAlias(secureAdmin), "TLS");

        // Instruct Jersey to use HostNameVerifier and SSLContext provided by us.
        final ClientBuilder clientBuilder = ClientBuilder.newBuilder().withConfig(client.getConfiguration())
                .hostnameVerifier(new BasicHostnameVerifier(server.getAdminHost())).sslContext(sslContext);

        return clientBuilder.build();
    }

    /**
     * TODO copied from HttpConnectorAddress. Need to refactor code there to reuse
     */
    private static class BasicHostnameVerifier implements HostnameVerifier {
        private final String host;

        public BasicHostnameVerifier(String host) {
            if (host == null)
                throw new IllegalArgumentException("null host");
            this.host = host;
        }

        public boolean verify(String s, SSLSession sslSession) {
            return host.equals(s);
        }
    }

}
