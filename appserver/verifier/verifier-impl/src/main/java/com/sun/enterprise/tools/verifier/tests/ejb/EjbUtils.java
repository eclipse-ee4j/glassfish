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

package com.sun.enterprise.tools.verifier.tests.ejb;

import org.glassfish.ejb.deployment.descriptor.FieldDescriptor;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.Vector;

/**
 * Exceptions checked for CreateException, FinderException, RemoteException
 * compliance test.
 * 
 */
public class EjbUtils { 



    /** 
     * The names of the fields in the primary key class must be a subset of the
     * names of the container-managed fields.
     * Verify the following:
     *
     *   The primary key class field must be a subset of the names of the 
     *   container-managed fields.  
     *
     * @param field the field to be checked for containment within the names of the 
     *        container-managed fields
     * @param CMPFields the Set of contianer managed fields 
     *
     * @return <code>boolean</code> true if field is subset of CMP fields, false otherwise
     */
    public static boolean isFieldSubsetOfCMP(Field field, Set CMPFields) {
	if (CMPFields.contains(new FieldDescriptor(field))) {
	    return true;
	} else {
	    return false;
	}
    }


    /**
     * The names of the fields in the primary key class must correspond to the
     * field names of the entity bean class that comprise the key.
     * Verify the following:
     *
     *   The primary key class field must correspond to the field names of the 
     *   entity bean class that comprise the key.
     *
     * @param field the field to be checked for matching bean field 
     * @param beanFields the Set of contianer managed fields 
     *
     * @return <code>boolean</code> true if field is subset of bean class fields, false otherwise
     */
    public static boolean isPKFieldMatchingBeanFields(Field field, Vector beanFields) {

      for (int i = 0; i < beanFields.size(); i++) {
          if (((FieldDescriptor)beanFields.elementAt(i)).getName().equals(field.getName())) {
	      return true;
	  } else {
              continue;
	  }
      }
      // if you made it here, then field[] didn't contain field
      return false;
    }


    /**
     * Method exception jakarta.ejb.CreateException checked for compliance 
     * test.
     *
     * Verify the following:
     *
     *   The home/remote interface methods exception types must be legal types for
     *   CreateException.  
     *   This means that their exception must throw jakarta.ejb.CreateException.
     *
     * @param methodExceptions the exceptions to be checked for throws
     *        jakarta.ejb.CreateException 
     *
     * @return <code>boolean</code> true if exceptions throw jakarta.ejb.CreateException, false otherwise
     */
    public static boolean isValidCreateException(Class [] methodExceptions) {
	// methods must throw jakarta.ejb.CreateException
	boolean throwsCreateException = false;
	for (int kk = 0; kk < methodExceptions.length; ++kk) {
	    if ((methodExceptions[kk].getName().equals("jakarta.ejb.CreateException")) ||
		(methodExceptions[kk].getName().equals("CreateException"))) {
		throwsCreateException = true;
		break;
	    }
	}
	return throwsCreateException;

    }



    /**
     * Method exception java.rmi.RemoteException checked for compliance 
     * test.
     *
     * Verify the following:
     *
     *   The home/remote interface methods exception types must be legal types for
     *   RemoteException.  
     *   This means that their exception must throw java.rmi.RemoteException
     *
     * @param methodExceptions the exceptions to be checked for throws
     *        java.rmi.RemoteException 
     *
     * @return <code>boolean</code> true if exceptions throw java.rmi.RemoteException, false otherwise
     */
    public static boolean isValidRemoteException(Class [] methodExceptions) {
	// methods must throw java.rmi.RemoteException
	boolean throwsRemoteException = false;
	for (int kk = 0; kk < methodExceptions.length; ++kk) {
	    if ((methodExceptions[kk].getName().equals("java.rmi.RemoteException")) ||
		(methodExceptions[kk].getName().equals("RemoteException"))) {
		throwsRemoteException = true;
		break;
	    }
	}
	return throwsRemoteException;

    }



    /**
     * Method exception jakarta.ejb.ObjectNotFoundException checked for compliance 
     * test.
     *
     * Verify the following:
     *
     *     The ObjectNotFoundException is a subclass of FinderException. It is
     *     thrown by the ejbFind<METHOD>(...) methods to indicate that the
     *     requested entity object does not exist.
     *
     *     Only single-object finders (see Subsection 9.1.8) may throw this
     *     exception.   Multi-object finders must not throw this exception.
     *
     * @param methodExceptions the exceptions to be checked for throws
     *        jakarta.ejb.ObjectNotFoundException 
     *
     * @return <code>boolean</code> true if exceptions throw jakarta.ejb.ObjectNotFoundException, false otherwise
     */
    public static boolean isValidObjectNotFoundExceptionException(Class [] methodExceptions) {
	// methods must throw jakarta.ejb.ObjectNotFoundException
	boolean throwsObjectNotFoundException = false;
	for (int kk = 0; kk < methodExceptions.length; ++kk) {
	    if ((methodExceptions[kk].getName().equals("jakarta.ejb.ObjectNotFoundException")) ||
		(methodExceptions[kk].getName().equals("ObjectNotFoundException"))) {
		throwsObjectNotFoundException = true;
		break;
	    }
	}
	return throwsObjectNotFoundException;
    }




    /**
     * Method exception jakarta.ejb.FinderException checked for compliance 
     * test.
     *
     * Verify the following:
     *
     *   The home/remote interface methods exception types must be legal types for
     *   FinderException  
     *   This means that their exception must throw jakarta.ejb.FinderException
     *
     * @param methodExceptions the exceptions to be checked for throws
     *        jakarta.ejb.FinderException 
     *
     * @return <code>boolean</code> true if exceptions throw jakarta.ejb.FinderException, false otherwise
     */
    public static boolean isValidFinderException(Class [] methodExceptions) {
	// methods must throw jakarta.ejb.FinderException
	boolean throwsFinderException = false;
	for (int kk = 0; kk < methodExceptions.length; ++kk) {
	    if ((methodExceptions[kk].getName().equals("jakarta.ejb.FinderException")) ||
		(methodExceptions[kk].getName().equals("FinderException"))) {
		throwsFinderException = true;
		break;
	    }
	}
	return throwsFinderException;

    }


    /** Class checked for implementing java.io.Serializable interface test.
     * Verify the following:
     *
     *   The class must implement the java.io.Serializable interface, either
     *   directly or indirectly.
     *
     * @param serClass the class to be checked for Rmi-IIOP value type
     *        compliance
     *
     * @return <code>boolean</code> true if class implements java.io.Serializable, false otherwise
     */
    public static boolean isValidSerializableType(Class serClass) {

        if (java.io.Serializable.class.isAssignableFrom(serClass))
           return true;
        else
           return false;

        /**
        // This complex logic is replaced by the above simple logic
	Class c = serClass;
	boolean validInterface = false;
	boolean badOne = false;
	// The class must implement the java.io.Serializable interface, either
	// directly or indirectly, 
	// walk up the class tree

	if (c.getName().equals("java.io.Serializable")) {
	    validInterface = true;
	    return validInterface;
	}
	do {	    
	    Class[] interfaces = c.getInterfaces();
	    for (int i = 0; i < interfaces.length; i++) {
		if (interfaces[i].getName().equals("java.io.Serializable")) {
		    validInterface = true;
		    break;
		} else {
		    // walk up the class tree of the interface and see if it
		    // implements java.io.Serializable
		    Class superClass = interfaces[i];
		    do {
			if (superClass.getName().equals("java.io.Serializable")) {
			    validInterface = true;
			    break;
			}
		    } while ((((superClass=superClass.getSuperclass()) != null) && (!validInterface)));
		}
	    }
	} while ((((c=c.getSuperclass()) != null) && (!validInterface)));


	if (validInterface) {
	    return true;
	} else {
	    return false;
	}
       **/
    }



    /** 
     * Method application exception checked for compliance test.
     *
     * Verify the following:
     *
     *   An application exception is an exception defined in the throws clause of 
     *   a method in the Bean's home interface, other than java.rmi.RemoteException.
     *   An application exception must not be defined as a subclass of the
     *   java.lang.RuntimeException, or of the java.rmi.RemoteException. These are
     *   reserved for system exceptions.
     *   The jakarta.ejb.CreateException, jakarta.ejb.RemoveException,
     *   jakarta.ejb.FinderException, and subclasses thereof, are considered to be
     *   application exceptions.
     *
     * @param methodExceptions the exceptions to be checked for throws
     *        application exception
     *
     * @return <code>boolean</code> true if exceptions are valid application exception, false otherwise
     */
    public static boolean isValidApplicationException(Class [] methodExceptions) {
	for (int kk = 0; kk < methodExceptions.length; ++kk) {
	 Class ex=methodExceptions[kk];
	 //check 0: app exception set is all exceptions minus java.rmi.RemoteException.
         if (java.rmi.RemoteException.class != ex) {
	 	//check 1: app exception must subclass java.lang.Exception
	 	if (!java.lang.Exception.class.isAssignableFrom(ex)) return false;
	 	//check 2: app exception must not subclass java.lang.RuntimeException or java.rmi.RemoteException
            	if(java.rmi.RemoteException.class.isAssignableFrom(ex) || java.lang.RuntimeException.class.isAssignableFrom(ex)) {
                	return false;
            	}
         }
	}
	return true;
    }
}
