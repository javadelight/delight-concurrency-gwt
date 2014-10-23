package de.mxro.concurrency.gwt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.google.gwt.user.client.Timer;

import de.mxro.concurrency.Concurrency;
import de.mxro.concurrency.factories.CollectionFactory;
import de.mxro.concurrency.factories.ExecutorFactory;
import de.mxro.concurrency.factories.TimerFactory;
import de.mxro.concurrency.wrappers.SimpleAtomicBoolean;
import de.mxro.concurrency.wrappers.SimpleAtomicInteger;
import de.mxro.concurrency.wrappers.SimpleExecutor;
import de.mxro.concurrency.wrappers.SimpleLock;
import de.mxro.concurrency.wrappers.SimpleTimer;

/**
 * GWT implementation of basic concurrency classes.
 * 
 * @author <a href="http://www.mxro.de">Max Rohde</a>
 *
 */
public final class GwtConcurrencyImpl implements Concurrency {
    private static final Object THREAD = new Object();

    @Override
    public TimerFactory newTimer() {

        return new TimerFactory() {

            @Override
            public SimpleTimer scheduleOnce(final int when, final Runnable runnable) {

                final Timer timer = new Timer() {

                    @Override
                    public void run() {
                        runnable.run();
                    }

                };
                timer.schedule(when);
                return new SimpleTimer() {

                    @Override
                    public void stop() {
                        timer.cancel();
                    }
                };
            }

            @Override
            public SimpleTimer scheduleRepeating(final int offsetInMs, final int intervallInMs, final Runnable runnable) {

                final Timer timer = new Timer() {

                    @Override
                    public void run() {
                        runnable.run();
                    }

                };
                timer.scheduleRepeating(intervallInMs);
                return new SimpleTimer() {

                    @Override
                    public void stop() {
                        timer.cancel();
                    }
                };

            }

        };
    }

    // public final static boolean IS_CHROME =
    // Window.Navigator.getUserAgent().toLowerCase().contains("chrome");

    @Override
    public ExecutorFactory newExecutor() {

        return new ExecutorFactory() {

            @Override
            public SimpleExecutor newSingleThreadExecutor(final Object owner) {

                return new SimpleExecutor() {

                    @Override
                    public void shutdown(final WhenExecutorShutDown callback) {
                        callback.thenDo();
                    }

                    @Override
                    public Object execute(final Runnable runnable) {
                        /*
                         * final int delay; if (IS_CHROME) { delay = 0; } else {
                         * delay = 1; } newTimer().scheduleOnce(delay,
                         * runnable);
                         */

                        runnable.run();

                        return THREAD; // only one Thread in JS
                    }

                    @Override
                    public Object getCurrentThread() {

                        return THREAD;
                    }
                };
            }

            @Override
            public SimpleExecutor newParallelExecutor(final int maxParallelThreads, final Object owner) {

                return newSingleThreadExecutor(owner);
            }

            @Override
            public SimpleExecutor newImmideateExecutor() {

                return new SimpleExecutor() {

                    @Override
                    public void shutdown(final WhenExecutorShutDown callback) {
                        callback.thenDo();
                    }

                    @Override
                    public Object execute(final Runnable runnable) {
                        // newTimer().scheduleOnce(1, runnable); // too prevent
                        // too deep recursion
                        runnable.run();
                        return THREAD; // only one Thread in JS
                    }

                    @Override
                    public Object getCurrentThread() {

                        return THREAD;
                    }
                };
            }

        };
    }

    @Override
    public void runLater(final Runnable runnable) {

    }

    @Override
    public SimpleLock newLock() {

        return new SimpleLock() {

            @Override
            public void lock() {

            }

            @Override
            public void unlock() {

            }

            @Override
            public boolean isHeldByCurrentThread() {
                return true;
            }

        };
    }

    @Override
    public CollectionFactory newCollection() {

        return new CollectionFactory() {

            @Override
            public <GPType> Queue<GPType> newThreadSafeQueue(final Class<GPType> itemType) {
                return new LinkedList<GPType>();
            }

            @Override
            public <KeyType, ValueType> Map<KeyType, ValueType> newThreadSafeMap(final Class<KeyType> keyType,
                    final Class<ValueType> valueType) {
                return new HashMap<KeyType, ValueType>();
            }

            @Override
            public <ItemType> List<ItemType> newThreadSafeList(final Class<ItemType> itemType) {
                return new ArrayList<ItemType>();
            }

            @Override
            public <ItemType> Set<ItemType> newThreadSafeSet(final Class<ItemType> itemType) {
                return new HashSet<ItemType>();
            }

        };
    }

    @Override
    public SimpleAtomicBoolean newAtomicBoolean(final boolean value) {

        return new SimpleAtomicBoolean() {

            boolean wrapped = value;

            @Override
            public void set(final boolean newValue) {
                wrapped = newValue;
            }

            @Override
            public boolean getAndSet(final boolean newValue) {
                final boolean oldValue = wrapped;
                wrapped = newValue;
                return oldValue;
            }

            @Override
            public boolean get() {
                return wrapped;
            }

            @Override
            public boolean compareAndSet(final boolean expect, final boolean update) {

                if (wrapped == expect) {
                    wrapped = update;
                    return true;
                }

                return false;
            }
        };
    }

    @Override
    public SimpleAtomicInteger newAtomicInteger(final int value) {

        return new SimpleAtomicInteger() {

            int wrapped = value;

            @Override
            public void set(final int newValue) {
                wrapped = newValue;
            }

            @Override
            public int incrementAndGet() {
                wrapped += 1;
                return wrapped;
            }

            @Override
            public int getAndSet(final int newValue) {
                final int oldwrapped = wrapped;
                wrapped = newValue;
                return oldwrapped;
            }

            @Override
            public int get() {
                return wrapped;
            }

            @Override
            public int decrementAndGet() {
                wrapped -= 1;
                return wrapped;
            }
        };
    }

}