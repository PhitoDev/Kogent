package com.ralphdugue.kogent.dataconnector.adapters.connectors

import com.ralphdugue.kogent.dataconnector.domain.entities.DataSource
import com.ralphdugue.kogent.dataconnector.domain.entities.api.APIDataConnector
import com.ralphdugue.kogent.dataconnector.domain.entities.api.KogentAPIResponse
import com.ralphdugue.kogent.dataconnector.domain.entities.embedding.EmbeddingModel
import com.ralphdugue.kogent.indexing.domain.entities.Document
import com.ralphdugue.kogent.retrieval.domain.entities.DataSourceRegistry
import io.ktor.client.HttpClient
import org.koin.core.annotation.Single

@Single(binds = [APIDataConnector::class])
class KogentAPIDataConnector(
    private val client: HttpClient,
    private val embeddingModel: EmbeddingModel,
    private val dataSourceRegistry: DataSourceRegistry,
) : APIDataConnector {
    override suspend fun indexData(dataSource: DataSource): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun fetchData(dataSource: DataSource): KogentAPIResponse<String> {
        TODO("Not yet implemented")
    }

    override suspend fun postData(
        dataSource: DataSource,
        data: String,
    ): KogentAPIResponse<String> {
        TODO("Not yet implemented")
    }

    private suspend fun createDocument(
        data: KogentAPIResponse<String>,
        source: DataSource,
    ): Document {
        TODO("Not yet implemented")
    }
}
