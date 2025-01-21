package com.example

import com.example.model.ErrorEvents
import com.example.model.SensorData
import com.example.pipeline.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class DataPipelineTest {


    @Test
    fun testCompletePipeline() = runTest {
        val pipeline = DataPipeline()

        val result = pipeline.startPipeline(10).toList()

        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        result.forEach {
            assertTrue(it.location.isNotEmpty())
        }
        pipeline.close()
    }

    @Test
    fun testDataProducerWithDelayAndError() = runTest {
        val dataProducer = DataProducer
        val result = dataProducer.produceData(10).withDelayAndError(50, 0.5, "Simulated Error").catch {
            assertNotNull(it)
            assertEquals(it.message, "Simulated Error")
            throw it
        }.catch {
            assertNotNull(it)
            assertEquals(it.message, "Simulated Error")
        }.toList()
    }


    @Test
    fun testDataFilteringStage() = runTest {

        val flow = kotlinx.coroutines.flow.flow {
            emit("valid data 1")
            emit(ErrorEvents.INVALID_DATA.name)
            emit("valid data 2")
            emit(ErrorEvents.INVALID_DATA.name)
            emit("valid data 3")

        }

        val result = flow.filterData().toList()

        assertEquals(3, result.size)
    }

    @Test
    fun testDataTransformationStage() = runTest {

        val flow = kotlinx.coroutines.flow.flow {
            emit("""{"sensorId":"sensor1","value":25.5,"timestamp":1704067200000}""")
            emit("""invalid json""")
            emit("""{"sensorId":"sensor2","value":26.5,"timestamp":1704067200000}""")

        }

        val result = flow.transformData().toList()
        assertEquals(2, result.size)
    }

    @Test
    fun testEnrichDataStage() = runTest {
        val flow = kotlinx.coroutines.flow.flow {
            emit(SensorData("sensor1", 25.5, 1704067200000))
            emit(SensorData("sensor2", 26.5, 1704067200000))

        }

        val result = flow.enrichData().toList()
        assertEquals(2, result.size)
    }
}
