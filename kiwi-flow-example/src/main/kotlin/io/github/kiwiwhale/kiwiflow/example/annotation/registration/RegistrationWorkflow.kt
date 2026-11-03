package io.github.kiwiwhale.kiwiflow.example.annotation.registration

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.annotation.factory.AnnotatedWorkflowFactory
import io.github.kiwiwhale.kiwiflow.example.common.dto.RegistrationRequest
import io.github.kiwiwhale.kiwiflow.example.common.dto.User
import kotlinx.coroutines.runBlocking

class UserService {

    fun validate(request: RegistrationRequest): RegistrationRequest {
        require(request.email.contains("@"))
        require(request.password.length >= 8)
        require(request.nickname.isNotBlank())

        println("User information validation completed")

        return request
    }

    fun checkDuplicate(request: RegistrationRequest): RegistrationRequest {
        require(request.email != "existing@example.com") {
            "This email address is already registered."
        }

        println("Email duplication check completed")

        return request
    }

    fun create(request: RegistrationRequest): User {
        println("User creation completed")

        return User(
            id = 1L,
            email = request.email,
            nickname = request.nickname
        )
    }

    fun sendWelcomeEmail(user: User): User {
        println("Welcome email sent to ${user.email}")

        return user
    }
}

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

fun main() = runBlocking {
    val workflow = AnnotatedWorkflowFactory
        .fromInstance<RegistrationRequest, User>(
            RegistrationWorkflow()
        )

    val user = workflow.execute(
        RegistrationRequest(
            email = "hello@example.com",
            password = "password123",
            nickname = "KFlow"
        )
    )

    println("Registration completed: $user")
}
