class GameStats {

    var budget: Int = 100
        private set
    var morale: Int = 10
        private set
    var readiness: Int = 0
        private set
    var currentDay: Int = 1
        private set

    var todayReadinessGain: Int = 0
    var cntDevelopers: Int = 0
    var cntTemplate: Int = 0
    var cntConsecutiveDays: Int = 0
    var status: Int? = null

    fun changeBudget(amount: Int) {budget += amount}
    fun changeMorale(amount: Int) {
        if (morale + amount > 12) {
            morale = 12
        } else {
            morale += amount
        }
    }
    fun changeReadiness(amount: Int) {readiness += amount; todayReadinessGain += amount}
    fun resetDailyGain() { todayReadinessGain = 0 }
    fun nextDay() { currentDay++ }

    fun changeStatus() {
        when {
            readiness >= 100 -> {status = 0}
            budget <= 0 -> {status = 1}
            morale <= 0 -> {status = 2}
            cntConsecutiveDays >= 4 -> {status = 3}
            currentDay >= 7 -> { status = 4 }
        }
    }
    fun isEnd(): String {
        return if (status == 0) {
            isVictory()
        } else {
            isDefeat(status!!)
        }
    }
    fun isDefeat(status: Int): String {
        return when (status) {
            1 -> "Проигрыш. Деньги закончились. Заказчик отключил финансирование и выключил свет. Игра осталась на диске."
            2 -> "Проигрыш. Разработчики закрывают ноутбуки и уходят. Никто не кричит и не ругается, просто всем всё равно."
            3 -> "Проигрыш. Четвёртый день подряд без сна. На рассвете команда выгорела окончательно. Билд так и остался в $readiness%."
            4 -> "Проигрыш. Дедлайн наступил. Игра вышла сырой, и отзывы в сети говорят сами за себя. Итоговая готовность: $readiness%."
            else -> "Ошибка статуса"
        }
    }
    fun isVictory(): String {
        return "Билд ушёл в сеть. Заказчик пишет в чат: «Это именно то, что мы хотели». Команда падает на диваны. Релиз состоялся, студия жива."
    }
}

abstract class Event(val name: String) {
    abstract fun applyEvent(stats: GameStats): String
}

class FixedEvent(
    name: String,
    val budgetChange: Int,
    val moraleChange: Int,
    val readinessChange: Int,
    val resultMessage: String
): Event(name) {

    override fun applyEvent(stats: GameStats): String {
        stats.changeBudget(budgetChange)
        stats.changeMorale(moraleChange)
        stats.changeReadiness(readinessChange)
        return resultMessage
    }
}

class ConditionalEvent(
    name: String,
    val condition: (GameStats) -> Boolean,
    val actionIfTrue: (GameStats) -> String,
    val actionIfFalse: (GameStats) -> String
): Event(name) {

    override fun applyEvent(stats: GameStats): String {
        return if (condition(stats)) {
            actionIfTrue(stats)
        } else {
            actionIfFalse(stats)
        }
    }
}

fun createEvents(): MutableList<Event> {
    return mutableListOf(
        FixedEvent(
            name = "Заказчик расщедрился",
            budgetChange = 15,
            moraleChange = 0,
            readinessChange = 0,
            resultMessage = "Событие: Заказчик расщедрился! Бюджет подняли на 15."
        ),
        FixedEvent(
            name = "Гениальная идея",
            budgetChange = 0,
            moraleChange = 0,
            readinessChange = 8,
            resultMessage = "Событие: Гениальная идея! + 8 к готовности."
        ),
        FixedEvent(
            name = "Баг на проде",
            budgetChange = 0,
            moraleChange = -2,
            readinessChange = 0,
            resultMessage = "Событие: Баг на проде! Боевой дух упал на 2. "
        ),
        FixedEvent(
            name = "Стажёр Вася пришёл за опытом",
            budgetChange = 0,
            moraleChange = 1,
            readinessChange = 0,
            resultMessage = "Событие: Стажёр Вася пришел за опытом! Боевой дух поднялся на 1."
        ),
        FixedEvent(
            name = "Нашли бесплатные ассеты на форуме",
            budgetChange = 0,
            moraleChange = 0,
            readinessChange = 6,
            resultMessage = "Событие: Команда нашла бесплатные ассеты на форуме! + 6 к готовности"
        ),
        ConditionalEvent(
            name = "Сломался роутер",
            condition = { true },
            actionIfTrue = { stats ->
                stats.changeBudget(-5)
                val lostReadiness = (stats.todayReadinessGain * 0.3).toInt()
                stats.changeReadiness(-lostReadiness)
                "Событие: Сломался роутер! Сгорело 5 бюджета и $lostReadiness готовности."
            },
            actionIfFalse = { "" }
        ),
        ConditionalEvent(
            name = "Заказчик поменялся",
            condition = { stats -> stats.budget < 40 },
            actionIfTrue = { stats ->
                stats.changeBudget(25)
                stats.changeReadiness(-10)
                "Событие: Заказчик поменялся (бюджет < 40). Новые хотелки: +25 бюджета, -10 готовности."
            },
            actionIfFalse = { stats ->
                stats.changeReadiness(4)
                "Событие: Заказчик поменялся. Новому нравится демо: +4 к готовности."
            }
        ),
        ConditionalEvent(
            name = "Подгон от заказчика",
            condition = { stats -> stats.readiness > 75 && stats.currentDay >= 6},
            actionIfTrue = { stats ->
                stats.changeReadiness(3)
                "Событие: Подгон от заказчика (неделя почти закончилась, а готовность выше 75). Заказчик прислал тестера: +3 готовности."
            },
            actionIfFalse = { stats ->
                stats.changeMorale(1)
                "Событие: Подгон от заказчика. Команда получила от заказчика кофе и пиццу: + 1 к боевому духу"
            }
        ),
        ConditionalEvent(
            name = "DDoS-атака",
            condition = { stats -> stats.readiness > 50 && stats.morale < 5 },
            actionIfTrue = { stats ->
                stats.changeReadiness(-15)
                "Событие: DDoS-атака (проект готов минимум на половину, но у команды заканчивается боевой дух). Атака прошла: -15 готовности."
            },
            actionIfFalse = { stats ->
                stats.changeBudget(-5)
                "Событие: DDoS-атака (команда готова защищать свой проект). Атака отбита, но пришлось платить за защиту: -5 бюджета."
            }
        ),
        ConditionalEvent(
            name = "Конкурент анонсировал похожую игру",
            condition = { stats -> stats.readiness >= 60 },
            actionIfTrue = { stats ->
                stats.changeReadiness(10)
                "Событие: Конкурент анонсировал похожую игру. Команда ускорилась назло: +10 готовности."
            },
            actionIfFalse = { stats ->
                stats.changeMorale(-3)
                "Событие: Конкурент анонсировал похожую игру. Команда в тильте: -3 от боевого духа"
            }
        )
    )
}

fun printAnalytics(stats: GameStats, history: List<DayResult>) {
    val totalBudgetSpent = 100 - stats.budget
    val bestDay = history.maxByOrNull { it.readinessGain }
    val worstDay = history.minByOrNull { it.readinessGain }

    println("\n--- Аналитика ---")
    println("Потрачено бюджета: $totalBudgetSpent")
    bestDay?.let { println("Лучший день: ${it.day} (прирост ${it.readinessGain})") }
    worstDay?.let { println("Худший день: ${it.day} (прирост ${it.readinessGain})") }
}

data class DayResult(val day: Int, val readinessGain: Int, val commit: String?)

fun main() {
    val stats = GameStats()
    val events = createEvents()
    val history = mutableListOf<DayResult>()

    println("Инди-студия «Пара из Булатниково». У вас 7 дней до релиза.")
    println("\nНе забывай о трех законах студии!")
    println("1. Не больше 2 дополнительно нанятых разработчиков. Третий найм запрещён: «Команда не сработается, лучше оставить как есть».")
    println("2. Не больше 2 купленных шаблонов. Третья покупка запрещена: «Юристы заказчика заметят плагиат».")
    println("3. Не больше 3 дней разработки подряд. Четвёртый подряд день: дух падает до 0, команда выгорает, поражение")
    for (day in 1..7) {
        println("\n--- День $day ---")
        println("Бюджет: ${stats.budget} | Дух: ${stats.morale} | Готовность: ${stats.readiness}%")

        stats.resetDailyGain()

        while (true) {
            println("Выберите действие:")
            println("1. Нанять разработчика (-25 бюджета, +4 духа)")
            println("2. Разработка (-2 духа, +16 готовности + бонус за нанятых)")
            println("3. Купить шаблон (-30 бюджета, +20 готовности)")
            print("> ")

            when (readln()) {
                "1" -> {
                    if (stats.cntDevelopers < 2) {
                        stats.cntDevelopers++
                        println("Нанят 1 разработчик. Теперь нанято: ${stats.cntDevelopers}")
                        stats.changeBudget(-25)
                        println("Бюджет уменьшился на 25.")
                        stats.changeMorale(4)
                        println("Боевой дух вырос на 4.")
                        stats.cntConsecutiveDays = 0
                        break
                    } else {
                        println("Нанято уже 2 доп. разработчиков. Не будем нарушать первый закон студии!")
                    }
                }
                "2" -> {
                    stats.cntConsecutiveDays++
                    stats.changeMorale(-2)
                    println("Потратили 2 единицы боевого духа.")
                    stats.changeReadiness(16 + 2 * stats.cntDevelopers)
                    println("Готовность увеличилась на ${16 + 2 * stats.cntDevelopers}.")
                    break
                }
                "3" -> {
                    if (stats.cntTemplate < 2) {
                        stats.cntTemplate++
                        println("Команда купила готовый ассет. Теперь ассетов: ${stats.cntTemplate}")
                        stats.changeBudget(-30)
                        println("Это не дешевое удовольствие: -30 от бюджета.")
                        stats.changeReadiness(20)
                        println("Разработчики сэкономили время. Готовность увеличилась на 20")
                        stats.cntConsecutiveDays = 0
                        break
                    } else {
                        println("Команда уже купила 2 шаблона. Не будем нарушать второй закон студии!")
                    }
                }
                else -> println("Неверный ввод, введите 1, 2 или 3.")
            }
        }

        val todayEvent = events.random()
        events.remove(todayEvent)
        println(todayEvent.applyEvent(stats))

        println("Сообщение коммита? (Enter для пропуска):")
        val input = readln()
        val commitMsg = input.ifBlank { null }

        history.add(DayResult(day, stats.todayReadinessGain, commitMsg))
        stats.changeStatus()

        if (stats.status != null) {
            println("\n=== ИГРА ОКОНЧЕНА ===")
            println(stats.isEnd())
            printAnalytics(stats, history)
            return
        }
        stats.nextDay()
    }
}