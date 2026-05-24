package com.example.workorderextractor.utils

import com.example.workorderextractor.data.WorkOrder

object WorkOrderExtractor {
    fun extract(rawText: String): WorkOrder {
        val lines = rawText.split("\n")

        // 更靈活：搵包含關鍵字嘅行，取冒號後面或下一行
        fun findValue(vararg keywords: String): String {
            for (i in lines.indices) {
                val line = lines[i].trim()
                for (kw in keywords) {
                    if (line.equals(kw, ignoreCase = true)) {
                        // 精確匹配關鍵字行 → 取下一行
                        if (i + 1 < lines.size) return lines[i + 1].trim()
                    }
                    // 冒號格式： "Job ID: 12345"
                    if (line.contains(kw, ignoreCase = true) && line.contains(":")) {
                        val parts = line.split(":", limit = 2)
                        if (parts.size == 2) return parts[1].trim()
                    }
                    // 斜杠格式： "Appointment Date:/ 2026/05/21"
                    if (line.contains(kw, ignoreCase = true) && line.contains(":/") || line.contains(kw, ignoreCase = true) && line.contains("：")) {
                        val parts = line.split(Regex("[:/：]"), limit = 2)
                        if (parts.size >= 2) return parts[1].trim()
                    }
                }
            }
            return ""
        }

        val jobId = findValue("Job ID", "JobID", "Job No", "Job#")
        val grid = findValue("Grid", "Exchange / Grid", "Exchange")
        val serviceNumber = findValue("Service Number", "Service#", "Service No")
        val address = findValue("Address", "A End Address", "Site Address", "Location")
        val appointmentDate = findValue("Appointment Date", "Date", "Appt Date")
        val appointmentTime = findValue("Time", "Appointment Time", "Appt Time")
        val contactRaw = findValue("Contact", "Contact Name", "Customer")
        val status = findValue("Status")
        val pidDesc = findValue("PID Desc", "PID", "Description", "Work Desc")

        // 嘗試從 contact raw 分離 name 和 phone
        var contactName = contactRaw
        var contactPhone = ""
        if (contactRaw.isNotEmpty()) {
            // 搵末尾數字（電話號碼）
            val phoneMatch = Regex("(\\d{8})$").find(contactRaw)
            if (phoneMatch != null) {
                contactPhone = phoneMatch.value
                contactName = contactRaw.removeSuffix(phoneMatch).trim().removeSuffix("/").removeSuffix("-").trim()
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
