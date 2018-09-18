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

package org.glassfish.deployment.client;

/**
 * This class defines all necessary information to 
 * connect to a particular application server deployment
 * backend. 
 *
 * @author Jerome Dochez
 */
public class ServerConnectionIdentifier {
    
    // XXX these may not be needed - verify - copied from com.sun.enterprise.admin.jmx.remote.DefaultConfiguration to avoid dependency
    private static class DefaultConfiguration {
        public static final String S1_HTTP_PROTOCOL = "s1ashttp";
        public static final String S1_HTTPS_PROTOCOL = "s1ashttps";
    }
    
    /**
     * Holds value of property hostName.
     */
    private String hostName;
    
    /**
     * Holds value of property hostPort.
     */
    private int hostPort;
    
    /**
     * Holds value of property userName.
     */
    private String userName;
    
    /**
     * Holds value of property password.
     */
    private String password;
    
    /**
     * Holds value of property protocol.
     */
    private String protocol;

    private boolean secure = false;

    private ServerConnectionEnvironment env;
    
    /** Creates a new instance of ServerConnectionIdentifier */
    public ServerConnectionIdentifier() {
    }
    
    public ServerConnectionIdentifier(
            String hostName,
            int hostPort,
            String userName,
            String password,
            boolean secure
            ) {
        this.hostName = hostName;
        this.hostPort = hostPort;
        this.userName = userName;
        this.password = password;
        setSecure(secure);
    }

    /**
     * Getter for property hostName.
     * @return Value of property hostName.
     */
    public String getHostName() {
        return this.hostName;
    }    
    
    /**
     * Setter for property hostName.
     * @param hostName New value of property hostName.
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }    
    
    /**
     * Getter for property hostPort.
     * @return Value of property hostPort.
     */
    public int getHostPort() {
        return this.hostPort;
    }
    
    /**
     * Setter for property hostPort.
     * @param hostPort New value of property hostPort.
     */
    public void setHostPort(int hostPort) {
        this.hostPort = hostPort;
    }
    
    /**
     * Getter for property userName.
     * @return Value of property userName.
     */
    public String getUserName() {
        return this.userName;
    }
    
    /**
     * Setter for property userName.
     * @param userName New value of property userName.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    /**
     * Getter for property password.
     * @return Value of property password.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Setter for property password.
     * @param password New value of property password.
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    public void setSecure(boolean secure) {
        this.secure = secure;
        this.protocol = (secure ? 
            DefaultConfiguration.S1_HTTPS_PROTOCOL : DefaultConfiguration.S1_HTTP_PROTOCOL);
    }

    public boolean isSecure() {
        return this.secure;
    }

    public ServerConnectionEnvironment getConnectionEnvironment() {
        if (env == null) {
            env = new ServerConnectionEnvironment();
        }
        return env;
    }

    public void setConnectionEnvironment(ServerConnectionEnvironment env) {
        this.env = env;
    }
    
    /**
     * Getter for property protocol.
     * the protocol can only be two values:
     * either DefaultConfiguration.S1_HTTPS_PROTOCOL, if secure
     * or DefaultConfiguration.S1_HTTP_PROTOCOL, if not secure
     * @return Value of property protocol.
     */
    public String getProtocol() {
        if (isSecure()) {
            return DefaultConfiguration.S1_HTTPS_PROTOCOL;
        } else {
            return DefaultConfiguration.S1_HTTP_PROTOCOL;
        }
    }
    
    /**
    * @return true if I am the equals to the other object
    */
    public boolean equals(Object other) {
        if (other instanceof ServerConnectionIdentifier) {
            ServerConnectionIdentifier dci = (ServerConnectionIdentifier) other;
            if (hostName==null) {
                return false;
            } else {
                if (!hostName.equals(dci.hostName))
                    return false;
            }
            if (hostPort!=dci.hostPort) {
                return false;
            }
            if (!getConnectionEnvironment().equals(dci.getConnectionEnvironment())) {
                return false;
            }
	    if (protocol==null) {
		return dci.protocol==null;
            } else { 
		return protocol.equals(dci.protocol);
            }
        } else {
            return false;
        }
    }     
    
    public int hashCode() {
        int result = 17;
        result = 37*result + (hostName == null ? 0 : hostName.hashCode());
        result = 37*result + hostPort;
        result = 37*result + getConnectionEnvironment().hashCode();
        result = 37*result + (protocol == null ? 0: protocol.hashCode());
        return result;
    }

    public String toString() {
        return getUserName()+"("+ getPassword()+")@(" + getHostName()
            + "):(" + getHostPort()+")" + ":" + getProtocol();
    }
}
