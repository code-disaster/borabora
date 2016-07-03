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

import com.noctarius.borabora.spi.QueryContext;
import com.noctarius.borabora.spi.SelectStatementStrategy;
import com.noctarius.borabora.spi.SelectStatementStrategyAware;

import java.util.Collections;
import java.util.List;

final class ChainGraphQuery
        implements GraphQuery, SelectStatementStrategyAware {

    private final List<GraphQuery> graphQueries;
    private SelectStatementStrategy selectStatementStrategy;

    ChainGraphQuery(List<GraphQuery> graphQueries, SelectStatementStrategy selectStatementStrategy) {
        this.graphQueries = graphQueries;
        this.selectStatementStrategy = selectStatementStrategy;
    }

    @Override
    public long access(long offset, QueryContext queryContext) {
        for (GraphQuery graphQuery : graphQueries) {
            offset = graphQuery.access(offset, queryContext);
            if (offset == -1) {
                return -1;
            }
        }
        return offset;
    }

    @Override
    public String toString() {
        return "ChainGraphQuery{" + "graphQueries=" + graphQueries + '}';
    }

    @Override
    public SelectStatementStrategy selectStatementStrategy() {
        return selectStatementStrategy;
    }

    List<GraphQuery> nodes() {
        return Collections.unmodifiableList(graphQueries);
    }

}
