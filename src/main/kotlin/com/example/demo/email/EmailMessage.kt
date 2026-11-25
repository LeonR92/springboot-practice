package com.example.demo.email

class EmailAttachment(
    val filename: String,
    val contentType: String,
    val content: ByteArray
)

class EmailMessage(
    val to: String,
    val subject: String,
    val body: String,
    val attachments: List<EmailAttachment> = emptyList()
)
