/*
 * Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.akraino.validation.ui.service.utils;

import java.util.function.Supplier;

public class PrioritySupplier<T> implements Supplier<T>, WithPriority {
    private final int priorityField;
    private final Supplier<T> supplier;

    public PrioritySupplier(int priorityField, Supplier<T> supplier) {
        this.priorityField = priorityField;
        this.supplier = supplier;
    }

    @Override
    public T get() {
        return supplier.get();
    }

    @Override
    public int priority() {
        return priorityField;
    }
}
