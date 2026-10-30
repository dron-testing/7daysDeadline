enum class EndStatus { VICTORY, NO_MONEY, NO_MORALE, BURNOUT, DEADLINE }

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
        private set
    var cntDevelopers: Int = 0
        private set
    var cntTemplate: Int = 0
        private set
    var cntConsecutiveDays: Int = 0
        private set
    var status: EndStatus? = null
        private set

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
    fun hireDeveloper() { cntDevelopers++ }
    fun buyTemplate() { cntTemplate++ }
    fun incrementConsecutiveDays() { cntConsecutiveDays++ }
    fun resetConsecutiveDays() { cntConsecutiveDays = 0 }

    fun changeStatus() {
        when {
            readiness >= 100 -> {status = EndStatus.VICTORY}
            budget <= 0 -> {status = EndStatus.NO_MONEY}
            morale <= 0 -> {status = EndStatus.NO_MORALE}
            cntConsecutiveDays >= 4 -> {status = EndStatus.BURNOUT}
            currentDay >= 7 -> {status = EndStatus.DEADLINE}
        }
    }
    fun isEnd(): String {
        return if (status == EndStatus.VICTORY) {
            isVictory()
        } else {
            isDefeat(status)
        }
    }
    fun isDefeat(status: EndStatus?): String {
        return when (status) {
            EndStatus.NO_MONEY -> "Проигрыш. Деньги закончились. Заказчик отключил финансирование и выключил свет. Игра осталась на диске."
            EndStatus.NO_MORALE -> "Проигрыш. Разработчики закрывают ноутбуки и уходят. Никто не кричит и не ругается, просто всем всё равно."
            EndStatus.BURNOUT -> "Проигрыш. Четвёртый день подряд без сна. На рассвете команда выгорела окончательно. Билд так и остался в $readiness%."
            EndStatus.DEADLINE -> "Проигрыш. Дедлайн наступил. Игра вышла сырой, и отзывы в сети говорят сами за себя. Итоговая готовность: $readiness%."
            else -> "Ошибка статуса"
        }
    }
    fun isVictory(): String {
        return "Билд ушёл в сеть. Заказчик пишет в чат: «Это именно то, что мы хотели». Команда падает на диваны. Релиз состоялся, студия жива."
    }
}