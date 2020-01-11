package com.andruid.magic.newsdaily.util

class CategoryUtil {
    companion object {
        @JvmStatic
        fun getCategories(): List<String> {
            return listOf("general", "business", "entertainment", "health", "science", "sports",
                    "technology")
        }
    }
}