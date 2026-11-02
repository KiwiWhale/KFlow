# kiwi-flow-example

**Practical examples demonstrating how to define and execute workflows using kiwi-flow.**

This module provides examples of both DSL and annotation-based APIs, covering basic workflow execution, data transformation, retry policies, and multi-step business processes.

## Examples

### 1. Calculation

A simple example demonstrating how to define a workflow with a single step.

#### DSL

```kotlin
val workflow = KiwiFlow.workflow<Int, Int>("Calculation") {
    step("Multiply by 2") {
        execute { input * 2 }
    }
}

val result = workflow.execute(10)

println("Result: $result") // Result: 20
```

#### Annotation-based

```kotlin
@Workflow(name = "Calculation")
class CalculationWorkflow {

    @Step(name = "Multiply by 2")
    fun multiply(input: Int): Int {
        return input * 2
    }
}

val workflow = AnnotatedWorkflowFactory
    .fromInstance<Int, Int>(CalculationWorkflow())

val result = workflow.execute(10)

println("Result: $result") // Result: 20
```

### 2. Image Processing

Demonstrates a sequential image processing workflow with type-safe data passing and asynchronous execution.

The workflow consists of three steps:

1. **Validate Image:** Validate image dimensions.
2. **Resize Image:** Generate a thumbnail.
3. **Compress Image:** Compress the resized image.

#### DSL

```kotlin
val workflow = KiwiFlow.workflow<ImageFile, ProcessedImage>(
    "ImageWorkflow"
) {
    val validated = step<ImageFile>("Validate Image") {
        execute {
            service.validate(input)
        }
    }

    val resized = step<ResizedImage>("Resize Image") {
        timeout(5.seconds)

        execute {
            service.resize(validated)
        }
    }

    step<ProcessedImage>("Compress Image") {
        timeout(5.seconds)

        execute {
            service.compress(resized)
        }
    }
}
```

#### Annotation-based

```kotlin
@Workflow(name = "ImageWorkflow")
class ImageWorkflow(
    private val service: ImageService
) {

    @Step(order = 1)
    fun validate(image: ImageFile): ImageFile {
        return service.validate(image)
    }

    @Step(order = 2, timeoutMillis = 5000)
    suspend fun resize(image: ImageFile): ResizedImage {
        return service.resize(image)
    }

    @Step(order = 3, timeoutMillis = 5000)
    suspend fun compress(image: ResizedImage): ProcessedImage {
        return service.compress(image)
    }
}
```

This example also demonstrates support for suspend functions and step-level timeouts.

### 3. Order Processing

Demonstrates a multi-step order processing workflow covering validation, payment calculation, and shipment preparation.

The workflow consists of four steps:

1. **Validate Order:** Validate order details.
2. **Calculate Payment:** Calculate the total payment amount.
3. **Prepare Shipment:** Generate shipment information.
4. **Complete Order:** Assemble the final order result.

#### DSL

```kotlin
val workflow = KiwiFlow.workflow<Order, OrderResult>(
    "OrderWorkflow"
) {
    val validated = step<Order>("Validate Order") {
        execute {
            service.validate(input)
        }
    }

    val payment = step<Payment>("Calculate Payment") {
        execute {
            service.calculate(validated)
        }
    }

    val shipment = step<Shipment>("Prepare Shipment") {
        execute {
            service.ship(validated)
        }
    }

    step<OrderResult>("Complete Order") {
        execute {
            OrderResult(
                orderId = validated.id,
                payment = payment,
                shipment = shipment
            )
        }
    }
}
```

#### Annotation-based

```kotlin
@Workflow(name = "OrderWorkflow")
class OrderWorkflow(
    private val service: OrderService
) {

    @Step(order = 1)
    fun validate(order: Order): Order {
        return service.validate(order)
    }

    @Step(order = 2)
    fun calculate(order: Order): Payment {
        return service.calculate(order)
    }

    @Step(order = 3)
    fun ship(payment: Payment): Shipment {
        return service.ship(payment)
    }

    @Step(order = 4)
    fun complete(shipment: Shipment): OrderResult {
        val payment = Payment(
            orderId = shipment.orderId,
            amount = 100_000L
        )

        return OrderResult(
            orderId = shipment.orderId,
            payment = payment,
            shipment = shipment
        )
    }
}
```

### 4. Payment with Retry

Demonstrates configurable retry policies with exponential backoff by simulating temporary payment failures before a successful transaction.

#### DSL

```kotlin
val workflow = KiwiFlow.workflow<PaymentRequest, PaymentReceipt>(
    "PaymentWorkflow"
) {
    step<PaymentReceipt>("Process Payment") {
        retry(
            RetryPolicy(
                maxAttempts = 3,
                initialDelay = 100.milliseconds
            )
        )

        execute {
            gateway.charge(input)
        }
    }
}
```

#### Annotation-based

```kotlin
@Workflow(name = "PaymentWorkflow")
class PaymentWorkflow(
    private val gateway: PaymentGateway
) {

    @Step(
        order = 1,
        retry = Retry(
            maxAttempts = 3,
            initialDelayMillis = 100,
            maxDelayMillis = 1000,
            multiplier = 2.0,
            jitter = RetryJitter.NONE
        )
    )
    fun charge(request: PaymentRequest): PaymentReceipt {
        println("Payment request: ${request.orderId}")

        return gateway.charge(request)
    }
}
```

This example demonstrates configurable retry policies with exponential backoff and jitter.

### 5. User Registration

Demonstrates a user registration workflow covering input validation, duplicate email checks, and account creation.

The workflow consists of four steps:

1. **Validate Input:** Validate email, password, and nickname.
2. **Check Email Duplication:** Ensure the email address is not already registered.
3. **Create User:** Create a new user account.
4. **Send Welcome Email:** Send a welcome email to the newly registered user.

#### DSL

```kotlin
val workflow = KiwiFlow.workflow<RegistrationRequest, User>(
    "RegistrationWorkflow"
) {
    val validated = step<RegistrationRequest>("Validate Input") {
        execute {
            service.validate(input)
        }
    }

    val checked = step<RegistrationRequest>("Check Email Duplication") {
        execute {
            service.checkDuplicate(validated)
        }
    }

    val user = step<User>("Create User") {
        execute {
            service.create(checked)
        }
    }

    step<User>("Send Welcome Email") {
        execute {
            service.sendWelcomeEmail(user)
        }
    }
}
```

#### Annotation-based

```kotlin
@Workflow(name = "RegistrationWorkflow")
class RegistrationWorkflow(
    private val service: UserService = UserService()
) {

    @Step(order = 1, name = "Validate Input")
    fun validate(request: RegistrationRequest): RegistrationRequest {
        return service.validate(request)
    }

    @Step(order = 2, name = "Check Email Duplication")
    fun checkDuplicate(request: RegistrationRequest): RegistrationRequest {
        return service.checkDuplicate(request)
    }

    @Step(order = 3, name = "Create User")
    fun createUser(request: RegistrationRequest): User {
        return service.create(request)
    }

    @Step(order = 4, name = "Send Welcome Email")
    fun sendWelcomeEmail(user: User): User {
        return service.sendWelcomeEmail(user)
    }
}
```