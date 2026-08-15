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
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_main
        )

        // Android 15 / SDK 35 status-bar fix
        val root =
            findViewById<View>(
                R.id.mainRoot
            )

        root.setOnApplyWindowInsetsListener {
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

        root.requestApplyInsets()

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

        findViewById<Button>(
            R.id.saveButton
        ).setOnClickListener {
            saveToday()
        }

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
        return fmt.format(Date())
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

        todayTotal.text =
            formatNum(
                map[keyToday()] ?: 0L
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

    override fun onResume() {
        super.onResume()

        if (::todayTotal.isInitialized) {
            refresh()
        }
    }
}
