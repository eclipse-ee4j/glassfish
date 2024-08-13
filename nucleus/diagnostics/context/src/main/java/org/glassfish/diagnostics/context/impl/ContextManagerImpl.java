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
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.glassfish.contextpropagation.ContextMap;
import org.glassfish.contextpropagation.ContextViewFactory;
import org.glassfish.contextpropagation.InsufficientCredentialException;
import org.glassfish.contextpropagation.Location;
import org.glassfish.contextpropagation.PropagationMode;
import org.glassfish.contextpropagation.View;
import org.glassfish.contextpropagation.spi.ContextMapHelper;
import org.glassfish.diagnostics.context.Context;
import org.glassfish.diagnostics.context.ContextManager;
import org.glassfish.hk2.api.Rank;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.jvnet.hk2.annotations.Service;


/**
 * Default implementation of ContextManager.
 */
@Service
@Rank(500)
public class ContextManagerImpl
  implements ContextManager{

  private static final String CLASS_NAME = ContextManagerImpl.class.getName();

 /**
  * Implementation of Location for those (hopfefully) rare occasions
  * in which we need to hand back to our fans a Context that could
  * not be found or constructed in the usual way.
  */
  private static class HackedUnavailableLocation extends Location{
    HackedUnavailableLocation()
    {
      super((View)null);
    }

    public String getLocationId(){
      return "<unavailable>";
    }

    public String getOrigin(){
      return "<unavailable>";
    }
  }

 /**
  * Context implementation for those (hopfefully) rare occasions
  * in which we need to hand back to our fans a Context that could
  * not be found or constructed in the usual way.
  */
  private static final Context dummyContextInstance = new Context()
  {
    private final Location sLocation = new HackedUnavailableLocation();

    @Override
    public Location getLocation() {
      return sLocation;
    }

    @Override
    public <T> T put(String name, String value, boolean propagates) {
      return null;
    }

    @Override
    public <T> T put(String name, Number value, boolean propagates) {
      return null;
    }

    @Override
    public <T> T remove(String name) {
      return null;
    }

    @Override
    public <T> T get(String name) {
      return null;
    }
  };

  @LogMessageInfo(
    message = "Can not fulfill request to get diagnostics context.",
    comment = "A diagnostics context can not be found (or created).",
    cause   = "",
    action  = "",
    level   ="WARNING")
  private static final String CAN_NOT_GET_CONTEXT_AS_DIAG_3000 = "NCLS-DIAG-03000";

  @LogMessageInfo(
    message = "An exception has prevented a diagnostics context from being created.",
    comment = "A diagnostics context can not be found or created.",
    cause   = "(see underlying exception)",
    action  = "(see underlying exception)",
    level   = "WARNING")
  private static final String EXCEPTION_CREATING_CONTEXT_AS_DIAG_3001 = "NCLS-DIAG-03001";

  /**
   *  Propagation modes for data that is set as propagates in the put
   *  methods.
   *
   *  This must always match the set of modes returned from
   *  PropagationMode.defaultSetOneway to guarantee that the data
   *  of this ViewCapable are propagated over the same set of modes as
   *  the Location. We do not reference defaultSetOneway because
   *  we need to avoid propagation modes other than those we specify below.
   */
  static final EnumSet<PropagationMode> sfGlobalPropagationModes =
      EnumSet.of(PropagationMode.THREAD,
                 PropagationMode.RMI,
                 PropagationMode.JMS_QUEUE,
                 PropagationMode.SOAP,
                 PropagationMode.MIME_HEADER,
                 PropagationMode.ONEWAY);
  static final EnumSet<PropagationMode> sfLocalPropagationModes =
      EnumSet.of(PropagationMode.LOCAL);

  // Check that PropagationMode.defaultSetOneway() and sfGlobalPropagationModes
  // match (if they do not then we lose data integrity).
  static
  {
    Set<PropagationMode> tmp = new HashSet(PropagationMode.defaultSetOneway());
    tmp.removeAll(sfGlobalPropagationModes);
    if (!tmp.isEmpty())
    {
      throw new IllegalStateException(
        "Mismatched propagation modes - PropagationMode.defaultSetOneWay()" +
        " has more modes than sfGlobalPropagationModes.");
    }

    tmp = new HashSet(sfGlobalPropagationModes);
    tmp.removeAll(PropagationMode.defaultSetOneway());
    if (!tmp.isEmpty())
    {
      throw new IllegalStateException(
        "Mismatched propagation modes - sfGlobalPropagationModes has more" +
        " modes than PropagationMode.defaultSetOneWay().");
    }
  }

 // Register the DiagnosticContextViewFactory with the contextpropagation
 // framework.
  static{
    LOGGER.entering(CLASS_NAME,
      "<clinit::registration of DiagnosticContextViewFactory with ContextMapHelper>");
    ContextMapHelper.registerContextFactoryForPrefixNamed(
      WORK_CONTEXT_KEY,
      new DiagnosticContextViewFactory());
   LOGGER.exiting(CLASS_NAME,
      "<clinit::registration of DiagnosticContextViewFactory with ContextMapHelper>");
 }

  /**
   * ContextViewFactory class implementation that fulfills a contract
   * with contextpropagation package.
   */
  private static class DiagnosticContextViewFactory
      implements ContextViewFactory {

    @Override // ContextViewFactory
    public ContextImpl createInstance(final View view)
    {
      LOGGER.entering(CLASS_NAME, "createInstance(View)");
      final Location location =
          ContextMapHelper.getScopeAwareContextMap().getLocation();
      ContextImpl retVal = new ContextImpl(view, location);
      LOGGER.exiting(CLASS_NAME, "createInstance(View)", retVal.toString());
      return retVal;
    }

    @Override // ContextViewFactory
    public EnumSet<PropagationMode> getPropagationModes(){
      return sfGlobalPropagationModes;
    }
  }

  @Override // from ContextManager
  public Context getContext()
  {
    LOGGER.entering(CLASS_NAME, "getContext()");

    Context retVal = null;

    ContextMap contextMap = ContextMapHelper.getScopeAwareContextMap();
    try{
      retVal = contextMap.get(WORK_CONTEXT_KEY);
      if (retVal == null){
        LOGGER.logp(Level.FINEST, CLASS_NAME, "getContext()",
          "No ContextImpl found in ContextMap, creating a new one.");
        ContextImpl contextImpl = contextMap.createViewCapable(WORK_CONTEXT_KEY);
        retVal = contextImpl;
      }
    }
    catch (InsufficientCredentialException isce)
    {
      // Nothing we can do to remedy isce so log the exception and move on

      // Do we return null or a dummy Context implementation?
      // Most callers won't be looking too closely at the context and
      // they probably won't want to concern themselves with problems
      // bubbling up from the contextpropagation layer, so a dummy
      // Context is preferred - the buck stops here!
      LOGGER.logp(Level.WARNING, CLASS_NAME, "getContext()",
                     EXCEPTION_CREATING_CONTEXT_AS_DIAG_3001, isce);
      retVal = dummyContextInstance;
    }

    if (retVal == null)
    {
      LOGGER.logp(Level.WARNING, CLASS_NAME, "getContext()",
                     CAN_NOT_GET_CONTEXT_AS_DIAG_3000);
      retVal = dummyContextInstance;
    }

    LOGGER.exiting(CLASS_NAME, "getContext()", retVal);

    return retVal;
  }
}
