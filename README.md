# kiwi-flow

[![Reposilite](https://repo.mrjimin.xyz/api/badge/latest/releases/io/github/kiwiwhale/kiwi-flow-core?color=40c14a&name=Reposilite)](https://repo.mrjimin.xyz/#/releases/io/github/kiwiwhale/kiwi-flow-core)
[![GitHub License](https://img.shields.io/github/license/KiwiWhale/kiwi-flow?style=flat-square)](LICENSE)

**A lightweight and extensible workflow engine for Kotlin.**

kiwi-flow is a lightweight workflow engine for defining and executing workflows in Kotlin applications.

It provides a Kotlin-first DSL and an annotation-based API, allowing developers to define workflows in a clean, readable, and flexible way.

> [!WARNING]
> Early development. APIs and implementation are subject to change.

## Why kiwi-flow?

kiwi-flow aims to provide a simple and flexible way to define and execute workflows in Kotlin applications.

- **Kotlin-first:** Express workflows using an intuitive Kotlin DSL.
- **Annotation-based:** Define workflows using familiar Kotlin annotations.
- **Extensible:** Integrate with frameworks such as Ktor and Spring Boot.
- **Configurable:** Customize workflow execution behavior with retry policies.
- **Modular:** Use only the modules you need.

## Features

- Kotlin-first DSL for workflow definitions
- Annotation-based workflow definitions
- Configurable retry policies
- Modular and extensible architecture
- Ktor and Spring Boot integrations

## Installation

Add the KiwiWhale Maven repository to your `build.gradle.kts`:

```kotlin
repositories {
    maven("https://repo.mrjimin.xyz/releases")
}

dependencies {
    implementation("io.github.kiwiwhale:kiwi-flow-MODULE:1.3.0")
}
```

Replace `MODULE` with the desired release version.

## Quick Start

For more examples, check out [kiwi-flow-example](kiwi-flow-example).

### DSL

Define a workflow using the Kotlin DSL:

```kotlin
val workflow = KiwiFlow.workflow<Int, Int>("Calculation") {
    step("Multiply by 2") {
        execute { input * 2 }
    }
}

val result = workflow.execute(10)

println(result) // 20
```

### Annotation-based

Alternatively, define workflows using annotations:

```kotlin
@Workflow(name = "Calculation")
class CalculationWorkflow {

    @Step(name = "Multiply by 2")
    fun multiply(input: Int): Int = input * 2
}

val workflow = AnnotatedWorkflowFactory
    .fromInstance<Int, Int>(CalculationWorkflow())

val result = workflow.execute(10)

println(result) // 20
```

Both approaches provide different ways to define workflows while using the same execution model.

## Project Structure

| Module                          | Description                           |
|---------------------------------|---------------------------------------|
| `kiwi-flow-core`                | Core workflow implementation and DSL  |
| `kiwi-flow-annotation`          | Annotation-based workflow definitions |
| `kiwi-flow-ktor`                | Ktor integration                      |
| `kiwi-flow-spring-boot-starter` | Spring Boot integration               |
| `kiwi-flow-example`             | Usage examples                        |

## DSL vs Annotation-based API

kiwi-flow provides two ways to define workflows.

| Feature               | DSL                     | Annotation-based     |
|-----------------------|-------------------------|----------------------|
| Workflow definition   | Programmatic            | Declarative          |
| Step definition       | `step { }`              | `@Step`              |
| Workflow declaration  | `KiwiFlow.workflow { }` | `@Workflow`          |
| Execution             | `workflow.execute()`    | `workflow.execute()` |
| Retry configuration   | `retry()`               | `@Retry`             |
| Timeout configuration | `timeout()`             | `timeoutMillis`      |
| Dependency injection  | Explicit                | Constructor-based    |

Choose the DSL when you prefer programmatic workflow composition and explicit control over workflow definitions.

Choose the annotation-based API when you prefer defining workflows through classes and annotations.

## Building

Clone the repository:

```bash
git clone https://github.com/KiwiWhale/kiwi-flow.git
cd kiwi-flow
```

Build the project:

```bash
./gradlew build
```

Run tests:

```bash
./gradlew test
```

## Project Status

kiwi-flow is currently under active development.

APIs and implementation details are subject to change
as the project evolves.

## License

kiwi-flow is licensed under the [Apache License 2.0](LICENSE).