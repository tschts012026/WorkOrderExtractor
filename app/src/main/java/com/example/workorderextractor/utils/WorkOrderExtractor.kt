package com.example.workorderextractor.utils

import com.example.workorderextractor.data.WorkOrder

object WorkOrderExtractor {
    fun extract(rawText: String): WorkOrder {
        var jobId = ""
        var grid = ""
        var serviceNumber = ""
        var addressA = ""
        var addressB = ""
        var appointmentDate = ""
        var appointmentTime = ""
        var contactName = ""
        var contactPhone = ""
        var status = ""
        var pidDesc = ""

        val lines = rawText.lines().map { it.trim() }

        // === 第一優先：冒號格式（用戶最常用）===
        // 例如：日期: 2026/05/21  或  時間: 10:00 - 10:00 (EX)
        for (line in lines) {
            when {
                // 冒號格式：標題在冒號前，數值在冒號後
                Regex("^日期\\s*[:：]\\s*(.+)").find(line) != null ->
                    appointmentDate = line.substringAfter(":").substringAfter("：").trim()
                Regex("^時間\\s*[:：]\\s*(.+)").find(line) != null ->
                    appointmentTime = line.substringAfter(":").substringAfter("：").trim()
                Regex("^聯絡人\\s*[:：]\\s*(.+)").find(line) != null ->
                    contactName = line.substringAfter(":").substringAfter("：").trim()
                Regex("^電話\\s*[:：]\\s*(.+)").find(line) != null ->
                    contactPhone = line.substringAfter(":").substringAfter("：").trim()
                Regex("^Status\\s*[:：]\\s*(.+)", setOf(RegexOption.IGNORE_CASE)).find(line) != null ->
                    status = line.substringAfter(":").substringAfter("：").trim()
                Regex("^PID Desc\\s*[:：](.*)", setOf(RegexOption.IGNORE_CASE)).find(line) != null ->
                    pidDesc = line.substringAfter(":").substringAfter("：").trim()
                // A/B 端地址（冒號格式）
                Regex("^A端地址\\s*[:：](.*)", setOf(RegexOption.IGNORE_CASE)).find(line) != null ->
                    addressA = line.substringAfter(":").substringAfter("：").trim()
                Regex("^B端地址\\s*[:：](.*)", setOf(RegexOption.IGNORE_CASE)).find(line) != null ->
                    addressB = line.substringAfter(":").substringAfter("：").trim()
                // Job ID
                Regex("^Job ID\\s*[:：]?\\s*(\\d+)", setOf(RegexOption.IGNORE_CASE)).find(line) != null ->
                    jobId = Regex("\\d+").find(line)?.value ?: ""
                // Service Number
                Regex("^Service No\\.?\\s*[:：]?\\s*(\\d+)", setOf(RegexOption.IGNORE_CASE)).find(line) != null ->
                    serviceNumber = Regex("\\d+").find(line)?.value ?: ""
                // Grid
                Regex("^Grid\\s*[:：]?\\s*(.+)", setOf(RegexOption.IGNORE_CASE)).find(line) != null ->
                    grid = line.substringAfter(":").substringAfter("：").trim().ifEmpty { line.substringAfter(" ").trim() }
            }
        }

        // === 第二優先：標題 + 下一行格式（完整文章）===
        if (jobId.isEmpty()) {
            for (i in lines.indices) {
                val line = lines[i]
                when {
                    line.contains("Job ID", ignoreCase = true) && i + 1 < lines.size && jobId.isEmpty() ->
                        if (lines[i + 1].matches(Regex("\\d+"))) jobId = lines[i + 1].trim()
                    line.contains("Exchange / Grid", ignoreCase = true) && i + 1 < lines.size && grid.isEmpty() ->
                        grid = lines[i + 1].trim()
                    line.contains("Service Number", ignoreCase = true) && i + 1 < lines.size && serviceNumber.isEmpty() ->
                        serviceNumber = lines[i + 1].trim()
                    line.contains("A End Address", ignoreCase = true) && i + 1 < lines.size && addressA.isEmpty() ->
                        addressA = lines[i + 1].trim()
                    line.contains("B End Address", ignoreCase = true) && i + 1 < lines.size && addressB.isEmpty() ->
                        addressB = lines[i + 1].trim()
                    line.contains("Appointment Date", ignoreCase = true) && i + 1 < lines.size && appointmentDate.isEmpty() ->
                        appointmentDate = lines[i + 1].trim()
                    line.contains("Appointment Time", ignoreCase = true) && i + 1 < lines.size && appointmentTime.isEmpty() ->
                        appointmentTime = lines[i + 1].trim()
                    line.contains("Contact Name", ignoreCase = true) && i + 1 < lines.size && contactName.isEmpty() ->
                        contactName = lines[i + 1].trim()
                    line.contains("Contact No.", ignoreCase = true) && i + 1 < lines.size && contactPhone.isEmpty() ->
                        contactPhone = lines[i + 1].trim()
                    line.equals("Status", ignoreCase = true) && i + 1 < lines.size && status.isEmpty() ->
                        status = lines[i + 1].trim()
                    line.contains("PID Desc", ignoreCase = true) && i + 1 < lines.size && pidDesc.isEmpty() ->
                        pidDesc = lines[i + 1].trim()
                }
            }
        }

        return WorkOrder(
            jobId = jobId,
            grid = grid,
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
