/*
 * @(#) JSONCloudEventConverter.kt
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

import io.kjson.JSON.asObject
import io.kjson.JSON.asString
import io.kjson.JSONConfig
import io.kjson.JSONException
import io.kjson.JSONObject
import io.kjson.JSONSerializer
import io.kjson.JSONValue
import io.kjson.fromJSONValue

/**
 * Custom serialization and deserialization functions for the [CloudEventExt] class, using the `kjson` library.
 *
 * Because the Extension Context Attributes are required to be serialized as part of the outer Cloud Events envelope,
 * custom code is needed to serialize and deserialize these objects correctly.  These functions will perform that
 * custom serialization and deserialization for the `kjson` library.
 *
 * @author  Peter Wall
 */
object JSONCloudEventConverter {

    val standardAttributes = setOf("id", "source", "specversion", "type", "datacontenttype", "dataschema", "subject",
            "time", "data", "data_base64")

    inline fun <reified T : Any, reified E : Any> JSONConfig.cloudEventExtFromJSON(json: JSONValue?):
            CloudEventExt<T, E>? = json?.let {
        val jsonObject = json.asObject
        val extObject = JSONObject.build {
            for (entry in jsonObject.entries)
                if (entry.key !in standardAttributes)
                    add(entry.key, entry.value)
        }
        CloudEventExt(
            id = UUID.fromString(jsonObject["id"].asString),
            source = URI(jsonObject["source"].asString),
            specversion = jsonObject["specversion"].asString,
            type = jsonObject["type"].asString,
            datacontenttype = jsonObject["datacontenttype"]?.asString,
            dataschema = jsonObject["dataschema"]?.asString?.let { URI(it) },
            subject = jsonObject["subject"]?.asString,
            time = jsonObject["time"]?.asString?.let { OffsetDateTime.parse(it) },
            extension = extObject.fromJSONValue(this),
            data = jsonObject["data"]?.fromJSONValue(this),
            data_base64 = jsonObject["data_base64"]?.asString,
        )
    }

    inline fun <reified T : Any, reified E : Any> JSONConfig.cloudEventExtToJSON(event: CloudEventExt<T, E>?):
            JSONObject? = event?.let {
        JSONObject.build {
            add("id", event.id.toString())
            add("source", event.source.toString())
            add("specversion", event.specversion)
            add("type", event.type)
            event.datacontenttype?.let { add("datacontenttype", it) }
            event.dataschema?.let { add("dataschema", it.toString()) }
            event.subject?.let { add("subject", it) }
            event.time?.let { add("time", it.toString()) }
            val extObject = JSONSerializer.serialize(event.extension, this@cloudEventExtToJSON)
            if (extObject !is JSONObject)
                throw JSONException("Extension must serialize to an object")
            for (entry in extObject.entries)
                add(entry.key, entry.value)
            event.data?.let { add("data", JSONSerializer.serialize(it, this@cloudEventExtToJSON)) }
            event.data_base64?.let { add("data_base64", it) }
        }
    }

    inline fun <reified T : Any, reified E : Any> JSONConfig.addCloudEventExtFromJSON() {
        fromJSON<CloudEventExt<T, E>> {
            cloudEventExtFromJSON(it)
        }
    }

    inline fun <reified T : Any, reified E : Any> JSONConfig.addCloudEventExtToJSON() {
        toJSON<CloudEventExt<T, E>> {
            cloudEventExtToJSON(it)
        }
    }

}
