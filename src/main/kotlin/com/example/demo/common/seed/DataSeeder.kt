package com.example.demo.common.seed

import com.example.demo.hotel.model.Address
import com.example.demo.hotel.model.Amenity
import com.example.demo.hotel.model.AmenityCategory
import com.example.demo.hotel.model.Hotel
import com.example.demo.hotel.model.Room
import com.example.demo.hotel.model.RoomStatus
import com.example.demo.hotel.model.RoomTier
import com.example.demo.hotel.model.RoomType
import com.example.demo.hotel.repository.AmenityRepository
import com.example.demo.hotel.repository.HotelRepository
import com.example.demo.hotel.repository.RoomRepository
import com.example.demo.hotel.repository.RoomTypeRepository
import com.example.demo.payment.model.Payment
import com.example.demo.payment.model.PaymentMethod
import com.example.demo.payment.model.PaymentStatus
import com.example.demo.payment.repository.PaymentRepository
import com.example.demo.pricing.model.CouponCode
import com.example.demo.pricing.model.DiscountType
import com.example.demo.pricing.model.WeekendRate
import com.example.demo.pricing.repository.CouponCodeRepository
import com.example.demo.pricing.repository.WeekendRateRepository
import com.example.demo.reservation.model.Reservation
import com.example.demo.reservation.model.ReservationStatus
import com.example.demo.reservation.repository.ReservationRepository
import com.example.demo.user.model.User
import com.example.demo.user.model.UserRole
import com.example.demo.user.repository.UserRepository
import net.datafaker.Faker
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Random

@Component
@Profile("seed")
class DataSeeder(
    private val users: UserRepository,
    private val amenities: AmenityRepository,
    private val roomTypes: RoomTypeRepository,
    private val hotels: HotelRepository,
    private val rooms: RoomRepository,
    private val coupons: CouponCodeRepository,
    private val weekendRates: WeekendRateRepository,
    private val reservations: ReservationRepository,
    private val payments: PaymentRepository,
    private val passwordEncoder: PasswordEncoder,
) : CommandLineRunner {
    private val log = LoggerFactory.getLogger(javaClass)
    private val random = Random(SEED)
    private val faker = Faker(random)

    @Transactional
    override fun run(vararg args: String) {
        if (users.count() > 0) {
            log.info("Database already has users, skipping seed")
            return
        }
        val seededUsers = seedUsers()
        val amenityList = seedAmenities()
        val types = seedRoomTypes()
        seedCoupons()
        val hotelList = seedHotels(amenityList)
        val roomList = seedRooms(hotelList, types)
        seedWeekendRates(hotelList)
        seedReservationsAndPayments(roomList, seededUsers)
        log.info(
            "Seeded {} users, {} hotels, {} rooms, {} reservations, {} payments",
            users.count(),
            hotels.count(),
            rooms.count(),
            reservations.count(),
            payments.count(),
        )
    }

    private fun seedUsers(): List<User> {
        val hash = passwordEncoder.encode(PASSWORD)!!
        val admin = User(name = "Admin", email = "admin@example.com", hashedPassword = hash, role = UserRole.ADMIN)
        val regular =
            (1..USER_COUNT).map { i ->
                val name = faker.name().fullName()
                User(name = name, email = "user$i@example.com", hashedPassword = hash)
            }
        users.save(admin)
        return users.saveAll(regular)
    }

    private fun seedAmenities(): List<Amenity> =
        amenities.saveAll(
            listOf(
                Amenity(name = "Free WiFi", category = AmenityCategory.GENERAL, isFree = true),
                Amenity(name = "Parking", category = AmenityCategory.TRANSPORT, isFree = false),
                Amenity(name = "Airport Shuttle", category = AmenityCategory.TRANSPORT, isFree = false),
                Amenity(name = "Spa", category = AmenityCategory.WELLNESS, isFree = false),
                Amenity(name = "Pool", category = AmenityCategory.WELLNESS, isFree = true),
                Amenity(name = "Gym", category = AmenityCategory.WELLNESS, isFree = true),
                Amenity(name = "Breakfast Buffet", category = AmenityCategory.FOOD, isFree = false),
                Amenity(name = "Restaurant", category = AmenityCategory.FOOD, isFree = false),
                Amenity(name = "Bar", category = AmenityCategory.FOOD, isFree = false),
                Amenity(name = "Air Conditioning", category = AmenityCategory.GENERAL, isFree = true),
            ),
        )

    private fun seedRoomTypes(): List<RoomType> =
        roomTypes.saveAll(
            listOf(
                RoomType(name = "Standard Single", tier = RoomTier.STANDARD, basePrice = BigDecimal("79.00"), capacity = 1),
                RoomType(name = "Standard Double", tier = RoomTier.STANDARD, basePrice = BigDecimal("99.00"), capacity = 2),
                RoomType(name = "Superior Double", tier = RoomTier.SUPERIOR, basePrice = BigDecimal("139.00"), capacity = 2),
                RoomType(name = "Deluxe King", tier = RoomTier.DELUXE, basePrice = BigDecimal("199.00"), capacity = 3),
                RoomType(name = "Family Suite", tier = RoomTier.SUITE, basePrice = BigDecimal("299.00"), capacity = 4),
            ),
        )

    private fun seedCoupons() {
        val now = Instant.now()
        coupons.saveAll(
            listOf(
                CouponCode(code = "SUMMER10", discountType = DiscountType.PERCENTAGE, discountValue = BigDecimal("10.00"), active = true),
                CouponCode(
                    code = "WELCOME25",
                    discountType = DiscountType.FIXED_AMOUNT,
                    discountValue = BigDecimal("25.00"),
                    active = true,
                ),
                CouponCode(code = "DISABLED", discountType = DiscountType.PERCENTAGE, discountValue = BigDecimal("15.00"), active = false),
                CouponCode(
                    code = "EXPIRED20",
                    discountType = DiscountType.PERCENTAGE,
                    discountValue = BigDecimal("20.00"),
                    active = true,
                    validFrom = now.minus(90, ChronoUnit.DAYS),
                    validUntil = now.minus(30, ChronoUnit.DAYS),
                ),
            ),
        )
    }

    private fun seedHotels(amenityList: List<Amenity>): List<Hotel> =
        hotels.saveAll(
            (1..HOTEL_COUNT).map {
                val address =
                    Address(
                        country = faker.address().country(),
                        city = faker.address().city(),
                        street = faker.address().streetAddress(),
                        latitude = faker.address().latitude().toDouble(),
                        longitude = faker.address().longitude().toDouble(),
                    )
                Hotel(
                    name = "${faker.company().name()} Hotel",
                    address = address,
                    amenities = amenityList.shuffled(random).take(4 + random.nextInt(5)).toMutableSet(),
                )
            },
        )

    private fun seedRooms(
        hotelList: List<Hotel>,
        types: List<RoomType>,
    ): List<Room> =
        rooms.saveAll(
            hotelList.flatMap { hotel ->
                (1..(10 + random.nextInt(11))).map { n ->
                    val floor = 1 + (n - 1) / 5
                    Room(
                        hotel = hotel,
                        roomType = types[random.nextInt(types.size)],
                        roomNumber = "${floor}0${(n - 1) % 5 + 1}",
                        floor = floor,
                        status = if (random.nextInt(10) == 0) RoomStatus.OUT_OF_SERVICE else RoomStatus.ACTIVE,
                    )
                }
            },
        )

    private fun seedWeekendRates(hotelList: List<Hotel>) {
        weekendRates.saveAll(
            hotelList.map {
                WeekendRate(
                    hotel = it,
                    priceMultiplier = BigDecimal("1.${10 + random.nextInt(30)}").setScale(2, RoundingMode.HALF_UP),
                    active = random.nextInt(4) != 0,
                )
            },
        )
    }

    private fun seedReservationsAndPayments(
        roomList: List<Room>,
        seededUsers: List<User>,
    ) {
        val today = LocalDate.now()
        val now = Instant.now()
        val seededReservations = mutableListOf<Reservation>()
        for (room in roomList.filter { it.status == RoomStatus.ACTIVE }) {
            var cursor = today.minusDays(60L)
            repeat(random.nextInt(5)) {
                val checkIn = cursor.plusDays(random.nextLong(1, 15))
                val checkOut = checkIn.plusDays(random.nextLong(1, 8))
                cursor = checkOut
                val user = seededUsers[random.nextInt(seededUsers.size)]
                val status = pickStatus(checkIn, today)
                seededReservations +=
                    Reservation(
                        room = room,
                        guestName = user.name,
                        guestEmail = user.email,
                        checkIn = checkIn,
                        checkOut = checkOut,
                        status = status,
                        lockUntil =
                            if (status ==
                                ReservationStatus.PENDING
                            ) {
                                now.plus(15, ChronoUnit.MINUTES)
                            } else {
                                now.minus(1, ChronoUnit.DAYS)
                            },
                        totalPrice = price(room, checkIn, checkOut),
                        userId = user.id,
                    )
            }
        }
        reservations.saveAll(seededReservations)

        payments.saveAll(
            seededReservations.filter { it.status == ReservationStatus.CONFIRMED }.map { r ->
                val method = PaymentMethod.entries[random.nextInt(PaymentMethod.entries.size)]
                Payment(
                    userId = r.userId!!,
                    reservationId = r.id!!,
                    method = method,
                    amount = r.totalPrice,
                    status = paymentStatus(method),
                    token = if (method == PaymentMethod.INVOICE) null else faker.internet().uuid(),
                    createdAt =
                        r.checkIn
                            .atStartOfDay()
                            .minusDays(random.nextLong(1, 10))
                            .toInstant(java.time.ZoneOffset.UTC),
                )
            },
        )
    }

    private fun pickStatus(
        checkIn: LocalDate,
        today: LocalDate,
    ): ReservationStatus =
        when {
            checkIn.isAfter(today) && random.nextInt(4) == 0 -> ReservationStatus.PENDING
            random.nextInt(8) == 0 -> ReservationStatus.CANCELLED
            random.nextInt(10) == 0 -> ReservationStatus.EXPIRED
            else -> ReservationStatus.CONFIRMED
        }

    private fun paymentStatus(method: PaymentMethod): PaymentStatus =
        when {
            method == PaymentMethod.INVOICE -> PaymentStatus.OUTSTANDING
            random.nextInt(6) == 0 -> PaymentStatus.FAILED
            random.nextInt(6) == 0 -> PaymentStatus.PENDING
            else -> PaymentStatus.SUCCEEDED
        }

    private fun price(
        room: Room,
        checkIn: LocalDate,
        checkOut: LocalDate,
    ): BigDecimal {
        val nights = ChronoUnit.DAYS.between(checkIn, checkOut)
        val weekendNights =
            checkIn.datesUntil(checkOut).filter { it.dayOfWeek == DayOfWeek.FRIDAY || it.dayOfWeek == DayOfWeek.SATURDAY }.count()
        val base = room.roomType.basePrice
        return base
            .multiply(BigDecimal(nights))
            .add(base.multiply(BigDecimal("0.15")).multiply(BigDecimal(weekendNights)))
            .setScale(2, RoundingMode.HALF_UP)
    }

    companion object {
        private const val SEED = 42L
        private const val PASSWORD = "password1"
        private const val USER_COUNT = 20
        private const val HOTEL_COUNT = 5
    }
}
