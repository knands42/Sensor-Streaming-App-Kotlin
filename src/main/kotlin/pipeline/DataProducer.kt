package com.example.pipeline

import com.example.model.ErrorEvents
import com.example.model.SensorData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.random.Random

object DataProducer {

    val log: Logger = LoggerFactory.getLogger(DataProducer::class.java)
    private val json = Json

    fun produceData(amount: Int =100): Flow<String> = flow {
        repeat(amount) {
            delay(Random.nextLong(100, 1000))

            val sensorData = SensorData(
                "sensor-${Random.nextInt(1, 5)}",
                Random.nextDouble(20.0, 30.0),
                System.currentTimeMillis()
            )
            val data = json.encodeToString(SensorData.serializer(), sensorData)
            if (Random.nextFloat() < 0.1) {
                log.error("Simulating error for: $data")
                emit(ErrorEvents.INVALID_DATA.name)
            } else {
                log.info("Emitting: $data")
                emit(data)
            }
        }
    }
}


fun <T>Flow<T>.withDelayAndError(delay: Long, errorChance: Double, errorMessage: String): Flow<T> = flow {
    collect { value ->
        delay(delay)
        if (Random.nextDouble() < errorChance) {
            DataProducer.log.error("Simulating error $errorMessage")
            throw Exception(errorMessage)
        } else {
            emit(value)
        }
    }
}