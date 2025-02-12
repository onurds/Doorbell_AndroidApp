package com.onurds.doorbell

import java.time.LocalDateTime

data class NotificationLog(
    val timestamp: LocalDateTime,
    val message: String
)
