/**
*    Copyright 2011, Big Switch Networks, Inc. 
*    Originally created by David Erickson, Stanford University
* 
*    Licensed under the Apache License, Version 2.0 (the "License"); you may
*    not use this file except in compliance with the License. You may obtain
*    a copy of the License at
*
*         http://www.apache.org/licenses/LICENSE-2.0
*
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
*    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
*    License for the specific language governing permissions and limitations
*    under the License.
**/

package net.floodlightcontroller.core.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MockScheduledExecutor implements ScheduledExecutorService {
    ScheduledExecutorService ses = null;

    public static class MockFuture<T> implements Future<T>,ScheduledFuture<T>{
        T result;
        ExecutionException e;
        
        /**
         * @param result
         */
        public MockFuture(T result) {
            super();
            this.result = result;
        }
        
        /**
         * @param result
         */
        public MockFuture(ExecutionException e) {
            super();
            this.e = e;
        }

        
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        
        public T get() throws InterruptedException, ExecutionException {
            if (e != null) throw e;
            return result;
        }

        
        public T get(long timeout, TimeUnit unit) throws InterruptedException,
                ExecutionException, TimeoutException {
            if (e != null) throw e;
            return result;
        }

        
        public boolean isCancelled() {
            return false;
        }

        
        public boolean isDone() {
            return true;
        }

        
        public long getDelay(TimeUnit arg0) {
            return 0;
        }

        
        public int compareTo(Delayed arg0) {
            return 0;
        }
    }
    
    
    public boolean awaitTermination(long arg0, TimeUnit arg1)
            throws InterruptedException {
        return false;
    }

    
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> arg0)
            throws InterruptedException {
        List<Future<T>> rlist = new ArrayList<Future<T>>();
        for (Callable<T> arg : arg0) {
            try {
                rlist.add(new MockFuture<T>(arg.call()));
            } catch (Exception e) {
                rlist.add(new MockFuture<T>(new ExecutionException(e)));
            }
        }
        return rlist;
    }

    
    public <T> List<Future<T>> invokeAll(
            Collection<? extends Callable<T>> arg0, long arg1, TimeUnit arg2)
            throws InterruptedException {
        return this.invokeAll(arg0);
    }

    
    public <T> T invokeAny(Collection<? extends Callable<T>> arg0)
            throws InterruptedException, ExecutionException {
        for (Callable<T> arg : arg0) {
            try {
                return arg.call();
            } catch (Exception e) {

            }
        }
        throw new ExecutionException(new Exception("no task completed successfully"));
    }

    
    public <T> T invokeAny(Collection<? extends Callable<T>> arg0, long arg1,
            TimeUnit arg2) throws InterruptedException, ExecutionException,
            TimeoutException {
        return invokeAny(arg0);
    }

    
    public boolean isShutdown() {
        if (ses != null)
            return ses.isShutdown();

        return false;
    }

    
    public boolean isTerminated() {
        if (ses != null)
            return ses.isTerminated();
        
        return false;
    }

    
    public void shutdown() {
        if (ses != null)
            ses.shutdown();
    }

    
    public List<Runnable> shutdownNow() {
        if (ses != null)
            return ses.shutdownNow();
        return null;
    }

    
    public <T> Future<T> submit(Callable<T> arg0) {
        try {
            return new MockFuture<T>(arg0.call());
        } catch (Exception e) {
            return new MockFuture<T>(new ExecutionException(e));
        }
    }

    
    public Future<?> submit(Runnable arg0) {
        try {
            arg0.run();
            return new MockFuture<Object>(null);
        } catch (Exception e) {
            return new MockFuture<Object>(new ExecutionException(e));
        }
    }

    
    public <T> Future<T> submit(Runnable arg0, T arg1) {
        try {
            arg0.run();
            return new MockFuture<T>((T)null);
        } catch (Exception e) {
            return new MockFuture<T>(new ExecutionException(e));
        }
    }

    
    public void execute(Runnable arg0) {
        arg0.run();
    }

    
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        if (ses == null)
            ses = Executors.newScheduledThreadPool(1);
        try {
            return ses.schedule(command, delay, unit);
        } catch (Exception e) {
            return new MockFuture<Object>(new ExecutionException(e));
        }
    }

    
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay,
            TimeUnit unit) {
        if (ses == null)
            ses = Executors.newScheduledThreadPool(1);
        try {
            return ses.schedule(callable, delay, unit);
        } catch (Exception e) {
            return new MockFuture<V>(new ExecutionException(e));
        }
    }

    
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay,
                                                  long period, TimeUnit unit) {
        if (ses == null)
            ses = Executors.newScheduledThreadPool(1);
        try {
            return ses.scheduleAtFixedRate(command, initialDelay, period, unit);
        } catch (Exception e) {
            return new MockFuture<Object>(new ExecutionException(e));
        }
    }

    
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay,
                                                     long delay, TimeUnit unit) {
        if (ses == null)
            ses = Executors.newScheduledThreadPool(1);
        try {
            return ses.scheduleWithFixedDelay(command, initialDelay, delay, unit);
        } catch (Exception e) {
            return new MockFuture<Object>(new ExecutionException(e));
        }
    }

}
