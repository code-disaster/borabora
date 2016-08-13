/*
 * Copyright (c) 2016, Christoph Engelbert (aka noctarius) and
 * contributors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.noctarius.borabora;

import com.noctarius.borabora.builder.ParserBuilder;
import com.noctarius.borabora.impl.ParserBuilderImpl;

import java.util.function.Consumer;

public interface Parser {

    Value read(Input input, Query query);

    Value read(Input input, String query);

    Value read(Input input, long offset);

    void read(Input input, Query query, Consumer<Value> consumer);

    void read(Input input, String query, Consumer<Value> consumer);

    void read(Input input, long offset, Consumer<Value> consumer);

    byte[] extract(Input input, Query query);

    byte[] extract(Input input, String query);

    byte[] extract(Input input, long offset);

    Query prepareQuery(String query);

    static ParserBuilder newBuilder() {
        return new ParserBuilderImpl();
    }

}
