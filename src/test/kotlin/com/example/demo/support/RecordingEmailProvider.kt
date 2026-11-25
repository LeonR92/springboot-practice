package com.example.demo.support

import com.example.demo.email.EmailMessage
import com.example.demo.email.EmailProvider
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import java.util.concurrent.CopyOnWriteArrayList

class RecordingEmailProvider : EmailProvider {
    val sent = CopyOnWriteArrayList<EmailMessage>()

    override fun send(message: EmailMessage) {
        sent += message
    }
}

@TestConfiguration
class EmailTestConfig {
    @Bean
    @Primary
    fun recordingEmailProvider() = RecordingEmailProvider()
}
