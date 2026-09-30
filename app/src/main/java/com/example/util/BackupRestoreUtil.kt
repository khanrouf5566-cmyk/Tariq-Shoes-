package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.PurchaseEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.data.local.entity.StockTransactionEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.repository.BackupData
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupRestoreUtil {

    fun createBackupJson(data: BackupData): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())
        root.put("appName", "Tariq Shoes & Wholesale Dealer")

        // Products
        val prodsArr = JSONArray()
        data.products.forEach { p ->
            val obj = JSONObject().apply {
                put("id", p.id)
                put("productId", p.productId)
                put("name", p.name)
                put("category", p.category)
                put("design", p.design)
                put("brand", p.brand)
                put("size", p.size)
                put("color", p.color)
                put("purchasePrice", p.purchasePrice)
                put("retailPrice", p.retailPrice)
                put("wholesalePrice", p.wholesalePrice)
                put("quantity", p.quantity)
                put("supplierName", p.supplierName ?: "")
                put("description", p.description)
                put("dateAdded", p.dateAdded)
                put("minStockLevel", p.minStockLevel)
                put("barcode", p.barcode)
            }
            prodsArr.put(obj)
        }
        root.put("products", prodsArr)

        // Customers
        val custArr = JSONArray()
        data.customers.forEach { c ->
            val obj = JSONObject().apply {
                put("id", c.id)
                put("customerId", c.customerId)
                put("name", c.name)
                put("phone", c.phone)
                put("address", c.address)
                put("totalPurchases", c.totalPurchases)
                put("totalPaid", c.totalPaid)
                put("remainingBalance", c.remainingBalance)
                put("createdAt", c.createdAt)
            }
            custArr.put(obj)
        }
        root.put("customers", custArr)

        // Suppliers
        val suppArr = JSONArray()
        data.suppliers.forEach { s ->
            val obj = JSONObject().apply {
                put("id", s.id)
                put("supplierId", s.supplierId)
                put("name", s.name)
                put("phone", s.phone)
                put("address", s.address)
                put("productsSupplied", s.productsSupplied)
                put("totalPurchased", s.totalPurchased)
                put("totalPaid", s.totalPaid)
                put("remainingBalance", s.remainingBalance)
            }
            suppArr.put(obj)
        }
        root.put("suppliers", suppArr)

        // Sales
        val salesArr = JSONArray()
        data.sales.forEach { s ->
            val obj = JSONObject().apply {
                put("id", s.id)
                put("invoiceNumber", s.invoiceNumber)
                put("customerId", s.customerId ?: -1L)
                put("customerName", s.customerName)
                put("customerPhone", s.customerPhone)
                put("subtotal", s.subtotal)
                put("discount", s.discount)
                put("grandTotal", s.grandTotal)
                put("paidAmount", s.paidAmount)
                put("remainingAmount", s.remainingAmount)
                put("profit", s.profit)
                put("paymentMethod", s.paymentMethod)
                put("date", s.date)
                put("notes", s.notes)
            }
            salesArr.put(obj)
        }
        root.put("sales", salesArr)

        // Sale Items
        val itemsArr = JSONArray()
        data.saleItems.forEach { i ->
            val obj = JSONObject().apply {
                put("id", i.id)
                put("saleId", i.saleId)
                put("invoiceNumber", i.invoiceNumber)
                put("productId", i.productId)
                put("productName", i.productName)
                put("size", i.size)
                put("color", i.color)
                put("quantity", i.quantity)
                put("purchasePrice", i.purchasePrice)
                put("unitPrice", i.unitPrice)
                put("subtotal", i.subtotal)
                put("profit", i.profit)
            }
            itemsArr.put(obj)
        }
        root.put("saleItems", itemsArr)

        // Purchases
        val purchasesArr = JSONArray()
        data.purchases.forEach { pu ->
            val obj = JSONObject().apply {
                put("id", pu.id)
                put("purchaseNumber", pu.purchaseNumber)
                put("supplierName", pu.supplierName)
                put("productId", pu.productId)
                put("productName", pu.productName)
                put("quantity", pu.quantity)
                put("purchasePrice", pu.purchasePrice)
                put("totalCost", pu.totalCost)
                put("paidAmount", pu.paidAmount)
                put("remainingAmount", pu.remainingAmount)
                put("date", pu.date)
            }
            purchasesArr.put(obj)
        }
        root.put("purchases", purchasesArr)

        // Payments
        val paymentsArr = JSONArray()
        data.payments.forEach { pay ->
            val obj = JSONObject().apply {
                put("id", pay.id)
                put("receiptNumber", pay.receiptNumber)
                put("customerId", pay.customerId ?: -1L)
                put("customerName", pay.customerName)
                put("amount", pay.amount)
                put("paymentMethod", pay.paymentMethod)
                put("date", pay.date)
            }
            paymentsArr.put(obj)
        }
        root.put("payments", paymentsArr)

        // Stock Transactions
        val transArr = JSONArray()
        data.stockTransactions.forEach { st ->
            val obj = JSONObject().apply {
                put("id", st.id)
                put("productId", st.productId)
                put("productName", st.productName)
                put("quantityChange", st.quantityChange)
                put("remainingStockAfter", st.remainingStockAfter)
                put("type", st.type)
                put("referenceId", st.referenceId)
                put("reason", st.reason)
                put("date", st.date)
            }
            transArr.put(obj)
        }
        root.put("stockTransactions", transArr)

        // Invoices
        val invArr = JSONArray()
        data.invoices.forEach { inv ->
            val obj = JSONObject().apply {
                put("id", inv.id)
                put("invoiceNumber", inv.invoiceNumber)
                put("saleId", inv.saleId)
                put("customerName", inv.customerName)
                put("customerPhone", inv.customerPhone)
                put("date", inv.date)
                put("grandTotal", inv.grandTotal)
                put("paidAmount", inv.paidAmount)
                put("remainingAmount", inv.remainingAmount)
                put("paymentMethod", inv.paymentMethod)
            }
            invArr.put(obj)
        }
        root.put("invoices", invArr)

        return root.toString(2)
    }

    fun saveBackupToFile(context: Context, jsonData: String): File {
        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) backupDir.mkdirs()

        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val filename = "tariq_shoes_backup_${sdf.format(Date())}.json"
        val file = File(backupDir, filename)

        FileOutputStream(file).use { out ->
            out.write(jsonData.toByteArray(Charsets.UTF_8))
        }
        return file
    }

    fun shareBackupFile(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Database Backup"))
    }

    fun parseBackupJson(jsonString: String): BackupData {
        val root = JSONObject(jsonString)

        val prods = mutableListOf<ProductEntity>()
        val prodsArr = root.optJSONArray("products") ?: JSONArray()
        for (i in 0 until prodsArr.length()) {
            val obj = prodsArr.getJSONObject(i)
            prods.add(
                ProductEntity(
                    productId = obj.optString("productId", "TS-${1000 + i}"),
                    name = obj.optString("name", "Product $i"),
                    category = obj.optString("category", "General"),
                    design = obj.optString("design", ""),
                    brand = obj.optString("brand", "Tariq Shoes"),
                    size = obj.optString("size", "42"),
                    color = obj.optString("color", "Black"),
                    purchasePrice = obj.optDouble("purchasePrice", 0.0),
                    retailPrice = obj.optDouble("retailPrice", 0.0),
                    wholesalePrice = obj.optDouble("wholesalePrice", 0.0),
                    quantity = obj.optInt("quantity", 0),
                    supplierName = obj.optString("supplierName", ""),
                    description = obj.optString("description", ""),
                    dateAdded = obj.optLong("dateAdded", System.currentTimeMillis()),
                    minStockLevel = obj.optInt("minStockLevel", 5),
                    barcode = obj.optString("barcode", obj.optString("productId", ""))
                )
            )
        }

        val customers = mutableListOf<CustomerEntity>()
        val custArr = root.optJSONArray("customers") ?: JSONArray()
        for (i in 0 until custArr.length()) {
            val obj = custArr.getJSONObject(i)
            customers.add(
                CustomerEntity(
                    customerId = obj.optString("customerId", "CUST-$i"),
                    name = obj.optString("name", "Customer $i"),
                    phone = obj.optString("phone", ""),
                    address = obj.optString("address", ""),
                    totalPurchases = obj.optDouble("totalPurchases", 0.0),
                    totalPaid = obj.optDouble("totalPaid", 0.0),
                    remainingBalance = obj.optDouble("remainingBalance", 0.0),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        val suppliers = mutableListOf<SupplierEntity>()
        val suppArr = root.optJSONArray("suppliers") ?: JSONArray()
        for (i in 0 until suppArr.length()) {
            val obj = suppArr.getJSONObject(i)
            suppliers.add(
                SupplierEntity(
                    supplierId = obj.optString("supplierId", "SUP-$i"),
                    name = obj.optString("name", "Supplier $i"),
                    phone = obj.optString("phone", ""),
                    address = obj.optString("address", ""),
                    productsSupplied = obj.optString("productsSupplied", ""),
                    totalPurchased = obj.optDouble("totalPurchased", 0.0),
                    totalPaid = obj.optDouble("totalPaid", 0.0),
                    remainingBalance = obj.optDouble("remainingBalance", 0.0)
                )
            )
        }

        val sales = mutableListOf<SaleEntity>()
        val salesArr = root.optJSONArray("sales") ?: JSONArray()
        for (i in 0 until salesArr.length()) {
            val obj = salesArr.getJSONObject(i)
            val cId = obj.optLong("customerId", -1L)
            sales.add(
                SaleEntity(
                    invoiceNumber = obj.optString("invoiceNumber", "INV-$i"),
                    customerId = if (cId > 0) cId else null,
                    customerName = obj.optString("customerName", "Customer"),
                    customerPhone = obj.optString("customerPhone", ""),
                    subtotal = obj.optDouble("subtotal", 0.0),
                    discount = obj.optDouble("discount", 0.0),
                    grandTotal = obj.optDouble("grandTotal", 0.0),
                    paidAmount = obj.optDouble("paidAmount", 0.0),
                    remainingAmount = obj.optDouble("remainingAmount", 0.0),
                    profit = obj.optDouble("profit", 0.0),
                    paymentMethod = obj.optString("paymentMethod", "Cash"),
                    date = obj.optLong("date", System.currentTimeMillis()),
                    notes = obj.optString("notes", "")
                )
            )
        }

        val saleItems = mutableListOf<SaleItemEntity>()
        val itemsArr = root.optJSONArray("saleItems") ?: JSONArray()
        for (i in 0 until itemsArr.length()) {
            val obj = itemsArr.getJSONObject(i)
            saleItems.add(
                SaleItemEntity(
                    saleId = obj.optLong("saleId", 0L),
                    invoiceNumber = obj.optString("invoiceNumber", ""),
                    productId = obj.optString("productId", ""),
                    productName = obj.optString("productName", ""),
                    size = obj.optString("size", "42"),
                    color = obj.optString("color", "Black"),
                    quantity = obj.optInt("quantity", 1),
                    purchasePrice = obj.optDouble("purchasePrice", 0.0),
                    unitPrice = obj.optDouble("unitPrice", 0.0),
                    subtotal = obj.optDouble("subtotal", 0.0),
                    profit = obj.optDouble("profit", 0.0)
                )
            )
        }

        val purchases = mutableListOf<PurchaseEntity>()
        val purchasesArr = root.optJSONArray("purchases") ?: JSONArray()
        for (i in 0 until purchasesArr.length()) {
            val obj = purchasesArr.getJSONObject(i)
            purchases.add(
                PurchaseEntity(
                    purchaseNumber = obj.optString("purchaseNumber", "PUR-$i"),
                    supplierName = obj.optString("supplierName", ""),
                    productId = obj.optString("productId", ""),
                    productName = obj.optString("productName", ""),
                    quantity = obj.optInt("quantity", 1),
                    purchasePrice = obj.optDouble("purchasePrice", 0.0),
                    totalCost = obj.optDouble("totalCost", 0.0),
                    paidAmount = obj.optDouble("paidAmount", 0.0),
                    remainingAmount = obj.optDouble("remainingAmount", 0.0),
                    date = obj.optLong("date", System.currentTimeMillis())
                )
            )
        }

        val payments = mutableListOf<PaymentEntity>()
        val paymentsArr = root.optJSONArray("payments") ?: JSONArray()
        for (i in 0 until paymentsArr.length()) {
            val obj = paymentsArr.getJSONObject(i)
            payments.add(
                PaymentEntity(
                    receiptNumber = obj.optString("receiptNumber", "REC-$i"),
                    customerName = obj.optString("customerName", ""),
                    amount = obj.optDouble("amount", 0.0),
                    paymentMethod = obj.optString("paymentMethod", "Cash"),
                    date = obj.optLong("date", System.currentTimeMillis())
                )
            )
        }

        val stockTransactions = mutableListOf<StockTransactionEntity>()
        val transArr = root.optJSONArray("stockTransactions") ?: JSONArray()
        for (i in 0 until transArr.length()) {
            val obj = transArr.getJSONObject(i)
            stockTransactions.add(
                StockTransactionEntity(
                    productId = obj.optString("productId", ""),
                    productName = obj.optString("productName", ""),
                    quantityChange = obj.optInt("quantityChange", 0),
                    remainingStockAfter = obj.optInt("remainingStockAfter", 0),
                    type = obj.optString("type", "ADJUSTMENT"),
                    referenceId = obj.optString("referenceId", ""),
                    reason = obj.optString("reason", ""),
                    date = obj.optLong("date", System.currentTimeMillis())
                )
            )
        }

        val invoices = mutableListOf<InvoiceEntity>()
        val invArr = root.optJSONArray("invoices") ?: JSONArray()
        for (i in 0 until invArr.length()) {
            val obj = invArr.getJSONObject(i)
            invoices.add(
                InvoiceEntity(
                    invoiceNumber = obj.optString("invoiceNumber", "INV-$i"),
                    saleId = obj.optLong("saleId", 0L),
                    customerName = obj.optString("customerName", ""),
                    customerPhone = obj.optString("customerPhone", ""),
                    date = obj.optLong("date", System.currentTimeMillis()),
                    subtotal = obj.optDouble("subtotal", 0.0),
                    discount = obj.optDouble("discount", 0.0),
                    grandTotal = obj.optDouble("grandTotal", 0.0),
                    paidAmount = obj.optDouble("paidAmount", 0.0),
                    remainingAmount = obj.optDouble("remainingAmount", 0.0),
                    paymentMethod = obj.optString("paymentMethod", "Cash")
                )
            )
        }

        return BackupData(
            products = prods,
            customers = customers,
            suppliers = suppliers,
            sales = sales,
            saleItems = saleItems,
            purchases = purchases,
            payments = payments,
            stockTransactions = stockTransactions,
            invoices = invoices,
            settings = AppSettingsEntity()
        )
    }
}
