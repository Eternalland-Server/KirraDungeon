package net.sakuragame.eternal.kirradungeon.server

import java.text.DecimalFormat
import kotlin.math.abs


@Suppress("DuplicatedCode")
enum class UnitConvert(value: Long, symbol_cn: String, symbol_en: String) {

    THOUSAND(1000, "千", "K"),
    TEN_THOUSAND(10000, "万", "W"),
    MILLION(1000000, "百万", "M"),
    TEN_MILLION(10000000, "千万", "TM"),
    HUNDREDS_MILLION(100000000, "亿", "HM");

    private val value: Double
    private val symbolCN: String
    private val symbolEN: String

    init {
        this.value = value.toDouble()
        this.symbolCN = symbol_cn
        this.symbolEN = symbol_en
    }

    companion object {

        fun doCommonInputInference(input: Double): String {
            return when {
                input > 10000000 -> UnitConvert.formatCN(TEN_MILLION, input, 2)
                input > 1000000 -> UnitConvert.formatCN(MILLION, input, 2)
                input > 10000 -> UnitConvert.formatCN(TEN_THOUSAND, input, 2)
                else -> input.toInt().toString()
            }
        }

        @JvmOverloads
        fun formatCN(unitConvert: UnitConvert, value: Double, decimalPlace: Int = 0): String {
            var cnValue = value
            val negative = cnValue < 0
            cnValue = abs(cnValue)
            return if (cnValue >= unitConvert.value) {
                (if (negative) "-" else "") + getFormat(decimalPlace).format(cnValue / unitConvert.value) + unitConvert.symbolCN
            } else {
                (if (negative) "-" else "") + cnValue.toInt() + ""
            }
        }

        @JvmOverloads
        fun formatEN(unitConvert: UnitConvert, value: Double, decimalPlace: Int = 0): String {
            var cnValue = value
            val negative = cnValue < 0
            cnValue = abs(cnValue)
            return if (cnValue >= unitConvert.value) {
                (if (negative) "-" else "") + getFormat(decimalPlace).format(cnValue / unitConvert.value) + unitConvert.symbolEN
            } else (if (negative) "-" else "") + cnValue.toInt() + ""
        }

        private fun getFormat(size: Int): DecimalFormat {
            val sb = StringBuilder("0")
            if (size > 0) {
                sb.append(".")
                for (i in 0 until size) {
                    sb.append("0")
                }
            }
            return DecimalFormat(sb.toString())
        }
    }
}