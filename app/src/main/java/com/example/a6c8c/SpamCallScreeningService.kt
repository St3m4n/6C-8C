package com.example.a6c8c

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.Call.Details

class SpamCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        BlockedNumbersRepository.load(this)
        val phoneNumber = getPhoneNumber(callDetails)
        
        val shouldBlock = if (phoneNumber != null) {
            val normalizedNumber = phoneNumber.replace(" ", "").replace("-", "")
            
            val block600 = BlockedNumbersRepository.block600 && 
                (normalizedNumber.startsWith("+56600") || normalizedNumber.startsWith("600"))
            
            val block809 = BlockedNumbersRepository.block809 && 
                (normalizedNumber.startsWith("+56809") || normalizedNumber.startsWith("809"))
                
            val isSpecificBlocked = BlockedNumbersRepository.isBlocked(phoneNumber)

            val blockUnknown = BlockedNumbersRepository.blockUnknown && !isContact(this, phoneNumber)
            
            if (block600) {
                BlockedCallHistoryRepository.addCall(this, BlockedCall(phoneNumber, BlockType.TYPE_600, System.currentTimeMillis()))
                true
            } else if (block809) {
                BlockedCallHistoryRepository.addCall(this, BlockedCall(phoneNumber, BlockType.TYPE_809, System.currentTimeMillis()))
                true
            } else if (isSpecificBlocked || blockUnknown) {
                BlockedCallHistoryRepository.addCall(this, BlockedCall(phoneNumber, BlockType.TYPE_OTHER, System.currentTimeMillis()))
                true
            } else {
                false
            }
        } else {
            false
        }

        val response = if (shouldBlock) {
            // Block the call
            CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(false)
                .setSkipNotification(true)
                .build()
        } else {
            // Allow the call
            CallResponse.Builder()
                .setDisallowCall(false)
                .setRejectCall(false)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build()
        }

        respondToCall(callDetails, response)
    }

    private fun getPhoneNumber(callDetails: Call.Details): String? {
        return callDetails.handle?.schemeSpecificPart
    }

    private fun isContact(context: Context, number: String): Boolean {
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return false
    }
}
