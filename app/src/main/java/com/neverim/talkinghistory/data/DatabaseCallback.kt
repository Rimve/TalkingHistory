package com.neverim.talkinghistory.data

import com.neverim.talkinghistory.data.models.IDatabaseResponse

interface DatabaseCallback {
    fun onResponse(response: IDatabaseResponse)
}