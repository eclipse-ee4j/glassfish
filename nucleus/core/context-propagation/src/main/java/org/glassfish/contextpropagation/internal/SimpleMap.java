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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.glassfish.contextpropagation.ContextLifecycle;
import org.glassfish.contextpropagation.PropagationMode;
import org.glassfish.contextpropagation.bootstrap.ContextBootstrap;
import org.glassfish.contextpropagation.bootstrap.LoggerAdapter;
import org.glassfish.contextpropagation.bootstrap.LoggerAdapter.Level;
import org.glassfish.contextpropagation.bootstrap.LoggerAdapter.MessageID;
import org.glassfish.contextpropagation.internal.Entry.ContextType;

/*
 * This map is used by the AccessControlleMap, which is itself used by ContextMap.
 * It has full access to the ContextMap data. It takes care of the
 * following tasks:
 *  - validating the data
 *  - notifying LifecycleListeners of the following events: context
 *    added*, changed or removed.
 *  * records the object to notify so that the ContextMapPropagator may send
 *    the notifications after we are done reading all the contexts.
 */
public class SimpleMap {
  private final LoggerAdapter logger = ContextBootstrap.getLoggerAdapter();
  HashMap<String, Entry> map = new HashMap<String, Entry>();
  private List<ContextLifecycle> addedContexts;

  Entry getEntry(String key) {
    validate("get", key);
    Entry entry = map.get(key);
    if (logger.isLoggable(Level.DEBUG)) {
      logger.log(Level.DEBUG, MessageID.OPERATION, "getEntry", key, entry);
    }
    return entry;
  }

  protected void prepareToPropagate() {
    addedContexts = new LinkedList<ContextLifecycle>();
  }

  protected List<ContextLifecycle> getAddedContextLifecycles() {
    List<ContextLifecycle> result = addedContexts;
    addedContexts = null;
    return result;
  }

  @SuppressWarnings("unchecked")
  public <T> T get(String key) {
    return (T) extractResult(key, getEntry(key), "get");
  }

  @SuppressWarnings("unchecked")
  private <T> T extractResult(String key, Entry entry, String operation) {
    T result =  (T) (entry == null ? null : entry.value);
    if (logger.isLoggable(Level.DEBUG)) {
      logger.log(Level.DEBUG, MessageID.OPERATION, operation, key, result);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  public <T> T put(String key, Entry entry) {
    validate(key, entry);
    T value = (T) entry.getValue();
    Entry oldEntry = map.put(key, entry);
    if (oldEntry != null && (oldEntry.getValue() instanceof ContextLifecycle)) {
      ((ContextLifecycle) oldEntry.value).contextChanged(value);
    }
    if (entry.getValue() instanceof ContextLifecycle) {
      ContextLifecycle ctx = (ContextLifecycle) entry.getValue();
      if (addedContexts == null) {
        ctx.contextAdded();
      } else {
        addedContexts.add(ctx);
      }
    }
    if (logger.isLoggable(Level.DEBUG)) {
      logger.log(Level.DEBUG, MessageID.PUT, key, value, oldEntry == null ? null : oldEntry.value);
    }
    return (T) (oldEntry == null ? null : oldEntry.value);
  }

  @SuppressWarnings("unchecked")
  public <T> T remove(String key) {
    validate("remove", key);
    Entry entry = (Entry) map.remove(key);
    if (entry != null) {
      if (entry.getValue() instanceof ContextLifecycle) {
        ((ContextLifecycle) entry.getValue()).contextRemoved();
      }
      if (entry.contextType == ContextType.VIEW_CAPABLE) {
        ((ViewImpl) entry.getView()).clean();
      }
    }
    return (T) extractResult(key, entry, "remove");
  }

  private void validate(String key, Entry entry) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("Cannot use null key");
    }
    if (entry == null) {
      throw new IllegalArgumentException("Cannot use null entry");
    }
    if (entry.value == null) {
      throw new IllegalArgumentException("Cannot use null value");
    }
    entry.validate();
  }

  private void validate(String operation, String key) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("Cannot use null key");
    }
  }

  public Iterator<Map.Entry<String, Entry>> iterator(final Filter filter, final PropagationMode mode) {
    return new Iterator<Map.Entry<String, Entry>>() {
      Iterator<Map.Entry<String, Entry>> it = map.entrySet().iterator();
      Map.Entry<String, Entry> next;
      Map.Entry<String, Entry> last;

      @Override
      public boolean hasNext() {
        return next == null ? findNext() : true;
      }

      @Override
      public Map.Entry<String, Entry> next() {
        if (next == null && !findNext()) {
          throw new NoSuchElementException();
        } else {
          last = next;
          next = null;
          return last;
        }
      }

      private boolean findNext() {
        while (it.hasNext()) {
          Map.Entry<String, Entry> entry = it.next();
          if (filter.keep(entry, mode)) {
            next = entry;
            return true;
          }
        }
        next = null;
        return false;
      }

      @Override
      public void remove() {
        if (last == null) throw new IllegalStateException("Make sure that next is called before calling remove, or that there are elements to remove");
        it.remove();
        if (logger.isLoggable(Level.DEBUG)) {
          logger.log(Level.DEBUG, MessageID.OPERATION, "remove", last.getKey(), last.getValue().value);
        }
        last = null;
      }

    };
  }

  public interface Filter {
    boolean keep(Map.Entry<String, Entry> mapEntry, PropagationMode mode);
  }

}
