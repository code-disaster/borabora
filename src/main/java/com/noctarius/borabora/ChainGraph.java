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

import java.util.List;

final class ChainGraph implements Graph {

    private final List<Graph> graphs;

    ChainGraph(List<Graph> graphs) {
        this.graphs = graphs;
    }

    @Override
    public Decoder access(Decoder stream) {
        Decoder substream = stream.subStream();
        for (Graph graph : graphs) {
            substream = graph.access(substream);
            if (substream == null) {
                return null;
            }
        }
        return substream;
    }
}
