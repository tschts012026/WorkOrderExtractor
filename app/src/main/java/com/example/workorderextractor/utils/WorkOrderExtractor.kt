package com.example.workorderextractor.utils

import com.example.workorderextractor.data.WorkOrder

object WorkOrderExtractor {
    fun extract(rawText: String): WorkOrder {
        // 將文本中的連續空白（包括換行）壓縮為單一空格，方便匹配
        val compressed = rawText.replace(Regex("\\s+"), " ")

        // 1. Job ID
        val jobId = extractByPattern(compressed, Regex("Job ID\\s*(\\d+)", RegexOption.IGNORE_CASE))

        // 2. Grid / Exchange
        val grid = extractByPattern(compressed, Regex("(?:Exchange|Grid)\\s*([A-Z]+)", RegexOption.IGNORE_CASE))

        // 3. Service Number
        val serviceNumber = extractByPattern(compressed, Regex("Service Number\\s*(\\d+)", RegexOption.IGNORE_CASE))

        // 4. A End Address
        val addressA = extractMultiLineAddress(rawText, "A End Address", "B End Address")

        // 5. B End Address  
        val addressB = extractMultiLineAddress(rawText, "B End Address", "2N B/W Building|Customer Num|Appointment Information")

        // 6. Appointment Date
        val appointmentDate = extractByPattern(compressed, Regex("Appointment Date\\s*(\\d{4}/\\d{2}/\\d{2})", RegexOption.IGNORE_CASE))

        // 7. Appointment Time
        val appointmentTime = extractByPattern(compressed, Regex("(?:Appointment )?Time\\s*([\\d: -]+\\([^)]+\\))?", RegexOption.IGNORE_CASE))

        // 8. Contact Name
        val contactName = extractByPattern(compressed, Regex("Contact Name\\s*([A-Za-z]+(?:\\s+[A-Za-z]+)?)", RegexOption.IGNORE_CASE))

        // 9. Contact No.
        val contactPhone = extractByPattern(compressed, Regex("Contact No\\.?\\s*(\\d+)", RegexOption.IGNORE_CASE))

        // 10. Status
        val status = extractByPattern(compressed, Regex("Status\\s*(\\w+)", RegexOption.IGNORE_CASE))

        // 11. PID Desc - 先嘗試原始文本（保留換行），再試壓縮文本
        var pidDesc = extractByPattern(rawText, Regex("PID Desc[\\s\\S]*?\\n\\s*(.+?)(?=\\n\\s*STB|\\n\\s*\\n|$)", RegexOption.IGNORE_CASE))
        if (pidDesc.isEmpty()) {
            pidDesc = extractByPattern(compressed, Regex("PID Desc\\s*(.+?)(?=\\s+STB|\\s+Appointment|\\s+$)", RegexOption.IGNORE_CASE))
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

    private fun extractByPattern(text: String, regex: Regex): String {
        return regex.find(text)?.groupValues?.get(1)?.trim() ?: ""
    }

    private fun extractMultiLineAddress(fullText: String, startKeyword: String, endKeyword: String): String {
        // 嘗試匹配標題 + 內容（跨多行）
        val multiLinePattern = Regex("$startKeyword\\s*(.+?)(?=\\s*$endKeyword|\\s+\\n|\\n\\s*$|\\s*$)", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val match = multiLinePattern.find(fullText)
        if (match != null) {
            return match.groupValues[1].trim().replace(Regex("\\s+"), " ")
        }
        // 冒號格式：標題: 內容
        val colonPattern = Regex("$startKeyword\\s*[：:]\\s*(.+?)(?=\\s*[B端A端]|$)", RegexOption.IGNORE_CASE)
        val colonMatch = colonPattern.find(fullText)
        if (colonMatch != null) {
            return colonMatch.groupValues[1].trim()
        }
        return ""
    }
}
