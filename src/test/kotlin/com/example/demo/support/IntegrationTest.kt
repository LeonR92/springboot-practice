package com.example.demo.support

import com.example.demo.auth.service.JwtService
import com.example.demo.hotel.model.Address
import com.example.demo.hotel.model.Hotel
import com.example.demo.hotel.model.Room
import com.example.demo.hotel.model.RoomTier
import com.example.demo.hotel.model.RoomType
import com.example.demo.hotel.repository.HotelRepository
import com.example.demo.hotel.repository.RoomRepository
import com.example.demo.hotel.repository.RoomTypeRepository
import com.example.demo.user.model.User
import com.example.demo.user.model.UserRole
import com.example.demo.user.repository.UserRepository
import com.jayway.jsonpath.JsonPath
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.nio.file.Files
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(EmailTestConfig::class)
abstract class IntegrationTest {
    @Autowired lateinit var mvc: MockMvc

    @Autowired lateinit var jdbc: JdbcTemplate

    @Autowired lateinit var users: UserRepository

    @Autowired lateinit var hotels: HotelRepository

    @Autowired lateinit var roomTypes: RoomTypeRepository

    @Autowired lateinit var rooms: RoomRepository

    @Autowired lateinit var jwt: JwtService

    @Autowired lateinit var emails: RecordingEmailProvider

    @Value("\${payment.webhook-secret}")
    lateinit var webhookSecret: String

    @BeforeEach
    fun cleanDatabase() {
        jdbc
            .queryForList(
                "SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%' AND name <> 'flyway_schema_history'",
                String::class.java,
            ).forEach { jdbc.execute("DELETE FROM $it") }
        emails.sent.clear()
    }

    protected class Seed(
        val hotelId: Long,
        val roomIds: List<Long>,
    )

    protected fun createUser(role: UserRole = UserRole.USER): User {
        val n = counter.incrementAndGet()
        return users.save(User(name = "User $n", email = "user$n@test.com", hashedPassword = passwordHash, role = role))
    }

    protected fun bearer(user: User) = "Bearer " + jwt.generateToken(requireNotNull(user.id), user.role)

    protected fun adminToken() = bearer(createUser(UserRole.ADMIN))

    protected fun userToken() = bearer(createUser())

    protected fun seedRooms(count: Int = 2): Seed {
        val hotel = hotels.save(Hotel(name = "Hotel", address = Address(country = "DE", city = "Berlin")))
        val type = roomTypes.save(RoomType(name = "Type", tier = RoomTier.STANDARD, basePrice = 100.toBigDecimal(), capacity = 2))
        val ids = (1..count).map { rooms.save(Room(hotel = hotel, roomType = type, roomNumber = "10$it")).id!! }
        return Seed(hotel.id!!, ids)
    }

    protected fun reservationBody(
        roomId: Long,
        checkIn: String = "2026-12-10",
        checkOut: String = "2026-12-12",
        coupon: String? = null,
    ): String {
        val couponField = coupon?.let { ""","couponCode":"$it"""" }.orEmpty()
        return """
            {"roomId":$roomId,"guestName":"Guest","guestEmail":"guest@test.com",
             "checkIn":"$checkIn","checkOut":"$checkOut"$couponField}
            """.trimIndent()
    }

    protected fun reserve(
        roomId: Long,
        token: String,
    ) = create("/api/v1/reservations", reservationBody(roomId), token)

    protected fun paymentBody(
        reservationId: Long,
        method: String,
    ) = """{"reservationId":$reservationId,"method":"$method"}"""

    protected fun webhook(
        method: String,
        token: String,
        secret: String? = webhookSecret,
        success: Boolean = true,
    ) = post(
        "/api/v1/payments/webhook/$method",
        """{"token":"$token","success":$success}""",
        headers = secret?.let { mapOf("X-Webhook-Secret" to it) }.orEmpty(),
    )

    protected fun create(
        url: String,
        body: String,
        token: String,
    ): Long = idOf(post(url, body, token).andExpect { status { isCreated() } }.body())

    protected fun expireHold(reservationId: Long) {
        jdbc.update("UPDATE reservations SET lock_until = ? WHERE id = ?", Instant.now().minusSeconds(60).toEpochMilli(), reservationId)
    }

    protected fun statusOf(
        table: String,
        id: Long,
    ): String = jdbc.queryForObject("SELECT status FROM $table WHERE id = ?", String::class.java, id)!!

    protected fun awaitEmails(count: Int) {
        await atMost Duration.ofSeconds(5) untilAsserted { assertEquals(count, emails.sent.size) }
    }

    protected fun <T> String.json(path: String): T = JsonPath.read(this, path)

    protected fun idOf(body: String): Long = body.json<Number>("$.id").toLong()

    protected fun ResultActionsDsl.body(): String = andReturn().response.contentAsString

    protected fun get(
        url: String,
        token: String? = null,
    ) = mvc.get(url) { token?.let { header("Authorization", it) } }

    protected fun post(
        url: String,
        body: String = "{}",
        token: String? = null,
        headers: Map<String, String> = emptyMap(),
    ) = mvc.post(url) {
        contentType = MediaType.APPLICATION_JSON
        content = body
        token?.let { header("Authorization", it) }
        headers.forEach { (k, v) -> header(k, v) }
    }

    protected fun put(
        url: String,
        body: String,
        token: String? = null,
    ) = mvc.put(url) {
        contentType = MediaType.APPLICATION_JSON
        content = body
        token?.let { header("Authorization", it) }
    }

    protected fun patch(
        url: String,
        body: String,
        token: String? = null,
    ) = mvc.patch(url) {
        contentType = MediaType.APPLICATION_JSON
        content = body
        token?.let { header("Authorization", it) }
    }

    protected fun delete(
        url: String,
        token: String? = null,
    ) = mvc.delete(url) { token?.let { header("Authorization", it) } }

    companion object {
        private val counter = AtomicInteger()

        private val passwordHash by lazy { BCryptPasswordEncoder(4).encode("password1")!! }

        private val dbFile = Files.createTempFile("integration-", ".db").also { it.toFile().deleteOnExit() }

        @JvmStatic
        @DynamicPropertySource
        fun database(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { "jdbc:sqlite:$dbFile?synchronous=OFF" }
        }
    }
}
