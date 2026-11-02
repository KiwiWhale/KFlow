package io.github.kiwiwhale.kiwiflow.example.dsl.image

import io.github.kiwiwhale.kiwiflow.core.KiwiFlow
import io.github.kiwiwhale.kiwiflow.example.common.dto.ImageFile
import io.github.kiwiwhale.kiwiflow.example.common.dto.ProcessedImage
import io.github.kiwiwhale.kiwiflow.example.common.dto.ResizedImage
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

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

    val result = workflow.execute(
        ImageFile("photo.png", 1920, 1080)
    )

    println("Image processing completed: $result")
}
