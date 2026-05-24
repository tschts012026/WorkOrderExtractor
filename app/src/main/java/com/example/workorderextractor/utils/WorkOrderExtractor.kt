package com.example.workorderextractor.utils

import com.example.workorderextractor.data.WorkOrder

object WorkOrderExtractor {
    fun extract(rawText: String): WorkOrder {
        // 預設空值
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

        // 將文章分成多行，並去除首尾空格
        val lines = rawText.lines().map { it.trim() }

        for (i in lines.indices) {
            val line = lines[i]
            when {
                // Job ID: 下一行就是數字
                line.contains("Job ID", ignoreCase = true) && i + 1 < lines.size -> {
                    jobId = lines[i + 1].trim()
                }
                // Service Number (可能出現在多處，但優先抓取 General Information 下的)
                line.contains("Service Number", ignoreCase = true) && i + 1 < lines.size -> {
                    serviceNumber = lines[i + 1].trim()
                }
                // A End Address
                line.contains("A End Address", ignoreCase = true) && i + 1 < lines.size -> {
                    addressA = lines[i + 1].trim()
                }
                // B End Address
                line.contains("B End Address", ignoreCase = true) && i + 1 < lines.size -> {
                    addressB = lines[i + 1].trim()
                }
                // Appointment Date
                line.contains("Appointment Date", ignoreCase = true) && i + 1 < lines.size -> {
                    appointmentDate = lines[i + 1].trim()
                }
                // Appointment Time
                line.contains("Appointment Time", ignoreCase = true) && i + 1 < lines.size -> {
                    appointmentTime = lines[i + 1].trim()
                }
                // Contact Name (在 "Contact" 區塊內)
                line.contains("Contact Name", ignoreCase = true) && i + 1 < lines.size -> {
                    contactName = lines[i + 1].trim()
                }
                // Contact No.
                line.contains("Contact No.", ignoreCase = true) && i + 1 < lines.size -> {
                    contactPhone = lines[i + 1].trim()
                }
                // Status (在 General Information 中)
                line.contains("Status", ignoreCase = true) && i + 1 < lines.size -> {
                    status = lines[i + 1].trim()
                }
                // PID Desc (有時在下一行)
                line.contains("PID Desc", ignoreCase = true) && i + 1 < lines.size -> {
                    pidDesc = lines[i + 1].trim()
                }
            }
        }

        // 如果還是沒抓到，嘗試用正則表達式直接從全文提取
        if (jobId.isEmpty()) {
            val regex = Regex("Job ID\\s*\\n\\s*(\\d+)", RegexOption.IGNORE_CASE)
            jobId = regex.find(rawText)?.groupValues?.get(1) ?: ""
        }
        if (serviceNumber.isEmpty()) {
            val regex = Regex("Service Number\\s*\\n\\s*(\\d+)", RegexOption.IGNORE_CASE)
            serviceNumber = regex.find(rawText)?.groupValues?.get(1) ?: ""
        }
        if (addressA.isEmpty()) {
            val regex = Regex("A End Address\\s*\\n\\s*(.+?)(?=\\n\\s*B End Address|\\n\\s*\\n|$)", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            addressA = regex.find(rawText)?.groupValues?.get(1)?.trim() ?: ""
        }
        if (addressB.isEmpty()) {
            val regex = Regex("B End Address\\s*\\n\\s*(.+?)(?=\\n\\s*\\n|\\n\\s*2N|$)", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            addressB = regex.find(rawText)?.groupValues?.get(1)?.trim() ?: ""
        }
        if (appointmentDate.isEmpty()) {
            val regex = Regex("Appointment Date\\s*\\n\\s*(\\d{4}/\\d{2}/\\d{2})", RegexOption.IGNORE_CASE)
            appointmentDate = regex.find(rawText)?.groupValues?.get(1) ?: ""
        }
        if (appointmentTime.isEmpty()) {
            val regex = Regex("Appointment Time\\s*\\n\\s*(.+?)(?=\\n)", RegexOption.IGNORE_CASE)
            appointmentTime = regex.find(rawText)?.groupValues?.get(1)?.trim() ?: ""
        }
        if (contactName.isEmpty()) {
            val regex = Regex("Contact Name\\s*\\n\\s*(.+?)(?=\\n)", RegexOption.IGNORE_CASE)
            contactName = regex.find(rawText)?.groupValues?.get(1)?.trim() ?: ""
        }
        if (contactPhone.isEmpty()) {
            val regex = Regex("Contact No\\.\\s*\\n\\s*(\\d+)", RegexOption.IGNORE_CASE)
            contactPhone = regex.find(rawText)?.groupValues?.get(1) ?: ""
        }
        if (status.isEmpty()) {
            val regex = Regex("Status\\s*\\n\\s*(\\w+)", RegexOption.IGNORE_CASE)
            status = regex.find(rawText)?.groupValues?.get(1) ?: ""
        }
        if (pidDesc.isEmpty()) {
            val regex = Regex("PID Desc\\s*\\n\\s*(.+?)(?=\\n\\s*STB|\\n\\s*\\n|$)", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            pidDesc = regex.find(rawText)?.groupValues?.get(1)?.trim() ?: ""
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
}
