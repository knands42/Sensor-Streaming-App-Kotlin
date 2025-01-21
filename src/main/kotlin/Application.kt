package com.example

import com.example.pipeline.DataPipeline
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

const val PRODUCER_AMOUNT = 100

fun main(args: Array<String>) = runBlocking {
    val log = LoggerFactory.getLogger("Main")
    val pipeline = DataPipeline()
    try {
        pipeline.startPipeline(PRODUCER_AMOUNT).collect {
            log.info("Output: $it")
        }
        log.info("Pipeline Finished successfully")
    } catch (e: Exception){
        log.error("Pipeline execution error: ${e.message}")
    }
    finally {
        pipeline.close()
    }
}
