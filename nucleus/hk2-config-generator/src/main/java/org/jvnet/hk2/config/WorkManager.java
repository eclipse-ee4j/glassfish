/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.jvnet.hk2.config.provider.internal.ConfigThreadContext;

/**
 * Simple helper for managing work sent to a foreign executor service.
 *
 * <p/>
 * Has similarities to Fork and Join.
 *
 * <p/>
 * The implementation is designed such that Tasks-1 are sent to the executor
 * service for possibly another thread to handle.  The last task is executed
 * by the caller thread so that all threads are attempted to be fully utilized
 * for processing including the caller's thread.
 *
 * @author Jeff Trent
 */
public class WorkManager implements Executor {

  private final Executor exec;
  private int tasksToDo;
  private volatile int count;
  private final AtomicInteger workInProgressCount;
  private volatile ArrayList<Exception> errors;

  public WorkManager(Executor exec) {
    this.exec = exec;
    this.workInProgressCount = new AtomicInteger();
  }

  public WorkManager(Executor exec, int tasksToDo) {
    this.exec = exec;
    this.workInProgressCount = new AtomicInteger();
    this.tasksToDo = tasksToDo;
  }

  public int getWorkInProgressCount() {
    return workInProgressCount.get();
  }

  public void awaitCompletion() {
    //synchronized (workInProgressCount) {
      if (workInProgressCount.get() > 0) {
        try {
          workInProgressCount.wait();
        } catch (InterruptedException e) {
          throw new ExecutionException(e);
        }
      }

      awaitCompletionResults();
    //}
  }

  public void awaitCompletion(long timeout, TimeUnit unit) throws TimeoutException {
    //synchronized (workInProgressCount) {
      if (workInProgressCount.get() > 0) {
        try {
          workInProgressCount.wait(unit.convert(timeout, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e) {
          throw new ExecutionException(e);
        }
        if (workInProgressCount.get() > 0) {
          throw new TimeoutException();
        }
      }

      awaitCompletionResults();
    //}
  }

  private void awaitCompletionResults() {
    assert(0 == workInProgressCount.get());

    if (null != errors && !errors.isEmpty()) {
      ArrayList<Exception> errors = new ArrayList<Exception>(this.errors);
      this.errors.clear();
      throw (1 == errors.size() && ConfigurationException.class.isInstance(errors.get(0)))
          ? (ConfigurationException)errors.get(0) : new ExecutionException(errors);
    }
  }

  protected void completed(Watcher<?> watcher, Exception e) {
//    System.out.print(watcher.toString() + " mark completed on thread " + Thread.currentThread() + "...");
    assert(null != watcher);
    //synchronized (workInProgressCount) {
      if (null != e) {
        if (null == errors) {
          errors = new ArrayList<Exception>();
        }
        errors.add(e);
      }

      int val = workInProgressCount.decrementAndGet();
      if (val <= 0) {
        workInProgressCount.notifyAll();
      }

//      System.out.println("done: " + val);
    //}
  }

  @SuppressWarnings("unchecked")
  public <V> Collection<Future<V>> submitAll(Collection<Callable<V>> tasks) {
    workInProgressCount.addAndGet(tasks.size());

    ArrayList<Future<V>> futures = new ArrayList<Future<V>>();
    for (Callable<?> task : tasks) {
      assert(null != task);

      Watcher watcherTask = new Watcher(task);
      futures.add(watcherTask);

      if (++count == tasksToDo) {
        watcherTask.runNow();
      } else {
        exec.execute(watcherTask);
      }
    }

    return futures;
  }

  @SuppressWarnings("unchecked")
  public void executeAll(Collection<Runnable> tasks) {
    workInProgressCount.addAndGet(tasks.size());

    for (Runnable task : tasks) {
      assert(null != task);

      Watcher watcherTask = new Watcher(task, null);

      if (++count == tasksToDo) {
        watcherTask.runNow();
      } else {
        exec.execute(watcherTask);
      }
    }
  }

  @SuppressWarnings({ "unused", "unchecked" })
  public <V> Future<V> submit(Callable<V> task) {
    assert(null != task);
    workInProgressCount.incrementAndGet();

    //    System.out.print("adding more: " + task + "; count=" + work);

    Watcher watcherTask = new Watcher(task);

    if (++count == tasksToDo) {
      watcherTask.runNow();
    } else {
      exec.execute(watcherTask);
    }

    return watcherTask;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void execute(Runnable task) {
    assert(null != task);
    workInProgressCount.incrementAndGet();

    Watcher watcherTask = new Watcher(task, null);

    if (++count == tasksToDo) {
      watcherTask.runNow();
    } else {
      exec.execute(watcherTask);
    }
  }


  private final class Watcher<V> extends FutureTask<V> {
    public Watcher(Callable<V> task) {
      super(task);
    }

    public Watcher(Runnable runnable, V result) {
      super(runnable, result);
    }

    @Override
    public void run() {
      Runnable runnable = new Runnable() {
        @Override
        public void run() {
          runNow();
        }
      };
      ConfigThreadContext.captureACCandRun(runnable);
    }

    private void runNow() {
      try {
        super.run();
        completed(this, null);
      } catch (Exception e){
        setException(e);
        completed(this, e);
      }
    }
  }


  @SuppressWarnings("serial")
  public static final class ExecutionException extends ConfigurationException {
    private List<? extends Throwable> cause;

    public ExecutionException(Throwable t) {
      super(t.getMessage(), t);
      this.cause = Collections.singletonList(t);
    }

    public ExecutionException(ExecutionException e) {
      super(e.getMessage(), e.getCause());
      this.cause = Collections.singletonList(e);
    }

    public ExecutionException(List<Exception> errors) {
      super(errors.get(0).getMessage(), errors.get(0));
      this.cause = errors;
    }

    public <T> T getCause(Class<T> eClass) {
      for (Throwable e : cause) {
        if (null != e.getCause() && eClass == e.getCause().getClass()) {
          return eClass.cast(e.getCause());
        }
      }
      return null;
    }

  }

}
