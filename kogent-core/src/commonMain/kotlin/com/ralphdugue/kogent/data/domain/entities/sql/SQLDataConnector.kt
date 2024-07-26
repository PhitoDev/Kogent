package com.ralphdugue.kogent.data.domain.entities.sql

import com.ralphdugue.kogent.data.domain.entities.DataConnector
import com.ralphdugue.kogent.data.domain.entities.SQLDataSource

/**
 * This is the interface for all SQLDataConnectors classes to inherit from.
 * SQLDataConnectors are classes that are responsible for sending queries to an SQL data source.
 */
interface SQLDataConnector : DataConnector {
    /**
     * This function fetches data from an SQL data source.
     * @param dataSource The data source to fetch data from.
     * @return The result of the query.
     */
    suspend fun readQuery(dataSource: SQLDataSource): Result<QueryResult.TableQuery>

    /**
     * This function updates the data of an SQL data source.
     * @param dataSource The data source to update the data of.
     * @param query The query to update the data.
     * @return The result of the query.
     */
    suspend fun writeQuery(
        dataSource: SQLDataSource,
        query: String,
    ): Result<Unit>
}
