package com.example

import android.net.Uri
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import com.example.data.AppDatabase
import com.example.data.BlockedCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallBlockingService : CallScreeningService() {
    override fun onScreenCall(callDetails: Call.Details) {
        val handle = callDetails.handle
        if (handle == null || handle.scheme != "tel") {
            // If it's not a telephone call, just allow it
            respondToCall(callDetails, CallResponse.Builder().setDisallowCall(false).build())
            return
        }
        val prefs = com.example.data.PreferencesManager.getInstance(applicationContext)
        prefs.incrementCallsScreened()

        if (!prefs.isProtectionActive) {
            // Protection turned off by user, allow call
            respondToCall(callDetails, CallResponse.Builder().setDisallowCall(false).build())
            return
        }

        val phoneNumber = handle.schemeSpecificPart

        if (isContact(phoneNumber)) {
            // Allow call
            val response = CallResponse.Builder()
                .setDisallowCall(false)
                .setRejectCall(false)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build()
            respondToCall(callDetails, response)
        } else {
            // Block call
            val response = CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(true)
                .setSkipNotification(true)
                .build()
            respondToCall(callDetails, response)

            // Log blocked call to Room Database
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(applicationContext)
                    db.blockedCallDao().insertBlockedCall(
                        BlockedCall(
                            phoneNumber = phoneNumber.ifBlank { "Unknown Number" },
                            timestamp = System.currentTimeMillis(),
                            reason = "Not in contacts"
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun isContact(phoneNumber: String): Boolean {
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
        val projection = arrayOf(ContactsContract.PhoneLookup.NUMBER)
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return true
            }
        }
        return false
    }
}
