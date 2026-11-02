package io.github.mrjimin.kflow.example.annotations

import io.github.mrjimin.kflow.annotation.Step
import io.github.mrjimin.kflow.annotation.Workflow
import io.github.mrjimin.kflow.annotation.factory.AnnotatedWorkflowFactory
import io.github.mrjimin.kflow.example.common.dto.RegistrationRequest
import io.github.mrjimin.kflow.example.common.dto.User
import kotlinx.coroutines.runBlocking

class UserService {

    fun validate(request: RegistrationRequest): RegistrationRequest {
        require(request.email.contains("@"))
        require(request.password.length >= 8)
        require(request.nickname.isNotBlank())

        println("회원 정보 검증 완료")

        return request
    }

    fun checkDuplicate(request: RegistrationRequest): RegistrationRequest {
        require(request.email != "existing@example.com") {
            "이미 가입된 이메일입니다."
        }

        println("이메일 중복 확인 완료")

        return request
    }

    fun create(request: RegistrationRequest): User {
        println("회원 생성 완료")

        return User(
            id = 1L,
            email = request.email,
            nickname = request.nickname
        )
    }

    fun sendWelcomeEmail(user: User): User {
        println("${user.email}로 환영 메일 발송")

        return user
    }
}

@Workflow(name = "RegistrationWorkflow")
class RegistrationWorkflow(
    private val service: UserService = UserService()
) {

    @Step(order = 1, name = "입력 검증")
    fun validate(request: RegistrationRequest): RegistrationRequest {
        return service.validate(request)
    }

    @Step(order = 2, name = "이메일 중복 확인")
    fun checkDuplicate(request: RegistrationRequest): RegistrationRequest {
        return service.checkDuplicate(request)
    }

    @Step(order = 3, name = "회원 생성")
    fun createUser(request: RegistrationRequest): User {
        return service.create(request)
    }

    @Step(order = 4, name = "환영 메일 발송")
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

    println("가입 완료: $user")
}
