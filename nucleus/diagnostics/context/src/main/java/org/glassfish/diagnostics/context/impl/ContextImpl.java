/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.diagnostics.context.impl;

import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.contextpropagation.Location;
import org.glassfish.contextpropagation.PropagationMode;
import org.glassfish.contextpropagation.View;
import org.glassfish.contextpropagation.ViewCapable;
import org.glassfish.diagnostics.context.Context;
import org.glassfish.diagnostics.context.ContextManager;


/**
 * Base implementation of {@code org.glassfish.diagnostics.context.Context}.
 *
 * Delegates to a {@code org.glassfish.contextpropagation.View}
 */
public class ContextImpl
  implements Context, ViewCapable // TODO - ContextLifecycle too?
{
  private static final Logger LOGGER = ContextManager.LOGGER;

  private static final String CLASS_NAME = ContextImpl.class.getName();

  /**
  * The View to which this ContextImpl will delegate.
  *
  * Will be populated via public constructor (part of
  * ViewCapable contract)
  */
  private final View mView;

 /**
  * The Location of this ContextImpl.
  */
  private final Location mLocation;


 /**
  * Constructor required by DiagnosticContextViewFactory.
  *
  * This constructor forms part of overall contract with
  * contextpropagation package.
  *
  * @param view  The View to which this object is expected to delegate.
  */
  ContextImpl(View view, Location location){

    if ((view == null) || (location == null)){
      throw new IllegalArgumentException(
        ((view == null) ? "View must not be null. " : "") +
        ((location == null) ? "Location must not be null"  : "")
      );
    }

    if (LOGGER.isLoggable(Level.FINER)){
      LOGGER.logp(Level.FINER, CLASS_NAME, "<init>",
        "(view, location{"+location.getOrigin()+","+location.getLocationId()+"})");
    }

    mView = view;
    mLocation = location;
  }

  @Override // from Context
  public Location getLocation(){
    return mLocation;
  }

  @Override // from Context
  public <T> T put(String name, String value, boolean propagates){
    final EnumSet<PropagationMode> propagationModes;

    if (LOGGER.isLoggable(Level.FINER)){
      LOGGER.logp(Level.FINER, CLASS_NAME, "put(String, String, boolean)",
        "{" + mLocation.getOrigin() + "," + mLocation.getLocationId() + "}" +
        "(" + name + "," + value + "," + Boolean.toString(propagates) + ")");
    }

    if (propagates){
      propagationModes = ContextManagerImpl.sfGlobalPropagationModes;
    }
    else{
      propagationModes = ContextManagerImpl.sfLocalPropagationModes;
    }

    T retVal = mView.put(name, value, propagationModes);

    if (LOGGER.isLoggable(Level.FINER)){
      LOGGER.logp(Level.FINER, CLASS_NAME, "put(String, String, boolean)",
      "{" + mLocation.getOrigin() + "," + mLocation.getLocationId() + "}" +
      " returning " + retVal);
    }

    return retVal;
  }

  @Override // from Context
  public <T> T put(String name, Number value, boolean propagates){
    final EnumSet<PropagationMode> propagationModes;

    if (LOGGER.isLoggable(Level.FINER)){
      LOGGER.logp(Level.FINER, CLASS_NAME, "put(String, Number, boolean)",
      "{" + mLocation.getOrigin() + "," + mLocation.getLocationId() + "}" +
      "(" + name + "," + value + "," + Boolean.toString(propagates) + ")");
    }


    if (propagates){
      propagationModes = ContextManagerImpl.sfGlobalPropagationModes;
    }
    else{
      propagationModes = ContextManagerImpl.sfLocalPropagationModes;
    }

    T retVal = mView.put(name, value, propagationModes);

    if (LOGGER.isLoggable(Level.FINER)){
      LOGGER.logp(Level.FINER, CLASS_NAME, "put(String, Number, boolean)",
      "{" + mLocation.getOrigin() + "," + mLocation.getLocationId() + "}" +
      " returning " + retVal);
    }


    return retVal;
  }

  @Override // from Context
  public <T> T get(String name){
    T retVal = mView.get(name);

    if (LOGGER.isLoggable(Level.FINER)){
      LOGGER.logp(Level.FINER, CLASS_NAME, "get(String)",
      "{" + mLocation.getOrigin() + "," + mLocation.getLocationId() + "}" +
      "("+name+") returning " + retVal);
    }

    return retVal;
  }

  @Override // from Context
  public <T> T remove(String name){
    T retVal = mView.remove(name);

    if (LOGGER.isLoggable(Level.FINER)){
      LOGGER.logp(Level.FINER, CLASS_NAME, "remove(String)",
      "{" + mLocation.getOrigin() + "," + mLocation.getLocationId() + "}" +
      "("+name+") returning " + retVal);
    }

    return retVal;
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("{ContextImpl:");
    sb.append("{location:")
      .append(mLocation.getOrigin())
      .append(",")
      .append(mLocation.getLocationId())
      .append("}");
    sb.append("}");
    return sb.toString();
  }

}
