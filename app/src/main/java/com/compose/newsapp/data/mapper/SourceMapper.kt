package com.compose.newsapp.data.mapper

import com.compose.newsapp.data.local.entity.SourceEmbeddable
import com.compose.newsapp.data.remote.dto.SourceDto
import com.compose.newsapp.domain.model.Source

// Maps SourceDto (from API) to Source (Domain)
fun SourceDto?.toDomainSource(): Source {
    // Provide default values if DTO fields are unexpectedly null
    val id = this?.id
    val name = this?.name ?: "Unknown Source"
    return Source(id = id, name = name)
}

// Maps Source (Domain) to SourceEmbeddable (for Room)
fun Source.toEntitySourceEmbeddable(): SourceEmbeddable {
    return SourceEmbeddable(
        id = this.id,
        name = this.name
    )
}

// Maps SourceEmbeddable (from Room) to Source (Domain)
fun SourceEmbeddable?.toDomainSource(): Source {
    val name = this?.name ?: "Unknown Source"
    return Source(id = this?.id, name = name)
}