package io.github.kiwiwhale.kiwiflow.example.annotation.image

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.annotation.factory.AnnotatedWorkflowFactory
import io.github.kiwiwhale.kiwiflow.example.common.dto.ImageFile
import io.github.kiwiwhale.kiwiflow.example.common.dto.ProcessedImage
import io.github.kiwiwhale.kiwiflow.example.common.dto.ResizedImage
import kotlin.time.Duration.Companion.milliseconds
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

fun main() = runBlocking {
    val workflow = AnnotatedWorkflowFactory
        .fromInstance<ImageFile, ProcessedImage>(
            ImageWorkflow(ImageService())
        )

    val result = workflow.execute(
        ImageFile("photo.png", 1920, 1080)
    )

    println("Image processing completed: $result")
}