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

final class ByteArrayOutput
        implements Output {

    private final byte[] array;

    ByteArrayOutput(byte[] array) {
        this.array = array;
    }

    @Override
    public void write(long offset, byte value) {
        array[(int) offset] = value;
    }

    @Override
    public boolean ensureCapacity(long offset, long length) {
        if (offset >= array.length) {
            return false;
        }
        if (offset + length >= array.length) {
            return false;
        }
        return true;
    }

}