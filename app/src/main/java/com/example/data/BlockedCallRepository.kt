package com.example.data

import kotlinx.coroutines.flow.Flow

class BlockedCallRepository(private val dao: BlockedCallDao) {
    val allBlockedCalls: Flow<List<BlockedCall>> = dao.getAllBlockedCalls()

    suspend fun insert(call: BlockedCall): Long = dao.insertBlockedCall(call)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun clearAll() = dao.clearAll()
}
