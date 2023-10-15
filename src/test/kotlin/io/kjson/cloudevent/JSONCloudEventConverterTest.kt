/*
 * @(#) JSONCloudEventConverterTest.kt
 *
 * kjson-cloud-event  Kotlin implementation of CloudEvents specification (v1)
 * Copyright (c) 2022, 2023 Peter Wall
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
import kotlin.test.assertNotNull
import kotlin.test.expect
import kotlin.test.fail

import java.net.URI
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

import io.kjson.JSONConfig
import io.kjson.JSONKotlinException
import io.kjson.cloudevent.JSONCloudEventConverter.addCloudEventExtFromJSON
import io.kjson.cloudevent.JSONCloudEventConverter.addCloudEventExtToJSON
import io.kjson.parseJSON
import io.kjson.pointer.JSONPointer
import io.kjson.stringifyJSON
import io.kjson.test.JSONExpect.Companion.expectJSON

class JSONCloudEventConverterTest {

    @Test fun `should serialize CloudEventExt`() {
        val id = UUID.fromString("274132d8-f2e2-11ec-9897-6f1fa956d500")
        val source = URI("https://kjson.io/test")
        val type = "test1"
        val subject = "ABC"
        val time = OffsetDateTime.of(2022, 6, 23, 22, 21, 27, 456_000_000, ZoneOffset.ofHours(10))
        val ext1 = "Fred"
        val ext2 = UUID.fromString("88adcea0-f2e2-11ec-abd8-cb487f52b4aa")
        val accountId = UUID.fromString("a0dfa09a-f2ef-11ec-80fc-137c580906c5")
        val accountName = "Test Account"
        val event = CloudEventExt(
            id = id,
            source = source,
            type = type,
            subject = subject,
            time = time,
            data = AccountOpen(accountId, accountName),
            extension = Extension1(ext1, ext2),
        )
        val config = JSONConfig {
            addCloudEventExtToJSON<AccountOpen, Extension1>()
        }
        expectJSON(event.stringifyJSON(config)) {
            property("id", id)
            property("source", source.toString())
            property("specversion", "1.0")
            property("type", type)
            property("datacontenttype", "application/json")
            property("subject", subject)
            property("time", time)
            property("ext1", ext1)
            property("ext2", ext2)
            property("data") {
                property("accountId", accountId)
                property("name", accountName)
            }
        }
    }

    @Test fun `should serialize CloudEventExt using map for extensions`() {
        val id = UUID.fromString("274132d8-f2e2-11ec-9897-6f1fa956d500")
        val source = URI("https://kjson.io/test2")
        val type = "test1"
        val subject = "ABC"
        val time = OffsetDateTime.of(2022, 6, 23, 22, 21, 27, 456_000_000, ZoneOffset.ofHours(10))
        val accountId = UUID.fromString("a0dfa09a-f2ef-11ec-80fc-137c580906c5")
        val accountName = "Test Account"
        val event = CloudEventExt(
            id = id,
            source = source,
            type = type,
            subject = subject,
            time = time,
            data = AccountOpen(accountId, accountName),
            extension = mapOf("value1" to "Horse", "value2" to "Zebra"),
        )
        val config = JSONConfig {
            addCloudEventExtToJSON<AccountOpen, Map<String, String>>()
        }
        expectJSON(event.stringifyJSON(config)) {
            exhaustive {
                property("id", id)
                property("source", source.toString())
                property("specversion", "1.0")
                property("type", type)
                property("datacontenttype", "application/json")
                property("subject", subject)
                property("time", time)
                property("value1", "Horse")
                property("value2", "Zebra")
                property("data") {
                    property("accountId", accountId)
                    property("name", accountName)
                }
            }
        }
    }

    @Test fun `should deserialize CloudEventExt`() {
        val id: UUID = UUID.fromString("274132d8-f2e2-11ec-9897-6f1fa956d500")
        val source = URI("https://kjson.io/test")
        val type = "test1"
        val subject = "ABC"
        val time = OffsetDateTime.of(2022, 6, 23, 22, 21, 27, 456_000_000, ZoneOffset.ofHours(10))
        val ext1 = "Fred"
        val ext2: UUID = UUID.fromString("88adcea0-f2e2-11ec-abd8-cb487f52b4aa")
        val accountId: UUID = UUID.fromString("a0dfa09a-f2ef-11ec-80fc-137c580906c5")
        val accountName = "Test Account"
        val event = CloudEventExt(
            id = id,
            source = source,
            type = type,
            subject = subject,
            time = time,
            data = AccountOpen(accountId, accountName),
            extension = Extension1(ext1, ext2),
        )
        val config = JSONConfig {
            addCloudEventExtToJSON<AccountOpen, Extension1>()
            addCloudEventExtFromJSON<AccountOpen, Extension1>()
        }
        val serialised = event.stringifyJSON(config)
        val deserialised: CloudEventExt<AccountOpen, Extension1> = serialised.parseJSON(config) ?: fail()
        with(deserialised) {
            expect(id) { this.id }
            expect(source) { this.source }
            expect(type) { this.type }
            expect(subject) { this.subject }
            expect(time) { this.time }
            with(this.extension) {
                expect(ext1) { this.ext1 }
                expect(ext2) { this.ext2 }
            }
            with(this.data) {
                assertNotNull(this)
                expect(accountId) { this.accountId }
                expect(accountName) { this.name }
            }
        }
    }

    @Test fun `should deserialize CloudEventExt using map for extensions`() {
        val id: UUID = UUID.fromString("274132d8-f2e2-11ec-9897-6f1fa956d500")
        val source = URI("https://kjson.io/test2")
        val type = "test1"
        val subject = "ABC"
        val time = OffsetDateTime.of(2022, 6, 23, 22, 21, 27, 456_000_000, ZoneOffset.ofHours(10))
        val accountId: UUID = UUID.fromString("a0dfa09a-f2ef-11ec-80fc-137c580906c5")
        val accountName = "Test Account"
        val event = CloudEventExt(
            id = id,
            source = source,
            type = type,
            subject = subject,
            time = time,
            data = AccountOpen(accountId, accountName),
            extension = mapOf("value1" to "Horse", "value2" to "Zebra"),
        )
        val config = JSONConfig {
            addCloudEventExtToJSON<AccountOpen, Map<String, String>>()
            addCloudEventExtFromJSON<AccountOpen, Map<String, String>>()
        }
        val serialised = event.stringifyJSON(config)
        val deserialised: CloudEventExt<AccountOpen, Map<String, String>> = serialised.parseJSON(config) ?: fail()
        with(deserialised) {
            expect(id) { this.id }
            expect(source) { this.source }
            expect(type) { this.type }
            expect(subject) { this.subject }
            expect(time) { this.time }
            with(this.extension) {
                expect(2) { this.size }
                expect("Horse") { this["value1"] }
                expect("Zebra") { this["value2"] }
            }
            with(this.data) {
                assertNotNull(this)
                expect(accountId) { this.accountId }
                expect(accountName) { this.name }
            }
        }
    }

    @Test fun `should report error correctly when deserializing CloudEventExt`() {
        val id: UUID = UUID.fromString("274132d8-f2e2-11ec-9897-6f1fa956d500")
        val source = URI("https://kjson.io/test")
        val type = "test1"
        val subject = "ABC"
        val time = OffsetDateTime.of(2022, 6, 23, 22, 21, 27, 456_000_000, ZoneOffset.ofHours(10))
        val ext1 = "Fred"
        val ext2: UUID = UUID.fromString("88adcea0-f2e2-11ec-abd8-cb487f52b4aa")
        val event = CloudEventExt(
            id = id,
            source = source,
            type = type,
            subject = subject,
            time = time,
            data = mapOf("accountId" to "123456789", "name" to "Test Account"),
            extension = Extension1(ext1, ext2),
        )
        val config = JSONConfig {
            addCloudEventExtToJSON<Map<String, String>, Extension1>()
            addCloudEventExtFromJSON<AccountOpen, Extension1>()
        }
        val serialised = event.stringifyJSON(config)
        assertFailsWith<JSONKotlinException> {
            serialised.parseJSON<CloudEventExt<AccountOpen, Extension1>>(config)
        }.let {
            expect("Error deserializing \"123456789\" as java.util.UUID at /data/accountId") { it.message }
            expect(JSONPointer("/data/accountId")) { it.pointer }
        }
    }

}
