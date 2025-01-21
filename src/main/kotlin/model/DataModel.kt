package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class SensorData(val sensorId: String, val value: Double, val timestamp: Long)

@Serializable
data class EnrichedSensorData(val sensorData: SensorData, val location: String)
