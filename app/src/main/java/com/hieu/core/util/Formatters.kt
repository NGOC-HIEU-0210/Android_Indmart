package com.hieu.core.util

import com.hieu.BuildConfig
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object Formatters {

    private val vndFormat: DecimalFormat = run {
        val symbols = DecimalFormatSymbols(Locale("vi", "VN")).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        DecimalFormat("#,###", symbols)
    }

    private val outputDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val outputDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    /**
     * Định dạng số tiền thành VND: ví dụ 35000.0 -> "35.000 ₫"
     */
    fun formatVnd(amount: Double?): String {
        if (amount == null) return "0 ₫"
        val formatted = vndFormat.format(amount)
        return "$formatted ₫"
    }

    fun formatVnd(amount: Long?): String {
        if (amount == null) return "0 ₫"
        val formatted = vndFormat.format(amount)
        return "$formatted ₫"
    }

    /**
     * Định dạng chuỗi ngày tháng từ backend (ISO hoặc dạng yyyy-MM-dd / yyyy-MM-ddTHH:mm:ss) sang dd/MM/yyyy
     */
    fun formatDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return ""
        return try {
            if (dateString.contains("T")) {
                val dt = LocalDateTime.parse(dateString)
                dt.format(outputDateFormatter)
            } else {
                val d = LocalDate.parse(dateString.substringBefore(" "))
                d.format(outputDateFormatter)
            }
        } catch (_: Exception) {
            try {
                // Thử cắt 10 ký tự đầu yyyy-MM-dd
                val part = dateString.take(10)
                val d = LocalDate.parse(part)
                d.format(outputDateFormatter)
            } catch (_: Exception) {
                dateString
            }
        }
    }

    /**
     * Định dạng ngày giờ: dd/MM/yyyy HH:mm
     */
    fun formatDateTime(dateTimeString: String?): String {
        if (dateTimeString.isNullOrBlank()) return ""
        return try {
            val dt = LocalDateTime.parse(dateTimeString)
            dt.format(outputDateTimeFormatter)
        } catch (_: Exception) {
            formatDate(dateTimeString)
        }
    }

    /**
     * Ghép đường dẫn ảnh tương đối với BASE_URL
     */
    fun toFullImageUrl(path: String?): String {
        if (path.isNullOrBlank()) return ""
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path
        }
        val baseUrl = BuildConfig.BASE_URL.trimEnd('/')
        val cleanPath = path.trimStart('/')
        return "$baseUrl/$cleanPath"
    }
}

fun formatVnd(amount: Double?): String = Formatters.formatVnd(amount)
fun formatVnd(amount: Long?): String = Formatters.formatVnd(amount)
fun toFullImageUrl(path: String?): String = Formatters.toFullImageUrl(path)


