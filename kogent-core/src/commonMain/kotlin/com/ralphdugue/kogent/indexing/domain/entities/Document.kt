package com.ralphdugue.kogent.indexing.domain.entities

sealed interface Document {
    val id: String
    val sourceType: String
    val sourceName: String
    val text: String
    val embedding: List<Float>

    data class SQLDocument(
        override val id: String,
        override val sourceType: String = SourceType.SQL.name,
        override val sourceName: String,
        val dialect: String,
        val schema: String,
        val query: String,
        override val text: String,
        override val embedding: List<Float>,
    ) : Document {
    }

    data class APIDocument(
        override val id: String,
        override val sourceType: String = SourceType.API.name,
        override val sourceName: String,
        val baseUrl: String,
        val endpoint: String,
        override val text: String,
        override val embedding: List<Float>,
    ) : Document {
    }
}

enum class SourceType {
    SQL,
    API,
}
