package org.example.common.exception


import org.slf4j.LoggerFactory

class CustomException(
    private val codeInterface: CodeInterface,
    private val additionalMessage: String? = null,
) : RuntimeException(
    if (additionalMessage == null) {
        codeInterface.message
    } else {
        "${codeInterface.message} - $additionalMessage"
    }
) {
    private val logger = LoggerFactory.getLogger(CustomException::class.java)

    init {
        logger.error("Exception created with code: ${codeInterface.code} and message: ${super.message}")
    }

    fun getCodeInterface(): CodeInterface {
        var codeInterface = codeInterface

        if (additionalMessage != null) {
            codeInterface.message += additionalMessage.toString()
        }

        return codeInterface
    }
}

interface CodeInterface {
    val code: Int
    var message: String
}