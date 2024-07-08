package com.ralphdugue.kogent.dataconnector.adapters.connectors

import com.ralphdugue.kogent.dataconnector.domain.entities.DataSourceRegistry
import com.ralphdugue.kogent.dataconnector.domain.entities.embedding.EmbeddingModel
import com.ralphdugue.kogent.dataconnector.domain.entities.sql.SQLDataConnector
import com.ralphdugue.kogent.indexing.domain.entities.Index

expect fun buildSQLDataConnector(
    embeddingModel: EmbeddingModel,
    index: Index,
    dataSourceRegistry: DataSourceRegistry,
): SQLDataConnector
