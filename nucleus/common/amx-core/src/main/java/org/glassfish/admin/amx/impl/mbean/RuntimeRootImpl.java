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

package org.glassfish.admin.amx.impl.mbean;

import javax.management.ObjectName;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Level;

import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.HK2Module;

import com.sun.enterprise.security.ssl.SSLUtils;
import javax.management.JMException;
import javax.management.remote.JMXServiceURL;
import org.glassfish.admin.amx.base.RuntimeRoot;
import org.glassfish.admin.amx.base.ServerRuntime;
import org.glassfish.api.admin.AdminCommandContextImpl;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.admin.amx.util.ExceptionUtil;
import org.glassfish.admin.amx.impl.util.InjectedValues;

import org.glassfish.external.amx.AMXGlassfish;


import org.glassfish.api.admin.AdminCommandContext;
import com.sun.enterprise.v3.admin.RestartDomainCommand;
import com.sun.enterprise.v3.common.PlainTextActionReporter;
import org.glassfish.api.admin.AdminCommand;
import com.sun.enterprise.v3.admin.commands.JVMInformation;
import java.util.Locale;
import javax.management.MBeanServer;
import org.glassfish.admin.amx.impl.util.ObjectNameBuilder;
import org.glassfish.admin.amx.util.AMXLoggerInfo;
import org.glassfish.admin.amx.util.StringUtil;


/**
AMX RealmsMgr implementation.
Note that realms don't load until {@link #loadRealms} is called.
 */
public final class RuntimeRootImpl extends AMXImplBase
{
    private final ServiceLocator mHabitat;

    public RuntimeRootImpl(final ObjectName parent)
    {
        super(parent, RuntimeRoot.class);

        mHabitat = InjectedValues.getInstance().getHabitat();
    }

    public ObjectName[] getServerRuntime()
    {
        return getChildren( ServerRuntime.class );
    }


    @Override
    protected final void registerChildren()
    {
        super.registerChildren();

        final ObjectName self = getObjectName();
        final MBeanServer server = getMBeanServer();
        final ObjectNameBuilder objectNames = new ObjectNameBuilder(server, self);

        ObjectName childObjectName = null;
        Object mbean = null;

        // when clustering comes along, some other party will need to register MBeans
        // for each non-DAS instance
        childObjectName = objectNames.buildChildObjectName(ServerRuntime.class, AMXGlassfish.DEFAULT.dasName());
        mbean = new ServerRuntimeImpl(self);
        registerChild(mbean, childObjectName);
    }


    public void stopDomain()
    {
        final ModulesRegistry registry = InjectedValues.getInstance().getModulesRegistry();
        final Collection<HK2Module> modules = registry.getModules("com.sun.enterprise.osgi-adapter");
        if (modules.size() == 1)
        {
            final HK2Module mgmtAgentModule = modules.iterator().next();
            mgmtAgentModule.stop();
        }
        else
        {
            AMXLoggerInfo.getLogger().warning(AMXLoggerInfo.cantFindOSGIAdapter);
        }

        AMXLoggerInfo.getLogger().warning(AMXLoggerInfo.stoppingServerForcibly);
        System.exit(0);
    }

    public void restartDomain()
    {
        final ModulesRegistry registry = InjectedValues.getInstance().getModulesRegistry();

        final AdminCommandContext ctx = new AdminCommandContextImpl(AMXLoggerInfo.getLogger(), new PlainTextActionReporter());
        final AdminCommand cmd = new RestartDomainCommand(registry);
        cmd.execute(ctx);
    }

    private NetworkConfig networkConfig()
    {
        final NetworkConfig config = InjectedValues.getInstance().getHabitat().getService(
                        NetworkConfig.class, ServerEnvironment.DEFAULT_INSTANCE_NAME);
        return config;
    }

    private static final String ADMIN_LISTENER_NAME = "admin-listener";

    private NetworkListener getAdminListener()
    {
        final NetworkConfig network = networkConfig();

        final NetworkListener listener = network.getNetworkListener(ADMIN_LISTENER_NAME);
        return listener;
    }

    private int getRESTPort()
    {
        return (int) Long.parseLong(getAdminListener().getPort());
    }

    private String get_asadmin()
    {
        final Protocol protocol = networkConfig().getProtocols().findProtocol(ADMIN_LISTENER_NAME);
        return protocol.getHttp().getDefaultVirtualServer();
    }

    public String getRESTBaseURL() throws MalformedURLException
    {
        final Protocol protocol = networkConfig().getProtocols().findProtocol(ADMIN_LISTENER_NAME);
        final String scheme = Boolean.parseBoolean(protocol.getSecurityEnabled()) ? "https" : "http";
        final String host = "localhost";
        URL url = new URL(scheme, host, getRESTPort(), "/" + get_asadmin());
        return url.toString() + "/";
    }


    public String executeREST(final String cmd)
    {
        String result = null;

        HttpURLConnection conn = null;
        try
        {
            final String url = getRESTBaseURL() + cmd;

            final URL invoke = new URL(url);
            //System.out.println( "Opening connection to: " + invoke );
            conn = (HttpURLConnection) invoke.openConnection();

            final InputStream is = conn.getInputStream();
            result = toString(is);
            is.close();
        }
        catch (Exception e)
        {
            result = ExceptionUtil.toString(e);
        }
        finally
        {
            if (conn != null)
            {
                conn.disconnect();
            }
        }
        return result;
    }

    public String[] getSupportedCipherSuites()
    {
        try
        {
            final SSLUtils sslUtils = mHabitat.getService(SSLUtils.class);
            return sslUtils.getSupportedCipherSuites();
        }
        catch (final Exception ex)
        {
            AMXLoggerInfo.getLogger().log( Level.INFO, AMXLoggerInfo.cantGetCipherSuites, ex);
            return new String[0];
        }
    }

    public String[] getJMXServiceURLs()
    {
        try
        {
            final AMXGlassfish amxg = AMXGlassfish.DEFAULT;
            final JMXServiceURL[] items = (JMXServiceURL[])getMBeanServer().getAttribute(amxg.getBootAMXMBeanObjectName(), "JMXServiceURLs");
            final String [] urls = new String[ items.length ];
            for( int i = 0; i < items.length; ++i )
            {
                urls[i] = "" + items[i];
            }
            return urls;
        }
        catch (final JMException e)
        {
            throw new RuntimeException(e);
        }
    }

    public String getJVMReport(final String type)
    {
        final JVMInformation info = new JVMInformation(getMBeanServer());

        final String NL = StringUtil.LS;
        final String target = "das";
        String result = "FAILED";
        if ("summary".equals(type))
        {
            result = info.getSummary(target);
        }
        else if ("memory".equals(type))
        {
            result = info.getMemoryInformation(target);
        }
        else if ("thread".equals(type))
        {
            result = info.getThreadDump(target);
        }
        else if ("class".equals(type))
        {
            result = info.getClassInformation(target);
        }
        else if ("log".equals(type))
        {
            result = info.getLogInformation(target);
        }
        else if ("all".equals(type))
        {
            result = "SUMMARY" + NL + NL + getJVMReport("summary") + NL + NL +
                     "MEMORY" + NL + NL + getJVMReport("memory") + NL + NL +
                     "THREADS" + NL + NL + getJVMReport("thread") + NL + NL +
                     "CLASSES" + NL + NL + getJVMReport("class") + NL + NL +
                     "LOGGING" + NL + NL + getJVMReport("log");
        }
        else
        {
            throw new IllegalArgumentException("Unsupported JVM report type: " + type);
        }

        if (result != null)
        {
            result = result.replace("%%%EOL%%%", NL);
        }
        return result;
    }

    public boolean isStartedInDebugMode()
    {
        boolean inDebugMode = false;

        final String s = System.getProperty("hk2.startup.context.args");
        if ( s != null )
        {
            final String prefix = "-debug=";
            final String[] ss = s.split("\n");

            for( final String opt : ss)
            {
                if( opt.startsWith(prefix) )
                {
                    final String value = opt.substring( prefix.length() ).toLowerCase(Locale.ENGLISH);
                    //System.out.println( "RuntimeRootImpl.isRunningInDebugMode(): found: " + prefix + value );
                    inDebugMode = Boolean.valueOf(value );
                    break;
                }
            }
        }
        return inDebugMode;
    }
}

























