package com.example.demo.email

interface EmailProvider {
    fun send(message: EmailMessage)
}
