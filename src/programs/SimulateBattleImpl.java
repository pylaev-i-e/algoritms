package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.PrintBattleLog;
import com.battle.heroes.army.programs.SimulateBattle;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Сложность: O(n² log n), n = общее количество юнитов в обеих армиях
// Внешний цикл (while): O(n)
// getAllLivingUnits(): O(n)
// Сортировка (sort): O(n log n)
// Внутренний цикл (for): O(n) итераций по юнитам

public class SimulateBattleImpl implements SimulateBattle {
    private PrintBattleLog printBattleLog;

    @Override
    public void simulate(Army playerArmy, Army computerArmy) throws InterruptedException {
        // Бой продолжается, пока в обеих армиях есть живые юниты
        while (hasLivingUnits(playerArmy) && hasLivingUnits(computerArmy)) {
            // Собираем всех живых юнитов из обеих армий для текущего раунда
            List<Unit> allLivingUnits = getAllLivingUnits(playerArmy, computerArmy);

            // Если никого не осталось (маловероятно), завершаем
            if (allLivingUnits.isEmpty()) {
                break;
            }

            // Сортируем по убыванию базовой атаки
            allLivingUnits.sort((u1, u2) -> Integer.compare(u2.getBaseAttack(), u1.getBaseAttack()));

            // Каждый юнит делает ход в порядке силы
            // Используем копию списка, потому что оригинал может измениться в процессе
            List<Unit> unitsToMove = new ArrayList<>(allLivingUnits);

            for (Unit attacker : unitsToMove) {
                // Проверяем, жив ли юнит ещё (мог умереть в этом же раунде)
                if (!attacker.isAlive()) {
                    continue;
                }

                // Юнит атакует
                Unit target = attacker.getProgram().attack();

                // Логируем атаку (даже если target == null)
                printBattleLog.printBattleLog(attacker, target);
            }
        }
    }

    private List<Unit> getAllLivingUnits(Army army1, Army army2) {
        List<Unit> result = new ArrayList<>();

        // Добавляем живых юнитов из первой армии
        for (Unit unit : army1.getUnits()) {
            if (unit.isAlive()) {
                result.add(unit);
            }
        }

        // Добавляем живых юнитов из второй армии
        for (Unit unit : army2.getUnits()) {
            if (unit.isAlive()) {
                result.add(unit);
            }
        }

        return result;
    }

    private boolean hasLivingUnits(Army army) {
        for (Unit unit : army.getUnits()) {
            if (unit.isAlive()) {
                return true;
            }
        }
        return false;
    }
}