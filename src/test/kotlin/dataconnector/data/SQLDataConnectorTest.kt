package dataconnector.data

import com.ralphdugue.kogent.dataconnector.adapters.connectors.KogentSQLDataConnector
import com.ralphdugue.kogent.dataconnector.domain.entities.api.APIDataSource
import com.ralphdugue.kogent.dataconnector.domain.entities.embedding.EmbeddingModel
import com.ralphdugue.kogent.dataconnector.domain.entities.sql.QueryResult
import com.ralphdugue.kogent.dataconnector.domain.entities.sql.SQLDataConnector
import com.ralphdugue.kogent.dataconnector.domain.entities.sql.SQLDataSource
import com.ralphdugue.kogent.indexing.domain.entities.Index
import common.BaseTest
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockkClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import utils.FakeDatabaseFactory
import utils.RandomsFactory
import java.sql.Connection
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SQLDataConnectorTest : BaseTest() {

    @MockK
    private lateinit var embeddingModel: EmbeddingModel

    @MockK
    private lateinit var index: Index

    private lateinit var subject: SQLDataConnector
    private val dbName: String = "DB_${RandomsFactory.genRandomString()}"
    private val dbUser: String = RandomsFactory.genRandomString()
    private val dbPassword: String = RandomsFactory.genRandomString()
    private val dbTable: String = "table_${RandomsFactory.genRandomString()}"
    private lateinit var dbConnection: Connection

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(mainCoroutineDispatcher)
        coEvery { embeddingModel.getEmbedding(any()) } returns listOf(FloatArray(0))
        coEvery { index.indexDocument(any()) } returns true
        subject = KogentSQLDataConnector(embeddingModel, index)
        dbConnection = FakeDatabaseFactory.createFakeDatabase(dbName, dbUser, dbPassword)
        FakeDatabaseFactory.createTestTable(
            connection = dbConnection,
            tableName = dbTable,
            columns =
                listOf(
                    Pair("id", "INT"),
                    Pair("name", "VARCHAR(255)"),
                    Pair("age", "INT"),
                ),
        )
    }

    @AfterTest
    fun tearDown() {
        dbConnection.close()
        mainCoroutineDispatcher.close()
    }

    /**
     * fetchData tests
     */

    @Test
    fun `fetchData should return a failed result when the data source is not an SQL data source`() =
        runTest {
            val actual =
                subject.fetchData(
                    dataSource = mockkClass(APIDataSource::class),
                )

            val expected =
                QueryResult.TableQuery(
                    tableName = "",
                    columnNames = emptySet(),
                    rows = emptyList(),
                    resultType = QueryResult.ResultType.FAILURE,
                )
            assertEquals(expected.toString().uppercase(), actual.toString().uppercase())
            assertEquals(expected.resultType, actual.resultType)
        }

    @Test
    fun `fetchData should return a failed result when the query is null`() =
        runTest {
            val actual =
                subject.fetchData(
                    dataSource =
                        SQLDataSource(
                            identifier = RandomsFactory.genRandomString(),
                            databaseType = SQLDataSource.DatabaseType.H2,
                            host = "mem",
                            databaseName = dbName,
                            username = dbUser,
                            password = dbPassword,
                        ),
                )

            val expected =
                QueryResult.TableQuery(
                    tableName = "",
                    columnNames = emptySet(),
                    rows = emptyList(),
                    resultType = QueryResult.ResultType.FAILURE,
                )
            assertEquals(expected.toString().uppercase(), actual.toString().uppercase())
            assertEquals(expected.resultType, actual.resultType)
        }

    @Test
    fun `fetchData should return a successful result, with the correct data, when the query is successful`() =
        runTest {
            FakeDatabaseFactory.insertTestData(
                connection = dbConnection,
                tableName = dbTable,
                data =
                    listOf(
                        listOf(1, "Alice", 25),
                        listOf(2, "Bob", 30),
                    ),
            )

            val actual =
                subject.fetchData(
                    dataSource =
                        SQLDataSource(
                            identifier = RandomsFactory.genRandomString(),
                            databaseType = SQLDataSource.DatabaseType.H2,
                            host = "mem",
                            databaseName = dbName,
                            username = dbUser,
                            password = dbPassword,
                            query = "SELECT * FROM $dbTable",
                        ),
                )

            val expected =
                QueryResult.TableQuery(
                    tableName = dbTable,
                    columnNames = setOf("ID", "NAME", "AGE"),
                    rows =
                        listOf(
                            mapOf("id" to 1, "NAME" to "Alice", "AGE" to 25),
                            mapOf("id" to 2, "NAME" to "Bob", "AGE" to 30),
                        ),
                )
            assertEquals(expected.toString().uppercase(), actual.toString().uppercase())
            assertEquals(expected.resultType, actual.resultType)
        }

    /**
     * updateData tests
     */

    @Test
    fun `updateData should return a failed result when the query did not update any rows`() =
        runTest {
            val actual =
                subject.updateData(
                    dataSource =
                        SQLDataSource(
                            identifier = RandomsFactory.genRandomString(),
                            databaseType = SQLDataSource.DatabaseType.H2,
                            host = "mem",
                            databaseName = dbName,
                            username = dbUser,
                            password = dbPassword,
                        ),
                    query = "UPDATE $dbTable SET name = 'Charlie' WHERE id = 3",
                )

            val expected =
                QueryResult.TableQuery(
                    tableName = "",
                    columnNames = emptySet(),
                    rows = emptyList(),
                    resultType = QueryResult.ResultType.FAILURE,
                )
            assertEquals(expected.toString().uppercase(), actual.toString().uppercase())
            assertEquals(expected.resultType, actual.resultType)
        }

    @Test
    fun `updateData should return a successful result when the query updated rows`() =
        runTest {
            FakeDatabaseFactory.insertTestData(
                connection = dbConnection,
                tableName = dbTable,
                data =
                    listOf(
                        listOf(1, "Alice", 25),
                        listOf(2, "Bob", 30),
                    ),
            )

            val actual =
                subject.updateData(
                    dataSource =
                        SQLDataSource(
                            identifier = RandomsFactory.genRandomString(),
                            databaseType = SQLDataSource.DatabaseType.H2,
                            host = "mem",
                            databaseName = dbName,
                            username = dbUser,
                            password = dbPassword,
                        ),
                    query = "UPDATE $dbTable SET name = 'Charlie' WHERE id = 2",
                )

            val expected =
                QueryResult.TableQuery(
                    tableName = "",
                    columnNames = emptySet(),
                    rows = emptyList(),
                    resultType = QueryResult.ResultType.SUCCESS,
                )
            assertEquals(expected.toString().uppercase(), actual.toString().uppercase())
            assertEquals(expected.resultType, actual.resultType)
        }

    /**
     * fetchSchema tests
     */

    @Test
    fun `fetchSchema should return a successful result, with the correct schema, when the query is successful`() =
        runTest {
            val actual =
                subject.fetchSchema(
                    dataSource =
                        SQLDataSource(
                            identifier = RandomsFactory.genRandomString(),
                            databaseType = SQLDataSource.DatabaseType.H2,
                            host = "mem",
                            databaseName = dbName,
                            username = dbUser,
                            password = dbPassword,
                        ),
                )

            val expected =
                QueryResult.SchemaQuery(
                    schema =
                        mapOf(
                            dbTable to
                                mutableMapOf(
                                    "ID" to "INTEGER",
                                    "NAME" to "CHARACTER VARYING",
                                    "AGE" to "INTEGER",
                                ),
                        ),
                )
            assertEquals(expected.toString().uppercase(), actual.toString().uppercase())
            assertEquals(expected.resultType, actual.resultType)
        }
}
