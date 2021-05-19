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

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;

import org.glassfish.contextpropagation.InsufficientCredentialException;
import org.glassfish.contextpropagation.PropagationMode;
import org.glassfish.contextpropagation.bootstrap.ContextAccessController;
import org.glassfish.contextpropagation.bootstrap.ContextBootstrap;
import org.glassfish.contextpropagation.internal.SimpleMap.Filter;

/**
 * This class is used by the ContextMap for:
 *  - checking permissions
 *  - setting isOriginator to true, since entries created via this API are
 *    created for the first time here.
 */
public class AccessControlledMap {
  private static final boolean IS_ORIGINATOR = true;
  protected SimpleMap simpleMap = new SimpleMap();
  private final ContextAccessController contextAccessController =
      ContextBootstrap.getContextAccessController();

  public <T> T get(String key) throws InsufficientCredentialException {
    Entry entry = simpleMap.getEntry(key);
    if (entry == null) {
      if (contextAccessController.isAccessAllowed(key, ContextAccessLevel.READ)) {
        return null;
      }
    } else {
      if (entry.allowAllToRead ||
          contextAccessController.isAccessAllowed(key, ContextAccessLevel.READ)) {
        return (T) entry.getValue();
      }
    }
    throw new InsufficientCredentialException();
  }

  @SuppressWarnings("unchecked")
  public <T> T put(String key, Entry entry) throws InsufficientCredentialException {
    Entry oldEntry = simpleMap.getEntry(key);
    contextAccessController.checkAccessAllowed(key,
        oldEntry == null ? ContextAccessLevel.CREATE : ContextAccessLevel.UPDATE);

    simpleMap.put(key, entry.init(IS_ORIGINATOR,
        contextAccessController.isEveryoneAllowedToRead(key)));
    return (T) (oldEntry == null ? null : oldEntry.getValue());
  }

  public <T> T remove(String key) throws InsufficientCredentialException {
    contextAccessController.checkAccessAllowed(key, ContextAccessLevel.DELETE);
    return (T) simpleMap.remove(key);
  }

  public EnumSet<PropagationMode> getPropagationModes(String key) throws InsufficientCredentialException {
    Entry entry = simpleMap.getEntry(key);
    if (entry == null) {
      if (contextAccessController.isAccessAllowed(key, ContextAccessLevel.READ)) {
        return null;
      }
    } else {
      if (entry.allowAllToRead ||
          contextAccessController.isAccessAllowed(key, ContextAccessLevel.READ)) {
        return entry.propagationModes;
      }}
    throw new InsufficientCredentialException();
  }

  public static enum ContextAccessLevel {
    CREATE,
    READ,
    UPDATE,
    DELETE
  } // Move to the same place as WorkcContextAccessController interface

  private final Filter AccessCheckerFilter = new Filter() {
    @Override
    public boolean keep(java.util.Map.Entry<String, Entry> mapEntry,
        PropagationMode mode) {
      return contextAccessController.isAccessAllowed(mapEntry.getKey(), ContextAccessLevel.READ);
    }
  };

  public Iterator<Map.Entry<String, Entry>> entryIterator() {
    return simpleMap.iterator(AccessCheckerFilter, null);
  }

  public Entry getEntry(String key) {
    return simpleMap.getEntry(key);
  }

  public Iterator<String> names() {
    final Iterator<Map.Entry<String, Entry>> iter = entryIterator();
    return new Iterator<String>() {
      @Override public boolean hasNext() {
        return iter.hasNext();
      }
      @Override public String next() {
        return iter.next().getKey();
      }
      @Override public void remove() {
        iter.remove();
      }
    };
  }

}
