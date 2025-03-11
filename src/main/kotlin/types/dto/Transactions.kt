package org.example.types.dto

import java.math.BigDecimal
import java.time.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.common.json.BigDecimalSerializer
import org.example.common.json.LocalDateTimeSerializer
import org.example.types.entity.HistoryDoc
import org.springframework.data.mongodb.core.mapping.Document

@Serializable
data class TransactionsMessage(
    @SerialName("fromUlid")
    val fromUlid: String,
    @SerialName("fromName")
    val fromName: String,
    @SerialName("fromAccountID")
    val fromAccountID: String,

    @SerialName("toUlid")
    val toUlid: String,
    @SerialName("toName")
    val toName: String,
    @SerialName("toAccountID")
    val toAccountID: String,

    @SerialName("value")
    @Serializable(with = BigDecimalSerializer::class)
    val value: BigDecimal,

    @SerialName("time")
    @Serializable(with = LocalDateTimeSerializer::class)
    val time: LocalDateTime
) {
    fun toEntity() : HistoryDoc  = HistoryDoc(
        fromUlid = fromUlid,
        toUlid = toUlid,
        value = value,
        time = time
    )

    fun toCacheDTO() : History = History(
        fromUlid = fromUlid,
        toUlid = toUlid,
        value = value,
        fromUser = fromName,
        toUser = toName,
        time = time
    )
}

@Serializable
data class History(
    val fromUlid : String,
    val fromUser : String,
    val toUlid : String,
    val toUser : String,

    @Serializable(with = BigDecimalSerializer::class)
    val value : BigDecimal,
    @Serializable(with = LocalDateTimeSerializer::class)
    val time : LocalDateTime
)