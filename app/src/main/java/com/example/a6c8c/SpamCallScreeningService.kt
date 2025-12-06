package com.example.a6c8c

import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.Call.Details

class SpamCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = getPhoneNumber(callDetails)
        
        val shouldBlock = if (phoneNumber != null) {
            val normalizedNumber = phoneNumber.replace(" ", "").replace("-", "")
            
            val block600 = BlockedNumbersRepository.block600 && 
                (normalizedNumber.startsWith("+56600") || normalizedNumber.startsWith("600"))
            
            val block800 = BlockedNumbersRepository.block800 && 
                (normalizedNumber.startsWith("+56800") || normalizedNumber.startsWith("800"))
                
            val isSpecificBlocked = BlockedNumbersRepository.isBlocked(phoneNumber)
            
            block600 || block800 || isSpecificBlocked
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
}
