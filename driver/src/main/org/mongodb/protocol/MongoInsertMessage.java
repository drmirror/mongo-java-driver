/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mongodb.protocol;

import org.mongodb.WriteConcern;
import org.mongodb.io.ChannelAwareOutputBuffer;
import org.mongodb.operation.MongoInsert;
import org.mongodb.serialization.Serializer;

public class MongoInsertMessage<T> extends MongoRequestMessage {
    private final MongoInsert<T> insert;
    private final Serializer<T> serializer;

    public MongoInsertMessage(final String collectionName, final MongoInsert<T> insert, final Serializer<T> serializer) {
        super(collectionName, OpCode.OP_INSERT);
        this.insert = insert;
        this.serializer = serializer;
    }

    @Override
    protected void serializeMessageBody(final ChannelAwareOutputBuffer buffer) {
        writeInsertPrologue(insert.getWriteConcern(), buffer);
        for (final T document : insert.getDocuments()) {
            addDocument(document, serializer, buffer);
        }
    }

    private void writeInsertPrologue(final WriteConcern concern, final ChannelAwareOutputBuffer buffer) {
        int flags = 0;
        if (concern.getContinueOnErrorForInsert()) {
            flags |= 1;
        }
        buffer.writeInt(flags);
        buffer.writeCString(getCollectionName());
    }
}