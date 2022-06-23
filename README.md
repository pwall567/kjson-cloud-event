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
Events created using this library may be serialized into JSON, and deserialized back into their internal form, using
the [`kjson`](https://github.com/pwall567/kjson) library, and probably by most other JSON libraries.

The only problems arise with the use of Extension Context Attributes.
The CloudEvents specification requires such attributes to be included in the JSON envelope of the event, and that means
that the JSON library must be provided with custom serialization and deserialization configuration to handle these
events.

The `kotlin-cloud-event` library provides two implementations of the CloudEvents specification:

- [`CloudEvent`](#cloudevent): this handles the simple case without Extension Context Attributes, and events using this
class may be serialized and deserialized without additional configuration.
- [`CloudEventExt`](#cloudeventext): this allows Extension Context Attributes, but this requires custom serialization
and deserialization to be used.
The library includes functions to perform this custom serialization and deserialization using the `kjson` library.

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

As an example, suppose that two addition Extension Context Attributes were required: a security principal id in the form
of a UUID, and a token in the form of an opaque string.
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

Other JSON libraries will probably have similar functionality, but that is outside the scope of these notes.

## Dependency Specification

The latest version of the library is 1.0, and it may be obtained from the Maven Central repository.

### Maven
```xml
    <dependency>
      <groupId>io.kjson</groupId>
      <artifactId>kjson-cloud-event</artifactId>
      <version>1.0</version>
    </dependency>
```
### Gradle
```groovy
    implementation 'io.kjson:kjson-cloud-event:1.0'
```
### Gradle (kts)
```kotlin
    implementation("io.kjson:kjson-cloud-event:1.0")
```

Peter Wall

2022-06-24
