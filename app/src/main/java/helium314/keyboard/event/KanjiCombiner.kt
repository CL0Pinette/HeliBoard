// SPDX-License-Identifier: GPL-3.0-only

package helium314.keyboard.event

import helium314.keyboard.keyboard.internal.keyboard_parser.floris.KeyCode
import helium314.keyboard.latin.common.Constants
import helium314.keyboard.latin.utils.Log
import java.lang.StringBuilder
import java.util.ArrayList

class KanjiCombiner : Combiner {

    enum class MORA_TYPE {
        SIMPLE, DAKUTEN, HANDAKUTEN, YOON, SOKUON
    }

    protected val composingWord = StringBuilder()

    val history: MutableList<Mora> = mutableListOf()
    private val syllable: Mora? get() = history.lastOrNull()

    override fun processEvent(previousEvents: ArrayList<Event>?, event: Event): Event {
        if (event.mKeyCode == KeyCode.SHIFT) return event
        if (Character.isWhitespace(event.mCodePoint)) {
            val text = combiningStateFeedback
            reset()
            return createEventChainFromSequence(text, event)
        } else if (event.isFunctionalKeyEvent) {
            if(event.mKeyCode == KeyCode.DELETE) {
                return when {
                    history.size == 1 && composingWord.isEmpty() || history.isEmpty() && composingWord.length == 1 -> {
                        reset()
                        Event.createHardwareKeypressEvent(0x20, Constants.CODE_SPACE, 0, event, event.isKeyRepeat)
                    }
                    history.isNotEmpty() -> {
                        history.removeAt(history.lastIndex)
                        Event.createConsumedEvent(event)
                    }
                    composingWord.isNotEmpty() -> {
                        composingWord.deleteCharAt(composingWord.lastIndex)
                        Event.createConsumedEvent(event)
                    }
                    else -> event
                }
            }
            val text = combiningStateFeedback
            reset()
            return createEventChainFromSequence(text, event)
        } else {
            val currentSyllable = syllable ?: Mora()
            val kana = Kana.of(event.mCodePoint)
            if (!event.isCombining || kana is Kana.NonKana) {
                composingWord.append(currentSyllable.string)
                composingWord.append(kana.string)
                history.clear()
            } else {
                when (kana) {
                    is Kana.HiraganaA ->{
                        val first = kana.toFirst()
                        history +=
                            if (currentSyllable.first != null) {
                                composingWord.append(currentSyllable.string)
                                history.clear()
                                Mora(first = first)
                            } else {
                                currentSyllable.copy(first = first)
                            }
                    }
                    is Kana.HiraganaKa ->{
                        val first = kana.toFirst()
                        history +=
                            if (currentSyllable.first != null) {
                                composingWord.append(currentSyllable.string)
                                history.clear()
                                Mora(first = first)
                            } else {
                                currentSyllable.copy(first = first)
                            }
                    }
                    is Kana.HiraganaSa ->{
                        val first = kana.toFirst()
                        history +=
                            if (currentSyllable.first != null) {
                                composingWord.append(currentSyllable.string)
                                history.clear()
                                Mora(first = first)
                            } else {
                                currentSyllable.copy(first = first)
                            }
                    }
                    is Kana.HiraganaTa ->{
                        val first = kana.toFirst()
                        history +=
                            if (currentSyllable.first != null) {
                                composingWord.append(currentSyllable.string)
                                history.clear()
                                Mora(first = first)
                            } else {
                                currentSyllable.copy(first = first)
                            }
                    }
                    is Kana.HiraganaNa ->{
                        val first = kana.toFirst()
                        history +=
                            if (currentSyllable.first != null) {
                                composingWord.append(currentSyllable.string)
                                history.clear()
                                Mora(first = first)
                            } else {
                                currentSyllable.copy(first = first)
                            }
                    }
                    is Kana.HiraganaHa ->{
                        val first = kana.toFirst()
                        history +=
                            if (currentSyllable.first != null) {
                                composingWord.append(currentSyllable.string)
                                history.clear()
                                Mora(first = first)
                            } else {
                                currentSyllable.copy(first = first)
                            }
                    }
                    is Kana.HiraganaMa ->{
                        val first = kana.toFirst()
                        history +=
                            if (currentSyllable.first != null) {
                                composingWord.append(currentSyllable.string)
                                history.clear()
                                Mora(first = first)
                            } else {
                                currentSyllable.copy(first = first)
                            }
                    }
                    is Kana.HiraganaYa ->{
                        val first = kana.toFirst()
                        history +=
                            if (currentSyllable.first != null) {
                                composingWord.append(currentSyllable.string)
                                history.clear()
                                Mora(first = first)
                            } else {
                                currentSyllable.copy(first = first)
                            }
                    }
                    is Kana.HiraganaRa ->{
                        val first = kana.toFirst()
                        history +=
                            if (currentSyllable.first != null) {
                                composingWord.append(currentSyllable.string)
                                history.clear()
                                Mora(first = first)
                            } else {
                                currentSyllable.copy(first = first)
                            }
                    }
                    is Kana.HiraganaWa ->{
                        val first = kana.toFirst()
                        history +=
                            if (currentSyllable.first != null) {
                                composingWord.append(currentSyllable.string)
                                history.clear()
                                Mora(first = first)
                            } else {
                                currentSyllable.copy(first = first)
                            }
                    }
                    is Kana.HiraganaN ->{
                        val first = kana.toFirst()
                        history +=
                            if (currentSyllable.first != null) {
                                composingWord.append(currentSyllable.string)
                                history.clear()
                                Mora(first = first)
                            } else {
                                currentSyllable.copy(first = first)
                            }
                    }
                    is Kana.Dakuten -> {
                        if (currentSyllable.first != null && currentSyllable.second == null) {
                            if (Kana.HiraganaKa(currentSyllable.first.codePoint).modern || Kana.HiraganaSa(currentSyllable.first.codePoint).modern || Kana.HiraganaTa(currentSyllable.first.codePoint).modern || Kana.HiraganaHa(currentSyllable.first.codePoint).modern) {
                                history += currentSyllable.copy(second = kana.toSecond(), type = MORA_TYPE.DAKUTEN)
                            }
                        } else if (currentSyllable.first != null) {
                            if (Kana.HiraganaKa(currentSyllable.first.codePoint).modern || Kana.HiraganaSa(currentSyllable.first.codePoint).modern || Kana.HiraganaTa(currentSyllable.first.codePoint).modern || Kana.HiraganaHa(currentSyllable.first.codePoint).modern) {
                                history.clear()
                                history += currentSyllable.copy(first = currentSyllable.first, second = null, type = MORA_TYPE.SIMPLE)
                            }
                        }
                    }
                    is Kana.Handakuten -> {
                        if (currentSyllable.first != null && currentSyllable.second == null) {
                            if (Kana.HiraganaHa(currentSyllable.first.codePoint).modern) {
                                history += currentSyllable.copy(second = kana.toSecond(), type = MORA_TYPE.HANDAKUTEN)
                            }
                        } else if (currentSyllable.first != null) {
                            if (Kana.HiraganaHa(currentSyllable.first.codePoint).modern) {
                                history.clear()
                                history += currentSyllable.copy(first = currentSyllable.first, second = null, type = MORA_TYPE.SIMPLE)
                            }
                        }
                    }
                    is Kana.Sokuon -> {
                        if (currentSyllable.first != null && currentSyllable.second == null) {
                            if (Kana.HiraganaYa(currentSyllable.first.codePoint).modern) {
                                history += currentSyllable.copy(second = kana.toSecond(), type = MORA_TYPE.YOON)
                            } else if (currentSyllable.first.codePoint == 0x3064) {
                                history += currentSyllable.copy(second = kana.toSecond(), type = MORA_TYPE.SOKUON)
                            }
                        } else if (currentSyllable.first != null) {
                            if (Kana.HiraganaYa(currentSyllable.first.codePoint).modern) {
                                history.clear()
                                history += currentSyllable.copy(first = currentSyllable.first, second = null, type = MORA_TYPE.SIMPLE)
                            } else if (currentSyllable.first.codePoint == 0x3064) {
                                history.clear()
                                history += currentSyllable.copy(first = currentSyllable.first, second = null, type = MORA_TYPE.SIMPLE)
                            }
                        }
                    }
                    // compiler bug? when it's not added, compiler complains that it's missing
                    // but when added, linter (correctly) states it's unreachable anyway
                    is Kana.NonKana -> Unit
                    else -> Unit
                }
            }
        }

        return Event.createConsumedEvent(event)
    }

    override val combiningStateFeedback: CharSequence
        get() = composingWord.toString() + (syllable?.string ?: "")

    override fun reset() {
        composingWord.setLength(0)
        history.clear()
    }

    sealed class Kana {
        abstract val codePoint: Int
        abstract val modern: Boolean
        val string: String get() = codePoint.toChar().toString()
        data class NonKana(override val codePoint: Int) : Kana() {
            override val modern: Boolean get() = false
        }
        data class First(override val codePoint: Int) : Kana() {
            override val modern: Boolean get() = true
        }
        data class Second(override val codePoint: Int) : Kana() {
            override val modern: Boolean get() = true
        }
        data class Sokuon(override val codePoint: Int) :Kana() {
            override val modern: Boolean get() = codePoint == 0x5c0f
            val ordinal: Int get() = codePoint - 0x5c0f
            fun toSecond(): Second? {
                if (codePoint == 0) return null
                return Second(codePoint)
            }
        }
        data class Dakuten(override val codePoint: Int) :Kana() {
            override val modern: Boolean get() = codePoint == 0x309b
            val ordinal: Int get() = codePoint - 0x309b
            fun toSecond(): Second? {
                if (codePoint == 0) return null
                return Second(codePoint)
            }
        }
        data class Handakuten(override val codePoint: Int) :Kana() {
            override val modern: Boolean get() = codePoint == 0x309c
            val ordinal: Int get() = codePoint - 0x309c
            fun toSecond(): Second? {
                if (codePoint == 0) return null
                return Second(codePoint)
            }
        }
        data class Yoon(override val codePoint: Int) :Kana() {
            override val modern: Boolean get() = codePoint in listOf(0x3083, 0x3085, 0x3087)
            val ordinal: Int get() = (codePoint - 0x3083) / 2
            fun toSecond(): Second? {
                if (codePoint == 0) return null
                return Second(codePoint)
            }
        }
        data class HiraganaA(override val codePoint: Int) :Kana() {
            override val modern: Boolean get() = codePoint in listOf(0x3042, 0x3044, 0x3046, 0x3048, 0x304a)
            val ordinal: Int get() = (codePoint - 0x3042) / 2
            fun toFirst(): First? {
                if (codePoint == 0) return null
                return First(codePoint)
            }
        }
        data class HiraganaKa(override val codePoint: Int) :Kana() {
            override val modern: Boolean get() = codePoint in listOf(0x304b, 0x304d, 0x304f, 0x3051, 0x3053)
            val ordinal: Int get() = (codePoint - 0x304b) / 2
            fun toFirst(): First? {
                if (codePoint == 0) return null
                return First(codePoint)
            }
        }
        data class HiraganaGa(override val codePoint: Int) :Kana() {
            override val modern: Boolean get() = codePoint in listOf(0x304c, 0x304e, 0x3050, 0x3052, 0x3054)
            val ordinal: Int get() = (codePoint - 0x304c) / 2
            fun toFirst(): First? {
                if (codePoint == 0) return null
                return First(codePoint)
            }
        }
        data class HiraganaSa(override val codePoint: Int) :Kana() {
            override val modern: Boolean get() = codePoint in listOf(0x3055, 0x3057, 0x3059, 0x305b, 0x305d)
            val ordinal: Int get() = (codePoint - 0x3055) / 2
            fun toFirst(): First? {
                if (codePoint == 0) return null
                return First(codePoint)
            }
        }
        data class HiraganaZa(override val codePoint: Int) :Kana() {
            override val modern: Boolean get() = codePoint in listOf(0x3056, 0x3058, 0x305a, 0x305c, 0x305e)
            val ordinal: Int get() = (codePoint - 0x3056) / 2
            fun toFirst(): First? {
                if (codePoint == 0) return null
                return First(codePoint)
            }
        }
        data class HiraganaTa(override val codePoint: Int) :Kana() {
            override val modern: Boolean get() = codePoint in listOf(0x305f, 0x3061, 0x3064, 0x3066, 0x3068)
            val ordinal: Int get() = (codePoint - 0x305f) / 2
            fun toFirst(): First? {
                if (codePoint == 0) return null
                return First(codePoint)
            }
        }
        data class HiraganaDa(override val codePoint: Int) :Kana() {
            override val modern: Boolean get() = codePoint in listOf(0x3050, 0x3062, 0x3065, 0x3067, 0x3069)
            val ordinal: Int get() = (codePoint - 0x3060) / 2
            fun toFirst(): First? {
                if (codePoint == 0) return null
                return First(codePoint)
            }
        }
        data class HiraganaNa(override val codePoint: Int) :Kana() {
            override val modern: Boolean get() = codePoint in listOf(0x306a, 0x306b, 0x306c, 0x306d, 0x306e)
            val ordinal: Int get() = (codePoint - 0x306a)
            fun toFirst(): First? {
                if (codePoint == 0) return null
                return First(codePoint)
            }
        }
        data class HiraganaHa(override val codePoint: Int) :Kana() {
            override val modern: Boolean get() = codePoint in listOf(0x306f, 0x3072, 0x3075, 0x3078, 0x307b)
            val ordinal: Int get() = (codePoint - 0x306f) / 3
            fun toFirst(): First? {
                if (codePoint == 0) return null
                return First(codePoint)
            }
        }
        data class HiraganaBa(override val codePoint: Int) :Kana() {
            override val modern: Boolean get() = codePoint in listOf(0x3070, 0x3073, 0x3076, 0x3079, 0x307c)
            val ordinal: Int get() = (codePoint - 0x3070) / 3
            fun toFirst(): First? {
                if (codePoint == 0) return null
                return First(codePoint)
            }
        }
        data class HiraganaPa(override val codePoint: Int) :Kana() {
            override val modern: Boolean get() = codePoint in listOf(0x3071, 0x3074, 0x3077, 0x307a, 0x307d)
            val ordinal: Int get() = (codePoint - 0x3071) / 3
            fun toFirst(): First? {
                if (codePoint == 0) return null
                return First(codePoint)
            }
        }
        data class HiraganaMa(override val codePoint: Int) :Kana() {
            override val modern: Boolean get() = codePoint in listOf(0x307e, 0x307f, 0x3080, 0x3081, 0x3082)
            val ordinal: Int get() = (codePoint - 0x307e)
            fun toFirst(): First? {
                if (codePoint == 0) return null
                return First(codePoint)
            }
        }
        data class HiraganaYa(override val codePoint: Int) :Kana() {
            override val modern: Boolean get() = codePoint in listOf(0x3084, 0x3086, 0x3088)
            val ordinal: Int get() = (codePoint - 0x3084) / 2
            fun toFirst(): First? {
                if (codePoint == 0) return null
                return First(codePoint)
            }
        }
        data class HiraganaRa(override val codePoint: Int) :Kana() {
            override val modern: Boolean get() = codePoint in listOf(0x3089, 0x308a, 0x308b, 0x308c, 0x308d)
            val ordinal: Int get() = (codePoint - 0x3089)
            fun toFirst(): First? {
                if (codePoint == 0) return null
                return First(codePoint)
            }
        }
        data class HiraganaWa(override val codePoint: Int) :Kana() {
            override val modern: Boolean get() = codePoint in listOf(0x308f, 0x3092)
            val ordinal: Int get() = (codePoint - 0x308f) / 3
            fun toFirst(): First? {
                if (codePoint == 0) return null
                return First(codePoint)
            }
        }
        data class HiraganaN(override val codePoint: Int) :Kana() {
            override val modern: Boolean get() = codePoint in listOf(0x3093)
            val ordinal: Int get() = (codePoint - 0x308f)
            fun toFirst(): First? {
                if (codePoint == 0) return null
                return First(codePoint)
            }
        }
        companion object {
            const val COMPAT_CONSONANTS = "ㄱㄲㄳㄴㄵㄶㄷㄸㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅃㅄㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ"
            const val COMPAT_VOWELS = "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ"
            const val CONVERT_INITIALS = "ᄀᄁ\u0000ᄂ\u0000\u0000ᄃᄄᄅ\u0000\u0000\u0000\u0000\u0000\u0000\u0000ᄆᄇᄈ\u0000ᄉᄊᄋᄌᄍᄎᄏᄐᄑᄒ"
            const val CONVERT_MEDIALS = "ᅡᅢᅣᅤᅥᅦᅧᅨᅩᅪᅫᅬᅭᅮᅯᅰᅱᅲᅳᅴᅵ"
            const val CONVERT_FINALS = "ᆨᆩᆪᆫᆬᆭᆮ\u0000ᆯᆰᆱᆲᆳᆴᆵᆶᆷᆸ\u0000ᆹᆺᆻᆼᆽ\u0000ᆾᆿᇀᇁᇂ"
            fun of(codePoint: Int): Kana {
                return when(codePoint) {
                    0x5c0f -> Sokuon(codePoint)
                    0x309b -> Dakuten(codePoint)
                    0x309c -> Handakuten(codePoint)
                    // in listOf(0x3083, 0x3085, 0x3087) -> Yoon(codePoint)
                    in listOf(0x3042, 0x3044, 0x3046, 0x3048, 0x304a) -> HiraganaA(codePoint)
                    in listOf(0x304b, 0x304d, 0x304f, 0x3051, 0x3053) -> HiraganaKa(codePoint)
                    in listOf(0x304c, 0x304e, 0x3050, 0x3052, 0x3054) -> HiraganaGa(codePoint)
                    in listOf(0x3055, 0x3057, 0x3059, 0x305b, 0x305d) -> HiraganaSa(codePoint)
                    in listOf(0x3056, 0x3058, 0x305a, 0x305c, 0x305e) -> HiraganaZa(codePoint)
                    in listOf(0x305f, 0x3061, 0x3064, 0x3066, 0x3068) -> HiraganaTa(codePoint)
                    in listOf(0x3060, 0x3062, 0x3065, 0x3067, 0x3069) -> HiraganaDa(codePoint)
                    in listOf(0x306a, 0x306b, 0x306c, 0x306d, 0x306e) -> HiraganaNa(codePoint)
                    in listOf(0x306f, 0x3072, 0x3075, 0x3078, 0x307b) -> HiraganaHa(codePoint)
                    in listOf(0x3070, 0x3073, 0x3076, 0x3079, 0x307c) -> HiraganaBa(codePoint)
                    in listOf(0x3071, 0x3074, 0x3077, 0x307a, 0x307d) -> HiraganaPa(codePoint)
                    in listOf(0x307e, 0x307f, 0x3080, 0x3081, 0x3082) -> HiraganaMa(codePoint)
                    in listOf(0x3084, 0x3086, 0x3088) -> HiraganaYa(codePoint)
                    in listOf(0x3089, 0x308a, 0x308b, 0x308c, 0x308d) -> HiraganaRa(codePoint)
                    in listOf(0x308f, 0x3092) -> HiraganaWa(codePoint)
                    in listOf(0x3093) -> HiraganaN(codePoint)
                    else -> NonKana(codePoint)
                }
            }
        }
    }

    data class Mora(
            val first: Kana.First? = null,
            val second: Kana.Second? = null,
            val type: MORA_TYPE = MORA_TYPE.SIMPLE,
    ) {
        val simple: String get() = (first?.string ?: "")
        val dakuten: String get() = COMBINATION_TABLE_DAKUTEN[this.first?.codePoint]?.toChar().toString()
        val handakuten: String get() = COMBINATION_TABLE_HANDAKUTEN[this.first?.codePoint]?.toChar().toString()
        val yoon: String get() = ((this.first?.codePoint ?: 0) - 1).toChar().toString()
        val sokuon: String get() = ((this.first?.codePoint ?: 0) - 1).toChar().toString()
        val string: String get() =
            when (type) {
                MORA_TYPE.SIMPLE -> {
                    this.simple
                }
                MORA_TYPE.DAKUTEN -> {
                    this.dakuten
                }
                MORA_TYPE.HANDAKUTEN -> {
                    this.handakuten
                }
                MORA_TYPE.YOON -> {
                    this.yoon
                }
                MORA_TYPE.SOKUON -> {
                    this.sokuon
                }
            }
    }

    companion object {
        val COMBINATION_TABLE_DAKUTEN = mapOf<Int, Int>(
            0x304b to 0x304c,
            0x304d to 0x304e,
            0x304f to 0x3050,
            0x3051 to 0x3052,
            0x3053 to 0x3054,

            0x3055 to 0x3056,
            0x3057 to 0x3058,
            0x3059 to 0x305a,
            0x305b to 0x305c,
            0x305d to 0x305e,

            0x305f to 0x3060,
            0x3061 to 0x3062,
            0x3064 to 0x3065,
            0x3066 to 0x3067,
            0x3068 to 0x3069,

            0x306f to 0x3070,
            0x3072 to 0x3073,
            0x3075 to 0x3076,
            0x3078 to 0x3079,
            0x307b to 0x307c,
        )
        val COMBINATION_TABLE_HANDAKUTEN = mapOf<Int, Int>(
            0x306f to 0x3071,
            0x3072 to 0x3074,
            0x3075 to 0x3077,
            0x3078 to 0x307a,
            0x307b to 0x307d,
        )
        private fun createEventChainFromSequence(text: CharSequence, originalEvent: Event): Event {
            return Event.createSoftwareTextEvent(text, KeyCode.MULTIPLE_CODE_POINTS, originalEvent)
        }
    }

}
