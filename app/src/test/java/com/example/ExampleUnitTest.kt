package com.example

import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testShoeBusinessProfitCalculation() {
        val purchasePrice = 1500.0
        val retailPrice = 2500.0
        val initialStock = 20
        val soldQuantity = 2

        val remainingStock = initialStock - soldQuantity
        val totalSale = soldQuantity * retailPrice
        val totalCost = soldQuantity * purchasePrice
        val profit = totalSale - totalCost

        assertEquals(18, remainingStock)
        assertEquals(5000.0, totalSale, 0.001)
        assertEquals(3000.0, totalCost, 0.001)
        assertEquals(2000.0, profit, 0.001)
    }

    @Test
    fun testInvoicePrefixFormatting() {
        val nextSeq = 1001
        val invoiceNumber = "INV-%04d".format(nextSeq)
        assertEquals("INV-1001", invoiceNumber)
    }
}
