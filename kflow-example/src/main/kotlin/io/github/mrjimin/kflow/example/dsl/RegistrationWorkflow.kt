package io.github.mrjimin.kflow.example.dsl

import io.github.mrjimin.kflow.KFlow
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

fun main() = runBlocking {
    val service = UserService()

    val workflow = KFlow.workflow<RegistrationRequest, User>(
        "RegistrationWorkflow"
    ) {
        val validated = step<RegistrationRequest>("입력 검증") {
            execute {
                service.validate(input)
            }
        }

        val checked = step<RegistrationRequest>("이메일 중복 확인") {
            execute {
                service.checkDuplicate(validated)
            }
        }

        val user = step<User>("회원 생성") {
            execute {
                service.create(checked)
            }
        }

        step<User>("환영 메일 발송") {
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

    println("가입 완료: $user")
}