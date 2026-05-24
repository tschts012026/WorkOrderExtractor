package com.example.workorderextractor.utils

import com.example.workorderextractor.data.WorkOrder

object WorkOrderExtractor {
    fun extract(rawText: String): WorkOrder {
        val lines = rawText.split("\n")

        fun findValue(keyword: String): String {
            for (i in lines.indices) {
                if (lines[i].trim().equals(keyword, ignoreCase = true)) {
                    if (i + 1 < lines.size) {
                        return lines[i + 1].trim()
                    }
                }
            }
            return ""
        }

        val jobId = findValue("Job ID")
        val grid = findValue("Exchange / Grid")
        val serviceNumber = findValue("Service Number")
        val address = findValue("A End Address")
        val appointmentDate = findValue("Appointment Date")
        val appointmentTime = findValue("Appointment Time")
        val contactNameRaw = findValue("Contact Name")
        val contactNoRaw = findValue("Contact No.")
        val contact = listOfNotNull(contactNameRaw, contactNoRaw).joinToString(" / ")
        val status = findValue("Status")
        val pidDesc = findValue("PID Desc")

        return WorkOrder(
            jobId = jobId,
            grid = grid,
            serviceNumber = serviceNumber,
            addressA = address,
            addressB = "",
            appointmentDate = appointmentDate,
            appointmentTime = appointmentTime,
            contactName = contact,
            contactPhone = "",
            status = status,
            pidDesc = pidDesc,
            rawText = rawText
        )
    }
}
