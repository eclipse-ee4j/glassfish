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

package org.glassfish.deployapi.config;

import java.io.*;
import java.util.Enumeration;
import java.util.jar.Manifest;
import java.util.Collection;
import javax.enterprise.deploy.model.DeployableObject;
import java.net.URI;

import org.glassfish.api.deployment.archive.ReadableArchive;
import com.sun.enterprise.deploy.shared.AbstractReadableArchive;
import com.sun.enterprise.util.LocalStringManagerImpl;

/**
 * This class act as an ReadableArchive implementation, delegating all possible
 * APIs to the JSR88 DeployObject object.
 *
 * @author  Jerome Dochez
 */
public class ConfigBeanArchive extends AbstractReadableArchive {
    
    private DeployableObject deployObject;
    
    private static LocalStringManagerImpl localStrings =
	  new LocalStringManagerImpl(ConfigBeanArchive.class);
    
    /** Creates a new instance of ConfigBeanArchive */
    public ConfigBeanArchive(DeployableObject deployObject) {
        this.deployObject = deployObject;
    }
    
    /**
     * close the abstract archive
     */
    public void close() throws IOException {
        // nothing to do here
    }
    
    /**
     * close a previously returned @see java.io.OutputStream returned
     * by an addEntry call
     *
     * @param the output stream to close
     */
    public void closeEntry(ReadableArchive os) throws IOException {
        throw new IOException(localStrings.getLocalString(
		    	    "enterprise.deployapi.config.configbeanarchive.notimplemented", 
		    	    "Operation not implemented"));
        
    }
    
    /**
     * close a previously returned @see java.io.OutputStream returned
     * by an addEntry call
     * @param the output stream to close
     */
    public void closeEntry(OutputStream os) throws IOException {
        throw new IOException(localStrings.getLocalString(
		    	    "enterprise.deployapi.config.configbeanarchive.notimplemented", 
		    	    "Operation not implemented"));
    }
    
    /**
     * delete the archive
     */
    public boolean delete() {
        return false;
    }
    
    /**
     * @return an @see java.util.Enumeration of entries in this abstract
     * archive
     */
    public Enumeration entries() {
        return deployObject.entries();
    }

    public Enumeration<String> entries(String prefix) {
        return null;
    }

    public Collection<String> getDirectories() throws IOException {
        return null;
    }

    public boolean isDirectory(String name) {
        return false;
    }

    public String getName() {
        return "";
    }

    
    /**
     * @return true if this archive exists
     */
    public boolean exists() {
        return false;
    }    
    
    public boolean exists(String name) throws IOException {
        if (getEntry(name) == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * @return the archive uri
     */
    public String getArchiveUri() {
        return null;
    }
    
    public long getEntrySize(String name) {
        return 0;
    }

    public void open(URI uri) throws IOException {
    }

    /**
     * @return the archive size
     */
    public long getArchiveSize() throws NullPointerException, SecurityException {
        return -1;
    }

    /**
     * create or obtain a subarchive within this abstraction.
     *
     * @param the name of the subarchive.
     */
    public ReadableArchive getSubArchive(String name) throws IOException {
        throw new IOException(localStrings.getLocalString(
		    	    "enterprise.deployapi.config.configbeanarchive.notimplemented", 
		    	    "Operation not implemented"));
        
    }
    
    /**
     * @return a @see java.io.InputStream for an existing entry in
     * the current abstract archive
     * @param the entry name
     */
    public InputStream getEntry(String name) throws IOException {
        return deployObject.getEntry(name);
    }
    
    /**
     * @return the manifest information for this abstract archive
     */
    public Manifest getManifest() throws IOException {
        return null;
    }
    
    /**
     * rename the archive
     *
     * @param name the archive name
     */
    public boolean renameTo(String name) {
        return false;
    }
    
    /** @return true if this archive abstraction supports overwriting of elements
     *
     */
    public boolean supportsElementsOverwriting() {
        return false;
    }
    
    public void closeEntry() throws IOException {
        throw new IOException(localStrings.getLocalString(
		    	    "enterprise.deployapi.config.configbeanarchive.notimplemented", 
		    	    "Operation not implemented"));
    }
    
    public java.net.URI getURI() {
        return null;        
    }
}
