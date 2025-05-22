/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.SystemPropertyBag;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

/**
 * Delete System Property Command
 *
 * Removes one system property of the domain, configuration, cluster, or server
 * instance, at a time
 *
 * Usage: delete-system-property [--terse=false] [--echo=false] [--interactive=true]
 * [--host localhost] [--port 4848|4849] [--secure|-s=true] [--user admin_user] [
 * --passwordfile file_name] [--target target(Default server)] property_name
 *
 */
@Service(name="delete-system-property")
@PerLookup
@ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType(value={CommandTarget.CLUSTER, CommandTarget.CLUSTERED_INSTANCE,
CommandTarget.CONFIG, CommandTarget.DAS, CommandTarget.DOMAIN, CommandTarget.STANDALONE_INSTANCE})
@I18n("delete.system.property")
public class DeleteSystemProperty implements AdminCommand, AdminCommandSecurity.Preauthorization, AdminCommandSecurity.AccessCheckProvider {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeleteSystemProperty.class);

    @Param(optional=true, defaultValue=SystemPropertyConstants.DAS_SERVER_NAME)
    String target;

    @Param(name="property_name", primary=true)
    String propName;

    @Inject
    Domain domain;

    private SystemPropertyBag spb;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        spb = CLIUtil.chooseTarget(domain, target);
        if (spb == null) {
            final ActionReport report = context.getActionReport();
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            String msg = localStrings.getLocalString("invalid.target.sys.props",
                    "Invalid target:{0}. Valid targets types are domain, config, cluster, default server, clustered instance, stand alone instance", target);
            report.setMessage(msg);
            return false;
        }
        return true;
    }

    @Override
    public Collection<? extends AccessRequired.AccessCheck> getAccessChecks() {
        final Collection<AccessRequired.AccessCheck> result = new ArrayList<>();
        result.add(new AccessRequired.AccessCheck(AccessRequired.Util.resourceNameFromConfigBeanProxy(spb), "update"));
        return result;
    }

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        Property domainProp = domain.getProperty("administrative.domain.name");
        String domainName = domainProp.getValue();
        if(!spb.containsProperty(propName)) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            String msg = localStrings.getLocalString("no.such.property",
                    "System Property named {0} does not exist at the given target {1}", propName, target);
            report.setMessage(msg);
            return;
        }
        if (definitions(propName) == 1) { //implying user is deleting the "last" definition of this property
            List<String> refs = new ArrayList<>();
            List<Dom> doms = new ArrayList<>();
            if ("domain".equals(target) || target.equals(domainName)) {
                for (Server s : domain.getServers().getServer()) {
                    Config config = s.getConfig();
                    Cluster cluster = s.getCluster();
                    if (!s.containsProperty(propName) && !config.containsProperty(propName)) {
                        if (cluster != null) {
                            if (!cluster.containsProperty(propName)) {
                                doms.add(Dom.unwrap(s));
                            }
                        } else {
                            doms.add(Dom.unwrap(s));
                        }
                    }
                }
            } else {
                Config config = domain.getConfigNamed(target);
                if (config != null) {
                    doms.add(Dom.unwrap(config));
                    String configName = config.getName();
                    for (Server s : domain.getServers().getServer()) {
                        String configRef = s.getConfigRef();
                        if (configRef.equals(configName)) {
                            if (!s.containsProperty(propName)) {
                                doms.add(Dom.unwrap(s));
                            }
                        }
                    }
                    for (Cluster c : domain.getClusters().getCluster()) {
                        String configRef = c.getConfigRef();
                        if (configRef.equals(configName)) {
                            if (!c.containsProperty(propName)) {
                                doms.add(Dom.unwrap(c));
                            }
                        }
                    }
                } else {
                    Cluster cluster = domain.getClusterNamed(target);
                    if (cluster != null) {
                        doms.add(Dom.unwrap(cluster));
                        Config clusterConfig = domain.getConfigNamed(cluster.getConfigRef());
                        doms.add(Dom.unwrap(clusterConfig));
                        for (Server s : cluster.getInstances()) {
                            if (!s.containsProperty(propName)) {
                                doms.add(Dom.unwrap(s));
                            }
                        }
                    } else {
                        Server server = domain.getServerNamed(target);
                        doms.add(Dom.unwrap(server));
                        doms.add(Dom.unwrap(domain.getConfigNamed(server.getConfigRef())));
                    }
                }
            }
            String sysPropName = getPropertyAsValue(propName);
            for (Dom d : doms) {
                listRefs(d, sysPropName, refs);
            }
            if (!refs.isEmpty()) {
                //there are some references
                String msg = localStrings.getLocalString("cant.delete.referenced.property",
                        "System Property {0} is referenced by {1} in the configuration. Please remove the references first.", propName, Arrays.toString(refs.toArray()));
                report.setMessage(msg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }
        //now we are sure that the target exits in the config, just remove the given property
        try {
            ConfigSupport.apply(new SingleConfigCode<SystemPropertyBag>() {
                @Override
                public Object run(SystemPropertyBag param) throws PropertyVetoException, TransactionFailure {
                    param.getSystemProperty().remove(param.getSystemProperty(propName));
                    return param;
                }
            }, spb);
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            String msg = localStrings.getLocalString("delete.sysprops.ok",
                    "System Property named {0} deleted from given target {1}. Make sure you check its references.", propName, target);
            report.setMessage(msg);
        } catch (TransactionFailure tf) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tf);
        }
    }

    private int definitions(String propName) {
        //are there multiple <system-property> definitions for the given name?
        int defs = 0;
        SystemPropertyBag bag = domain;
        if (bag.containsProperty(propName)) {
            defs++;
        }

        bag = domain.getServerNamed(target);
        if (bag != null && bag.containsProperty(propName)) {
            defs++;
            Server server = (Server)bag;
            Cluster cluster = server.getCluster();
            if (cluster != null && cluster.containsProperty(propName)) {
                defs++;
            }
            if (server.getConfig().containsProperty(propName)) {
                defs++;
            }
        }

        bag = domain.getClusterNamed(target);
        if (bag != null && bag.containsProperty(propName)) {
            defs++;
            Cluster c = (Cluster)bag;
            Config clusterConfig = domain.getConfigNamed(c.getConfigRef());
            if (clusterConfig.containsProperty(propName)) {
                defs++;
            }
        }

        bag = domain.getConfigNamed(target);
        if (bag != null && bag.containsProperty(propName)) {
            defs++;
        }

        return defs;
    }

    private static void listRefs(Dom dom, String value, List<String> refs) {
        //this method is rather ugly, but it works. See 9340 which presents a compatibility issue
        //frankly, it makes no sense to do an extensive search of all references of <system-property> being deleted,
        //but that's what resolution of this issue demands. --- Kedar 10/5/2009
        for (String aname : dom.getAttributeNames()) {
            String raw = dom.rawAttribute(aname);
            if (raw != null && raw.equals(value)) {
                refs.add(dom.model.getTagName() + ":" + aname);
            }
        }
        for (String ename : dom.getElementNames()) {
            List<Dom> nodes = null;
            try {
                nodes = dom.nodeElements(ename);
            } catch(Exception e) {
                //ignore, in some situations, HK2 might throw ClassCastException here
            }
            if (nodes != null) {
                for (Dom node : nodes)
                 {
                    listRefs(node, value, refs);  //beware: recursive call ...
                }
            }
        }
    }

    /**
     * A method that returns the passed String as a property that can be replaced at run time.
     *
     * @param name String that represents a property, e.g INSTANCE_ROOT_PROPERTY in this class. The String may not be null.
     * @return a String that represents the replaceable value of passed String. Generally speaking it will be decorated with
     * a pair of braces with $ in the front (e.g. "a" will be returned as "${a}").
     * @throws IllegalArgumentException if the passed String is null
     */
    private static String getPropertyAsValue(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("property");
        }
        return new StringBuilder().append("${").append(name).append('}').toString();
    }
}
