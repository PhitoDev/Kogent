package com.ralphdugue.kogent.indexing.adapters

import com.alibaba.fastjson.JSONObject
import com.ralphdugue.kogent.indexing.domain.entities.Document
import com.ralphdugue.kogent.indexing.domain.entities.Index
import com.ralphdugue.kogent.indexing.domain.entities.VectorStoreConfig
import io.milvus.param.MetricType
import io.milvus.v2.client.MilvusClientV2
import io.milvus.v2.service.collection.request.CreateCollectionReq
import io.milvus.v2.service.collection.request.GetLoadStateReq
import io.milvus.v2.service.collection.request.HasCollectionReq
import io.milvus.v2.service.collection.request.LoadCollectionReq
import io.milvus.v2.service.vector.request.DeleteReq
import io.milvus.v2.service.vector.request.InsertReq
import io.milvus.v2.service.vector.request.SearchReq
import io.milvus.v2.service.vector.request.UpsertReq
import io.milvus.v2.service.vector.response.SearchResp.SearchResult

actual fun buildMilvusIndex(config: VectorStoreConfig): Index =
    MilvusIndex(
        config = config,
        client =
            MilvusClientV2(
                io.milvus.v2.client.ConnectConfig
                    .builder()
                    .uri(config.connectionString)
                    .build(),
            ),
    )

class MilvusIndex(
    private val config: VectorStoreConfig,
    private val client: MilvusClientV2,
) : Index {
    override suspend fun indexDocument(document: Document): Boolean =
        try {
            if (!loadCollection(document.sourceName)) {
                if (!createCollection(document)) {
                    throw Exception("Failed to create collection")
                }
            }
            val data = createData(document)
            val response =
                client.insert(
                    InsertReq
                        .builder()
                        .collectionName(document.sourceName)
                        .data(data)
                        .build(),
                )
            response.insertCnt == data.size.toLong()
        } catch (e: Exception) {
            false
        }

    override suspend fun searchIndex(
        sourceName: String,
        query: List<Float>,
        topK: Int,
    ): List<Document> =
        try {
            val response =
                client.search(
                    SearchReq
                        .builder()
                        .collectionName(sourceName)
                        .data(listOf(query))
                        .topK(config.extraParams["topK"]?.toInt() ?: topK)
                        .build(),
                )
            response.searchResults[0].map { createDocument(it) }
        } catch (e: Exception) {
            emptyList()
        }

    override suspend fun deleteDocument(sourceName: String, id: String): Boolean =
        try {
            val response =
                client.delete(
                    DeleteReq
                        .builder()
                        .collectionName(sourceName)
                        .ids(listOf(id))
                        .build(),
                )
            response.deleteCnt == 1L
        } catch (e: Exception) {
            false
        }

    override suspend fun updateDocument(data: Document): Boolean =
        try {
            val response =
                client.upsert(
                    UpsertReq
                        .builder()
                        .collectionName(data.sourceName)
                        .data(createData(data))
                        .build(),
                )
            response.upsertCnt == 1L
        } catch (e: Exception) {
            false
        }

    private fun createData(document: Document): List<JSONObject> {
        val data =
            JSONObject()
                .fluentPut("docId", document.id)
                .fluentPut("sourceName", document.sourceName)
                .fluentPut("sourceType", document.sourceType)
                .fluentPut("text", document.text)
                .fluentPut("vector", document.embedding)
        return when (document) {
            is Document.SQLDocument -> {
                data["dialect"] = document.dialect
                data["schema"] = document.schema
                data["query"] = document.query
                listOf(data)
            }
            is Document.APIDocument -> {
                data["baseUrl"] = document.baseUrl
                data["endpoint"] = document.endpoint
                listOf(data)
            }
        }
    }

    private fun createDocument(searchResult: SearchResult): Document {
        val id = searchResult.entity["docId"] as String
        val sourceName = searchResult.entity["sourceName"] as String
        val sourceType = searchResult.entity["sourceType"] as String
        val text = searchResult.entity["text"] as String
        val embedding = searchResult.entity["embedding"] as List<Float>
        return when (sourceType) {
            "SQL" -> {
                val dialect = searchResult.entity["dialect"] as String
                val schema = searchResult.entity["schema"] as String
                val query = searchResult.entity["query"] as String
                Document.SQLDocument(
                    id = id,
                    sourceName = sourceName,
                    dialect = dialect,
                    schema = schema,
                    query = query,
                    text = text,
                    embedding = embedding,
                )
            }
            "API" -> {
                val baseUrl = searchResult.entity["baseUrl"] as String
                val endpoint = searchResult.entity["endpoint"] as String
                Document.APIDocument(
                    id = id,
                    sourceName = sourceName,
                    baseUrl = baseUrl,
                    endpoint = endpoint,
                    text = text,
                    embedding = embedding,
                )
            }
            else -> throw IllegalArgumentException("Invalid source type")
        }
    }

    private fun loadCollection(collectionName: String): Boolean {
        val hasCollectionRequest =
            HasCollectionReq
                .builder()
                .collectionName(collectionName)
                .build()
        val hasCollection = client.hasCollection(hasCollectionRequest)
        return if (hasCollection) {
            val loadCollectionRequest =
                LoadCollectionReq
                    .builder()
                    .collectionName(collectionName)
                    .build()
            client.loadCollection(loadCollectionRequest)
            hasCollection
        } else {
            false
        }
    }

    private fun createCollection(document: Document): Boolean {
        client.createCollection(
            CreateCollectionReq
                .builder()
                .autoID(true)
                .collectionName(document.sourceName)
                .dimension(document.embedding.size)
                .metricType(MetricType.COSINE.name)
                .build(),
        )
        return client.getLoadState(GetLoadStateReq.builder().collectionName(document.sourceName).build())
    }
}
