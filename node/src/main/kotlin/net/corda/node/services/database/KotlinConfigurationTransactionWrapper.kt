package net.corda.node.services.database

import io.requery.EntityCache
import io.requery.TransactionIsolation
import io.requery.TransactionListener
import io.requery.cache.WeakEntityCache
import io.requery.meta.EntityModel
import io.requery.sql.*
import io.requery.util.function.Function
import io.requery.util.function.Supplier
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection
import java.util.*
import java.util.concurrent.Executor
import javax.sql.DataSource

/**
 * Requery KotlinConfiguration wrapper class to enable us to pass in an existing database connection and
 * associated transaction context.
 */
class KotlinConfigurationTransactionWrapper(private val model: EntityModel,
                                            dataSource: DataSource,
                                            private val mapping: Mapping? = null,
                                            private val platform: Platform? = null,
                                            private val cache: EntityCache = WeakEntityCache(),
                                            private val useDefaultLogging: Boolean = false,
                                            private val statementCacheSize: Int = 0,
                                            private val batchUpdateSize: Int = 64,
                                            private val quoteTableNames: Boolean = false,
                                            private val quoteColumnNames: Boolean = false,
                                            private val tableTransformer: Function<String, String>? = null,
                                            private val columnTransformer: Function<String, String>? = null,
                                            private val transactionMode: TransactionMode = TransactionMode.AUTO,
                                            private val transactionIsolation: TransactionIsolation? = null,
                                            private val statementListeners: Set<StatementListener> = LinkedHashSet(),
                                            private val entityStateListeners: Set<EntityStateListener<Any>> = LinkedHashSet(),
                                            private val transactionListeners: Set<Supplier<TransactionListener>> = LinkedHashSet(),
                                            private val writeExecutor: Executor? = null) : Configuration {

    private val connectionProvider = CordaDataSourceConnectionProvider(dataSource)

    override fun getBatchUpdateSize(): Int {
        return batchUpdateSize
    }

    override fun getConnectionProvider(): ConnectionProvider? {
        return connectionProvider
    }

    override fun getCache(): EntityCache? {
        return cache
    }

    override fun getEntityStateListeners(): Set<EntityStateListener<Any>> {
        return entityStateListeners
    }

    override fun getMapping(): Mapping? {
        return mapping
    }

    override fun getModel(): EntityModel {
        return model
    }

    override fun getPlatform(): Platform? {
        return platform
    }

    override fun getQuoteTableNames(): Boolean {
        return quoteTableNames
    }

    override fun getQuoteColumnNames(): Boolean {
        return quoteColumnNames
    }

    override fun getTableTransformer(): Function<String, String>? {
        return tableTransformer
    }

    override fun getColumnTransformer(): Function<String, String>? {
        return columnTransformer
    }

    override fun getStatementCacheSize(): Int {
        return statementCacheSize
    }

    override fun getStatementListeners(): Set<StatementListener>? {
        return statementListeners
    }

    override fun getTransactionMode(): TransactionMode? {
        return transactionMode
    }

    override fun getTransactionIsolation(): TransactionIsolation? {
        return transactionIsolation
    }

    override fun getTransactionListenerFactories(): Set<Supplier<TransactionListener>>? {
        return transactionListeners
    }

    override fun getUseDefaultLogging(): Boolean {
        return useDefaultLogging
    }

    override fun getWriteExecutor(): Executor? {
        return writeExecutor
    }

    class CordaDataSourceConnectionProvider(val dataSource: DataSource) : ConnectionProvider {
        override fun getConnection(): Connection {
            val tx = TransactionManager.manager.currentOrNull()
            return CordaConnection(
                    tx?.connection ?:
                    TransactionManager.manager.newTransaction(Connection.TRANSACTION_REPEATABLE_READ).connection
            )
        }
    }

    class CordaConnection(connection: Connection) : Connection by connection {
        override fun close() {
            // Prevent Requery closing the connection
        }
    }
}