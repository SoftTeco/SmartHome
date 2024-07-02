package com.softteco.template.utils

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random

const val MIN_TEMPERATURE = -20
const val MAX_TEMPERATURE = 45
const val NUM_HISTORY_ELEMENTS = 5000
object ChartUtils {

    fun generateRandomThermometerValue() = Random.nextInt(MIN_TEMPERATURE, MAX_TEMPERATURE).toFloat()

    fun generateRandomHistory(): Map<LocalDateTime, Float> {
        val startDateTime = LocalDateTime.now().minus(NUM_HISTORY_ELEMENTS.toLong() - 1, ChronoUnit.MINUTES)
        val randomHistory = (0 until NUM_HISTORY_ELEMENTS).associate { i ->
            startDateTime.plus(i.toLong(), ChronoUnit.MINUTES) to generateRandomThermometerValue()
        }
        return randomHistory
    }

    fun aggregateByInterval(
        data: Map<LocalDateTime, Float>,
        chronoUnit: ChronoUnit
    ): Map<LocalDateTime, Float> {
        return data.entries.groupBy { entry ->
            when (chronoUnit) {
                ChronoUnit.HOURS -> entry.key.truncatedTo(ChronoUnit.HOURS)
                ChronoUnit.DAYS -> entry.key.toLocalDate().atStartOfDay()
                ChronoUnit.MONTHS -> entry.key.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS)
                else -> throw IllegalArgumentException("Unsupported ChronoUnit: $chronoUnit")
            }
        }.mapValues { (_, entries) ->
            entries.map { it.value }.average().toFloat()
        }
    }
}
