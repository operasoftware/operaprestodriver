/*
Copyright 2012 Opera Software ASA
Copyright 2012 Software Freedom Conservancy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.opera.core.systems.testing;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Allows parallelization of the OperaDriver test suites.  Use the system property
 * <code>parallelDrivers</code> to specify the number of drivers to spawn.
 */
public class ParallelSuite extends Suite {

  public static final String PARALLELIZATION_PROPERTY_NAME = "parallelDrivers";

  @SuppressWarnings("unused")
  public ParallelSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
    super(klass, builder);
  }

  @SuppressWarnings("unused")
  public ParallelSuite(RunnerBuilder builder, Class<?>[] klasses) throws InitializationError {
    super(builder, klasses);
  }

  @Override
  public void run(final RunNotifier notifier) {
    ThreadPool pool = new ThreadPool(getParallelization(), new Runnable() {
      public void run() {
        AbstractTestCase.removeDriver();
      }
    });

    for (final Runner runner : getChildren()) {
      pool.execute(new Runnable() {
        public void run() {
          runner.run(notifier);
        }
      });
    }

    try {
      pool.shutdownAndWait();
    } catch (InterruptedException e) {
      Throwables.propagateIfPossible(e);
      Thread.currentThread().interrupt();
    }
  }

  private static int getParallelization() {
    String var = System.getProperty(PARALLELIZATION_PROPERTY_NAME);

    try {
      if (var != null) {
        return Integer.parseInt(var);
      }
    } catch (NumberFormatException e) {
      // fall through
    }

    return 1;
  }

  /**
   * This isn't a standard Java thread pool because we want to specify a cleanup action per thread.
   */
  private static class ThreadPool {

    private final List<Thread> threads;
    private final ConcurrentLinkedQueue<Runnable> tasks = new ConcurrentLinkedQueue<Runnable>();
    private volatile boolean isStopped = false;

    public ThreadPool(int threadCount, final Runnable perThreadCleanup) {
      threads = new ArrayList<Thread>(threadCount);

      for (int i = 0; i < threadCount; ++i) {
        Thread thread = new Thread() {
          public void run() {
            while (!isStopped || !tasks.isEmpty()) {
              Runnable task = tasks.poll();

              if (task != null) {
                task.run();
              }

              Thread.yield();
            }

            perThreadCleanup.run();
          }
        };

        thread.start();
        threads.add(thread);
      }
    }

    public void execute(Runnable runnable) {
      Preconditions.checkState(!isStopped,
                               "Thread pool has been shut down, not admitting new tasks");
      tasks.add(runnable);
    }

    public void shutdownAndWait() throws InterruptedException {
      isStopped = true;

      for (Thread thread : threads) {
        thread.join();
      }
    }
  }

}