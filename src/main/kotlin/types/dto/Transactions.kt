package org.example.types.dto

import java.math.BigDecimal
import java.time.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.common.json.BigDecimalSerializer
import org.example.common.json.LocalDateTimeSerializer

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
)