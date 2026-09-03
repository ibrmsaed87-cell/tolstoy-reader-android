package com.spinel.tolstoyreader.data.model

data class Quote(val text: String, val author: String, val language: String)

object QuotesData {
    private val allQuotes = listOf(
        Quote("If you want to be happy, be.", "Leo Tolstoy", "en"),
        Quote("Everyone thinks of changing the world, but no one thinks of changing himself.", "Leo Tolstoy", "en"),
        Quote("The two most powerful warriors are patience and time.", "Leo Tolstoy", "en"),
        Quote("All, everything that I understand, I understand only because I love.", "Leo Tolstoy", "en"),
        Quote("Wrong does not cease to be wrong because the majority share in it.", "Leo Tolstoy", "en"),
        Quote("A man is like a fraction whose numerator is what he is and whose denominator is what he thinks of himself.", "Leo Tolstoy", "en"),
        Quote("There is no greatness where there is not simplicity, goodness, and truth.", "Leo Tolstoy", "en"),
        Quote("True life is lived when tiny changes occur.", "Leo Tolstoy", "en"),
        Quote("If you look for perfection, you'll never be content.", "Leo Tolstoy", "en"),
        Quote("Joy can only be real if people look upon their life as a service.", "Leo Tolstoy", "en"),
        Quote("Boredom: the desire for desires.", "Leo Tolstoy", "en"),
        Quote("Energy is the essence of life.", "Leo Tolstoy", "en"),
        Quote("We lost because we told ourselves we lost.", "Leo Tolstoy", "en"),
        Quote("Love is life. All, everything that I understand, I understand only because I love.", "Leo Tolstoy", "en"),

        Quote("إذا أردت أن تكون سعيداً، فكن.", "ليو تولستوي", "ar"),
        Quote("الجميع يفكر في تغيير العالم، لكن لا أحد يفكر في تغيير نفسه.", "ليو تولستوي", "ar"),
        Quote("أقوى المحاربين هما الصبر والوقت.", "ليو تولستوي", "ar"),
        Quote("كل ما أفهمه، أفهمه فقط لأنني أحب.", "ليو تولستوي", "ar"),
        Quote("الخطأ لا يصبح صحيحاً لمجرد أن الأغلبية تشارك فيه.", "ليو تولستوي", "ar"),
        Quote("الإنسان كسر اعتيادي، بسطه هو حقيقته ومقامه هو ما يعتقده عن نفسه.", "ليو تولستوي", "ar"),
        Quote("لا توجد عظمة حيث لا توجد بساطة، طيبة، وحقيقة.", "ليو تولستوي", "ar"),
        Quote("الحياة الحقيقية تُعاش عندما تحدث تغييرات صغيرة.", "ليو تولستوي", "ar"),
        Quote("إذا بحثت عن الكمال، فلن تكون راضياً أبداً.", "ليو تولستوي", "ar"),
        Quote("الفرح الحقيقي لا يكون إلا عندما ينظر الناس إلى حياتهم على أنها خدمة.", "ليو تولستوي", "ar"),
        Quote("الملل هو الرغبة في امتلاك الرغبات.", "ليو تولستوي", "ar"),
        Quote("الطاقة هي جوهر الحياة.", "ليو تولستوي", "ar"),
        Quote("لقد هُزمنا لأننا قلنا لأنفسنا أننا هُزمنا.", "ليو تولستوي", "ar"),
        Quote("الحب هو الحياة. كل ما أفهمه في هذا العالم، أفهمه فقط لأنني أحب.", "ليو تولستوي", "ar"),

        Quote("Если хочешь быть счастливым, будь им.", "Лев Толстой", "ru"),
        Quote("Каждый думает изменить мир, но никто не думает изменить себя.", "Лев Толстой", "ru"),
        Quote("Два самых сильных воина — терпение и время.", "Лев Толстой", "ru"),
        Quote("Все, что я понимаю, я понимаю только потому, что люблю.", "Лев Толстой", "ru"),
        Quote("Зло не перестает быть злом оттого, что в нем участвует большинство.", "Лев Толстой", "ru"),
        Quote("Человек подобен дроби: в знаменателе — то, что он о себе думает, в числителе — то, что он есть на самом деле.", "Лев Толстой", "ru"),
        Quote("Нет величия там, где нет простоты, добра и правды.", "Лев Толстой", "ru"),
        Quote("Истинная жизнь совершается там, где совершаются крошечные изменения.", "Лев Толстой", "ru"),
        Quote("Если искать совершенства, никогда не будешь доволен.", "Лев Толстой", "ru"),
        Quote("Радость только тогда настоящая, когда люди смотрят на свою жизнь как на служение.", "Лев Толстой", "ru"),
        Quote("Скука — это желание желаний.", "Лев Толстой", "ru"),
        Quote("Энергия — это сущность жизни.", "Лев Толстой", "ru"),
        Quote("Мы проиграли, потому что сказали себе, что проиграли.", "Лев Толстой", "ru"),
        Quote("Любовь есть жизнь. Все, что я понимаю, я понимаю только потому, что люблю.", "Лев Толстой", "ru")
    )

    private val lastQuoteIndices = mutableMapOf<String, Int>()

    fun getRandomQuote(language: String): Quote {
        // Only return quotes for the EXACT requested language to avoid mixed languages.
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
