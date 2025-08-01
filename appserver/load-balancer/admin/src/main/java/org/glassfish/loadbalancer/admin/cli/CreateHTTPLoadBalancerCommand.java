/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.loadbalancer.admin.cli;

import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.security.auth.Subject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandInvocation;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.api.Target;
import org.glassfish.loadbalancer.config.LoadBalancer;
import org.glassfish.loadbalancer.config.LoadBalancers;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.RetryableException;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.Transaction;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import static org.glassfish.config.support.Constants.NAME_REGEX;

/**
  * This method supports the create-http-lb CLI command. It creates a lb-config, cluster-ref, health-checker by using
  * the given parameters.
  * @param loadbalancername the name for the load-balancer element that will be created
  * @param target cluster-ref or server-ref parameter of lb-config
  * @param options Map of option name and option value. The valid options are
  *          responsetimeout response-timeout-in-seconds attribute of lb-config
  *          httpsrouting https-routing parameter of lb-config
  *          reloadinterval reload-poll-interval-in-seconds parameter of lb-config
  *          monitor monitoring-enabled parameter of lb-config
  *          routecookie route-cookie-enabled parameter of lb-config
  *          lb-policy load balancing policy to be used for the cluster target
  *          lb-policy-module specifies the path to the shared library implementing the user-defined policy
  *          healthcheckerurl url attribute of health-checker
  *          healthcheckerinterval interval-in-seconds parameter of health-checker
  *          healthcheckertimeout timeout-in-seconds parameter of health-checker
  * @author Yamini K B
  */
@Service(name = "create-http-lb")
@PerLookup
@I18n("create.http.lb")
@TargetType(value={CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER})
@org.glassfish.api.admin.ExecuteOn({RuntimeType.DAS})
public final class CreateHTTPLoadBalancerCommand extends LBCommandsBase
        implements AdminCommand {

    @Param (optional=false)
    String devicehost;

    @Param (optional=false)
    String deviceport;

    @Param (optional=true)
    String target;

    @Param (optional=true)
    String sslproxyhost;

    @Param (optional=true)
    String sslproxyport;

    @Param (optional=true)
    String lbpolicy;

    @Param (optional=true)
    String lbpolicymodule;

    @Param (optional=true, defaultValue="/")
    String healthcheckerurl;

    @Param (optional=true, defaultValue="30")
    String healthcheckerinterval;

    @Param (optional=true, defaultValue="10")
    String healthcheckertimeout;

    @Param (optional=true)
    String lbenableallinstances;

    @Param (optional=true)
    String lbenableallapplications;

    @Param (optional=true)
    String lbweight;

    @Param (optional=true, defaultValue="60")
    String responsetimeout;

    @Param (optional=true, defaultValue="false")
    Boolean httpsrouting;

    @Param (optional=true, defaultValue="60")
    String reloadinterval;

    @Param (optional=true, defaultValue="false")
    Boolean monitor;

    @Param (optional=true, defaultValue="true")
    Boolean routecookie;

    @Param (obsolete=true, optional=true)
    Boolean autoapplyenabled;

    @Param(optional=true, name="property", separator=':')
    Properties properties;

    @Param (primary=true)
    String load_balancer_name;

    @Inject
    Target tgt;

    @Inject
    Logger logger;

    @Inject
    CommandRunner runner;

    @Inject
    Applications applications;

    private ActionReport report;

    final private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(CreateHTTPLoadBalancerCommand.class);


    @Override
    public void execute(AdminCommandContext context) {
        //final Logger logger = context.getLogger();

        report = context.getActionReport();

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

        if (load_balancer_name == null) {
            String msg = localStrings.getLocalString("NullLBName", "Load balancer name cannot be null");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        if(!Pattern.matches(NAME_REGEX, load_balancer_name)){
            String msg = localStrings.getLocalString("loadbalancer.invalid.name", "Invalid load-balancer name");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        LoadBalancers loadBalancers = domain.getExtensionByType(LoadBalancers.class);
        if (loadBalancers != null && loadBalancers.getLoadBalancer(load_balancer_name) != null) {
            String msg = localStrings.getLocalString("LBExists", "Load balancer {0} already exists", load_balancer_name);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        if (target != null && !tgt.isValid(target)) {
            String msg = localStrings.getLocalString("InvalidTarget", "Invalid target", target);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        boolean isCluster = tgt.isCluster(target);
        String lbConfigName = load_balancer_name + "_LB_CONFIG";

        if(!isCluster){
            if((lbpolicy!=null) || (lbpolicymodule!=null)){
                String msg = localStrings.getLocalString("NotCluster",
                        "{0} not a cluster", target);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(msg);
                return;
            }
        }

        try {
            createLBConfig(lbConfigName, context.getSubject());

            if (target != null) {
                final CreateHTTPLBRefCommand command = (CreateHTTPLBRefCommand)runner
                        .getCommand("create-http-lb-ref", report);
                command.target = target;
                //command.lbname = load_balancer_name;
                command.config = lbConfigName;
                command.lbpolicy = lbpolicy;
                command.lbpolicymodule = lbpolicymodule;
                command.healthcheckerurl = healthcheckerurl;
                command.healthcheckerinterval = healthcheckerinterval;
                command.healthcheckertimeout = healthcheckertimeout;
                command.lbenableallinstances = lbenableallinstances;
                command.lbenableallapplications = lbenableallapplications;
                command.lbweight = lbweight;
                command.execute(context);
                checkCommandStatus(context);
            }
        } catch (CommandException e) {
            String msg = e.getLocalizedMessage();
            logger.warning(msg);
//                    report.setActionExitCode(ExitCode.FAILURE);
//                    report.setMessage(msg);
//                    return;
        }
        addLoadBalancer(lbConfigName);

        if (isCluster && lbweight != null) {
            try {
                final ConfigureLBWeightCommand command = (ConfigureLBWeightCommand)runner
                        .getCommand("configure-lb-weight", report);
                command.weights=lbweight;
                command.cluster=target;
                command.execute(context);
                checkCommandStatus(context);
            } catch (CommandException e) {
                String msg = e.getLocalizedMessage();
                logger.warning(msg);
    //                    report.setActionExitCode(ExitCode.FAILURE);
    //                    report.setMessage(msg);
    //                    return;
            }
        }
    }

    private void createLBConfig(String config, final Subject subject) throws CommandException {
        CommandInvocation<?> ci = runner.getCommandInvocation("create-http-lb-config", report, subject);
        ParameterMap map = new ParameterMap();
        //map.add("target", target);
        map.add("responsetimeout", responsetimeout);
        map.add("httpsrouting", httpsrouting==null ? null : httpsrouting.toString());
        map.add("reloadinterval", reloadinterval);
        map.add("monitor", monitor==null ? null : monitor.toString());
        map.add("routecookie", routecookie==null ? null : routecookie.toString());
        map.add("name", config);
        ci.parameters(map);
        ci.execute();
    }

   private void addLoadBalancer(final String lbConfigName) {
       LoadBalancers loadBalancers = domain.getExtensionByType(LoadBalancers.class);
       //create load-balancers parent element if it does not exist
       if (loadBalancers == null) {
           Transaction transaction = new Transaction();
           try {
               ConfigBeanProxy domainProxy = transaction.enroll(domain);
               loadBalancers = domainProxy.createChild(LoadBalancers.class);
               ((Domain) domainProxy).getExtensions().add(loadBalancers);
               transaction.commit();
           } catch (TransactionFailure ex) {
               transaction.rollback();
               String msg = localStrings.getLocalString("FailedToUpdateLB",
                       "Failed to update load-balancers");
               report.setActionExitCode(ActionReport.ExitCode.FAILURE);
               report.setMessage(msg);
               return;
           } catch (RetryableException ex) {
               transaction.rollback();
               String msg = localStrings.getLocalString("FailedToUpdateLB",
                       "Failed to update load-balancers");
               report.setActionExitCode(ActionReport.ExitCode.FAILURE);
               report.setMessage(msg);
               return;
           }
       }

        try {
            ConfigSupport.apply(new SingleConfigCode<LoadBalancers>() {
                    @Override
                    public Object run(LoadBalancers param) throws PropertyVetoException, TransactionFailure {
                        LoadBalancer lb = param.createChild(LoadBalancer.class);
                        lb.setDeviceHost(devicehost);
                        lb.setDevicePort(deviceport);
                        lb.setLbConfigName(lbConfigName);
                        lb.setName(load_balancer_name);

                        // add properties
                        if (properties != null) {
                            for (Object propname: properties.keySet()) {
                                Property newprop = lb.createChild(Property.class);
                                newprop.setName((String) propname);
                                newprop.setValue(properties.getProperty((String) propname));
                                lb.getProperty().add(newprop);
                            }
                        }

                        if (sslproxyhost != null) {
                            Property newprop = lb.createChild(Property.class);
                            newprop.setName("ssl-proxy-host");
                            newprop.setValue(sslproxyhost);
                            lb.getProperty().add(newprop);
                        }

                        if (sslproxyport != null) {
                            Property newprop = lb.createChild(Property.class);
                            newprop.setName("ssl-proxy-port");
                            newprop.setValue(sslproxyport);
                            lb.getProperty().add(newprop);
                        }
                        param.getLoadBalancer().add(lb);
                        return Boolean.TRUE;
                    }
            }, loadBalancers);
        } catch (TransactionFailure ex) {
            String msg = localStrings.getLocalString("FailedToUpdateLB",
                    "Failed to update load-balancers");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }
    }
}
