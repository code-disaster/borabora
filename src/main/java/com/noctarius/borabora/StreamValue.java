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

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

final class StreamValue
        extends AbstractValue {

    private final Collection<SemanticTagProcessor> processors;
    private final Decoder stream;
    private final long offset;
    private final long length;

    StreamValue(MajorType majorType, ValueType valueType, Decoder stream, long offset, long length,
                Collection<SemanticTagProcessor> processors) {

        super(majorType, valueType);

        if (offset == -1) {
            throw new IllegalArgumentException("No offset available for CBOR type");
        }
        if (length == -1) {
            throw new IllegalArgumentException(String.format("Length calculation for CBOR type %s is not available", majorType));
        }

        this.stream = stream;
        this.offset = offset;
        this.length = length;
        this.processors = processors;
    }

    @Override
    public <V> V tag() {
        return extract(() -> matchMajorType(MajorType.SemanticTag), () -> tag0(offset, length));
    }

    @Override
    public Number number() {
        return extract(() -> matchValueType(ValueTypes.UInt, ValueTypes.NInt, ValueTypes.NFloat),
                () -> stream.readNumber(valueType(), offset));
    }

    @Override
    public Sequence sequence() {
        return extract(() -> matchValueType(ValueTypes.Sequence), () -> stream.readSequence(offset, processors));
    }

    @Override
    public Dictionary dictionary() {
        return extract(() -> matchValueType(ValueTypes.Dictionary), () -> stream.readDictionary(offset, processors));
    }

    @Override
    public String string() {
        return extract(this::matchStringValueType, () -> stream.readString(offset));
    }

    @Override
    public Boolean bool() {
        return extract(() -> matchValueType(ValueTypes.Bool), () -> stream.getBooleanValue(offset));
    }

    @Override
    public byte[] raw() {
        return extract(() -> stream.readRaw(offset, length));
    }

    @Override
    public <V> V byValueType() {
        return valueType().value(this);
    }

    @Override
    protected <T> T extract(Validator validator, Supplier<T> supplier) {
        short head = stream.transientUint8(offset);
        // Null is legal for all types
        if (stream.isNull(head)) {
            return null;
        }
        // Not null? Validate value type
        if (validator != null) {
            validator.validate();
        }
        // Semantic tag? Extract real value
        if (MajorType.SemanticTag == majorType()) {
            return tag0(offset, length);
        }
        return supplier.get();
    }

    private <V> SemanticTagProcessor<V> findProcessor(long offset) {
        Optional<SemanticTagProcessor> optional = processors.stream().filter(p -> p.handles(stream, offset)).findFirst();
        return optional.isPresent() ? optional.get() : null;
    }

    private <V> V tag0(long offset, long length) {
        SemanticTagProcessor<V> processor = findProcessor(offset);
        if (processor == null) {
            return null;
        }
        return processor.process(stream, offset, length, processors);
    }

}