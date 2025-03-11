package org.example.consumer.repository

import org.example.config.MongoTableCollector
import org.example.types.dto.TransactionsMessage
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Repository

@Repository
class TransactionHandlerRepository(
    private val template: HashMap<String, MongoTemplate>,
) {


    fun saveTransactionHistory(eventData : TransactionsMessage) {

    }

    private fun template(c: MongoTableCollector) : MongoTemplate {
        val table = template[c.table]

        table?.let { return it }

       TODO("나중에 해")
    }
}
