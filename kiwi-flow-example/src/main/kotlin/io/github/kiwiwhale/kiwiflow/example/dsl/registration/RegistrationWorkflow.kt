package io.github.kiwiwhale.kiwiflow.example.dsl.registration

import io.github.kiwiwhale.kiwiflow.core.KiwiFlow
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

fun main() = runBlocking {
    val service = UserService()

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

    val user = workflow.execute(
        RegistrationRequest(
            email = "hello@example.com",
            password = "password123",
            nickname = "KFlow"
        )
    )

    println("Registration completed: $user")
}
