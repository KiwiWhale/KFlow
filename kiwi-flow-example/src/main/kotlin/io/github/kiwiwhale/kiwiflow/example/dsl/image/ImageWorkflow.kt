package io.github.kiwiwhale.kiwiflow.example.dsl.image

import io.github.kiwiwhale.kiwiflow.core.KiwiFlow
import io.github.kiwiwhale.kiwiflow.example.common.dto.ImageFile
import io.github.kiwiwhale.kiwiflow.example.common.dto.ProcessedImage
import io.github.kiwiwhale.kiwiflow.example.common.dto.ResizedImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ImageService {

    fun validate(image: ImageFile): ImageFile {
        require(image.width > 0)
        require(image.height > 0)
        return image
    }

    suspend fun resize(image: ImageFile): ResizedImage {
        delay(100.milliseconds)

        return ResizedImage(
            fileName = image.fileName,
            path = "/images/thumbnails/${image.fileName}"
        )
    }

    suspend fun compress(image: ResizedImage): ProcessedImage {
        delay(100.milliseconds)

        return ProcessedImage(
            fileName = image.fileName,
            thumbnailPath = image.path,
            compressed = true
        )
    }
}

fun main() = runBlocking {
    val service = ImageService()

    val workflow = KiwiFlow.workflow<ImageFile, ProcessedImage>(
        "ImageWorkflow"
    ) {
        val validated = step<ImageFile>("이미지 검증") {
            execute {
                service.validate(input)
            }
        }

        val resized = step<ResizedImage>("이미지 리사이즈") {
            timeout(5.seconds)

            execute {
                service.resize(validated)
            }
        }

        step<ProcessedImage>("이미지 압축") {
            timeout(5.seconds)

            execute {
                service.compress(resized)
            }
        }
    }

    val result = workflow.execute(
        ImageFile("photo.png", 1920, 1080)
    )

    println("이미지 처리 완료: $result")
}
