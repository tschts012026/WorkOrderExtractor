package com.example.workorderextractor.utils

import com.example.workorderextractor.data.WorkOrder

object WorkOrderExtractor {
    fun extract(rawText: String): WorkOrder {
        // 標準化換行，將所有 \r\n 換成 \n
        val text = rawText.replace("\r\n", "\n").replace("\r", "\n")
        val lines = text.lines()

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

        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) {
                i++
                continue
            }

            // 移除開頭的列表符號（·、-、*、• 等）
            val cleanLine = line.replace(Regex("^[·\\-*]\\s*"), "")

            when {
                // Job ID: 數字
                cleanLine.matches(Regex("Job\\s*ID\\s*[:：]\\s*(.+)", setOf(RegexOption.IGNORE_CASE))) -> {
                    jobId = extractValue(cleanLine)
                }
                // Grid: FTH (可選)
                cleanLine.matches(Regex("Grid\\s*[:：]\\s*(.+)", setOf(RegexOption.IGNORE_CASE))) -> {
                    // skip
                }
                // Service Number: 數字
                cleanLine.matches(Regex("Service\\s*Number\\s*[:：]\\s*(.+)", setOf(RegexOption.IGNORE_CASE))) -> {
                    serviceNumber = extractValue(cleanLine)
                }
                // Address: (可能後面沒有值，需看下一行縮排)
                cleanLine.matches(Regex("Address\\s*[:：]", setOf(RegexOption.IGNORE_CASE))) -> {
                    val (a, b) = parseAddressBlock(lines, i + 1)
                    if (a.isNotBlank()) addressA = a
                    if (b.isNotBlank()) addressB = b
                }
                // A End Address: 直接匹配（可能在縮排行）
                cleanLine.matches(Regex("A\\s*End\\s*Address\\s*[:：]\\s*(.+)", setOf(RegexOption.IGNORE_CASE))) -> {
                    addressA = extractValue(cleanLine)
                }
                // B End Address: 直接匹配
                cleanLine.matches(Regex("B\\s*End\\s*Address\\s*[:：]\\s*(.+)", setOf(RegexOption.IGNORE_CASE))) -> {
                    addressB = extractValue(cleanLine)
                }
                // Appointment Date:
                cleanLine.matches(Regex("Appointment\\s*Date\\s*[:：]\\s*(.+)", setOf(RegexOption.IGNORE_CASE))) -> {
                    appointmentDate = extractValue(cleanLine)
                }
                // Appointment Time:
                cleanLine.matches(Regex("Appointment\\s*Time\\s*[:：]\\s*(.+)", setOf(RegexOption.IGNORE_CASE))) -> {
                    appointmentTime = extractValue(cleanLine)
                }
                // Contact: (後面可能跟縮排的子項目)
                cleanLine.matches(Regex("Contact\\s*[:：]", setOf(RegexOption.IGNORE_CASE))) -> {
                    val (name, phone) = parseContactBlock(lines, i + 1)
                    if (name.isNotBlank()) contactName = name
                    if (phone.isNotBlank()) contactPhone = phone
                }
                // Contact Name:
                cleanLine.matches(Regex("Contact\\s*Name\\s*[:：]\\s*(.+)", setOf(RegexOption.IGNORE_CASE))) -> {
                    contactName = extractValue(cleanLine)
                }
                // Contact No.:
                cleanLine.matches(Regex("Contact\\s*No\\.?\\s*[:：]\\s*(.+)", setOf(RegexOption.IGNORE_CASE))) -> {
                    contactPhone = extractValue(cleanLine)
                }
                // Status:
                cleanLine.matches(Regex("Status\\s*[:：]\\s*(.+)", setOf(RegexOption.IGNORE_CASE))) -> {
                    status = extractValue(cleanLine)
                }
                // PID Desc:
                cleanLine.matches(Regex("PID\\s*Desc\\s*[:：]\\s*(.+)", setOf(RegexOption.IGNORE_CASE))) -> {
                    pidDesc = extractValue(cleanLine)
                }
                // 如果一行中直接包含 PW ALARM 關鍵字（沒有 PID Desc 前綴）
                cleanLine.contains("PW ALARM", ignoreCase = true) && pidDesc.isEmpty() -> {
                    pidDesc = cleanLine.trim()
                }
            }
            i++
        }

        // 如果地址還沒有抓到，嘗試用正則直接從全文撈（針對傳統格式）
        if (addressA.isEmpty() && addressB.isEmpty()) {
            val aRegex = Regex("A\\s*End\\s*Address\\s*[:：]\\s*(.+?)(?=\\s*B\\s*End|\\s*\\n\\s*\\n|\\s*Appointment|$)", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            val bRegex = Regex("B\\s*End\\s*Address\\s*[:：]\\s*(.+?)(?=\\s*\\n\\s*\\n|\\s*Appointment|$)", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            addressA = aRegex.find(text)?.groupValues?.get(1)?.trim() ?: ""
            addressB = bRegex.find(text)?.groupValues?.get(1)?.trim() ?: ""
        }

        // 如果聯絡人仍為空，嘗試從 Customer Name 等欄位獲取
        if (contactName.isEmpty()) {
            val nameRegex = Regex("Customer\\s*Name\\s*[:：]\\s*(.+)", setOf(RegexOption.IGNORE_CASE))
            contactName = nameRegex.find(text)?.groupValues?.get(1)?.trim() ?: ""
        }
        if (contactPhone.isEmpty()) {
            val phoneRegex = Regex("(?:Contact|Customer)\\s*No\\.?\\s*[:：]\\s*(\\d+)", setOf(RegexOption.IGNORE_CASE))
            contactPhone = phoneRegex.find(text)?.groupValues?.get(1)?.trim() ?: ""
        }

        // 清理地址：去除多餘空格
        addressA = cleanAddress(addressA)
        addressB = cleanAddress(addressB)

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

    private fun extractValue(line: String): String {
        val idx = line.indexOfAny(charArrayOf(':', '：'))
        return if (idx != -1) line.substring(idx + 1).trim() else ""
    }

    private fun parseAddressBlock(lines: List<String>, startIdx: Int): Pair<String, String> {
        var a = ""
        var b = ""
        var i = startIdx
        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) {
                i++
                continue
            }
            // 如果遇到非縮排行（即下一層級的標題沒有縮排），停止
            if (!line.startsWith("·") && !line.startsWith("-") && !line.startsWith("•") && !line.matches(Regex("^\\s{2,}.*"))) {
                break
            }
            val clean = line.replace(Regex("^[·\\-*]\\s*"), "").trim()
            when {
                clean.matches(Regex("A\\s*End\\s*Address\\s*[:：]\\s*(.+)", setOf(RegexOption.IGNORE_CASE))) -> {
                    a = extractValue(clean)
                }
                clean.matches(Regex("B\\s*End\\s*Address\\s*[:：]\\s*(.+)", setOf(RegexOption.IGNORE_CASE))) -> {
                    b = extractValue(clean)
                }
                clean.contains("A End", ignoreCase = true) && clean.contains(":") -> {
                    a = extractValue(clean)
                }
                clean.contains("B End", ignoreCase = true) && clean.contains(":") -> {
                    b = extractValue(clean)
                }
            }
            i++
        }
        return Pair(a, b)
    }

    private fun parseContactBlock(lines: List<String>, startIdx: Int): Pair<String, String> {
        var name = ""
        var phone = ""
        var i = startIdx
        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) {
                i++
                continue
            }
            if (!line.startsWith("·") && !line.startsWith("-") && !line.matches(Regex("^\\s{2,}.*"))) {
                break
            }
            val clean = line.replace(Regex("^[·\\-*]\\s*"), "").trim()
            when {
                clean.matches(Regex("Contact\\s*Name\\s*[:：]\\s*(.+)", setOf(RegexOption.IGNORE_CASE))) -> {
                    name = extractValue(clean)
                }
                clean.matches(Regex("Contact\\s*No\\.?\\s*[:：]\\s*(.+)", setOf(RegexOption.IGNORE_CASE))) -> {
                    phone = extractValue(clean)
                }
            }
            i++
        }
        return Pair(name, phone)
    }

    private fun cleanAddress(addr: String): String {
        var cleaned = addr.replace(Regex("\\s+"), " ")
        cleaned = cleaned.replace(Regex("^[AB]\\s*End\\s*[:：]\\s*", setOf(RegexOption.IGNORE_CASE)), "")
        cleaned = cleaned.replace(Regex("^[AB]端\\s*[:：]\\s*"), "")
        return cleaned.trim()
    }
}