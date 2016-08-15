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
package com.noctarius.borabora.spi.codec;

import com.noctarius.borabora.Input;
import com.noctarius.borabora.MajorType;
import com.noctarius.borabora.Output;
import com.noctarius.borabora.ValueType;
import com.noctarius.borabora.ValueTypes;
import com.noctarius.borabora.spi.Constants;
import com.noctarius.borabora.spi.StreamValue;
import com.noctarius.borabora.spi.TypeSpec;
import com.noctarius.borabora.spi.TypeSpecs;
import com.noctarius.borabora.spi.query.QueryContext;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.function.Predicate;

public final class CommonTagCodec
        implements TagDecoder<Object>, TagEncoder<Object> {

    public enum TYPE_MATCHER
            implements Predicate<Object> {

        DATE_TIME_MATCHER((v) -> //
                Date.class.isAssignableFrom(v.getClass()) || java.sql.Date.class.isAssignableFrom(v.getClass())),

        TIMESTAMP_MATCHER((v) -> Timestamp.class.isAssignableFrom(v.getClass())),

        UBIG_NUM_MATCHER((v) -> //
                BigInteger.class.isAssignableFrom(v.getClass()) && ((BigInteger) v).signum() >= 0),

        NBIG_NUM_MATCHER((v) -> //
                BigInteger.class.isAssignableFrom(v.getClass()) && ((BigInteger) v).signum() < 0),

        URI_MATCHER((v) -> URI.class.isAssignableFrom(v.getClass())),

        ENCODED_CBOR_MATCHER((v) -> false /* TODO */);

        private final Predicate<Object> predicate;

        TYPE_MATCHER(Predicate<Object> predicate) {
            this.predicate = predicate;
        }

        @Override
        public boolean test(Object value) {
            return predicate.test(value);
        }
    }

    public enum TAG_WRITER
            implements TagWriter<Object> {

        DATE_TIME_WRITER((value, offset, encoderContext) -> {
            Output output = encoderContext.output();
            Instant instant = ((Date) value).toInstant();
            ZonedDateTime zonedDateTime = instant.atZone(Constants.UTC);
            return Encoder.putDateTime(zonedDateTime, offset, output);
        }),

        BIG_NUM_WRITER((value, offset, encoderContext) -> {
            Output output = encoderContext.output();
            return Encoder.putNumber((BigInteger) value, offset, output);
        }),

        TIMESTAMP_WRITER((value, offset, encoderContext) -> {
            Output output = encoderContext.output();
            return Encoder.putTimestamp(((Number) value).longValue(), offset, output);
        }),

        URI_WRITER((value, offset, encoderContext) -> {
            Output output = encoderContext.output();
            return Encoder.putUri((URI) value, offset, output);
        }),

        ENCODED_CBOR_WRITER((value, offset, encoderContext) -> {
            return offset; /* TODO */
        });

        private final TagWriter<Object> tagWriter;

        TAG_WRITER(TagWriter<Object> tagWriter) {
            this.tagWriter = tagWriter;
        }

        @Override
        public long process(Object value, long offset, EncoderContext encoderContext) {
            return tagWriter.process(value, offset, encoderContext);
        }
    }

    public enum TAG_READER
            implements TagReader<Object> {

        DATE_TIME_READER((valueType, offset, length, queryContext) -> {
            Input input = queryContext.input();
            int byteSize = ByteSizes.intByteSize(input, offset);
            String date = Decoder.readString(input, offset + byteSize);
            return DateParser.parseDate(date, Locale.ENGLISH);
        }),

        UBIG_NUM_READER((valueType, offset, length, queryContext) -> {
            Input input = queryContext.input();
            int byteSize = ByteSizes.intByteSize(input, offset);
            // We need to extract raw bytes to prevent creating strings
            // and since new String(..) transforms the byte array for char[]
            byte[] bytes = Decoder.extractStringBytes(input, offset + 1);
            return new BigInteger(1, bytes);
        }),

        NBIG_NUM_READER((valueType, offset, length, queryContext) -> //
                BigInteger.valueOf(-1).xor((BigInteger) UBIG_NUM_READER.process(valueType, offset, length, queryContext))),

        URI_READER((valueType, offset, length, queryContext) -> {
            Input input = queryContext.input();
            int byteSize = ByteSizes.intByteSize(input, offset);
            String uri = Decoder.readString(input, offset + byteSize);
            try {
                return new URI(uri);
            } catch (URISyntaxException e) {
                throw new RuntimeException("URI could not be read", e);
            }
        }),

        TIMESTAMP_READER((vt, offset, length, queryContext) -> {
            Input input = queryContext.input();
            // Move offset to the actual data item
            offset += ByteSizes.headByteSize(input, offset);
            // Timestamp might have different types of items
            ValueType valueType = ValueTypes.valueType(input, offset);
            return Decoder.readNumber(input, valueType, offset);
        }),

        ENCODED_CBOR_READER((vt, offset, length, queryContext) -> {
            Input input = queryContext.input();
            int headByteSize = ByteSizes.intByteSize(input, offset);

            offset += headByteSize;
            short head = Decoder.readUInt8(input, offset);
            MajorType majorType = MajorType.findMajorType(head);
            // Normally this is a bytestring for real
            ValueType valueType = ValueTypes.valueType(input, offset);
            return new StreamValue(majorType, valueType, offset, queryContext);
        }),

        UNKNOWN_TAG_READER((valueType, offset, length, queryContext) -> {
            Input input = queryContext.input();
            // Move offset to the actual data item
            offset += ByteSizes.headByteSize(input, offset);
            short itemHead = Decoder.readUInt8(input, offset);
            MajorType majorType = MajorType.findMajorType(itemHead);
            long size = ByteSizes.byteSizeByMajorType(majorType, input, offset);
            byte[] bytes = new byte[(int) size];
            input.read(bytes, offset, size);
            return bytes;
        });

        private final TagReader<Object> tagReader;

        TAG_READER(TagReader<Object> tagReader) {
            this.tagReader = tagReader;
        }

        @Override
        public Object process(ValueType valueType, long offset, long length, QueryContext queryContext) {
            return tagReader.process(valueType, offset, length, queryContext);
        }
    }

    public static final TagDecoder INSTANCE = new CommonTagCodec();

    private CommonTagCodec() {
    }

    @Override
    public boolean handles(Input input, long offset) {
        return ValueTypes.valueType(input, offset) != null;
    }

    @Override
    public Object process(ValueType valueType, long offset, long length, QueryContext queryContext) {
        return ((ValueTypes) valueType).process(valueType, offset, length, queryContext);
    }

    @Override
    public TypeSpec handles(int tagId) {
        for (TypeSpec typeSpec : TypeSpecs.values()) {
            if (typeSpec.tagId() == tagId) {
                return typeSpec;
            }
        }
        return null;
    }

    @Override
    public boolean handles(Object value) {
        return ValueTypes.valueType(value) != null;
    }

    @Override
    public long process(Object value, long offset, EncoderContext encoderContext) {
        ValueTypes valueType = ValueTypes.valueType(value);
        if (valueType == null) {
            throw new IllegalStateException("Unknown ValueType found: " + value.getClass());
        }
        return valueType.process(value, offset, encoderContext);
    }

}
