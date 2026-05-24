package com.example.workorderextractor.utils

import com.example.workorderextractor.data.WorkOrder

object WorkOrderExtractor {
    fun extract(rawText: String): WorkOrder {
        var jobId = ""
        var serviceNumber = ""
        var addressA = ""
        var addressB = ""
        var appointmentDate = ""
        var appointmentTime = ""
        var contactName = ""
        var contactPhone = ""
        var status = ""
        var pidDesc = ""

        val lines = rawText.lineSequence()
        for (line in lines) {
            val trimmedLine = line.trim()
            when {
                trimmedLine.contains("Job ID", ignoreCase = true) -> jobId = extractAfterColon(trimmedLine)
                trimmedLine.contains("Service Number", ignoreCase = true) -> serviceNumber = extractAfterColon(trimmedLine)
                trimmedLine.contains("A端", ignoreCase = true) && trimmedLine.contains("B端", ignoreCase = true) -> {
                    val parts = trimmedLine.split("；", "；")
                    for (part in parts) {
                        val p = part.trim()
                        if (p.contains("A端", ignoreCase = true)) addressA = extractAfterColon(p)
                        if (p.contains("B端", ignoreCase = true)) addressB = extractAfterColon(p)
                    }
                }
                trimmedLine.contains("Address A端", ignoreCase = true) -> addressA = extractAfterColon(trimmedLine)
                trimmedLine.contains("Address B端", ignoreCase = true) -> addressB = extractAfterColon(trimmedLine)
                trimmedLine.contains("Appointment Date", ignoreCase = true) -> appointmentDate = extractAfterColon(trimmedLine)
                trimmedLine.contains("Appointment Time", ignoreCase = true) -> appointmentTime = extractAfterColon(trimmedLine)
                trimmedLine.contains("Contact", ignoreCase = true) && trimmedLine.contains("Name", ignoreCase = true) -> contactName = extractAfterColon(trimmedLine)
                trimmedLine.contains("Contact No", ignoreCase = true) -> contactPhone = extractAfterColon(trimmedLine)
                trimmedLine.contains("Status", ignoreCase = true) -> status = extractAfterColon(trimmedLine)
                trimmedLine.contains("PID Desc", ignoreCase = true) -> pidDesc = extractAfterColon(trimmedLine)
                trimmedLine.contains("PW ALARM", ignoreCase = true) -> if (pidDesc.isBlank()) pidDesc = trimmedLine
            }
        }

        if (addressA.isBlank() && addressB.isBlank()) {
            val addrLine = lines.find { it.contains("Address", ignoreCase = true) }
            addrLine?.let { addressA = extractAfterColon(it) }
        }

        return WorkOrder(
            jobId = jobId,
            serviceNumber = serviceNumber,
            addressA = addressA,
            addressB = addressB,
            appointmentDate = appointmentDate,
            appointmentTime = appointmentTime,
            contactName = contactName,
            contactPhone = contactPhone,
            status = status,
            pidDesc = pidDesc,
            rawText = rawText
        )
    }

    private fun extractAfterColon(line: String): String {
        val index = line.indexOf(':')
        return if (index != -1) line.substring(index + 1).trim() else ""
    }
}
