/*
Copyright 2011 Selenium committers
Copyright 2012 Opera Software ASA

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

package com.opera.core.systems.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.Clock;
import org.openqa.selenium.support.ui.Duration;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Sleeper;
import org.openqa.selenium.support.ui.SystemClock;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.opera.core.systems.scope.internal.OperaIntervals.INTERNAL_FREQUENCY;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * A variation of Selenium's {@link org.openqa.selenium.support.ui.Wait} that uses {@link Callable}
 * and that may have its timeout and polling interval configured on the fly.
 *
 * Each CallbackWait instance defines the maximum amount of time to wait for a condition, as well as
 * the frequency with which to check the condition.  Furthermore, the user may configure the wait to
 * ignore specific types of exceptions whilst waiting, such as {@link
 * org.openqa.selenium.NoSuchElementException NoSuchElementException} when searching for an element
 * on the page.
 */
public abstract class CallbackWait {

  private final Clock clock;
  private final Sleeper sleeper;

  private Duration timeout = FluentWait.FIVE_HUNDRED_MILLIS;
  private Duration interval = INTERNAL_FREQUENCY.getValue();
  private String message = null;

  private List<Class<? extends RuntimeException>> ignoredExceptions = Lists.newLinkedList();

  /**
   * Callback with a fresh clock and a the system's default thread sleeper.
   */
  public CallbackWait() {
    this(new SystemClock(), Sleeper.SYSTEM_SLEEPER);
  }

  /**
   * Callback with a specified clock and sleeper.
   *
   * @param clock   the clock to use when measuring the timeout
   * @param sleeper used to put the thread to sleep between evaluation loops
   */
  public CallbackWait(Clock clock, Sleeper sleeper) {
    this.clock = checkNotNull(clock);
    this.sleeper = checkNotNull(sleeper);
  }

  /**
   * Sets how long to wait for the evaluated condition to be true.  The default timeout is {@link
   * org.openqa.selenium.support.ui.FluentWait#FIVE_HUNDRED_MILLIS}.
   *
   * @param timeout the timeout duration
   * @return self reference
   */
  public CallbackWait withTimeout(Duration timeout) {
    this.timeout = timeout;
    return this;
  }

  /**
   * Sets the message to be displayed when time expires.
   *
   * @param message to be appended to default
   * @return self reference
   */
  @SuppressWarnings("unused")
  public CallbackWait withMessage(String message) {
    this.message = message;
    return this;
  }

  /**
   * Sets how often the condition should be evaluated.
   *
   * In reality, the interval may be greater as the cost of actually evaluating a condition function
   * is not factored in. The default polling interval is {@link org.openqa.selenium.support.ui.FluentWait#FIVE_HUNDRED_MILLIS}.
   *
   * @param interval the interval duration
   * @return self reference
   */
  public CallbackWait pollingEvery(Duration interval) {
    this.interval = interval;
    return this;
  }

  /**
   * Configures this instance to ignore specific types of exceptions while waiting for a condition.
   * Any exceptions not whitelisted will be allowed to propagate, terminating the wait.
   *
   * @param types the types of exceptions to ignore
   * @return self reference
   */
  public CallbackWait ignoreAll(Collection<Class<? extends RuntimeException>> types) {
    ignoredExceptions.addAll(types);
    return this;
  }

  /**
   * @see #ignoreAll(java.util.Collection)
   */
  @SuppressWarnings("unused")
  public CallbackWait ignoring(Class<? extends RuntimeException> exceptionType) {
    return this.ignoreAll(ImmutableList.<Class<? extends RuntimeException>>of(exceptionType));
  }

  /**
   * @see #ignoreAll(java.util.Collection)
   */
  @SuppressWarnings("unused")
  public CallbackWait ignoring(Class<? extends RuntimeException> firstType,
                               Class<? extends RuntimeException> secondType) {

    return this
        .ignoreAll(ImmutableList.<Class<? extends RuntimeException>>of(firstType, secondType));
  }

  /**
   * Repeatedly applies this instance's input value to the given callable until one of the following
   * occurs:
   *
   * <ol>
   *   <li>the function returns neither null nor false.</li>
   *   <li>the function throws an unignored exception.</li>
   *   <li>the timeout expires.</li>
   *   <li>the current thread is interrupted.</li>
   * </ol>
   *
   * @param condition the {@link Callable} to evaluate
   * @return the callable's return value
   */
  public <X> X until(Callable<X> condition) {
    long end = clock.laterBy(timeout.in(MILLISECONDS));
    Exception lastException = null;

    while (true) {
      try {
        X toReturn = condition.call();

        if (toReturn != null && Boolean.class.equals(toReturn.getClass())) {
          if (Boolean.TRUE.equals(toReturn)) {
            return toReturn;
          }
        } else if (toReturn != null) {
          return toReturn;
        }
      } catch (Exception e) {
        lastException = propagateIfNotIgnored(e);
      }

      if (!clock.isNowBefore(end)) {
        String toAppend =
            (message == null) ? " waiting for " + condition.toString() : ": " + message;

        String timeoutMessage = String.format("Timed out after %d milliseconds%s",
                                              timeout.in(MILLISECONDS), toAppend);
        throw timeoutException(timeoutMessage, lastException);
      }

      try {
        sleeper.sleep(interval);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new WebDriverException(e);
      }
    }
  }

  private Exception propagateIfNotIgnored(Exception e) {
    for (Class<? extends RuntimeException> ignoredException : ignoredExceptions) {
      if (ignoredException.isInstance(e)) {
        return e;
      }
    }

    throw new TimeoutException(e);
  }

  /**
   * Throws a timeout exception.  This method may be overridden to throw an exception that is
   * idiomatic for a particular test infrastructure, such as an AssertionError in JUnit4.
   *
   * @param message       the timeout message
   * @param lastException the last exception to be thrown and subsequently suppressed while waiting
   *                      on a function
   * @return nothing will ever be returned; this return type is only specified as a convenience
   */
  protected RuntimeException timeoutException(String message, Exception lastException) {
    throw new TimeoutException(message, lastException);
  }

}