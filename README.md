# kjson-cloud-event

[![Build Status](https://travis-ci.com/pwall567/kjson-cloud-event.svg?branch=main)](https://app.travis-ci.com/github/pwall567/kjson-cloud-event)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/static/v1?label=Kotlin&message=v1.6.10&color=7f52ff&logo=kotlin&logoColor=7f52ff)](https://github.com/JetBrains/kotlin/releases/tag/v1.6.10)
[![Maven Central](https://img.shields.io/maven-central/v/io.kjson/kjson-cloud-event?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.kjson%22%20AND%20a:%22kjson-cloud-event%22)

Kotlin implementation of CloudEvents specification (v1)

## Background

The [CloudEvents](https://cloudevents.io/) specification is becoming widely accepted as a means of describing events
passed between processes.
The `kjson-cloud-event` library provides a simple implementation of the CloudEvents structure in Kotlin.

Many uses (possibly the vast majority of uses) of CloudEvents will use JSON as the external representation of the event.
The [specification](https://github.com/cloudevents/spec/blob/main/cloudevents/spec.md) sets out how events are to be
serialized, and in most cases, any general-purpose JSON library should be able to convert the events correctly. 

The only problems arise with the use of
[Extension Context Attributes](https://github.com/cloudevents/spec/blob/main/cloudevents/spec.md#extension-context-attributes).
The CloudEvents specification requires such attributes to be included in the JSON envelope of the event, and that means
that the JSON library must be provided with custom serialization and deserialization configuration in order to handle
these events.

The `kjson-cloud-event` library was created with the [`kjson`](https://github.com/pwall567/kjson) library in mind, but
the event classes from this library should also be usable in conjunction with any of the other popular JSON libraries.
The library provides two implementations of the CloudEvents specification:

- [`CloudEvent`](#cloudevent): this handles the simple case without Extension Context Attributes, and events using this
class may be serialized and deserialized without additional configuration.
- [`CloudEventExt`](#cloudeventext): this allows Extension Context Attributes, but this requires custom serialization
and deserialization to be used.
The library includes functions to perform this custom serialization and deserialization when using `kjson`.

## `CloudEvent`

Cloud Events that do not require the use of Extension Context Attributes can use the data class `CloudEvent`.
This is a generic class, parameterized with the type of the data payload, and it has the great virtue of simplicity.

For example:
```kotlin
    val accountOpen = AccountOpen(accountId = newAccountId, name = customerName)
    val cloudEvent = CloudEvent(
        id = UUID.randomUUID(),
        source = eventSource,
        type = "com.example.accounts.open",
        subject = accountOpen.accountId,
        time = OffsetDateTime.now(),
        data = accountOpen,
    )
```
will create an object of type `CloudEvent<AccountOpen>`.

## `CloudEventExt`

Cloud Events that use Extension Context Attributes can use the data class `CloudEventExt`.
This is a generic class, parameterized with both the type of the data payload, and the type of the extensions object.

As an example, suppose that two additional Extension Context Attributes were required: a security principal id in the
form of a UUID, and a token in the form of an opaque string.
To hold these attributes, a data class may be defined:
```kotlin
data class Extension(val principalId: UUID, val token: String)
```

Then, a `CloudEventExt` instance may be created as follows:
```kotlin
    val accountOpen = AccountOpen(accountId = newAccountId, name = customerName)
    val cloudEvent = CloudEventExt(
        id = UUID.randomUUID(),
        source = eventSource,
        type = "com.example.accounts.open",
        subject = accountOpen.accountId,
        time = OffsetDateTime.now(),
        extension = Extension(currentPrincipal, token1),
        data = accountOpen,
    )
```
This will create an object of type `CloudEventExt<AccountOpen, Extension>`, and when serialized with the appropriate
configuration, the `principalId` and `token` attributes will appear in the envelope of the event.

Alternatively, a `Map` may be used to hold the Extension Context Attributes.
The following example creates a `CloudEventExt` with the same external representation as the above:
```kotlin
    val accountOpen = AccountOpen(accountId = newAccountId, name = customerName)
    val cloudEvent = CloudEventExt(
        id = UUID.randomUUID(),
        source = eventSource,
        type = "com.example.accounts.open",
        subject = accountOpen.accountId,
        time = OffsetDateTime.now(),
        extension = mapOf("principalId" to currentPrincipal.toString(), "token" to token1),
        data = accountOpen,
    )
```
The object type in this case will be `CloudEventExt<AccountOpen, Map<String, String>>` (the key of the map must be of
type `String`; the value may be of any type, but if mixed types are used, the deserialization functions will not have
sufficient information to determine the target type of, say, a `UUID`).

## Serialization and Deserialization

The `CloudEvent` objects will serialize correctly using the [`kjson`](https://github.com/pwall567/kjson) library,
and most other JSON libraries should handle the class without problems.

The only difficulties arise when using the `CloudEventExt` class.
Events created using this class will require configuration like the following to be added:
```kotlin
    val config = JSONConfig {
        addCloudEventExtToJSON<AccountOpen, Extension>()
        addCloudEventExtFromJSON<AccountOpen, Extension>()
    }
```
Then, this `config` object must be supplied to the `parseJSON()` or `stringifyJSON()` functions that handle these
objects:
```kotlin
    val event = data.parseJSON<CloudEventExt<AccountOpen, Extension>>(config)
```

The custom deserialization may be combined with [`kjson`](https://github.com/pwall567/kjson) polymorphic
deserialization of the cloud event payload.
See the
[`kjson` Custom Serialization and Deserialization Guide](https://github.com/pwall567/kjson/blob/main/CUSTOM.md#fromjsonpolymorphic)
for more information.

Other JSON libraries will probably have similar functionality; consult the documentation for the library for further
information.

## Dependency Specification

The latest version of the library is 1.4, and it may be obtained from the Maven Central repository.

### Maven
```xml
    <dependency>
      <groupId>io.kjson</groupId>
      <artifactId>kjson-cloud-event</artifactId>
      <version>1.4</version>
    </dependency>
```
### Gradle
```groovy
    implementation 'io.kjson:kjson-cloud-event:1.4'
```
### Gradle (kts)
```kotlin
    implementation("io.kjson:kjson-cloud-event:1.4")
```

Peter Wall

2023-01-04
