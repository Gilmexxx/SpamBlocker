package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.BlockedCall
import com.example.data.BlockedCallDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BlockedCallDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: BlockedCallDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.blockedCallDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveBlockedCall() = runBlocking {
        val call = BlockedCall(
            phoneNumber = "+5511999998888",
            timestamp = 1000L,
            reason = "Not in contacts"
        )
        val id = dao.insertBlockedCall(call)
        assertTrue(id > 0)

        val calls = dao.getAllBlockedCalls().first()
        assertEquals(1, calls.size)
        assertEquals("+5511999998888", calls[0].phoneNumber)
        assertEquals("Not in contacts", calls[0].reason)
    }

    @Test
    fun deleteAndClearBlockedCalls() = runBlocking {
        val call1 = BlockedCall(phoneNumber = "111", timestamp = 100L)
        val call2 = BlockedCall(phoneNumber = "222", timestamp = 200L)

        val id1 = dao.insertBlockedCall(call1)
        dao.insertBlockedCall(call2)

        dao.deleteById(id1)
        var calls = dao.getAllBlockedCalls().first()
        assertEquals(1, calls.size)
        assertEquals("222", calls[0].phoneNumber)

        dao.clearAll()
        calls = dao.getAllBlockedCalls().first()
        assertTrue(calls.isEmpty())
    }
}
