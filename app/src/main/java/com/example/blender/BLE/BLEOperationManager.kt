package com.example.blender.BLE

import android.util.Log
import com.example.blender.models.MessageWithProfileUUID
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.schedule

class BLEOperationManager {

    @Synchronized
    fun enqueueOperation(operation: BLEOperationType) {
        operationQueue.add(operation)
        if (pendingOperation == null) {
            doNextOperation()
        }
    }

    @Synchronized
    fun doNextOperation() {
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
                setTimeout(5000)
                op.peripheral.readCharacteristic(
                    op.serviceUUID,
                    op.characteristicUUID
                )
            }
            is CharacteristicWrite -> {
                setTimeout(5000)
                op.peripheral.writeCharacteristic(
                    op.serviceUUID,
                    op.characteristicUUID,
                    op.value,
                    op.writeType
                )
            }
        }
    }

    @Synchronized
    fun setTimeout(delay: Long) {
        timer.schedule(delay) {
            operationTimeout()
        }
    }

    @Synchronized
    private fun resetTimer() {
        timer.cancel()
        timer = Timer()
    }

    @Synchronized
    fun operationDone() {
        resetTimer()
        loadNextOperation()
    }

    @Synchronized
    private fun operationTimeout() {
        enqueueOperation(pendingOperation!!)
        resetTimer()
        loadNextOperation()
    }

    @Synchronized
    private fun loadNextOperation() {
        pendingOperation = null
        if (operationQueue.isNotEmpty()) {
            doNextOperation()
        }
    }

    @Synchronized
    fun getPendingOperation(): BLEOperationType? {
        return pendingOperation
    }

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