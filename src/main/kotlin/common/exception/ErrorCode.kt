package org.example.common.exception

enum class ErrorCode(
    override val code : Int,
    override var message : String
) : CodeInterface {
    FAILED_TO_FIND_TOPIC_HANDLER(-100, "Failed to find topic handler"),
    FAILED_TO_CREATE_MONGO_CONFIG(-101, "Failed to create mongo config")
}