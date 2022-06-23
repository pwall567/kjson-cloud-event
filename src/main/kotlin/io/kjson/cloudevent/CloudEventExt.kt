/*
 * @(#) CloudEventExt.kt
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

import java.net.URI
import java.time.OffsetDateTime
import java.util.UUID

/**
 * A Cloud Event with Extension Context Attributes.
 *
 * @author  Peter Wall
 * @param   T       the payload data type
 * @param   E       the payload data type
 */
data class CloudEventExt<T : Any, E : Any>(
    val id: UUID,
    val source: URI,
    val specversion: String = "1.0",
    val type: String,
    val datacontenttype: String? = "application/json",
    val dataschema: URI? = null,
    val subject: String? = null,
    val time: OffsetDateTime? = null,
    val extension: E,
    val data: T? = null,
    val data_base64: String? = null,
) {

    init {
        require(specversion.isNotEmpty()) { "specversion must not be empty" }
        require(type.isNotEmpty()) { "type must not be empty" }
        require(datacontenttype == null || datacontenttype.isNotEmpty()) { "datacontenttype must not be empty" }
        require(subject == null || subject.isNotEmpty()) { "subject must not be empty" }
        require(data == null || data_base64 == null) { "data and data_base64 must not both be present" }
    }

}
