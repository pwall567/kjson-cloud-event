/*
 * @(#) CloudEventExtTest.kt
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
import kotlin.test.assertNull
import kotlin.test.expect

import java.net.URI
import java.time.OffsetDateTime
import java.util.UUID

class CloudEventExtTest {

    @Test fun `should create cloud event with extension attributes`() {
        val id = UUID.fromString("e0c9a45c-dbc1-11ec-929c-5be38bfa231a")
        val source = URI("http://kjson.io/test")
        val type = "io.kjson.cloudevent.test"
        val subject = "Dummy"
        val time = OffsetDateTime.now()
        val data = "String content"
        val ext1 = "Fred"
        val ext2: UUID = UUID.fromString("88adcea0-f2e2-11ec-abd8-cb487f52b4aa")
        val extension = Extension1(ext1, ext2)
        val event = CloudEventExt(
            id = id,
            source = source,
            type = type,
            subject = subject,
            time = time,
            data = data,
            extension = extension,
        )
        expect(id) { event.id }
        expect(source) { event.source }
        expect(type) { event.type }
        expect(subject) { event.subject }
        expect(time) { event.time }
        expect(data) { event.data }
        expect(extension) { event.extension }
        expect("application/json") { event.datacontenttype }
        assertNull(event.dataschema)
    }

    @Test fun `should create cloud event with map for extension attributes`() {
        val id = UUID.fromString("e0c9a45c-dbc1-11ec-929c-5be38bfa231a")
        val source = URI("http://kjson.io/test")
        val type = "io.kjson.cloudevent.test2"
        val subject = "Dummy"
        val time = OffsetDateTime.now()
        val event = CloudEventExt(
            id = id,
            source = source,
            type = type,
            subject = subject,
            time = time,
            data = "Dummy content",
            extension = mapOf("value1" to "Horse", "value2" to "Zebra"),
        )
        expect(id) { event.id }
        expect(source) { event.source }
        expect(type) { event.type }
        expect(subject) { event.subject }
        expect(time) { event.time }
        expect("Dummy content") { event.data }
        expect("Horse") { event.extension["value1"] }
        expect("Zebra") { event.extension["value2"] }
        expect("application/json") { event.datacontenttype }
        assertNull(event.dataschema)
    }

}
