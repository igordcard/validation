/*
 * Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.akraino.validation.ui.conf;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class UiUtils {

    private static final int QUEUE_CAPACITY = 500;
    private static final int EXECUTOR_SIZE = 20; // the number of threads to keep in the pool, even if
    // they are idle, unless allowCoreThreadTimeOut is
    // set
    private static final int EXECUTOR_MAX_SIZE = 20; // the maximum number of threads to allow in the pool
    private static final int KEEPALIVE_TIME = 20; // when the number of threads is greater than the
    // core, this is the maximum time that excess idle
    // threads will wait for new tasks before
    // terminating.
    private static final PriorityBlockingQueue<Runnable> BLOCKING_QUEUE =
            new PriorityBlockingQueue<Runnable>(QUEUE_CAPACITY, new CFRunnableComparator());
    public static ExecutorService executorService =
            new ThreadPoolExecutor(EXECUTOR_SIZE, EXECUTOR_MAX_SIZE, KEEPALIVE_TIME, TimeUnit.SECONDS, BLOCKING_QUEUE);

    public static final String NEXUS_URL = "https://nexus.akraino.org/content/sites/logs";

    private static class CFRunnableComparator implements Comparator<Runnable> {
        @Override
        @SuppressWarnings("unchecked")
        public int compare(Runnable runnable1, Runnable runnable2) {
            return ((Comparable) unwrap(runnable1)).compareTo(unwrap(runnable2));
        }

        private Object unwrap(Runnable runnable) {
            try {
                Field field = runnable.getClass().getDeclaredField("fn");
                field.setAccessible(true);
                return field.get(runnable);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new IllegalArgumentException("Couldn't unwrap " + runnable, e);
            }
        }
    }

}
