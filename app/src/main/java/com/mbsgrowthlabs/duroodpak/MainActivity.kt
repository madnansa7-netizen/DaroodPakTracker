package com.mbsgrowthlabs.duroodpak

import android.app.AlertDialog
import android.os.Bundle
import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : android.app.Activity() {
    private lateinit var input: EditText
    private lateinit var dateText: TextView
    private lateinit var todayTotal: TextView
    private lateinit var overallTotal: TextView
    private val prefs by lazy { getSharedPreferences("durood_records", Context.MODE_PRIVATE) }
    private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val pretty = SimpleDateFormat("dd MMMM yyyy, EEEE", Locale.US)

    private fun records(): MutableMap<String, Long> {
        val obj = JSONObject(prefs.getString("data", "{}"))
        val map = mutableMapOf<String, Long>()
        val keys = obj.keys(); while (keys.hasNext()) { val k = keys.next(); map[k] = obj.optLong(k, 0) }
        return map
    }
    private fun save(map: Map<String, Long>) {
        val obj = JSONObject(); map.forEach { (k,v) -> obj.put(k,v) }
        prefs.edit().putString("data", obj.toString()).apply()
    }
    private fun keyToday() = fmt.format(Date())
    private fun formatNum(n: Long) = String.format(Locale.US, "%,d", n)

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContentView(R.layout.activity_main)
        input = findViewById(R.id.countInput); dateText = findViewById(R.id.dateText); todayTotal=findViewById(R.id.todayTotal); overallTotal=findViewById(R.id.overallTotal)
        dateText.text = pretty.format(Date())
        findViewById<Button>(R.id.saveButton).setOnClickListener { saveToday() }
        findViewById<Button>(R.id.monthsButton).setOnClickListener { showMonths() }
        findViewById<Button>(R.id.overallButton).setOnClickListener { showOverall() }
        refresh()
    }
    private fun refresh() { val m=records(); val today=m[keyToday()] ?: 0L; todayTotal.text=formatNum(today); overallTotal.text=formatNum(m.values.sum()); if(today>0) input.setText(today.toString()) }
    private fun saveToday() {
        val value=input.text.toString().trim().toLongOrNull()
        if(value==null || value<0){ input.error="Enter a valid number"; return }
        val m=records(); m[keyToday()]=value; save(m); refresh()
        Toast.makeText(this,"Today's record saved: ${formatNum(value)}",Toast.LENGTH_SHORT).show()
    }
    private fun showMonths() {
        val m=records(); if(m.isEmpty()){ toast("Abhi koi record nahi hai."); return }
        val months=mutableMapOf<String,Long>()
        m.forEach{(d,v)-> val month=d.substring(0,7); months[month]=(months[month]?:0)+v }
        val keys=months.keys.sortedDescending(); val labels=keys.map{ monthLabel(it)+"   •   "+formatNum(months[it]!!)}.toTypedArray()
        AlertDialog.Builder(this).setTitle("📅 Month-wise Records").setItems(labels){_,which-> showMonth(keys[which], months[keys[which]]?:0)}.setNegativeButton("Close",null).show()
    }
    private fun monthLabel(key:String):String { val d=SimpleDateFormat("yyyy-MM",Locale.US).parse(key)!!; return SimpleDateFormat("MMMM yyyy",Locale.US).format(d) }
    private fun showMonth(month:String,total:Long) {
        val m=records(); val days=m.keys.filter{it.startsWith(month)}.sortedDescending()
        val text=StringBuilder("${monthLabel(month)}\nTotal: ${formatNum(total)} times\n\n")
        days.forEach{ d-> val date=SimpleDateFormat("yyyy-MM-dd",Locale.US).parse(d)!!; text.append(SimpleDateFormat("dd MMM yyyy, EEEE",Locale.US).format(date)).append("  —  ").append(formatNum(m[d]!!)).append("\n") }
        val builder=AlertDialog.Builder(this).setTitle("${monthLabel(month)} • ${formatNum(total)}").setMessage(text.toString())
        builder.setPositiveButton("Edit Record") {_,_-> chooseEdit(days) }.setNegativeButton("Close",null).show()
    }
    private fun chooseEdit(days:List<String>) {
        if(days.isEmpty()) return
        val labels=days.map{d-> val date=SimpleDateFormat("yyyy-MM-dd",Locale.US).parse(d)!!; SimpleDateFormat("dd MMM yyyy",Locale.US).format(date)+"  —  "+formatNum(records()[d]!!)}.toTypedArray()
        AlertDialog.Builder(this).setTitle("Choose a date").setItems(labels){_,which-> editRecord(days[which])}.setNegativeButton("Close",null).show()
    }
    private fun editRecord(date:String) {
        val m=records(); val box=EditText(this); box.inputType=2; box.setText((m[date]?:0).toString()); box.setSelectAllOnFocus(true); box.hint="Durood count"
        val day=SimpleDateFormat("yyyy-MM-dd",Locale.US).parse(date)!!
        AlertDialog.Builder(this).setTitle("Edit • "+SimpleDateFormat("dd MMMM yyyy",Locale.US).format(day)).setView(box)
            .setPositiveButton("UPDATE"){_,_-> val v=box.text.toString().toLongOrNull(); if(v!=null&&v>=0){m[date]=v;save(m);refresh();toast("Record updated")} }
            .setNeutralButton("DELETE"){_,_-> m.remove(date);save(m);refresh();toast("Record deleted")}.setNegativeButton("Cancel",null).show()
    }
    private fun showOverall(){ val total=records().values.sum(); AlertDialog.Builder(this).setTitle("✦ All-Time Durood e Pak Total").setMessage("${formatNum(total)} times\n\nMay Allah accept your Durood e Pak. Aameen.").setPositiveButton("Close",null).show() }
    private fun toast(s:String){Toast.makeText(this,s,Toast.LENGTH_SHORT).show()}
}
