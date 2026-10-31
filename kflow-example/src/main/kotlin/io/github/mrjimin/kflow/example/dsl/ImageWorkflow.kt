package io.github.mrjimin.kflow.example.dsl

import io.github.mrjimin.kflow.KFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.seconds

data class ImageFile(
    val fileName: String,
    val width: Int,
    val height: Int
)

data class ProcessedImage(
    val originalName: String,
    val thumbnailPath: String,
    val compressed: Boolean
)

class ImageService {

    suspend fun validate(image: ImageFile): ImageFile {
        require(image.width > 0 && image.height > 0)
        return image
    }

    suspend fun resize(image: ImageFile): String {
        delay(100)
        return "/images/thumbnails/${image.fileName}"
    }

    suspend fun compress(path: String): ProcessedImage {
        delay(100)

        return ProcessedImage(
            originalName = path.substringAfterLast("/"),
            thumbnailPath = path,
            compressed = true
        )
    }
}

fun main() = runBlocking {
    val service = ImageService()

    val workflow = KFlow.workflow<ImageFile, ProcessedImage>(
        "ImageWorkflow"
    ) {
        val image = step<ImageFile>("이미지 검증") {
            execute {
                service.validate(input)
            }
        }

        val thumbnail = step<String>("썸네일 생성") {
            timeout(5.seconds)

            execute {
                service.resize(image)
            }
        }

        step<ProcessedImage>("이미지 압축") {
            execute {
                service.compress(thumbnail)
            }
        }
    }

    val result = workflow.execute(
        ImageFile("photo.png", 1920, 1080)
    )

    println("이미지 처리 완료: $result")
}
