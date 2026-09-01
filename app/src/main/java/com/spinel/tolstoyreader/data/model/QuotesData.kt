package com.spinel.tolstoyreader.data.model

data class Quote(val text: String, val author: String, val language: String)

object QuotesData {
    private val allQuotes = listOf(
        Quote("If you want to be happy, be.", "Leo Tolstoy", "en"),
        Quote("Everyone thinks of changing the world, but no one thinks of changing himself.", "Leo Tolstoy", "en"),
        Quote("The two most powerful warriors are patience and time.", "Leo Tolstoy", "en"),
        Quote("All, everything that I understand, I understand only because I love.", "Leo Tolstoy", "en"),
        
        Quote("إذا أردت أن تكون سعيداً، فكن.", "ليو تولستوي", "ar"),
        Quote("الجميع يفكر في تغيير العالم، لكن لا أحد يفكر في تغيير نفسه.", "ليو تولستوي", "ar"),
        Quote("أقوى المحاربين هما الصبر والوقت.", "ليو تولستوي", "ar"),
        Quote("كل ما أفهمه، أفهمه فقط لأنني أحب.", "ليو تولستوي", "ar"),
        
        Quote("Если хочешь быть счастливым, будь им.", "Лев Толстой", "ru"),
        Quote("Каждый думает изменить мир, но никто не думает изменить себя.", "Лев Толстой", "ru"),
        Quote("Два самых сильных воина — терпение и время.", "Лев Толстой", "ru"),
        Quote("Все, что я понимаю, я понимаю только потому, что люблю.", "Лев Толстой", "ru")
    )

    private val lastQuoteIndices = mutableMapOf<String, Int>()

    fun getRandomQuote(language: String): Quote {
        val langQuotes = allQuotes.filter { it.language == language }.ifEmpty { allQuotes.filter { it.language == "en" } }
        if (langQuotes.size <= 1) return langQuotes.firstOrNull() ?: allQuotes.first()

        val lastIndex = lastQuoteIndices[language] ?: -1
        var newIndex: Int
        do {
            newIndex = langQuotes.indices.random()
        } while (newIndex == lastIndex)
        
        lastQuoteIndices[language] = newIndex
        return langQuotes[newIndex]
    }
}
