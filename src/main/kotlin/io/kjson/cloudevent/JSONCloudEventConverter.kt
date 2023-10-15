/*
 * @(#) JSONCloudEventConverter.kt
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

import io.kjson.JSONConfig
import io.kjson.JSONContext
import io.kjson.JSONKotlinException
import io.kjson.JSONObject

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

    inline fun <reified T : Any, reified E : Any> JSONContext.cloudEventExtFromJSON(json: JSONObject):
            CloudEventExt<T, E> {
        val extObject = JSONObject.build {
            for ((key, value) in json.entries)
                if (key !in standardAttributes)
                    add(key, value)
        }
        return CloudEventExt(
            id = deserializeProperty("id", json),
            source = deserializeProperty("source", json),
            specversion = deserializeProperty("specversion", json),
            type = deserializeProperty("type", json),
            datacontenttype = deserializeProperty("datacontenttype", json),
            dataschema = deserializeProperty("dataschema", json),
            subject = deserializeProperty("subject", json),
            time = deserializeProperty("time", json),
            extension = deserialize(extObject),
            data = deserializeProperty("data", json),
            data_base64 = deserializeProperty("data_base64", json),
        )
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun <T : Any, E : Any> JSONContext.cloudEventExtToJSON(event: CloudEventExt<T, E>?): JSONObject? = event?.let {
        JSONObject.build {
            addProperty("id", event.id)
            addProperty("source", event.source)
            addProperty("specversion", event.specversion)
            addProperty("type", event.type)
            addProperty("datacontenttype", event.datacontenttype)
            addProperty("dataschema", event.dataschema)
            addProperty("subject", event.subject)
            addProperty("time", event.time)
            val extObject = serialize(event.extension)
            if (extObject !is JSONObject)
                throw JSONKotlinException("Extension must serialize to an object", pointer)
            for (entry in extObject.entries)
                add(entry.key, entry.value)
            addProperty("data", event.data)
            addProperty("data_base64", event.data_base64)
        }
    }

    inline fun <reified T : Any, reified E : Any> JSONConfig.addCloudEventExtFromJSON() {
        fromJSONObject<CloudEventExt<T, E>> {
            cloudEventExtFromJSON(it)
        }
    }

    fun <T : Any, E : Any> JSONConfig.addCloudEventExtToJSON() {
        toJSON<CloudEventExt<T, E>> {
            cloudEventExtToJSON(it)
        }
    }

}
