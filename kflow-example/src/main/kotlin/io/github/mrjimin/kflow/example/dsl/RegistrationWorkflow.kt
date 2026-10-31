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

    fun validate(request: RegistrationRequest) {
        require(request.email.contains("@"))
        require(request.password.length >= 8)
        require(request.nickname.isNotBlank())
    }

    fun checkDuplicate(email: String) {
        // 실제 환경에서는 데이터베이스 조회
        require(email != "existing@example.com") {
            "이미 가입된 이메일입니다."
        }
    }

    fun create(request: RegistrationRequest): User {
        return User(
            id = 1L,
            email = request.email,
            nickname = request.nickname
        )
    }

    fun sendWelcomeEmail(user: User) {
        println("${user.email}로 환영 메일 발송")
    }
}

fun main() = runBlocking {
    val service = UserService()

    val workflow = KFlow.workflow<RegistrationRequest, User>(
        "RegistrationWorkflow"
    ) {
        step<Unit>("입력 검증") {
            execute {
                service.validate(input)
            }
        }

        step<Unit>("이메일 중복 확인") {
            execute {
                service.checkDuplicate(input.email)
            }
        }

        val user = step<User>("회원 생성") {
            execute {
                service.create(input)
            }
        }

        step<User>("환영 메일 발송") {
            execute {
                service.sendWelcomeEmail(user)
                user
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
