package com.example.myapplication.extensions

import android.text.Editable
import android.text.TextWatcher
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.min

const val STRING_DOT = "."
const val CHAR_COMMA = ','
const val PATTERN_FORMAT_DECIMAL_FORCE_THREE = "#,###."
const val MINUS = "-"
const val STRING_EMPTY = ""

fun CharSequence.replace(index: Int, newString: String): CharSequence {
    return this.replaceRange(index, index + 1, newString)
}

fun EditText.formatInputToDecimalPlacesWithDollar(
    decimalNumber: String,
    limitValue: Int = Int.MAX_VALUE,
    callback: ((String) -> Unit)? = null
) {
    addTextChangedListener(object : TextWatcher {
        var selectionStart: Int = 0
        var selectionEnd: Int = 0
        var inputBefore = ""

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            inputBefore = s.toString()
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit

        override fun afterTextChanged(editable: Editable?) {
            selectionStart = this@formatInputToDecimalPlacesWithDollar.selectionStart
            selectionEnd = this@formatInputToDecimalPlacesWithDollar.selectionEnd
            val decimalPos = decimalNumber.convertStringToNumber().toInt()

            removeTextChangedListener(this)

            // Định dạng số
            var pattern = if (decimalPos == 0) "#,###" else PATTERN_FORMAT_DECIMAL_FORCE_THREE
            for (i in 1..decimalNumber.convertStringToNumberToLong()) {
                pattern += "#"
            }
            val formatter = DecimalFormat(
                pattern,
                DecimalFormatSymbols.getInstance(Locale.ENGLISH)
            )
            val input = editable.toString().replace("$ ", "").trim() // Loại bỏ dấu `$` trước khi định dạng
            val strBeforeDot = input.substringBefore(".").remove { it == ',' }

            var text = if (strBeforeDot.length > limitValue) inputBefore else input
            text = text.replace(MINUS, STRING_EMPTY)

            var formattedText = ""
            if (text.isNotEmpty()) {
                val lastChar = text.substring(text.length - 1)
                val firstChar = text.substring(0, 1)
                val midChar = text.substring(1)
                val indexOfDot = text.indexOf(".")
                val indexOfComma = text.lastIndexOf(",")

                formattedText =
                    when {
                        indexOfComma > indexOfDot && indexOfDot != -1 -> formatter.format(
                            text.substringBeforeLast(",").plus(text.substringAfterLast(","))
                                .convertStringToDouble()
                        )
                        lastChar == "." -> text
                        lastChar == "." && decimalPos == 0 -> inputBefore
                        firstChar == "-" -> text
                        firstChar == "." -> {
                            if (midChar.convertStringToNumberToLong()
                                    .toString().length <= decimalNumber.convertStringToNumber()
                            ) formatter.format(
                                "0.$midChar".convertStringToDouble()
                            ) else inputBefore
                        }
                        text.contains(".") && (text.indexOf(".") < (text.length - decimalNumber.convertStringToNumberToLong() - 1)) -> {
                            text.substring(
                                0,
                                indexOfDot + decimalNumber.convertStringToNumber().toInt() + 1
                            )
                        }
                        lastChar == "0" && text.contains(".") -> "${
                            formatter.format(
                                text.substring(0, indexOfDot).convertStringToDouble()
                            )
                        }.${text.subSequence(indexOfDot + 1, text.length)}"
                        else -> formatter.format(text.convertStringToBigDecimalNumber())
                    }
            }

            formattedText = if (formattedText.isNotEmpty()) "$ $formattedText" else ""

            // Cập nhật văn bản
            setText(formattedText)
            addTextChangedListener(this)

            var newStart = min(selectionStart, formattedText.length)
            var newEnd = min(selectionEnd, formattedText.length)
            if (editable.toString().length < formattedText.length) {
                newEnd++
                newStart++
            }
            if (editable.toString().length > formattedText.length && selectionEnd != editable.toString().length && selectionEnd != 0) {
                newEnd--
                newStart--
            }
            newStart = min(newStart, formattedText.length)
            newEnd = min(newEnd, formattedText.length)
            setSelection(newStart, newEnd)

            callback?.invoke(formattedText)
        }
    })
}


fun EditText.formatInputToDecimalPlaces(
    decimalNumber: String,
    limitValue: Int = Int.MAX_VALUE,
    onFocus: (Boolean) -> Unit = {},
    callback: ((String) -> Unit)? = null,
) {
    addTextChangedListener(object : TextWatcher {
        var selectionStart: Int = 0
        var selectionEnd: Int = 0
        var inputBefore = ""
        var isEditComma = false
        var isRemoveDot = false

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            inputBefore = s.toString()
            isEditComma = s.length > start && s[start] == CHAR_COMMA
            isRemoveDot = s.length > start && s[start].toString() == STRING_DOT
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit

        override fun afterTextChanged(editable: Editable?) {
            var inputAfter = editable.toString()
            selectionStart = this@formatInputToDecimalPlaces.selectionStart
            selectionEnd = this@formatInputToDecimalPlaces.selectionEnd
            if ((inputAfter.length < inputBefore.length)) {
                if (isEditComma)
                    inputAfter =
                        inputAfter.replace(selectionStart - 1, CHAR_COMMA.toString()).toString()
                if (isRemoveDot) {
                    inputAfter = inputBefore.substringBefore(STRING_DOT)
                }
            }
            val decimalPos = decimalNumber.convertStringToNumber().toInt()

            removeTextChangedListener(this)
            var pattern = if (decimalPos == 0) "#,###" else "#,###."
            for (i in 1..decimalNumber.convertStringToNumberToLong()) {
                pattern += "#"
            }
            val formatter = DecimalFormat(
                pattern,
                DecimalFormatSymbols.getInstance(Locale.ENGLISH)
            )
            val strBeforeDot = inputAfter.substringBefore(".")
            val text = if (strBeforeDot.length > limitValue) inputBefore else inputAfter

            var barStackedLabel = ""
            if (text.isNotEmpty()) {
                val lastChar = text.substring(text.length - 1)
                val firstChar = text.substring(0, 1)
                val midChar = text.substring(1)
                val indexOfDot = text.indexOf(".")
                val indexOfComma = text.lastIndexOf(",")
                barStackedLabel =
                    when {
                        indexOfComma > indexOfDot && indexOfDot != -1 -> formatter.format(
                            text.substringBeforeLast(
                                ","
                            ).plus(text.substringAfterLast(",")).convertStringToDouble()
                        )

                        lastChar == "." -> text
                        lastChar == "." && decimalPos == 0 -> inputBefore
                        firstChar == "-" -> text
                        firstChar == "." -> {
                            if (midChar.convertStringToNumberToLong()
                                    .toString().length <= decimalNumber.convertStringToNumber()
                            ) formatter.format(
                                "0.$midChar".convertStringToDouble()
                            ) else inputBefore
                        }

                        text.contains(".") && (text.indexOf(".") < (text.length - decimalNumber.convertStringToNumberToLong() - 1)) -> {
                            text.substring(
                                0,
                                indexOfDot + decimalNumber.convertStringToNumber().toInt() + 1
                            )
                        }

                        lastChar == "0" && text.contains(".") -> "${
                            formatter.format(
                                text.substring(0, indexOfDot).convertStringToDouble()
                            )
                        }.${text.subSequence(indexOfDot + 1, text.length)}"

                        else -> formatter.format(text.convertStringToBigDecimalNumber())
                    }
            }
            setText(barStackedLabel)
            addTextChangedListener(this)

            var newStart = min(selectionStart, barStackedLabel.length)
            var newEnd = min(selectionEnd, barStackedLabel.length)
            if (inputAfter.length < barStackedLabel.length) {
                newEnd++
                newStart++
            }
            if (inputAfter.length > barStackedLabel.length && selectionEnd != inputAfter.length && selectionEnd != 0) {
                newEnd--
                newStart--
            }
            newStart = min(newStart, barStackedLabel.length)
            newEnd = min(newEnd, barStackedLabel.length)
            setSelection(newStart, newEnd)
            callback?.invoke(barStackedLabel)
        }
    })
    setOnFocusChangeListener { _, hasFocus ->
        val inputText = this.text
        if (!hasFocus && inputText.endsWith(STRING_DOT))
            this.text = inputText.delete(inputText.length - 1, inputText.length)
        onFocus.invoke(hasFocus)
    }
}


fun EditText.disablePaste() {
    this.customSelectionActionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(p0: ActionMode?, p1: Menu?): Boolean {
            return false
        }

        override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(p0: ActionMode?, p1: MenuItem?): Boolean {
            return false
        }

        override fun onDestroyActionMode(p0: ActionMode?) {

        }
    }
}

fun CharSequence.remove(condition: (Char) -> Boolean): CharSequence {
    var newSource = this
    for (i in this.length - 1 downTo 0) {
        val char = newSource[i]
        if (condition(char)) {
            newSource = newSource.removeRange(i, i + 1)
        }
    }
    return newSource
}
