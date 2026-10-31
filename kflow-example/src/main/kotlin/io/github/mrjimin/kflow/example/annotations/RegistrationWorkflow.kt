package io.github.mrjimin.kflow.example.annotations

import io.github.mrjimin.kflow.annotation.Step
import io.github.mrjimin.kflow.annotation.WorkflowSpec
import io.github.mrjimin.kflow.annotation.factory.AnnotatedWorkflowFactory
import kotlinx.coroutines.runBlocking

data class RegistrationRequest(
    val email: String,
    val password: String,
    val nickname: String
)

data class User(
    val id: Long,
    val email: String,
    val nickname: String
)

@WorkflowSpec(name = "RegistrationWorkflow")
class RegistrationWorkflow {

    @Step(order = 1)
    fun validate(request: RegistrationRequest): RegistrationRequest {
        require(request.email.contains("@"))
        require(request.password.length >= 8)
        require(request.nickname.isNotBlank())

        return request
    }

    @Step(order = 2)
    fun checkDuplicate(
        request: RegistrationRequest
    ): RegistrationRequest {
        require(request.email != "existing@example.com") {
            "이미 가입된 이메일입니다."
        }

        return request
    }

    @Step(order = 3)
    fun createUser(request: RegistrationRequest): User {
        println("회원 생성")

        return User(
            id = 1L,
            email = request.email,
            nickname = request.nickname
        )
    }

    @Step(order = 4)
    fun sendWelcomeEmail(user: User): User {
        println("${user.email}로 환영 메일 발송")

        return user
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

    println("가입 완료: $user")
}
