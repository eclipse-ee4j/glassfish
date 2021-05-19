/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.contextpropagation.internal;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.glassfish.contextpropagation.PropagationMode;
import org.glassfish.contextpropagation.View;
import org.glassfish.contextpropagation.bootstrap.ContextBootstrap;
import org.glassfish.contextpropagation.internal.Entry.ContextType;

/*
 *  - Consider a View that uses shorter keys (key minus the prefix)
 *     - It would not use the workarea map storage but would have its own with its own serialization methods
 *     - Could produce more compact on the wire representation even for the WLS adapter
 *     - The long keys have the advantage that the serialization is already handled by workarea,
 *       and that the constituents would be accessible from WLS -- that is both good and bad.
 *  - View does not need to extend SimpleMap, it was done that way for a quick prototype
 *     - The method signatures can be even more convenient
 *       put(String,  <TYPE see ContextMapAccessor>) instead of put(String, Entry)
 */
/**
 * Provides access to a subset of the ContextMap.
 * Views bypass security checks. However Views are hidden in ViewCapable
 * instances for which access is verified.
 */
public class ViewImpl implements View {
  private String prefix;
  private SimpleMap sMap;
  private Set<String> names = new HashSet<String>();

  protected ViewImpl(String prefix) {
    this.prefix = prefix + ".";
    sMap = ((AccessControlledMap) Utils.mapFinder.getMapAndCreateIfNeeded()).simpleMap;
  }

  @Override
  public <T> T get(String name) {
    return (T) sMap.get(makeKey(name));
  }

  private String makeKey(String name) {
    return name == null ? null : prefix + name;
  }

  private String newKey(String name) {
    names.add(name);
    return makeKey(name);
  }

  private boolean allowAllToRead(String name) {
    return ContextBootstrap.getContextAccessController().isEveryoneAllowedToRead(newKey(name));
  }

  @Override
  public <T> T put(String name, String context,
      EnumSet<PropagationMode> propagationModes) {
    return (T) sMap.put(newKey(name), new Entry(context, propagationModes, ContextType.STRING).init(true, allowAllToRead(name)));
  }

  @Override
  public <T, U extends Number> T put(String name, U context,
      EnumSet<PropagationMode> propagationModes) {
    return (T) sMap.put(newKey(name), new Entry(context, propagationModes,
        ContextType.fromNumberClass(context.getClass())).init(true, allowAllToRead(name)));
  }

  @Override
  public <T> T put(String name, Boolean context,
      EnumSet<PropagationMode> propagationModes) {
    return (T) sMap.put(newKey(name), new Entry(context, propagationModes, ContextType.BOOLEAN).init(true, allowAllToRead(name)));  }

  @Override
  public <T> T put(String name, Character context,
      EnumSet<PropagationMode> propagationModes) {
    return (T) sMap.put(newKey(name), new Entry(context, propagationModes, ContextType.CHAR).init(true, allowAllToRead(name)));
  }

   public <T> T putSerializable(String name, Serializable context,
      EnumSet<PropagationMode> propagationModes, boolean allowAllToRead) {
    return (T) sMap.put(newKey(name), new Entry(context, propagationModes, ContextType.SERIALIZABLE).init(true, allowAllToRead));
  }

  @Override
  public <T> T remove(String name) {
    names.remove(name);
    return (T) sMap.remove(makeKey(name));
  }

  public void clean() {
    for (String name : names) remove(name);
  }

}
