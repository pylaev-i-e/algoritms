package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.GeneratePreset;

import java.util.*;

// Реализован жадный алгоритм
// Сложность: O(n * m), где n - число типов, m - максимальное число юнитов

public class GeneratePresetImpl implements GeneratePreset {

    private static final int MAX_UNITS_PER_TYPE = 11;
    private static final int FIELD_WIDTH = 3;
    private static final int FIELD_HEIGHT = 21;
    private static final int MAX_FIELD_CELLS = FIELD_WIDTH * FIELD_HEIGHT;
    private static final int RANDOM_SEARCH_ATTEMPTS = 200;
    private static final int SEARCH_THRESHOLD = 50;

    @Override
    public Army generate(List<Unit> unitList, int maxPoints) {
        // Защита от некорректных входных данных
        if (unitList == null || unitList.isEmpty() || maxPoints <= 0) {
            return new Army();
        }

        // Сортируем юниты по эффективности: сначала attack/cost, потом health/cost (по убыванию)
        List<Unit> sortedUnits = new ArrayList<>(unitList);
        sortedUnits.sort((u1, u2) -> {
            double eff1Attack = (double) u1.getBaseAttack() / u1.getCost();
            double eff2Attack = (double) u2.getBaseAttack() / u2.getCost();

            int attackCompare = Double.compare(eff2Attack, eff1Attack); // убывание
            if (attackCompare != 0) {
                return attackCompare;
            }

            double eff1Health = (double) u1.getHealth() / u1.getCost();
            double eff2Health = (double) u2.getHealth() / u2.getCost();
            return Double.compare(eff2Health, eff1Health); // убывание
        });

        // Инициализация армии и вспомогательных структур
        Army army = new Army();
        Set<String> occupiedCoords = new HashSet<>();
        Map<String, Integer> unitCount = new HashMap<>();
        int remainingPoints = maxPoints;
        int totalUsedPoints = 0;
        Random random = new Random();

        // Проходим по юнитам в порядке убывания эффективности
        for (Unit prototype : sortedUnits) {
            if (remainingPoints <= 0) break;

            String type = prototype.getUnitType();
            int cost = prototype.getCost();

            int currentCount = unitCount.getOrDefault(type, 0);
            if (currentCount >= MAX_UNITS_PER_TYPE) continue; // лимит на тип

            // Пытаемся добавить как можно больше юнитов этого типа,
            // пока не превышен лимит и есть очки и свободные клетки
            while (currentCount < MAX_UNITS_PER_TYPE && remainingPoints >= cost) {
                int[] coords = findRandomAvailableCoordinate(occupiedCoords, random);
                if (coords == null) {
                    break;
                }

                String name = type + " " + (currentCount + 1);
                Unit newUnit = new Unit(
                        name,
                        prototype.getUnitType(),
                        prototype.getHealth(),
                        prototype.getBaseAttack(),
                        prototype.getCost(),
                        prototype.getAttackType(),
                        new HashMap<>(prototype.getAttackBonuses()),
                        new HashMap<>(prototype.getDefenceBonuses()),
                        coords[0], // x
                        coords[1]  // y
                );

                occupiedCoords.add(coords[0] + "," + coords[1]);
                army.getUnits().add(newUnit);
                unitCount.put(type, currentCount + 1);
                remainingPoints -= cost;
                totalUsedPoints += cost;

                currentCount++;
            }
        }

        return army;
    }


    // Ищем рандомную свободную клетку на поле
    // Если заполнено более чем на SEARCH_THRESHOLD,
    // переключаемся на последовательный поиск
    // Рандомайзер нужен для генерации разнообразных пресетов, как в видео
    private int[] findRandomAvailableCoordinate(Set<String> occupiedCoords, Random random) {
        if (occupiedCoords.size() > SEARCH_THRESHOLD) {
            return findSequentialAvailableCoordinate(occupiedCoords);
        }

        // Пытаемся найти случайное свободное поле
        for (int attempt = 0; attempt < RANDOM_SEARCH_ATTEMPTS; attempt++) {
            int x = random.nextInt(FIELD_WIDTH);    // 0, 1, 2
            int y = random.nextInt(FIELD_HEIGHT);   // 0..20
            String key = x + "," + y;
            if (!occupiedCoords.contains(key)) {
                return new int[]{x, y};
            }
        }

        // Если случайный поиск не дал результата, используем последовательный поиск
        return findSequentialAvailableCoordinate(occupiedCoords);
    }

    // Последовательный поиск, после заполнения на SEARCH_THRESHOLD
    // это более быстрая/надежная стратегия поиска полей
    private int[] findSequentialAvailableCoordinate(Set<String> occupiedCoords) {
        for (int x = 0; x < FIELD_WIDTH; x++) {
            for (int y = 0; y < FIELD_HEIGHT; y++) {
                String key = x + "," + y;
                if (!occupiedCoords.contains(key)) {
                    return new int[]{x, y};
                }
            }
        }
        return null;
    }
}