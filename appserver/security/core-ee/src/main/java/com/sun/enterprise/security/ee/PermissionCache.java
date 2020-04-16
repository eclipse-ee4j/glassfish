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

package com.sun.enterprise.security.ee;

import com.sun.enterprise.security.common.AppservAccessController;
import java.security.CodeSource;
import java.security.AllPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import jakarta.security.jacc.PolicyContext;
import com.sun.enterprise.security.ee.CachedPermissionImpl.Epoch;

import com.sun.logging.LogDomains;

/**
 * This class is 
 * @author Ron Monzillo
 */

public class PermissionCache extends Object {

    private static Logger _logger = 
    LogDomains.getLogger(PermissionCache.class,LogDomains.SECURITY_LOGGER);
    private static Policy policy = Policy.getPolicy();
    private static AllPermission allPermission = new AllPermission();

    private Permissions cache;
    private CodeSource codesource;
    private Permission[] protoPerms;
    private Class[] classes;
    private String name;
    private String pcID;
    private final Integer factoryKey;
    private volatile int epoch;
    private volatile boolean loading;
    private ReadWriteLock rwLock;
    private Lock rLock;
    private Lock wLock;
 
    /*
     * USE OF THIS CONSTRUCTOR WITH IS DISCOURAGED PLEASE USE THE Permission 
     * (object) based CONSTRUCTOR.
     * @param key -  Integer that uniquely identifies the cache at the factory
     * @param pcID - a string identifying the policy context and which must 
     *     be set when getPermissions is called (internally). this value may be 
     *     null, in which case the permisions of the default policy context
     *     will be cached.
     * @param codesource - the codesource argument to be used in the call to 
     *     getPermissions. this value may be null.
     * @param class - a single Class object that identifies the permission 
     *     type that will be managed by the cache. This value may be 
     *     null. When this argument is not null, only permissions of the 
     *     identified type  or that resolve to the identified type, 
     *     will be managed within the cache. When null is passed to this
     *     argument, permission type will not be a factor in determining
     *     the cached permissions.
     * @param name - a string corresponding to a value returned by 
     *     Permission.getName(). Only permissions whose getName() value 
     *     matches the name parameter will be included in the cache. This value 
     *     may be null, in which case permission name does not factor into
     *     the permission caching.
     */
    public PermissionCache(Integer key, String pcID, CodeSource codesource,
			   Class clazz, String name){
  	if (codesource == null) {
 	    this.codesource = 
 		new CodeSource(null,
 			       (java.security.cert.Certificate[])null);
  	} else {
  	    this.codesource = codesource;
  	}
 	this.factoryKey = key;
  	this.cache  = null;
  	this.pcID = pcID;
 	this.protoPerms = null;
 	if (clazz != null) {
 	    this.classes = new Class[] {clazz};
 	} else {
 	    this.classes = null;
 	}
  	this.name = name;
 	this.epoch = 1;
 	this.loading = false;
 	this.rwLock = new ReentrantReadWriteLock(true);
 	this.rLock = rwLock.readLock();
 	this.wLock = rwLock.writeLock();
    }
  
    /*
     * @param key -  Integer that uniquely identifies the cache at the factory
     * @param pcID - a string identifying the policy context and which must 
     *     be set when getPermissions is called (internally). this value may be 
     *     null, in which case the permisions of the default policy context
     *     will be cached.
     * @param codesource - the codesource argument to be used in the call to 
     *     getPermissions. this value may be null.
     * @param perms - an array of permission objects identifying the 
     *     permission types that will be managed by the cache. This value may be
     *     null. When this argument is not null, only permissions of the types 
     *     passed in the array or that resolve to the types identified in the 
     *     will be managed within the cache. When null is passed to this
     *     argument, permission type will not be a factor in determining the
     *     cached permissions.
     * @param name - a string corresponding to a value returned by 
     *     Permission.getName(). Only permissions whose getName() value 
     *     matches the name parameter will be included in the cache. This value 
     *     may be null, in which case permission name does not factor into
     *     the permission caching.
     */
    public PermissionCache(Integer key, String pcID, CodeSource codesource,
			   Permission[] perms, String name){
 	if (codesource == null) {
 	    this.codesource = 
 		new CodeSource(null,
 			       (java.security.cert.Certificate[])null);
 	} else {
 	    this.codesource = codesource;
 	}
 	this.factoryKey = key;
 	this.cache  = null;
 	this.pcID = pcID;
 	this.protoPerms = perms;
 	if (perms != null && perms.length>0) {
 	    this.classes = new Class[perms.length];
 	    for (int i=0; i<perms.length; i++) {
 		this.classes[i] = perms[i].getClass();
 	    }
 	} else {
 	    this.classes = null;
 	}
 	this.name = name;
 	this.epoch = 1;
 	this.loading = false;
 	this.rwLock = new ReentrantReadWriteLock(true);
 	this.rLock = rwLock.readLock();
 	this.wLock = rwLock.writeLock();
    }
 
    public Integer getFactoryKey() {
	return this.factoryKey;
    }

    private boolean checkLoadedCache(Permission p, Epoch e) {
	if (e == null) {
	    return cache.implies(p);
	} else {
	    if (e.epoch != epoch) {
		e.granted = cache.implies(p);
		e.epoch = epoch;
	    }
	    return e.granted;
	}
    }

    private boolean checkCache(Permission p, Epoch e) {

 	// test-and-set to guard critical section
 	rLock.lock();
	try {
	    if (loading) {
		return false;
	    } else if (cache != null) {
		// cache is loaded and readlock is held
		// check permission and return
		return checkLoadedCache(p,e);
	    }
	} finally {
	    rLock.unlock();
	}

	wLock.lock();
	if (loading) {
	    // another thread started the load
	    // release the writelock and return
	    wLock.unlock();
	    return false;
	} else if (cache != null) {
	    // another thread loaded the cache 
	    // get readlock inside writelock. 
	    // check permission and return
	    rLock.lock();
	    wLock.unlock();
	    try {
		// cache is loaded and readlock is held
		// check permission and return
		return checkLoadedCache(p,e);
	    } finally {
		rLock.unlock();
	    }
	} else {
	    // set the load indicators so that readers will 
	    // bypass the cache until it is loaded
	    // release the writelock and return
	    cache = null;
	    loading = true;
	    wLock.unlock();
	}
	
	// cache will be null if we proceed past this point
	// NO LOCKS ARE HELD AT THIS POINT

	Permissions nextCache = new Permissions();

	boolean setPc = false;
	String oldpcID = null;
	try {
	    oldpcID = PolicyContext.getContextID();
	    if (this.pcID == null || !this.pcID.equals(oldpcID)) {
		setPc = true;
	    }
	} catch (Exception ex) {
	    _logger.log(Level.SEVERE,"JACC: Unexpected security exception on access decision"
			, ex);
	    return false;
	}

	PermissionCollection pc = null;
	try {
	    if (setPc) {
		setPolicyContextID(this.pcID);
	    }
		
	    pc = policy.getPermissions(this.codesource);
	} catch(Exception ex) {
	    _logger.log(Level.SEVERE,"JACC: Unexpected security exception on access decision"
			, ex);
	    return false;
	} finally {
	    if (setPc) {
		try {
		    setPolicyContextID(oldpcID);
		} catch(Exception ex) {
		    _logger.log(Level.SEVERE,"JACC: Unexpected security exception on access decision", ex);
		    return false;
		}
	    }
	}

 	// force resolution of unresolved permissions
 	// so that we can filter out all but the permissions
 	// that are supposed to be in the cache.
 
 	resolvePermissions(pc,p);

	Enumeration granted = pc.elements();
	while (granted.hasMoreElements()) {
	    Permission i = (Permission) granted.nextElement();
	    if (i.equals(allPermission)) {
		nextCache.add(i);
	    } else {
		boolean classMatch = true;
		if (this.classes != null) {
		    classMatch = false;
		    Class iClazz = i.getClass();
		    for (int j=0; j<this.classes.length; j++) {
			if (this.classes[j].equals(iClazz)) {
			    classMatch = true;
			    break;
			}
		    }
		}
		if (classMatch) {
		    if (this.name != null) {
			String iName = i.getName();
			if (iName != null && this.name.equals(iName)) {
			    nextCache.add(i);
			}
		    } else {
			nextCache.add(i);
		    }
		}
	    }
	}

	// get the writelock to mark cache as loaded
 	wLock.lock();
	cache = nextCache;
	loading = false;
	try {
	    // get readlock inside writelock. 
	    rLock.lock();
	    wLock.unlock();
	    // cache is loaded and readlock is held
	    // check permission and return
	    return checkLoadedCache(p,e);
	} finally {
	    rLock.unlock();
	}
    }
    
    boolean checkPermission(Permission p, Epoch e) {
 	return checkCache(p,e);
    }
  
    public boolean checkPermission(Permission p) {
 	return checkCache(p,null);
    }
   
    public synchronized void reset() {
 	wLock.lock();
 	try {
  	    if (cache != null) {
  		// since cache is non-null, we know we are NOT loading
  		// setting cache to null will force a (re)load
		cache = null;
		epoch = (epoch + 1 == 0) ? 1 : epoch + 1;
	    }
	} finally {
 	    wLock.unlock();
  	}
    }

    private void setPolicyContextID(final String newID)
            throws PrivilegedActionException {
        AppservAccessController.doPrivileged(new PrivilegedExceptionAction(){
            public java.lang.Object run() throws Exception{
                PolicyContext.setContextID(newID);
                return null;
            }
        });
    }

    // use implies to resolve unresolved permissions
    private void resolvePermissions(PermissionCollection pc, Permission p) {
 	// each call to implies will resolve permissions of the
 	// argument permission type
 	if (this.protoPerms != null && this.protoPerms.length > 0) {
 	    for (int i=0; i<this.protoPerms.length; i++) {	    
 		pc.implies(this.protoPerms[i]);
  	    }
 	} else {
 	    pc.implies(p);
 	}
    }
}
