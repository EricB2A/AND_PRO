package com.example.blender.BLE

import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.schedule

class BLEOperationManager {

    /**
     * Function used to add a new operation to the queue
     */
    @Synchronized
    fun enqueueOperation(operation: BLEOperationType) {
        operationQueue.add(operation)
        if (pendingOperation == null) {
            doNextOperation()
        }
    }

    /**
     * Function used to check if the next operation can be run
     */
    @Synchronized
    private fun doNextOperation() {
        if (pendingOperation != null) {
            return
        }

        val op = operationQueue.poll() ?: run {
            return
        }

        pendingOperation = op

        when (op) {
            is Connect -> {
                setTimeout(1000)
                op.central.connectPeripheral(op.peripheral, op.peripheralCallback)
            }
            is CharacteristicRead -> {
                setTimeout(10000)
                op.peripheral.readCharacteristic(
                    op.serviceUUID,
                    op.characteristicUUID
                )
            }
            is CharacteristicWrite -> {
                setTimeout(10000)
                op.peripheral.writeCharacteristic(
                    op.serviceUUID,
                    op.characteristicUUID,
                    op.value,
                    op.writeType
                )
            }
        }
    }

    /**
     * Function used to specify how much time we allow the operation to run before a timeout
     */
    @Synchronized
    private fun setTimeout(delay: Long) {
        timer.schedule(delay) {
            operationTimeout()
        }
    }

    /**
     * Function resetting the internal timeout timer
     */
    @Synchronized
    private fun resetTimer() {
        timer.cancel()
        timer = Timer()
    }

    /**
     * Function signaling that the operation is finished
     */
    @Synchronized
    fun operationDone() {
        resetTimer()
        loadNextOperation()
    }

    /**
     * Function called when an operation timeout
     */
    @Synchronized
    private fun operationTimeout() {
        enqueueOperation(pendingOperation!!)
        resetTimer()
        loadNextOperation()
    }

    /**
     * Function setting the next operation to be done if any
     */
    @Synchronized
    private fun loadNextOperation() {
        pendingOperation = null
        if (operationQueue.isNotEmpty()) {
            doNextOperation()
        }
    }

    /**
     * Function returning the operation currently being worked on
     */
    @Synchronized
    fun getPendingOperation(): BLEOperationType? {
        return pendingOperation
    }

    /**
     * Function returning if any operations in the queue match the given predicate
     */
    @Synchronized
    fun contains(predicate: (BLEOperationType) -> (Boolean)): Boolean {
        return operationQueue.any { predicate(it) }
    }

    companion object {
        private val TAG: String = BLEOperationManager::class.java.simpleName
        private val operationQueue = ConcurrentLinkedQueue<BLEOperationType>()
        private var pendingOperation: BLEOperationType? = null
        private var timer: Timer = Timer()
    }
}