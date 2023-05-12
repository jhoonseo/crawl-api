package com.costco.crawl.dao

object CategoryQuery {
    fun getCategoryLinks(): String {
        return """
            select category from costco_category where status = 1
        """.trimIndent()
    }
}