package io.github.mrjimin.kflow.example.annotations

import io.github.mrjimin.kflow.annotation.Step
import io.github.mrjimin.kflow.annotation.WorkflowSpec
import io.github.mrjimin.kflow.annotation.factory.AnnotatedWorkflowFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

data class ImageFile(
    val fileName: String,
    val width: Int,
    val height: Int
)

data class ResizedImage(
    val fileName: String,
    val path: String
)

data class ProcessedImage(
    val fileName: String,
    val thumbnailPath: String,
    val compressed: Boolean
)

class ImageService {

    suspend fun resize(image: ImageFile): ResizedImage {
        delay(100)

        return ResizedImage(
            fileName = image.fileName,
            path = "/images/thumbnails/${image.fileName}"
        )
    }

    suspend fun compress(image: ResizedImage): ProcessedImage {
        delay(100)

        return ProcessedImage(
            fileName = image.fileName,
            thumbnailPath = image.path,
            compressed = true
        )
    }
}

@WorkflowSpec(name = "ImageWorkflow")
class ImageWorkflow(
    private val service: ImageService
) {

    @Step(order = 1)
    fun validate(image: ImageFile): ImageFile {
        require(image.width > 0)
        require(image.height > 0)

        return image
    }

    @Step(
        order = 2,
        timeoutMillis = 5000
    )
    suspend fun resize(image: ImageFile): ResizedImage {
        return service.resize(image)
    }

    @Step(
        order = 3,
        timeoutMillis = 5000
    )
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
        ImageFile(
            fileName = "photo.png",
            width = 1920,
            height = 1080
        )
    )

    println("이미지 처리 완료: $result")
}
