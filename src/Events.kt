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