package com.messaoud.bonpeint

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class Item(val name: String, val quantity: String)

class MainActivity : Activity() {

    private val items = mutableListOf<Item>()
    private var isFrench = true

    private val docTypesFr = arrayOf(
        "ÉTAT DES FOURNITURES ACHETÉES",
        "BON DE COMMANDE",
        "BON DE LIVRAISON",
        "BON D’ACHAT"
    )

    private val docTypesAr = arrayOf(
        "كشف المواد المشتراة",
        "طلب شراء (سلعة)",
        "وصل تسليم السلعة",
        "وصل شراء"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val spinnerDocType = findViewById<Spinner>(R.id.spinnerDocType)
        val editClient = findViewById<EditText>(R.id.editClient)
        val editProject = findViewById<EditText>(R.id.editProject)
        val editItemName = findViewById<EditText>(R.id.editItemName)
        val editItemQty = findViewById<EditText>(R.id.editItemQty)
        val btnAdd = findViewById<Button>(R.id.btnAdd)
        val btnGenerate = findViewById<Button>(R.id.btnGenerate)
        val btnLang = findViewById<Button>(R.id.btnLang)

        fun updateSpinner() {
            val list = if (isFrench) docTypesFr else docTypesAr
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, list)
            spinnerDocType.adapter = adapter
        }

        updateSpinner()

        btnLang.setOnClickListener {
            isFrench = !isFrench
            btnLang.text = if (isFrench) "Langue: FR" else "اللغة: عربي"
            updateSpinner()
        }

        btnAdd.setOnClickListener {
            val name = editItemName.text.toString().trim()
            val qty = editItemQty.text.toString().trim()
            if (name.isNotEmpty() && qty.isNotEmpty()) {
                items.add(Item(name, qty))
                editItemName.text.clear()
                editItemQty.text.clear()
                Toast.makeText(this, if (isFrench) "Ajouté" else "تمت الإضافة", Toast.LENGTH_SHORT).show()
            }
        }

        btnGenerate.setOnClickListener {
            if (items.isEmpty()) {
                Toast.makeText(this, if (isFrench) "La liste est vide!" else "القائمة فارغة!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedType = spinnerDocType.selectedItem.toString()
            val client = editClient.text.toString().ifEmpty { if (isFrench) "Client" else "الزبون" }
            val project = editProject.text.toString().ifEmpty { if (isFrench) "Chantier" else "الورشة" }
            generatePdf(selectedType, client, project, items, isFrench)
        }
    }

    private fun generatePdf(docTitle: String, client: String, project: String, list: List<Item>, langFr: Boolean) {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        val paintText = Paint().apply { color = Color.BLACK; textSize = 11f }
        val paintTitle = Paint().apply { color = Color.BLACK; textSize = 14f; isFakeBoldText = true }
        val paintHeader = Paint().apply { color = Color.BLACK; textSize = 15f; isFakeBoldText = true }
        val paintBorder = Paint().apply { color = Color.DKGRAY; strokeWidth = 1f; style = Paint.Style.STROKE }

        val dateStr = SimpleDateFormat("dd MMMM yyyy", if (langFr) Locale.FRENCH else Locale("ar")).format(Date())

        if (langFr) {
            canvas.drawText(docTitle, 160f, 60f, paintHeader)
            canvas.drawText("PROJET : ${project.uppercase()}", 180f, 85f, paintTitle)
            canvas.drawText("Hassi Messaoud, le $dateStr", 370f, 120f, paintText)
            canvas.drawText("Destinataire : $client", 40f, 150f, paintText)
            canvas.drawText("Objet : $docTitle pour les travaux de la villa.", 40f, 175f, paintText)
        } else {
            canvas.drawText(docTitle, 220f, 60f, paintHeader)
            canvas.drawText("المشروع: $project", 230f, 85f, paintTitle)
            canvas.drawText("حاسي مسعود، في $dateStr", 370f, 120f, paintText)
            canvas.drawText("المستلم: $client", 40f, 150f, paintText)
            canvas.drawText("الموضوع: $docTitle الخاصة بأشغال الورشة.", 40f, 175f, paintText)
        }

        var y = 230f
        canvas.drawRect(40f, y - 20f, 550f, y + 10f, paintBorder)
        canvas.drawText("N°", 55f, y, paintTitle)
        canvas.drawText(if (langFr) "Désignation" else "التعيين / المادة", 120f, y, paintTitle)
        canvas.drawText(if (langFr) "Quantité" else "الكمية", 440f, y, paintTitle)

        y += 20f
        list.forEachIndexed { index, item ->
            canvas.drawRect(40f, y - 15f, 550f, y + 15f, paintBorder)
            canvas.drawText("${index + 1}", 55f, y + 5f, paintText)
            canvas.drawText(item.name, 120f, y + 5f, paintText)
            canvas.drawText(item.quantity, 440f, y + 5f, paintText)
            y += 30f
        }

        y += 40f
        val closing = if (langFr) {
            "Fournitures enregistrées pour assurer la bonne continuité des travaux."
        } else {
            "تم تسجيل المواد المذكورة لضمان استمرار ومتابعة الأشغال في أحسن الظروف."
        }
        canvas.drawText(closing, 40f, y, paintText)

        doc.finishPage(page)

        val file = File(getExternalFilesDir(null), "document_fournitures.pdf")
        doc.writeTo(FileOutputStream(file))
        doc.close()

        val uri: Uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Partager"))
    }
}
