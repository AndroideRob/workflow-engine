package com.robkonarski.workflow.core

import java.time.LocalDateTime
import java.util.*

abstract class AbstractModel {

    lateinit var id: UUID

    lateinit var dateCreated: LocalDateTime

    lateinit var dateUpdated: LocalDateTime
}
