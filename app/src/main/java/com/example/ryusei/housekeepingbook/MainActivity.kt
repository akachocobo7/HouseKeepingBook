package com.example.ryusei.housekeepingbook

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.list_item.view.*
import android.support.design.widget.Snackbar
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    // 家計簿のデータクラス
    data class CategoryAndPriceData(val category: String, val price: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.addButton)
        findViewById<EditText>(R.id.editCategory)
        findViewById<EditText>(R.id.editPrice)
        findViewById<ListView>(R.id.listView)

        val localFileName = "data.txt" // 家計簿のデータを保存するファイル
        // 家計簿のデータリスト
        val dataList: MutableList<CategoryAndPriceData> = mutableListOf()
        readFile(dataList, localFileName)

        data class ViewHolder(val categoryTextView: TextView, val priceTextView: TextView)

        class DataListAdapter(context: Context, data: List<CategoryAndPriceData>) :
                ArrayAdapter<CategoryAndPriceData>(context, 0, data){
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                var view = convertView
                var holder: ViewHolder

                if(view == null){
                    view = layoutInflater.inflate(R.layout.list_item, parent, false)
                    holder = ViewHolder(
                        view?.categoryTextView!!,
                        view.priceTextView
                    )
                    view.tag = holder
                }
                else{
                    holder = view.tag as ViewHolder
                }

                var tmp = getItem(position) as CategoryAndPriceData
                holder.categoryTextView.text = tmp.category
                holder.priceTextView.text = tmp.price.toString()

                return view
            }
        }

        val adapter = DataListAdapter(this, dataList)
        listView.adapter = adapter



        addButton.setOnClickListener { view ->
            if(editCategory.text.isEmpty() || editPrice.text.isEmpty()){
                Snackbar.make(view, "内容と金額を入力してください", Snackbar.LENGTH_SHORT).show()
            }
            else {
                try {
                    dataList.add( CategoryAndPriceData(editCategory.text.toString(), editPrice.text.toString().toInt() ) )
                    adapter.notifyDataSetChanged() // adapterに変更を通知
                    saveFile(dataList, localFileName)
                } catch (e: NumberFormatException) {
                    Snackbar.make(view, "金額には数字を入力してください", Snackbar.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }

        // 長押しで削除
        listView.setOnItemLongClickListener { parent, view, position, id ->
            dataList.removeAt(position)
            adapter.notifyDataSetChanged() // adapterに変更を通知
            saveFile(dataList, localFileName)
            Snackbar.make(findViewById(android.R.id.content), "削除しました", Snackbar.LENGTH_SHORT).show()
            return@setOnItemLongClickListener true
        }
    }

    // ファイルのデータを読み込み
    fun readFile(dataList: MutableList<CategoryAndPriceData>,
                 fileName: String) {
        try {
            openFileInput(fileName).use { fileInputStream ->
                BufferedReader(
                    InputStreamReader(fileInputStream, "UTF-8")
                ).use({ reader ->
                    var lineBuffer: String? = reader.readLine()
                    while (lineBuffer != null) {
                        val text = lineBuffer.split((","))
                        dataList.add( CategoryAndPriceData(text[0], text[1].toInt()))
                        lineBuffer = reader.readLine()
                    }
                })
            }
        } catch (e: IOException){
            e.printStackTrace()
        }
    }

    // dataをファイルへ保存
    fun saveFile(dataList: MutableList<CategoryAndPriceData>, fileName: String){
        try {
            openFileOutput(
                fileName,
                Context.MODE_PRIVATE
            ).use { fileOutputstream ->
                for (data in dataList) {
                    fileOutputstream.write((data.category + "," + data.price + "\n").toByteArray())
                }
            }
        } catch (e: IOException){
            e.printStackTrace()
        }
    }
}
