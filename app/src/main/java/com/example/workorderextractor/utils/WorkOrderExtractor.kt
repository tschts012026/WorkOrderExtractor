package com.example.workorderextractor.utils

import com.example.workorderextractor.data.WorkOrder

object WorkOrderExtractor {
    fun extract(rawText: String): WorkOrder {
        val lines = rawText.split("\n")

        fun findValue(vararg keywords: String): String {
            for (i in lines.indices) {
                val line = lines[i].trim()
                for (kw in keywords) {
                    // Case 1: exact match keyword line, take next line
                    if (line.equals(kw, ignoreCase = true)) {
                        if (i + 1 < lines.size) return lines[i + 1].trim()
                    }
                    // Case 2: colon format "Keyword: value"
                    if (line.contains(kw, ignoreCase = true)) {
                        val colonIdx = line.indexOf(":")
                        if (colonIdx >= 0 && colonIdx < line.length - 1) {
                            return line.substring(colonIdx + 1).trim()
                        }
                    }
                }
            }
            return ""
        }

        val jobId = findValue("Job ID", "JobID", "Job No")
        val grid = findValue("Grid", "Exchange / Grid", "Exchange")
        val serviceNumber = findValue("Service Number", "Service#", "Service No")
        val address = findValue("Address", "A End Address", "Site Address", "Location")
        val appointmentDate = findValue("Appointment Date", "Date", "Appt Date")
        val appointmentTime = findValue("Time", "Appointment Time", "Appt Time")
        val contactRaw = findValue("Contact", "Contact Name", "Customer")
        val status = findValue("Status")
        val pidDesc = findValue("PID Desc", "PID", "Description", "Work Desc")

        var contactName = contactRaw
        var contactPhone = ""
        if (contactRaw.isNotEmpty()) {
            val phoneMatch = Regex("\\d{8}$").find(contactRaw)
            if (phoneMatch != null) {
                contactPhone = phoneMatch.value
                contactName = contactRaw.removeSuffix(phoneMatch).trim().removeSuffix("/").trim()
            }
        }

        return WorkOrder(
            jobId = jobId,
            grid = grid,
            serviceNumber = serviceNumber,
            addressA = address,
            addressB = "",
            appointmentDate = appointmentDate,
            appointmentTime = appointmentTime,
            contactName = contactName,
            contactPhone = contactPhone,
            status = status,
            pidDesc = pidDesc,
            rawText = rawText
        )
    }
}
