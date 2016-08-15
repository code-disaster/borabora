package com.noctarius.borabora.impl.query;

import com.noctarius.borabora.spi.pipeline.QueryBuilderNode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
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

public class BTPipelineStageTestCase {

    @Test
    public void test_btree_equals() {
        BTreePipelineStage expected = new BTreePipelineStage(BTreePipelineStage.NIL, BTreePipelineStage.NIL,
                QueryBuilderNode.QUERY_BASE);
        BTreePipelineStage actual = new BTreePipelineStage(BTreePipelineStage.NIL, BTreePipelineStage.NIL,
                QueryBuilderNode.QUERY_BASE);
        assertEquals(expected, actual);
    }

    @Test
    public void test_btree_equals_false() {
        BTreePipelineStage expected = new BTreePipelineStage(BTreePipelineStage.NIL, BTreePipelineStage.NIL,
                QueryBuilderNode.QUERY_BASE);
        assertFalse(expected.equals(new Object()));
    }

    @Test
    public void test_btree_equals_same() {
        BTreePipelineStage expected = new BTreePipelineStage(BTreePipelineStage.NIL, BTreePipelineStage.NIL,
                QueryBuilderNode.QUERY_BASE);
        assertTrue(expected.equals(expected));
    }

    @Test
    public void test_btree_hashcode() {
        BTreePipelineStage expected = new BTreePipelineStage(BTreePipelineStage.NIL, BTreePipelineStage.NIL,
                QueryBuilderNode.QUERY_BASE);
        BTreePipelineStage actual = new BTreePipelineStage(BTreePipelineStage.NIL, BTreePipelineStage.NIL,
                QueryBuilderNode.QUERY_BASE);
        assertEquals(expected.hashCode(), actual.hashCode());
    }

    @Test
    public void test_printTree() {
        String expected = " /----- <null>\n" //
                + " |       \\----- QUERY_BASE\n" //
                + "<null>\n" //
                + " |       /----- QUERY_BASE\n" //
                + " \\----- <null>\n";

        BTreePipelineStage five = new BTreePipelineStage(BTreePipelineStage.NIL, BTreePipelineStage.NIL,
                QueryBuilderNode.QUERY_BASE);
        BTreePipelineStage four = new BTreePipelineStage(BTreePipelineStage.NIL, BTreePipelineStage.NIL,
                QueryBuilderNode.QUERY_BASE);
        BTreePipelineStage three = new BTreePipelineStage(four, BTreePipelineStage.NIL, null);
        BTreePipelineStage two = new BTreePipelineStage(BTreePipelineStage.NIL, five, null);
        BTreePipelineStage one = new BTreePipelineStage(two, three, null);

        String actual = PipelineStagePrinter.printTree(one);
        assertEquals(expected, actual);
    }

}