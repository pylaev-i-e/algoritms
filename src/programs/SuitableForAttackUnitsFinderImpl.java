package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.SuitableForAttackUnitsFinder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Сложность: O(n)
// Внешний цикл по 3 рядам → O(3) = O(1)
// Для каждого ряда:
// Сбор занятых Y-координат: O(m), где m — юнитов в ряду
// Проверка каждого юнита: O(m)
// В сумме: O(3 × 2m) = O(6m) = O(n)

public class SuitableForAttackUnitsFinderImpl implements SuitableForAttackUnitsFinder {

    @Override
    public List<Unit> getSuitableUnits(List<List<Unit>> unitsByRow, boolean isLeftArmyTarget) {
        List<Unit> suitableUnits = new ArrayList<>();
        
        // Проходим по всем 3 рядам
        for (List<Unit> row : unitsByRow) {
            if (row == null || row.isEmpty()) {
                continue;
            }
            
            // Собираем все занятые Y-координаты в этом ряду
            Set<Integer> occupiedY = new HashSet<>();
            for (Unit unit : row) {
                if (unit != null) {
                    occupiedY.add(unit.getyCoordinate());
                }
            }
            
            // Для каждого юнита проверяем условие "не закрытости"
            for (Unit unit : row) {
                if (unit == null) continue;
                
                int currentY = unit.getyCoordinate();
                boolean isSuitable;
                
                if (isLeftArmyTarget) {
                    // Атакуем армию игрока (компьютер атакует)
                    // Юнит НЕ закрыт справа, если нет юнита на y+1
                    isSuitable = !occupiedY.contains(currentY + 1);
                } else {
                    // Атакуем армию компьютера (игрок атакует)
                    // Юнит НЕ закрыт слева, если нет юнита на y-1
                    isSuitable = !occupiedY.contains(currentY - 1);
                }
                
                if (isSuitable) {
                    suitableUnits.add(unit);
                }
            }
        }
        
        return suitableUnits;
    }
}