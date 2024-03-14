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

/*
 * HAStoreBase.java
 *
 * Created on November 22, 2005, 12:34 PM
 *
 */

package org.glassfish.web.ha.session.management;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import com.sun.enterprise.container.common.spi.util.JavaEEIOUtils;
import org.apache.catalina.*;
import org.apache.catalina.session.StoreBase;
import org.glassfish.web.ha.LogFacade;

/**
 *
 * @author Larry White
 * @author Rajiv Mordani
 */
public abstract class HAStoreBase extends StoreBase {

    protected JavaEEIOUtils ioUtils;

    static final Logger _logger = LogFacade.getLogger();


    /** Creates a new instance of HAStoreBase */
    public HAStoreBase(JavaEEIOUtils ioUtils) {
        this.ioUtils =  ioUtils;
    }

    /**
     * Controls the verbosity of the web container subsystem's debug messages.
     *
     * This value is non-zero only when the level is one of FINE, FINER
     * or FINEST.
     *
     */
    protected int _debug = 0;

    protected void debug(String message) {
        log(message);
    }

    /**
     * The current level of logging verbosity for this object.
     */
    protected Level _logLevel = Level.FINE;

    /**
     * Set _debug flag and _logLevel based on the log level.
     */
    protected void setLogLevel() {
        Level level = _logger.getLevel();
        _logLevel = level;
        if(_logLevel == null) {
            _logLevel = Level.FINE;
        }

        // Determine the appropriate value our debug level
        if (_logLevel.equals(Level.FINE))
            _debug = 1;
        else if (_logLevel.equals(Level.FINER))
            _debug = 2;
        else if (_logLevel.equals(Level.FINEST))
            _debug = 5;
        else
            _debug = 0;
    }

    /**
    * The application id
    */
    protected String applicationId = null;

    public String getApplicationId() {
        if(applicationId != null)
            return applicationId;
        Container container = manager.getContainer();
        StringBuffer sb = new StringBuffer(50);
        sb.append(this.getClusterId());
        List<String> list = new ArrayList<String>();
        while (container != null) {
            if(container.getName() != null) {
                list.add(":" + container.getName());
            }
            container = container.getParent();
        }
        for(int i=(list.size() -1); i>-1; i--) {
            String nextString = (String) list.get(i);
            sb.append(nextString);
        }
        applicationId = sb.toString();
        return applicationId;
    }

    /**
    * Return the cluster id for this Store as defined in server.xml.
    */
    protected String getClusterIdFromConfig() {

        return null;
    }

    /**
    * The cluster id
    */
    protected String clusterId = null;

    /**
    * Return the cluster id for this Store
    */
    protected String getClusterId() {
        if(clusterId == null)
            clusterId = getClusterIdFromConfig();
        return clusterId;
    }

    //possible generic methods begin

    /**
    * Create serialized byte[] for <code>obj</code>.
    *
    * @param session - serialize obj
    * @return byte[] containing serialized data stream for obj
    */
    public byte[] getByteArray(Session session)
      throws IOException {
        return getByteArray(session, false);
    }

    /**
    * Create an byte[] for the session that we can then pass to
    * the HA Store.
    *
    * @param session
    *   The session we are serializing
    *
    */
    protected byte[] getByteArray(Session session, boolean compress)
      throws IOException {
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;



        byte[] obs;
        try {
            bos = new ByteArrayOutputStream();
            // ObjectInputOutputStreamFactory oiosf = ObjectInputOutputStreamFactoryFactory.getFactory();


            try {
                if (compress) {
                    oos = ioUtils.createObjectOutputStream(
                        new GZIPOutputStream(new BufferedOutputStream(bos)), true);
                } else {
                    oos = ioUtils.createObjectOutputStream(new BufferedOutputStream(bos), true);
                }
            } catch (Exception ex) {}

            //use normal ObjectOutputStream if there is a failure during stream creation
            if(oos == null) {
                if (compress) {
                    oos = new ObjectOutputStream(new GZIPOutputStream(new BufferedOutputStream(bos)));
                } else {
                    oos = new ObjectOutputStream(new BufferedOutputStream(bos));
                }
            }

            writeSession(session, oos);
            oos.close();
            oos = null;

            obs = bos.toByteArray();
            //for monitoring
        }
        finally {
            if ( oos != null )  {
                oos.close();
            }
        }

        return obs;
    }

    //SJSAS 6406580 START
    /**
    * Remove the Session with the specified session identifier from
    * this Store, if present.  If no such Session is present, this method
    * takes no action.
    *
    * @param id Session identifier of the Session to be removed
    *
    * @exception IOException if an input/output error occurs
    */
    public void remove(String id) throws IOException  {

        if (_debug > 0) {
            debug("in remove");
        }

        if ( id == null )  {
            if (_debug > 0) {
                debug("In remove, got a null id");
            }
            return;
        }
        Manager mgr = this.getManager();
        if(mgr instanceof ReplicationManagerBase) {
            ReplicationManagerBase pMgr = (ReplicationManagerBase)mgr;
            pMgr.doRemove(id);
        } else {
            this.removeSynchronized(id);
        }
    }

    /**
    * Remove the Session with the specified session identifier from
    * this Store, if present.  If no such Session is present, this method
    * takes no action. (This is the non-synchronized version of remove
    * called by a store element from the pool, not the singleton store
    * which must use removeSynchronized
    *
    * @param id Session identifier of the Session to be removed
    *
    * @exception IOException if an input/output error occurs
    */
    public void doRemove(String id) throws IOException  {
        //over-ridden in sub-classes
    }

    /**
    * Remove the Session with the specified session identifier from
    * this Store, if present.  If no such Session is present, this method
    * takes no action.
    *
    * @param id Session identifier of the Session to be removed
    *
    * @exception IOException if an input/output error occurs
    */
    public synchronized void removeSynchronized(String id) throws IOException {
        //over-ridden in sub-classes
    }

    /**
    * return the size of the store cache
    * will be over-ridden in subclasses
    */
    public int getSize() throws IOException {
        //FIXME
        return 0;
    }

    /**
     * get the utility class used to call into services from IOUtils
     */

    //possible generic methods end




}
