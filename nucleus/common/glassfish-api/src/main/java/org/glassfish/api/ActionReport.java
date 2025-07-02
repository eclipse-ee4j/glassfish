/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.api;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jvnet.hk2.annotations.Contract;

import static java.lang.System.Logger.Level.DEBUG;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;

/**
 * An action report is an abstract class allowing any type of server side action like a service execution, a command
 * execution to report on its execution to the originator of the action.
 *
 * Implementations of this interface should provide a good reporting experience based on the user's interface like a
 * browser or a command line shell.
 *
 * @author Jerome Dochez
 */
@Contract
public abstract class ActionReport implements Serializable {
    private static final Logger LOG = System.getLogger(ActionReport.class.getName());
    private static final long serialVersionUID = -238144192513668688L;

    private final AtomicBoolean locked = new AtomicBoolean(false);
    private Properties extraProperties;
    private MessagePart topMessage = new MessagePart(locked);

    public abstract void setActionDescription(String message);

    public abstract void setFailureCause(Throwable t);

    public abstract Throwable getFailureCause();

    /**
     * @param os output stream in UTF-8 encoding if required.
     * @throws IOException
     */
    public abstract void writeReport(OutputStream os) throws IOException;

    public abstract ActionReport addSubActionsReport();

    public abstract void setActionExitCode(ExitCode exitCode);

    public abstract ExitCode getActionExitCode();

    public abstract String getContentType();

    public abstract List<? extends ActionReport> getSubActionsReport();


    public final void lock() {
        LOG.log(DEBUG, "lock()");
        this.locked.set(true);
    }

    public final void unlock() {
        LOG.log(DEBUG, "unlock()");
        this.locked.set(false);
    }

    public MessagePart getTopMessagePart() {
        return topMessage;
    }

    public void setMessage(String message) {
        topMessage.setMessage(message);
    }

    public void appendMessage(String message) {
        topMessage.appendMessage(message);
    }

    public String getMessage() {
        return topMessage.getMessage();
    }

    /**
     * Short for {@code failure(logger,message,null)}
     */
    public final void failure(java.util.logging.Logger logger, String message) {
        failure(logger, message, null);
    }

    /**
     * Report a failure to the logger and {@link ActionReport}.
     *
     * This is more of a convenience to the caller.
     */
    public final void failure(java.util.logging.Logger logger, String message, Throwable e) {
        logger.log(FINE, message, e);
        logger.log(SEVERE, message);
        if (e == null) {
            setMessage(message);
        } else {
            setMessage(message + " : " + e.toString());
            setFailureCause(e);
        }
        setActionExitCode(ActionReport.ExitCode.FAILURE);
    }

    /**
     * return true if the action report or a subaction report has ExitCode.SUCCESS.
     */
    public abstract boolean hasSuccesses();

    /**
     * return true if the action report or a subaction report has ExitCode.WARNING.
     */
    public abstract boolean hasWarnings();

    /**
     * return true if the action report or a subaction report has ExitCode.FAILURE.
     */
    public abstract boolean hasFailures();

    public final Properties getExtraProperties() {
        return extraProperties;
    }

    public void setExtraProperties(Properties properties) {
        extraProperties = properties;
    }

    private final Map resultTypes = new ConcurrentHashMap();

    /**
     * Gets a type that was set by the command implementation
     *
     * @param resultType the type requested
     * @return <T> the actual instance that was set
     */
    public <T> T getResultType(Class<T> resultType) {
        return (T) resultTypes.get(resultType);
    }

    /**
     * Stores the supplies type and its instance. This is a way for the command implementation to pass information between
     * Supplemental command(s) and the main command. For example, the Supplemental command for DeployCommand requires
     * information on pay load, generated directories etc. In this case, the DeployCommand will be expected to set this
     * information in, for example DeployResult, and set it in the ActionReport. The Supplemental Command will then retrieve
     * the DeployResult for its use.
     *
     * @param resultType the type
     * @param resultTypeInstance the actual instance
     */
    public <T> void setResultType(Class<T> resultType, T resultTypeInstance) {
        resultTypes.put(resultType, resultTypeInstance);
    }

    /**
     * Search in message parts properties then in extra properties and then in sub reports. Returns first occurrence of the
     * key.
     */
    public String findProperty(String key) {
        MessagePart topMessagePart = getTopMessagePart();
        if (topMessagePart != null) {
            String value = topMessagePart.findProperty(key);
            if (value != null) {
                return value;
            }
        }
        if (extraProperties != null) {
            String value = extraProperties.getProperty(key);
            if (value != null) {
                return value;
            }
        }
        if (getSubActionsReport() != null) {
            for (ActionReport subReport : getSubActionsReport()) {
                String value = subReport.findProperty(key);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return super.toString() + "[exitCode=" + getActionExitCode() + ", message=" + getMessage() + "]";
    }

    public static final class MessagePart implements Serializable {

        private static final long serialVersionUID = -8708934987452414280L;

        Properties props = new Properties();
        StringBuilder message;
        String childrenType;

        List<MessagePart> children = new ArrayList<>();

        private final AtomicBoolean locked;

        private MessagePart(AtomicBoolean locked) {
            this.childrenType = "default";
            this.locked = locked;
        }

        public MessagePart addChild() {
            waitForUnlock();
            MessagePart newPart = new MessagePart(locked);
            children.add(newPart);
            return newPart;
        }

        public void setChildrenType(String type) {
            waitForUnlock();
            this.childrenType = type;
        }

        public void setMessage(String message) {
            waitForUnlock();
            if (this.message != null) {
                LOG.log(DEBUG, () -> "Overwriting message '" + this.message + "' with '" + message + "'",
                    new RuntimeException());
            }
            synchronized (this) {
                this.message = new StringBuilder(message);
            }
        }

        public synchronized void appendMessage(String message) {
            waitForUnlock();
            if (this.message == null) {
                this.message = new StringBuilder(message);
            } else {
                this.message.append(message);
            }
        }

        public void addProperty(String key, String value) {
            waitForUnlock();
            props.put(key, value);
        }

        public Properties getProps() {
            return props;
        }

        public synchronized String getMessage() {
            return message == null ? null : message.toString();
        }

        public String getChildrenType() {
            return childrenType;
        }

        public List<MessagePart> getChildren() {
            return children;
        }

        protected String findPropertyImpl(final String key) {
            String value = props.getProperty(key);
            if (value != null) {
                return value;
            }
            for (MessagePart child : children) {
                value = child.findProperty(key);
                if (value != null) {
                    return value;
                }
            }
            return null;
        }

        /**
         * Search in message parts properties then in extra properties and then in sub reports. Returns first occurrence of the
         * key.
         */
        public String findProperty(String key) {
            if (key == null) {
                return null;
            }
            if (key.endsWith("_value")) {
                key = key.substring(0, key.length() - 6); // Because of back compatibility
            }
            return findPropertyImpl(key);
        }

        protected String toString(int indent) {
            StringBuilder result = new StringBuilder();
            if (message != null && message.length() > 0) {
                for (int i = 0; i < indent; i++) {
                    result.append(' ');
                }
                result.append(message);
            }
            for (MessagePart child : children) {
                String msg = child.toString(indent + 4);
                if (msg != null && !msg.isEmpty()) {
                    if (result.length() > 0) {
                        result.append('\n');
                    }
                    result.append(msg);
                }
            }
            return result.toString();
        }

        private void waitForUnlock() {
            while (locked.get()) {
                Thread.onSpinWait();
            }
        }

        @Override
        public String toString() {
            return toString(0);
        }
    }

    public enum ExitCode {
        SUCCESS, WARNING, FAILURE;

        public boolean isWorse(final ExitCode other) {
            return compareTo(other) > 0;
        }
    }
}
