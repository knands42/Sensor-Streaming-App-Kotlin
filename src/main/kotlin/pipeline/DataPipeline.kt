package com.example.pipeline

import com.example.model.EnrichedSensorData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

class DataPipeline : CoroutineScope  {

    override val coroutineContext: CoroutineContext = Dispatchers.IO + Job()

    private val log = LoggerFactory.getLogger(DataPipeline::class.java)

    fun startPipeline(amount: Int = 100): Flow<EnrichedSensorData> {
        return DataProducer.produceData(amount)
            .flowOn(Dispatchers.IO) // source is running on IO dispatcher
            .filterData()
            .transformData()
            .enrichData()
            .batchData()
            .debounceData()
            .catch {
                log.error("Error in pipeline: ${it.message}")
                throw it
            }

    }
    fun close() = this.cancel()
}