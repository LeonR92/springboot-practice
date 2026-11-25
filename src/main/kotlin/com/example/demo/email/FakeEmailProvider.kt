package com.example.demo.email

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class FakeEmailProvider : EmailProvider {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun send(message: EmailMessage) {
        log.info(
            "FAKE EMAIL to={} subject='{}' attachments={}",
            message.to,
            message.subject,
            message.attachments.map { "${it.filename} (${it.content.size} bytes)" },
        )
    }
}
