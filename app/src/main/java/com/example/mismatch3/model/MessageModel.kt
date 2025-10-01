package com.example.mismatch3.model

data class MessageModel(
    var message: String? = null,
    var senderId: String? = null,
    var currentTime: String? = null,
    var currentDate: String? = null
) {
    // No-argument constructor (required for Firebase)
    constructor() : this(null, null, null, null)
}
