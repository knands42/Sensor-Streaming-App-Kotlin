package com.example.pipeline

import com.example.model.EnrichedSensorData
import com.example.model.ErrorEvents
import com.example.model.SensorData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import kotlin.random.Random

object StageProcessors {
    val log = LoggerFactory.getLogger(StageProcessors::class.java)
    val json = Json
}

fun Flow<String>.filterData(): Flow<String> = filter {
    if (it == ErrorEvents.INVALID_DATA.name) {
        StageProcessors.log.warn("invalid data found, skipping ")
        false
    } else {
        true
    }
}.catch {
    StageProcessors.log.error("Error filtering: ${it.message}")
    emit("error")
}

fun Flow<String>.transformData(): Flow<SensorData> = map {
    try {
        withContext(Dispatchers.IO) {
            StageProcessors.log.info("Transforming data: $it")
            StageProcessors.json.decodeFromString(SensorData.serializer(), it)
        }
    } catch (e: Exception) {
        StageProcessors.log.error("Error transforming: ${e.message}")
        null
    }
}.filterNotNull()

@OptIn(ExperimentalCoroutinesApi::class)
fun Flow<SensorData>.enrichData(): Flow<EnrichedSensorData> =
    flatMapMerge { sensorData ->
        flow {
            withContext(Dispatchers.IO) {
                StageProcessors.log.info("Enriching data: $sensorData")
                delay(Random.nextLong(50, 500))

                val location = "Location-${Random.nextInt(1, 10)}"
                emit(EnrichedSensorData(sensorData, location))
            }
        }.catch {
            StageProcessors.log.error("Error enriching: ${it.message}")
            emit(EnrichedSensorData(sensorData, "Unknown Location"))
        }
    }

fun Flow<EnrichedSensorData>.batchData(capacity: Int = 5): Flow<EnrichedSensorData> = buffer(capacity)

@OptIn(FlowPreview::class)
fun Flow<EnrichedSensorData>.debounceData(): Flow<EnrichedSensorData> = debounce(500)
