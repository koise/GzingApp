package com.example.gzingapp.utils

object InitialsGenerator {
    
    /**
     * Generate initials from a name
     * @param name The full name (e.g., "Bart Jason")
     * @return Initials (e.g., "BJ")
     */
    fun generateInitials(name: String): String {
        if (name.isBlank()) return ""
        
        val words = name.trim().split("\\s+".toRegex())
            .filter { it.isNotBlank() }
            .map { it.trim() }
        
        return when (words.size) {
            0 -> ""
            1 -> words[0].take(2).uppercase()
            else -> {
                val firstInitial = words[0].first().uppercaseChar()
                val lastInitial = words.last().first().uppercaseChar()
                "$firstInitial$lastInitial"
            }
        }
    }
    
    /**
     * Generate initials from first and last name separately
     * @param firstName First name
     * @param lastName Last name
     * @return Initials (e.g., "BJ")
     */
    fun generateInitials(firstName: String, lastName: String): String {
        val first = firstName.trim().takeIf { it.isNotBlank() }?.first()?.uppercaseChar() ?: ""
        val last = lastName.trim().takeIf { it.isNotBlank() }?.first()?.uppercaseChar() ?: ""
        return "$first$last"
    }
    
    /**
     * Generate a color for the initials based on the name
     * @param name The name to generate color for
     * @return A color in hex format
     */
    fun generateColor(name: String): String {
        val colors = listOf(
            "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7",
            "#DDA0DD", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E9",
            "#F8C471", "#82E0AA", "#F1948A", "#85C1E9", "#D7BDE2"
        )
        
        val hash = name.hashCode()
        val index = kotlin.math.abs(hash) % colors.size
        return colors[index]
    }
}

