/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.gms;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Clusters;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.FailureDetection;
import com.sun.enterprise.config.serverbeans.GroupManagementService;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.config.ConfigurationUpgrade;
import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.Transaction;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

/**
 * Startup service to upgrade cluster/gms elements in domain.xml
 *
 * @author Bhakti Mehta
 */
@Service(name="gmsupgrade")
public class GMSConfigUpgrade implements ConfigurationUpgrade, PostConstruct {

    private static final Logger LOG = Logger.getLogger(GMSConfigUpgrade.class.getName());

    @Inject
    Clusters clusters;

    @Inject
    Configs configs;

    @Override
    public void postConstruct() {
        try {
            //This will upgrade all the cluster elements in the domain.xml
            upgradeClusterElements();

            // this will upgrade all the group-management-service elements in domain.xml
            upgradeGroupManagementServiceElements();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failure while upgrading cluster data.", e);
            throw new RuntimeException(e);
        }
    }

    private void upgradeClusterElements () throws TransactionFailure {
        List<Cluster> clusterList = clusters.getCluster();
        for (Cluster cl :clusterList) {
            ConfigSupport.apply(new ClusterConfigCode(), cl);
        }
    }

    private void upgradeGroupManagementServiceElements()
        throws TransactionFailure {
        List<Config> lconfigs = configs.getConfig();
        for (Config c : lconfigs) {
            LOG.log(Level.FINE, "Upgrade config: {0}", c.getName());
            ConfigSupport.apply(new GroupManagementServiceConfigCode(), c);
        }
    }

    private class ClusterConfigCode implements SingleConfigCode<Cluster> {
        @Override
        public Object run(Cluster cluster) throws PropertyVetoException, TransactionFailure {
            //set gms-enabled (default is true incase it may not appear in upgraded
            //domain.xml)
            String value = cluster.getHeartbeatEnabled();
            if (value != null) {
                cluster.setGmsEnabled(value);
                cluster.setHeartbeatEnabled(null);
            }

            //set gms-multicast-address the value obtained from heartbeat-address
            value = cluster.getHeartbeatAddress();
            if (value != null) {
                try {
                    cluster.setGmsMulticastAddress(value);
                } catch (Throwable t) {
                    // catch RuntimeException hk2 ValidationException.  Some values from v2 may not be valid in v3.
                }
                cluster.setHeartbeatAddress(null);
            }

            // ensure this propery is set to a valid value. Either it was missing or v2 value was invalid in v3 constraints.
            if (cluster.getGmsMulticastAddress() == null) {

                // generate a valid gms multicast address. Either heartbeataddress was missing OR had an invalid value from v2 domain.xml
                cluster.setGmsMulticastAddress(generateHeartbeatAddress());
            }

            //set gms-multicast-port the value of heartbeat-port.
            value = cluster.getHeartbeatPort();
            if (value != null) {
                try {
                    cluster.setGmsMulticastPort(value);
                } catch (Throwable t) {
                    // catch RuntimeException hk2 ValidationException. There are definitely values in v2 that are not valid in v3 for this field.
                    // There were bugs filed that this port was randomly generated with an IANA allocated port. So v3.1 min and max are more restrictive than v2 were.
                }
                cluster.setHeartbeatPort(null);
            }

            // ensure this property is set to a valid value. Either the value was missing or had a value in v2 that is considered invalid in tighter constrained v3.1.
            if (cluster.getGmsMulticastPort() == null) {
                // generate a valid gms multicastport. Either heartbeatport was not set or was set to a value that is now invalid in v3.1.
                // port range in v2 was quite large and outside the IANA recommended range being followed by v3.1.
                cluster.setGmsMulticastPort(generateHeartbeatPort());

            }

            //gms-bind-interface is an attribute of cluster in 3.1
            Property prop  = cluster.getProperty("gms-bind-interface-address");
            if (prop != null && prop.getValue() != null) {
                cluster.setGmsBindInterfaceAddress(prop.getValue());
                List<Property> props = cluster.getProperty();
                props.remove(prop);
            } else {
                value = cluster.getGmsBindInterfaceAddress();
                if (value == null) {
                    cluster.setGmsBindInterfaceAddress(String.format(
                            "${GMS-BIND-INTERFACE-ADDRESS-%s}",
                            cluster.getName()));
                }
            }
            Property gmsListenerPort = cluster.createChild(Property.class);
            gmsListenerPort.setName("GMS_LISTENER_PORT");
            gmsListenerPort.setValue(String.format("${GMS_LISTENER_PORT-%s}", cluster.getName()));
            cluster.getProperty().add(gmsListenerPort);
            return cluster;
        }
    }

    static private class GroupManagementServiceConfigCode implements SingleConfigCode<Config> {
        @Override
        public Object run(Config config) throws PropertyVetoException, TransactionFailure {
            GroupManagementService gms = config.getGroupManagementService();
            Transaction t = Transaction.getTransaction(config);
            gms = t.enroll(gms);
            String value = gms.getPingProtocolTimeoutInMillis();
            if (value != null) {
                try {
                    gms.setGroupDiscoveryTimeoutInMillis(value);
                } catch (Throwable re) {
                    // catch RuntimeException hk2 ValidationException. if v2 value is not valid for v3, just rely on v3 default
                }
                gms.setPingProtocolTimeoutInMillis(null);
            } // else null for server-config

            FailureDetection fd = gms.getFailureDetection();
            fd = t.enroll(fd);
            value = gms.getFdProtocolTimeoutInMillis();
            if (value != null){
                try {
                    fd.setHeartbeatFrequencyInMillis(value);
                } catch (Throwable re) {
                    // catch RuntimeException hk2 ValidationException. if v2 value is not valid for v3, just rely on v3 default
                }
                gms.setFdProtocolTimeoutInMillis(null);
            } // else  null for server-config

            value = gms.getFdProtocolMaxTries();
            if (value != null) {
                try {
                    fd.setMaxMissedHeartbeats(value);
                } catch (Throwable re) {
                    // catch RuntimeException hk2 ValidationException. if v2 value is not valid for v3, just rely on v3 default
                }
                gms.setFdProtocolMaxTries(null);
            } // else null for server config

            value = gms.getVsProtocolTimeoutInMillis();
            if (value != null) {
                try {
                    fd.setVerifyFailureWaittimeInMillis(value);
                } catch (Throwable re) {
                    // catch RuntimeException hk2 ValidationException. if v2 value is not valid for v3, just rely on v3 default
                }
                gms.setVsProtocolTimeoutInMillis(null);
            } // else null for server-config

            Property prop = gms.getProperty("failure-detection-tcp-retransmit-timeout");
            if (prop != null && prop.getValue() != null ) {
                try {
                    fd.setVerifyFailureConnectTimeoutInMillis(prop.getValue().trim());
                } catch (Throwable re) {
                    // catch RuntimeException hk2 ValidationException. if v2 value is not valid for v3, just rely on v3 default
                }
                List<Property> props = gms.getProperty();
                props.remove(prop);
            } //else v3.1 default value for VerifyFailureConnectTimeoutInMillis is sufficient.

            // remove v2.1 attributes that are no longer needed.  No info to transfer to v3.1 gms config.
            if (gms.getMergeProtocolMinIntervalInMillis() != null) {
                gms.setMergeProtocolMinIntervalInMillis(null);
            }
            if (gms.getMergeProtocolMaxIntervalInMillis() != null) {
                gms.setMergeProtocolMaxIntervalInMillis(null);
            }

            return config;
        }
    }

    // copied from config-api com.sun.enterprise.config.serverbeans.Cluster.java in order to generate a valid v3.1 value for this required properties.
    private String generateHeartbeatPort() {
        final int MIN_GMS_MULTICAST_PORT = 2048;
        final int MAX_GMS_MULTICAST_PORT = 32000;

        int portInterval = MAX_GMS_MULTICAST_PORT - MIN_GMS_MULTICAST_PORT;
        return Integer.toString(Math.round((float)(Math.random() * portInterval)) + MIN_GMS_MULTICAST_PORT);
    }

    // copied from config-api com.sun.enterprise.config.serverbeans.Cluster.java in order to generate a valid v3.1 value for this required properties.
    private String generateHeartbeatAddress() {
        final int MAX_GMS_MULTICAST_ADDRESS_SUBRANGE = 255;

        final StringBuilder heartbeatAddressBfr = new StringBuilder("228.9.");
        heartbeatAddressBfr.append(Math.round(Math.random() * MAX_GMS_MULTICAST_ADDRESS_SUBRANGE))
                .append('.')
                .append(Math.round(Math.random() * MAX_GMS_MULTICAST_ADDRESS_SUBRANGE));
        return heartbeatAddressBfr.toString();
    }
}
