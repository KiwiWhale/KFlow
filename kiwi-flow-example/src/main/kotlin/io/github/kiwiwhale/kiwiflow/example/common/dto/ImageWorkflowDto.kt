package io.github.kiwiwhale.kiwiflow.example.common.dto

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
