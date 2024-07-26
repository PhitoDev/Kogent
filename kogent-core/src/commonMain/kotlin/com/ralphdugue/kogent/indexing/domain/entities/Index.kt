package com.ralphdugue.kogent.indexing.domain.entities

/**
 * An Index
 *
 * An Index stores data from a [Document] in a way that makes it easy to search and retrieve.
 * It stores a mapping of embeddings to document fields.
 * @author Ralph Dugue
 */
interface Index {
    /**
     * This function inserts data from a [Document] into the index.
     * @param document The data to index.
     * @return True if the data was successfully indexed, false otherwise.
     */
    suspend fun indexDocument(document: Document): Boolean

    /**
     * This function searches the index for data that matches the query.
     * @param sourceName The name of the collection to search.
     * @param query The query to search for.
     * @param topK The number of results to return.
     * @return The data that matches the query.
     */
    suspend fun searchIndex(
        sourceName: String,
        query: List<Float>,
        topK: Int = 5,
    ): List<Document>

    /**
     * This function deletes data from the index that matches the query.
     * @param sourceName The name of the collection to delete from.
     * @param id The id of the data to delete.
     * @return True if the data was successfully deleted, false otherwise.
     */
    suspend fun deleteDocument(
        sourceName: String,
        id: String,
    ): Boolean

    /**
     * This function updates data in the index that matches the query.
     * @param data The new data to update with.
     * @return True if the data was successfully updated, false otherwise.
     */
    suspend fun updateDocument(data: Document): Boolean
}
