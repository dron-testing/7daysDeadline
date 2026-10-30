fun printAnalytics(stats: GameStats, history: List<DayResult>) {
    val totalBudgetSpent = 100 - stats.budget
    val bestDay = history.maxByOrNull { it.readinessGain }
    val worstDay = history.minByOrNull { it.readinessGain }
    val negativeDays = history.filter { it.readinessGain < 0 }.map { it.day }
    val daysWithCommits = history.count { it.commit != null }

    println("\n--- Аналитика ---")
    println("Потрачено бюджета: $totalBudgetSpent")
    bestDay?.let { println("Лучший день: ${it.day} (прирост ${it.readinessGain})") }
    worstDay?.let { println("Худший день: ${it.day} (прирост ${it.readinessGain})") }

    if (negativeDays.isNotEmpty()) {
        println("Дни с откатом готовности: ${negativeDays.joinToString(", ")}")
    } else {
        println("Откатов готовности не было")
    }
    println("Оставлено коммитов: $daysWithCommits из ${history.size}")
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
                        stats.hireDeveloper()
                        println("Нанят 1 разработчик. Теперь нанято: ${stats.cntDevelopers}")
                        stats.changeBudget(-25)
                        println("Бюджет уменьшился на 25.")
                        stats.changeMorale(4)
                        println("Боевой дух вырос на 4.")
                        stats.resetConsecutiveDays()
                        break
                    } else {
                        println("Нанято уже 2 доп. разработчиков. Не будем нарушать первый закон студии!")
                    }
                }
                "2" -> {
                    stats.incrementConsecutiveDays()
                    stats.changeMorale(-2)
                    println("Потратили 2 единицы боевого духа.")
                    stats.changeReadiness(16 + 2 * stats.cntDevelopers)
                    println("Готовность увеличилась на ${16 + 2 * stats.cntDevelopers}.")
                    break
                }
                "3" -> {
                    if (stats.cntTemplate < 2) {
                        stats.buyTemplate()
                        println("Команда купила готовый ассет. Теперь ассетов: ${stats.cntTemplate}")
                        stats.changeBudget(-30)
                        println("Это не дешевое удовольствие: -30 от бюджета.")
                        stats.changeReadiness(20)
                        println("Разработчики сэкономили время. Готовность увеличилась на 20")
                        stats.resetConsecutiveDays()
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