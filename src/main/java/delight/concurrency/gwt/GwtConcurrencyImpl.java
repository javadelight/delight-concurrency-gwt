package delight.concurrency.gwt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;

import delight.async.callbacks.SimpleCallback;
import delight.concurrency.Concurrency;
import delight.concurrency.factories.CollectionFactory;
import delight.concurrency.factories.ExecutorFactory;
import delight.concurrency.factories.TimerFactory;
import delight.concurrency.wrappers.SimpleAtomicBoolean;
import delight.concurrency.wrappers.SimpleAtomicInteger;
import delight.concurrency.wrappers.SimpleAtomicLong;
import delight.concurrency.wrappers.SimpleExecutor;
import delight.concurrency.wrappers.SimpleLock;
import delight.concurrency.wrappers.SimpleReadWriteLock;
import delight.concurrency.wrappers.SimpleTimer;

/**
 * GWT implementation of basic concurrency classes.
 * 
 * @author <a href="http://www.mxro.de">Max Rohde</a>
 *
 */
public final class GwtConcurrencyImpl implements Concurrency {

    @Override
    public TimerFactory newTimer() {

        return new TimerFactory() {

            @Override
            public SimpleTimer scheduleOnce(final int when, final Runnable runnable) {

                if (when == 0) {
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                        @Override
                        public void execute() {
                            runnable.run();
                        }
                    });
                    return new SimpleTimer() {

                        @Override
                        public void stop() {

                        }
                    };

                }

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
            public SimpleTimer scheduleRepeating(final int offsetInMs, final int intervallInMs,
                    final Runnable runnable) {

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

    @Override
    public ExecutorFactory newExecutor() {

        return new ExecutorFactory() {

            @Override
            public SimpleExecutor newAsyncExecutor(final Object owner) {

                return createExecutor(true);
            }

            @Override
            public SimpleExecutor newSingleThreadExecutor(final Object owner) {

                return createExecutor(false);
            }

            @Override
            public SimpleExecutor newParallelExecutor(final int maxParallelThreads, final Object owner) {

                return createExecutor(false);
            }

            @Override
            public SimpleExecutor newImmideateExecutor() {

                return createExecutor(false);
            }

            @Override
            public SimpleExecutor newParallelExecutor(final int minThreads, final int maxParallelThreads,
                    final Object owner) {
                return createExecutor(false);
            }

            private SimpleExecutor createExecutor(final boolean async) {
                return new SimpleExecutor() {

                    @Override
                    public void shutdown(final SimpleCallback callback) {
                        callback.onSuccess();
                    }

                    @Override
                    public void execute(final Runnable runnable) {
                        run(runnable);

                    }

                    private void run(final Runnable runnable) {

                        if (!async) {
                            runnable.run();
                        } else {

                            Scheduler.get().scheduleDeferred(new Command() {

                                @Override
                                public void execute() {
                                    runnable.run();
                                }
                            });
                        }
                    }

                    @Override
                    public void execute(final Runnable runnable, final int timeout, final Runnable onTimeout) {
                        run(runnable);

                    }

                    @Override
                    public int pendingTasks() {
                        return 0;
                    }

                };
            }

        };
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
	public SimpleReadWriteLock newReadWriteLock() {
		
    	final SimpleLock lock = newLock();
    	
		return new SimpleReadWriteLock() {
			
			@Override
			public SimpleLock writeLock() {
				
				return lock;
			}
			
			@Override
			public SimpleLock readLock() {
				
				return lock;
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

    @Override
    public SimpleAtomicLong newAtomicLong(final long value) {

        return new SimpleAtomicLong() {

            long wrapped = value;

            @Override
            public void set(final long newValue) {
                wrapped = newValue;
            }

            @Override
            public long incrementAndGet() {
                wrapped += 1;
                return wrapped;
            }

            @Override
            public long getAndSet(final long newValue) {
                final long oldwrapped = wrapped;
                wrapped = newValue;
                return oldwrapped;
            }

            @Override
            public long get() {
                return wrapped;
            }

            @Override
            public long decrementAndGet() {
                wrapped -= 1;
                return wrapped;
            }
        };
    }

}