package com.mbsgrowthlabs.duroodpak

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MonthRecordsActivity : Activity() {

    private val prefsName = "durood_records"
    private val dataKey = "data"

    private val darkGreen = Color.rgb(0, 73, 58)
    private val green = Color.rgb(0, 94, 73)
    private val gold = Color.rgb(190, 145, 45)
    private val cream = Color.rgb(250, 247, 238)
    private val lightCream = Color.rgb(255, 253, 247)
    private val textDark = Color.rgb(35, 45, 40)

    private val fmt =
        SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val monthFmt =
        SimpleDateFormat("yyyy-MM", Locale.US)

    private val prettyMonth =
        SimpleDateFormat("MMMM yyyy", Locale.US)

    private val prettyDate =
        SimpleDateFormat("dd MMM yyyy", Locale.US)

    private val dayFmt =
        SimpleDateFormat("EEEE", Locale.US)

    private lateinit var root: LinearLayout

    private fun records(): MutableMap<String, Long> {

        val prefs =
            getSharedPreferences(
                prefsName,
                Context.MODE_PRIVATE
            )

        val obj =
            JSONObject(
                prefs.getString(
                    dataKey,
                    "{}"
                ) ?: "{}"
            )

        val map =
            mutableMapOf<String, Long>()

        val keys = obj.keys()

        while (keys.hasNext()) {

            val key = keys.next()

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

        map.forEach { (key, value) ->

            obj.put(
                key,
                value
            )
        }

        getSharedPreferences(
            prefsName,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(
                dataKey,
                obj.toString()
            )
            .apply()
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        showMonthsPage()
    }

    private fun baseLayout(): LinearLayout {

        val layout =
            LinearLayout(this)

        layout.orientation =
            LinearLayout.VERTICAL

        layout.setBackgroundColor(
            cream
        )

        return layout
    }

    private fun header(
        title: String,
        showBack: Boolean
    ): LinearLayout {

        val bar =
            LinearLayout(this)

        bar.orientation =
            LinearLayout.HORIZONTAL

        bar.gravity =
            Gravity.CENTER_VERTICAL

        bar.setPadding(
            12,
            10,
            20,
            10
        )

        bar.setBackgroundColor(
            darkGreen
        )

        if (showBack) {

            val back =
                TextView(this)

            back.text = "‹"

            back.textSize = 42f

            back.setTextColor(
                Color.WHITE
            )

            back.gravity =
                Gravity.CENTER

            back.setPadding(
                0,
                0,
                8,
                0
            )

            back.setOnClickListener {

                showMonthsPage()
            }

            bar.addView(
                back,
                LinearLayout.LayoutParams(
                    55,
                    65
                )
            )
        }

        val titleView =
            TextView(this)

        titleView.text =
            title

        titleView.textSize =
            22f

        titleView.setTextColor(
            Color.WHITE
        )

        titleView.setTypeface(
            null,
            Typeface.BOLD
        )

        titleView.gravity =
            Gravity.CENTER_VERTICAL

        bar.addView(
            titleView,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        return bar
    }

    private fun showMonthsPage() {

        root =
            baseLayout()

        root.addView(
            header(
                "📖  Month-wise Records",
                false
            )
        )

        val scroll =
            ScrollView(this)

        val content =
            LinearLayout(this)

        content.orientation =
            LinearLayout.VERTICAL

        content.setPadding(
            16,
            18,
            16,
            30
        )

        val map =
            records()

        if (map.isEmpty()) {

            val empty =
                TextView(this)

            empty.text =
                "No Durood records found yet."

            empty.textSize =
                18f

            empty.setTextColor(
                textDark
            )

            empty.gravity =
                Gravity.CENTER

            empty.setPadding(
                20,
                80,
                20,
                80
            )

            content.addView(
                empty
            )

        } else {

            val months =
                mutableMapOf<String, Long>()

            map.forEach { (date, value) ->

                val month =
                    date.substring(
                        0,
                        7
                    )

                months[month] =
                    (months[month] ?: 0L) +
                            value
            }

            months.keys
                .sortedDescending()
                .forEach { month ->

                    val card =
                        LinearLayout(this)

                    card.orientation =
                        LinearLayout.VERTICAL

                    card.setPadding(
                        22,
                        18,
                        22,
                        18
                    )

                    card.setBackgroundColor(
                        lightCream
                    )

                    val monthTitle =
                        TextView(this)

                    monthTitle.text =
                        prettyMonth.format(
                            monthFmt.parse(
                                month
                            )!!
                        )

                    monthTitle.textSize =
                        21f

                    monthTitle.setTextColor(
                        darkGreen
                    )

                    monthTitle.setTypeface(
                        null,
                        Typeface.BOLD
                    )

                    val total =
                        TextView(this)

                    total.text =
                        "Total Durood  •  " +
                                formatNumber(
                                    months[month]
                                        ?: 0L
                                )

                    total.textSize =
                        17f

                    total.setTextColor(
                        gold
                    )

                    total.setPadding(
                        0,
                        8,
                        0,
                        0
                    )

                    val tap =
                        TextView(this)

                    tap.text =
                        "Tap to view date-wise records  ›"

                    tap.textSize =
                        13f

                    tap.setTextColor(
                        Color.DKGRAY
                    )

                    tap.setPadding(
                        0,
                        12,
                        0,
                        0
                    )

                    card.addView(
                        monthTitle
                    )

                    card.addView(
                        total
                    )

                    card.addView(
                        tap
                    )

                    card.setOnClickListener {

                        showMonthDetails(
                            month
                        )
                    }

                    val params =
                        LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )

                    params.setMargins(
                        0,
                        0,
                        0,
                        14
                    )

                    content.addView(
                        card,
                        params
                    )
                }
        }

        scroll.addView(
            content
        )

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(
            root
        )
    }

    private fun showMonthDetails(
        month: String
    ) {

        root =
            baseLayout()

        root.addView(
            header(
                prettyMonth.format(
                    monthFmt.parse(
                        month
                    )!!
                ),
                true
            )
        )

        val map =
            records()

        val monthRecords =
            map
                .filter {
                    it.key.startsWith(
                        "$month-"
                    )
                }
                .toList()
                .sortedByDescending {
                    it.first
                }

        val total =
            monthRecords.sumOf {
                it.second
            }

        val scroll =
            ScrollView(this)

        val content =
            LinearLayout(this)

        content.orientation =
            LinearLayout.VERTICAL

        content.setPadding(
            8,
            14,
            8,
            30
        )

        val totalCard =
            TextView(this)

        totalCard.text =
            "MONTHLY TOTAL\n" +
                    "${formatNumber(total)} Durood"

        totalCard.textSize =
            21f

        totalCard.setTextColor(
            Color.WHITE
        )

        totalCard.setTypeface(
            null,
            Typeface.BOLD
        )

        totalCard.gravity =
            Gravity.CENTER

        totalCard.setPadding(
            20,
            22,
            20,
            22
        )

        totalCard.setBackgroundColor(
            green
        )

        content.addView(
            totalCard
        )

        val hint =
            TextView(this)

        hint.text =
            "Tap any date record to Edit or Delete"

        hint.textSize =
            13f

        hint.setTextColor(
            Color.DKGRAY
        )

        hint.gravity =
            Gravity.CENTER

        hint.setPadding(
            10,
            12,
            10,
            12
        )

        content.addView(
            hint
        )

        addTableHeader(
            content
        )

        monthRecords.forEach {
                (date, value) ->

            val row =
                LinearLayout(this)

            row.orientation =
                LinearLayout.HORIZONTAL

            row.gravity =
                Gravity.CENTER_VERTICAL

            row.setPadding(
                8,
                15,
                8,
                15
            )

            row.setBackgroundColor(
                lightCream
            )

            val parsed =
                fmt.parse(
                    date
                )!!

            val dateText =
                TextView(this)

            dateText.text =
                prettyDate.format(
                    parsed
                )

            dateText.textSize =
                15f

            dateText.setTextColor(
                textDark
            )

            val dayText =
                TextView(this)

            dayText.text =
                dayFmt.format(
                    parsed
                )

            dayText.textSize =
                14f

            dayText.setTextColor(
                Color.DKGRAY
            )

            val countText =
                TextView(this)

            countText.text =
                formatNumber(
                    value
                )

            countText.textSize =
                17f

            countText.setTextColor(
                darkGreen
            )

            countText.setTypeface(
                null,
                Typeface.BOLD
            )

            countText.gravity =
                Gravity.RIGHT

            row.addView(
                dateText,
                weightParams(1.4f)
            )

            row.addView(
                dayText,
                weightParams(1.3f)
            )

            row.addView(
                countText,
                weightParams(1f)
            )

            row.setOnClickListener {

                editRecord(
                    date
                )
            }

            content.addView(
                row
            )

            val line =
                View(this)

            line.setBackgroundColor(
                Color.rgb(
                    225,
                    218,
                    200
                )
            )

            content.addView(
                line,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                )
            )
        }

        scroll.addView(
            content
        )

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(
            root
        )
    }

    private fun addTableHeader(
        parent: LinearLayout
    ) {

        val header =
            LinearLayout(this)

        header.orientation =
            LinearLayout.HORIZONTAL

        header.setPadding(
            8,
            14,
            8,
            14
        )

        header.setBackgroundColor(
            darkGreen
        )

        header.addView(
            headerText("DATE"),
            weightParams(1.4f)
        )

        header.addView(
            headerText("DAY"),
            weightParams(1.3f)
        )

        header.addView(
            headerText("DUROOD"),
            weightParams(1f)
        )

        parent.addView(
            header
        )
    }

    private fun headerText(
        text: String
    ): TextView {

        val view =
            TextView(this)

        view.text =
            text

        view.textSize =
            13f

        view.setTextColor(
            Color.WHITE
        )

        view.setTypeface(
            null,
            Typeface.BOLD
        )

        return view
    }

    private fun weightParams(
        weight: Float
    ): LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            weight
        )
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
            (map[date] ?: 0L)
                .toString()
        )

        box.setSelectAllOnFocus(
            true
        )

        box.hint =
            "Durood count"

        val parsed =
            fmt.parse(
                date
            )!!

        AlertDialog.Builder(this)
            .setTitle(
                "Edit Record\n" +
                        prettyDate.format(
                            parsed
                        )
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

                    showMonthDetails(
                        date.substring(
                            0,
                            7
                        )
                    )

                    toast(
                        "Record updated"
                    )
                }
            }
            .setNeutralButton(
                "DELETE"
            ) { _, _ ->

                AlertDialog.Builder(this)
                    .setTitle(
                        "Delete Record?"
                    )
                    .setMessage(
                        "Do you really want to delete this record?"
                    )
                    .setPositiveButton(
                        "DELETE"
                    ) { _, _ ->

                        map.remove(
                            date
                        )

                        save(map)

                        showMonthDetails(
                            date.substring(
                                0,
                                7
                            )
                        )

                        toast(
                            "Record deleted"
                        )
                    }
                    .setNegativeButton(
                        "CANCEL",
                        null
                    )
                    .show()
            }
            .setNegativeButton(
                "CANCEL",
                null
            )
            .show()
    }

    private fun formatNumber(
        number: Long
    ): String {

        return String.format(
            Locale.US,
            "%,d",
            number
        )
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
