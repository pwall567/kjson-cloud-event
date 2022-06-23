/*
 * @(#) CloudEventTest.kt
 *
 * kjson-cloud-event  Kotlin implementation of CloudEvents specification (v1)
 * Copyright (c) 2022 Peter Wall
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.kjson.cloudevent

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.expect
import kotlin.test.fail

import java.net.URI
import java.time.OffsetDateTime
import java.util.UUID
import io.kjson.parseJSON
import io.kjson.stringifyJSON
import io.kjson.test.JSONExpect

class CloudEventTest {

    @Test fun `should create simple cloud event`() {
        val id = UUID.fromString("e0c9a45c-dbc1-11ec-929c-5be38bfa231a")
        val source = URI("http://kjson.io/test")
        val type = "io.kjson.cloudevent.test"
        val subject = "Dummy"
        val time = OffsetDateTime.now()
        val data = "String content"
        val event = CloudEvent(
            id = id,
            source = source,
            type = type,
            subject = subject,
            time = time,
            data = data,
        )
        expect(id) { event.id }
        expect(source) { event.source }
        expect(type) { event.type }
        expect(subject) { event.subject }
        expect(time) { event.time }
        expect(data) { event.data }
        expect("application/json") { event.datacontenttype }
        assertNull(event.dataschema)
    }

    @Test fun `should throw exception when specversion is empty`() {
        assertFailsWith<IllegalArgumentException> {
            CloudEvent(
                id = UUID.fromString("e0c9a45c-dbc1-11ec-929c-5be38bfa231a"),
                source = URI("http://kjson.io/test"),
                specversion = "",
                type = "io.kjson.cloudevent.test",
                subject = "Dummy",
                time = OffsetDateTime.now(),
                data = "String content",
            )
        }.let {
            expect("specversion must not be empty") { it.message }
        }
    }

    @Test fun `should throw exception when type is empty`() {
        assertFailsWith<IllegalArgumentException> {
            CloudEvent(
                id = UUID.fromString("e0c9a45c-dbc1-11ec-929c-5be38bfa231a"),
                source = URI("http://kjson.io/test"),
                type = "",
                subject = "Dummy",
                time = OffsetDateTime.now(),
                data = "String content",
            )
        }.let {
            expect("type must not be empty") { it.message }
        }
    }

    @Test fun `should throw exception when datacontenttype is empty`() {
        assertFailsWith<IllegalArgumentException> {
            CloudEvent(
                id = UUID.fromString("e0c9a45c-dbc1-11ec-929c-5be38bfa231a"),
                source = URI("http://kjson.io/test"),
                type = "io.kjson.cloudevent.test",
                datacontenttype = "",
                subject = "Dummy",
                time = OffsetDateTime.now(),
                data = "String content",
            )
        }.let {
            expect("datacontenttype must not be empty") { it.message }
        }
    }

    @Test fun `should throw exception when subject is empty`() {
        assertFailsWith<IllegalArgumentException> {
            CloudEvent(
                id = UUID.fromString("e0c9a45c-dbc1-11ec-929c-5be38bfa231a"),
                source = URI("http://kjson.io/test"),
                type = "io.kjson.cloudevent.test",
                subject = "",
                time = OffsetDateTime.now(),
                data = "String content",
            )
        }.let {
            expect("subject must not be empty") { it.message }
        }
    }

    @Test fun `should serialise correctly`() {
        val id = UUID.fromString("e0c9a45c-dbc1-11ec-929c-5be38bfa231a")
        val source = URI("http://kjson.io/test")
        val type = "io.kjson.cloudevent.test"
        val subject = "Dummy"
        val time = OffsetDateTime.now()
        val data = AccountOpen(
            accountId = UUID.fromString("e62dd2d6-dce0-11ec-91d8-6b8cbe0bfb29"),
            name = "Dummy Account",
        )
        val event = CloudEvent(
            id = id,
            source = source,
            type = type,
            subject = subject,
            time = time,
            data = data,
        )
        val json = event.stringifyJSON()
        JSONExpect.expectJSON(json) {
            property("id", id.toString())
            property("source", source.toString())
            property("type", type)
            property("subject", subject)
            property("time", time.toString())
            property("data") {
                property("accountId", data.accountId.toString())
                property("name", data.name)
            }
        }
    }

    @Test fun `should deserialise correctly`() {
        val id = UUID.fromString("e0c9a45c-dbc1-11ec-929c-5be38bfa231a")
        val source = URI("http://kjson.io/test")
        val type = "io.kjson.cloudevent.test"
        val subject = "Dummy"
        val time = OffsetDateTime.now()
        val data = AccountOpen(
            accountId = UUID.fromString("e62dd2d6-dce0-11ec-91d8-6b8cbe0bfb29"),
            name = "Dummy Account",
        )
        val event = CloudEvent(
            id = id,
            source = source,
            type = type,
            subject = subject,
            time = time,
            data = data,
        )
        val json = event.stringifyJSON()
        val deserialised: CloudEvent<AccountOpen> = json.parseJSON() ?: fail()
        expect(event) { deserialised }
    }

}
