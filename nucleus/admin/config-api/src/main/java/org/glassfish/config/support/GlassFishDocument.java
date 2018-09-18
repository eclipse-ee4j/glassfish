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

package org.glassfish.config.support;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.jvnet.hk2.config.*;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.ExecutorService;
import java.io.IOException;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.util.ConfigApiLoggerInfo;
import com.sun.appserv.server.util.Version;

/**
 * plug our Dom implementation
 *
 * @author Jerome Dochez
 * 
 */
public class GlassFishDocument extends DomDocument<GlassFishConfigBean> {

    Logger logger = ConfigApiLoggerInfo.getLogger();

    public GlassFishDocument(final ServiceLocator habitat, final ExecutorService executor) {
        super(habitat);

        ServiceLocatorUtilities.addOneConstant(habitat, executor, "transactions-executor", ExecutorService.class);
        ServiceLocatorUtilities.addOneConstant(habitat, this, null, DomDocument.class);

        final DomDocument doc = this;
        
        habitat.<Transactions>getService(Transactions.class).addTransactionsListener(new TransactionListener() {
            public void transactionCommited(List<PropertyChangeEvent> changes) {
                if (!isGlassFishDocumentChanged(changes)) {
                    return;
                }
                
                for (ConfigurationPersistence pers : habitat.<ConfigurationPersistence>getAllServices(ConfigurationPersistence.class)) {
                    try {
                        if (doc.getRoot().getProxyType().equals(Domain.class)) {
                            Dom domainRoot = doc.getRoot();
                            domainRoot.attribute("version", Version.getBuildVersion());
                        }
                        pers.save(doc);
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, 
                        	ConfigApiLoggerInfo.glassFishDocumentIOException,e);
                    } catch (XMLStreamException e) {
                        logger.log(Level.SEVERE, 
                        	ConfigApiLoggerInfo.glassFishDocumentXmlException,e);
                    }
                }
            }

            // make sure domain.xml is changed
            private boolean isGlassFishDocumentChanged(
                    List<PropertyChangeEvent> changes) {
                for (PropertyChangeEvent event : changes) {
                    ConfigBeanProxy source = (ConfigBeanProxy) event.getSource();
                    if (Dom.unwrap(source) instanceof GlassFishConfigBean) {
                        return true;
                    }
                }
                return false;
            }

            public void unprocessedTransactedEvents(List<UnprocessedChangeEvents> changes) {

            }
        });
    }

    @Override
    public GlassFishConfigBean make(final ServiceLocator habitat, XMLStreamReader xmlStreamReader, GlassFishConfigBean dom, ConfigModel configModel) {
        // by default, people get the translated view.
        return new GlassFishConfigBean(habitat, this, dom, configModel, xmlStreamReader);
    }
}
