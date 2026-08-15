package com.mbsgrowthlabs.duroodpak

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var input: EditText
    private lateinit var dateText: TextView
    private lateinit var todayTotal: TextView
    private lateinit var overallTotal: TextView

    private val prefs by lazy {
        getSharedPreferences(
            "durood_records",
            Context.MODE_PRIVATE
        )
    }

    private val fmt =
        SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        )

    private val pretty =
        SimpleDateFormat(
            "dd MMM yyyy, EEE",
            Locale.US
        )

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        setContentView(
            R.layout.activity_main
        )

        /*
         * Android 15 / SDK 35 edge-to-edge fix.
         *
         * The app content receives the system bar
         * insets so the header does not go underneath
         * the time, signal and battery area.
         */
        val contentView =
            findViewById<View>(
                android.R.id.content
            )

        contentView.setOnApplyWindowInsetsListener {
                view,
                insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsets.Type.systemBars()
                )

            view.setPadding(
                0,
                systemBars.top,
                0,
                systemBars.bottom
            )

            insets
        }

        contentView.requestApplyInsets()

        input =
            findViewById(
                R.id.countInput
            )

        dateText =
            findViewById(
                R.id.dateText
            )

        todayTotal =
            findViewById(
                R.id.todayTotal
            )

        overallTotal =
            findViewById(
                R.id.overallTotal
            )

        dateText.text =
            pretty.format(
                Date()
            )

        /*
         * Save / Update button
         */
        findViewById<Button>(
            R.id.saveButton
        ).setOnClickListener {

            saveToday()
        }

        /*
         * Month-wise Records
         *
         * Opens the new full-screen page
         * instead of the old popup.
         */
        findViewById<Button>(
            R.id.monthsButton
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    MonthRecordsActivity::class.java
                )
            )
        }

        /*
         * Overall Total
         */
        findViewById<Button>(
            R.id.overallButton
        ).setOnClickListener {

            showOverall()
        }

        refresh()
    }

    private fun records():
            MutableMap<String, Long> {

        val obj =
            JSONObject(
                prefs.getString(
                    "data",
                    "{}"
                ) ?: "{}"
            )

        val map =
            mutableMapOf<String, Long>()

        val keys =
            obj.keys()

        while (keys.hasNext()) {

            val key =
                keys.next()

            map[key] =
                obj.optLong(
                    key,
                    0L
                )
        }

        return map
    }

    private fun save(
        map: Map<String, Long>
    ) {

        val obj =
            JSONObject()

        map.forEach {
                (key, value) ->

            obj.put(
                key,
                value
            )
        }

        prefs.edit()
            .putString(
                "data",
                obj.toString()
            )
            .apply()
    }

    private fun keyToday(): String {

        return fmt.format(
            Date()
        )
    }

    private fun formatNum(
        number: Long
    ): String {

        return String.format(
            Locale.US,
            "%,d",
            number
        )
    }

    private fun refresh() {

        val map =
            records()

        val today =
            map[keyToday()] ?: 0L

        todayTotal.text =
            formatNum(
                today
            )

        overallTotal.text =
            formatNum(
                map.values.sum()
            )
    }

    private fun saveToday() {

        val value =
            input.text
                .toString()
                .trim()
                .toLongOrNull()

        if (
            value == null ||
            value < 0
        ) {

            input.error =
                "Enter a valid number"

            return
        }

        val map =
            records()

        /*
         * Same-date records are combined.
         *
         * Example:
         *
         * First entry = 2,000
         * Second entry = 2,000
         * Today's total = 4,000
         */

        val oldValue =
            map[keyToday()] ?: 0L

        map[keyToday()] =
            oldValue + value

        save(map)

        input.text.clear()

        refresh()

        Toast.makeText(
            this,
            "Today's record updated: ${
                formatNum(
                    oldValue + value
                )
            }",
            Toast.LENGTH_SHORT
        ).show()
    }

    /*
     * Old popup method is kept only for compatibility.
     *
     * The main Month-wise button no longer uses it.
     */
    private fun showMonths() {

        val map =
            records()

        if (map.isEmpty()) {

            toast(
                "Abhi koi record nahi hai."
            )

            return
        }

        val months =
            mutableMapOf<String, Long>()

        map.forEach {
                (date, value) ->

            val month =
                date.substring(
                    0,
                    7
                )

            months[month] =
                (months[month] ?: 0L) +
                        value
        }

        val keys =
            months.keys
                .sortedDescending()

        val labels =
            keys.map {

                monthLabel(it) +
                        " • " +
                        formatNum(
                            months[it] ?: 0L
                        )

            }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle(
                "📅 Month-wise Records"
            )
            .setItems(
                labels
            ) { _, which ->

                showMonth(
                    keys[which],
                    months[
                        keys[which]
                    ] ?: 0L
                )
            }
            .setNegativeButton(
                "Close",
                null
            )
            .show()
    }

    private fun monthLabel(
        key: String
    ): String {

        val date =
            SimpleDateFormat(
                "yyyy-MM",
                Locale.US
            ).parse(
                key
            )!!

        return SimpleDateFormat(
            "MMMM yyyy",
            Locale.US
        ).format(
            date
        )
    }

    private fun showMonth(
        month: String,
        total: Long
    ) {

        val map =
            records()

        val days =
            map.keys
                .filter {
                    it.startsWith(
                        month
                    )
                }
                .sortedDescending()

        val text =
            StringBuilder()

        text.append(
            "${monthLabel(month)}\n"
        )

        text.append(
            "Total: ${
                formatNum(total)
            } Durood\n\n"
        )

        days.forEach { dateKey ->

            val date =
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.US
                ).parse(
                    dateKey
                )!!

            text.append(
                SimpleDateFormat(
                    "dd MMM yyyy, EEEE",
                    Locale.US
                ).format(
                    date
                )
            )

            text.append(
                " — ${
                    formatNum(
                        map[dateKey] ?: 0L
                    )
                }\n"
            )
        }

        AlertDialog.Builder(this)
            .setTitle(
                "${monthLabel(month)} • ${
                    formatNum(total)
                }"
            )
            .setMessage(
                text.toString()
            )
            .setPositiveButton(
                "Edit Record"
            ) { _, _ ->

                chooseEdit(
                    days
                )
            }
            .setNegativeButton(
                "Close",
                null
            )
            .show()
    }

    private fun chooseEdit(
        days: List<String>
    ) {

        if (days.isEmpty()) {
            return
        }

        val map =
            records()

        val labels =
            days.map { dateKey ->

                val date =
                    SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.US
                    ).parse(
                        dateKey
                    )!!

                SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.US
                ).format(
                    date
                ) +
                        " — " +
                        formatNum(
                            map[dateKey] ?: 0L
                        )

            }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle(
                "Choose a date"
            )
            .setItems(
                labels
            ) { _, which ->

                editRecord(
                    days[which]
                )
            }
            .setNegativeButton(
                "Close",
                null
            )
            .show()
    }

    private fun editRecord(
        date: String
    ) {

        val map =
            records()

        val box =
            EditText(this)

        box.inputType = 2

        box.setText(
            (
                map[date] ?: 0L
            ).toString()
        )

        box.setSelectAllOnFocus(
            true
        )

        box.hint =
            "Durood count"

        val day =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            ).parse(
                date
            )!!

        AlertDialog.Builder(this)
            .setTitle(
                "Edit • ${
                    SimpleDateFormat(
                        "dd MMMM yyyy",
                        Locale.US
                    ).format(day)
                }"
            )
            .setView(
                box
            )
            .setPositiveButton(
                "UPDATE"
            ) { _, _ ->

                val newValue =
                    box.text
                        .toString()
                        .toLongOrNull()

                if (
                    newValue != null &&
                    newValue >= 0
                ) {

                    map[date] =
                        newValue

                    save(map)

                    refresh()

                    toast(
                        "Record updated"
                    )
                }
            }
            .setNeutralButton(
                "DELETE"
            ) { _, _ ->

                map.remove(
                    date
                )

                save(map)

                refresh()

                toast(
                    "Record deleted"
                )
            }
            .setNegativeButton(
                "Cancel",
                null
            )
            .show()
    }

    private fun showOverall() {

        val total =
            records()
                .values
                .sum()

        AlertDialog.Builder(this)
            .setTitle(
                "✨ All-Time Durood e Pak Total"
            )
            .setMessage(
                "${formatNum(total)} Durood"
            )
            .setPositiveButton(
                "Close",
                null
            )
            .show()
    }

    private fun toast(
        message: String
    ) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}
