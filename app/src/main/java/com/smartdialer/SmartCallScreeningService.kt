package com.smartdialer

import android.telecom.Call
import android.telecom.CallScreeningService

class SmartCallScreeningService : CallScreeningService() {
    override fun onScreenCall(details: Call.Details) {
        val number = details.handle?.schemeSpecificPart.orEmpty()
        val silence = RuleStore(this).matches(number)
        respondToCall(
            details,
            CallResponse.Builder()
                .setSilenceCall(silence)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build()
        )
    }
}