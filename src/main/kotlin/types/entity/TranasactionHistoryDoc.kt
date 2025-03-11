package org.example.types.entity


import java.math.BigDecimal
import java.time.LocalDateTime
import kotlinx.serialization.Serializable
import org.example.common.json.BigDecimalSerializer
import org.example.common.json.LocalDateTimeSerializer
import org.springframework.data.mongodb.core.mapping.Document

@Serializable
@Document(collection = "transfer_history")
data class HistoryDoc (
    val fromUlid : String,
    val toUlid : String,

    @Serializable(with = BigDecimalSerializer::class)
    val value : BigDecimal,
    @Serializable(with = LocalDateTimeSerializer::class)
    val time : LocalDateTime
)