package com.example.demo.pricing.rules

/**
 * Order in which pricing rules run (lowest first). The order is by kind of rule, not by taste:
 *
 *  1. Calendar rates   - what a night costs on these dates (weekend)
 *  2. Demand rates     - what a night costs given how full the hotel is (occupancy)
 *  3. Stay discounts   - what the whole stay costs (coupon)
 *
 * Rate rules are multipliers, so their relative order doesn't change the result, but they MUST come
 * before any discount: WeekendRule spreads the price evenly over the nights, which breaks if a fixed
 * amount has already been taken off. Fixed-amount coupons go last so the guest always gets the full amount off.
 * The gaps leave room for new rules without renumbering (calendar rates below 20, stay discounts below 90).
 */
object PricingOrder {
    const val WEEKEND = 20
    const val OCCUPANCY = 30
    const val COUPON = 90
}
