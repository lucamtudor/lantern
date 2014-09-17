package com.tudorluca.lantern.utils

/**
 * Created by Tudor Luca on 16/09/14.
 */
public enum class Donation(val sku: String) {
    SMALL : Donation("donation_small")
    MEDIUM : Donation("donation_medium")
    GENEROUS : Donation("donation_generous")
    LARGE : Donation("donation_large")
}

val SKUS: List<String> = Donation.values().map { it.sku }.toList()
