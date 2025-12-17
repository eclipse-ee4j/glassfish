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

package org.glassfish.persistence.ejb.entitybean.container.distributed;

import com.sun.logging.LogDomains;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

class DistributedReadOnlyBeanServiceImpl implements DistributedReadOnlyBeanService {

    private Logger _logger = LogDomains.getLogger(DistributedReadOnlyBeanServiceImpl.class, LogDomains.EJB_LOGGER);

    private ConcurrentHashMap<Long, ReadOnlyBeanRefreshHandlerInfo> refreshHandlers = new ConcurrentHashMap<Long, ReadOnlyBeanRefreshHandlerInfo>();

    private DistributedReadOnlyBeanNotifier robNotifier;

    @Override
    public void setDistributedReadOnlyBeanNotifier(DistributedReadOnlyBeanNotifier notifier) {
        this.robNotifier = notifier;
        _logger.log(INFO, "Registered ReadOnlyBeanNotifier: " + notifier);
    }

    @Override
    public void addReadOnlyBeanRefreshEventHandler(long ejbID, ClassLoader loader, ReadOnlyBeanRefreshEventHandler handler) {
        refreshHandlers.put(ejbID, new ReadOnlyBeanRefreshHandlerInfo(loader, handler));
        _logger.log(INFO, "Registered ReadOnlyBeanRefreshEventHandler: " + ejbID + "; " + handler);
    }

    @Override
    public void removeReadOnlyBeanRefreshEventHandler(long ejbID) {
        refreshHandlers.remove(ejbID);
    }

    @Override
    public void notifyRefresh(long ejbID, Object pk) {
        if (robNotifier != null) {
            byte[] pkData = null;

            ByteArrayOutputStream bos = null;
            ObjectOutputStream oos = null;
            try {
                bos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(bos);

                oos.writeObject(pk);
                oos.flush();
                bos.flush();
                pkData = bos.toByteArray();
                robNotifier.notifyRefresh(ejbID, pkData);
            } catch (Exception ex) {
                _logger.log(WARNING, "Error during notifyRefresh", ex);
            } finally {
                if (oos != null) {
                    try {
                        oos.close();
                    } catch (IOException ioEx) {
                    }
                    ;
                }
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException ioEx) {
                    }
                    ;
                }
            }
        } else {
            if (_logger.isLoggable(FINE)) {
                _logger.log(FINE, "DistributedReadOnlyBeanService ignoring request " + "for notifyRefresh: " + ejbID);
            }
        }
    }

    @Override
    public void notifyRefreshAll(long ejbID) {
        if (robNotifier != null) {
            robNotifier.notifyRefreshAll(ejbID);
        } else {
            if (_logger.isLoggable(FINE)) {
                _logger.log(FINE, "DistributedReadOnlyBeanService ignoring request " + "for notifyRefreshAll: " + ejbID);
            }
        }
    }

    @Override
    public void handleRefreshRequest(long ejbID, byte[] pkData) {
        refreshRequestReceived(false, ejbID, pkData);
    }

    @Override
    public void handleRefreshAllRequest(long ejbID) {
        refreshRequestReceived(true, ejbID, null);
    }

    private void refreshRequestReceived(boolean refreshAll, long ejbID, byte[] pkData) {
        final ReadOnlyBeanRefreshHandlerInfo info = refreshHandlers.get(ejbID);
        if (info == null) {
            // TODO: Log something
            return;
        }

        final Thread currentThread = Thread.currentThread();
        final ClassLoader prevClassLoader = currentThread.getContextClassLoader();

        try {
            currentThread.setContextClassLoader(info.loader);

            if (!refreshAll) {
                ByteArrayInputStream bis = null;
                ObjectInputStream ois = null;
                Serializable pk = null;
                try {
                    bis = new ByteArrayInputStream(pkData);
                    ois = new ObjectInputStream(bis);

                    pk = (Serializable) ois.readObject();
                } catch (IOException ioEx) {
                    _logger.log(WARNING, "Error during refresh", ioEx);
                } catch (ClassNotFoundException cnfEx) {
                    _logger.log(WARNING, "Error during refresh", cnfEx);
                } finally {
                    if (ois != null) {
                        try {
                            ois.close();
                        } catch (IOException ioEx) {
                            _logger.log(WARNING, "Error while closing object stream", ioEx);
                        }
                        ;
                    }
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (IOException ioEx) {
                            _logger.log(WARNING, "Error while closing byte stream", ioEx);
                        }
                        ;
                    }
                }
                if (pk != null) {
                    info.handler.handleRefreshRequest(pk);
                }
            } else {
                info.handler.handleRefreshAllRequest();
            }
        } catch (Exception ex) {
            _logger.log(WARNING, "Error during refresh", ex);
        } finally {
            currentThread.setContextClassLoader(prevClassLoader);
        }
    }

    private static class ReadOnlyBeanRefreshHandlerInfo {
        public ClassLoader loader;
        public ReadOnlyBeanRefreshEventHandler handler;

        public ReadOnlyBeanRefreshHandlerInfo(ClassLoader loader, ReadOnlyBeanRefreshEventHandler handler) {
            this.loader = loader;
            this.handler = handler;
        }
    }
}
