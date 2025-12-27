package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.Edge;
import com.battle.heroes.army.programs.UnitTargetPathFinder;

import java.util.*;

// Реализация алгоритма A* для поиска кратчайшего пути между двумя юнитами на игровом поле.
// Сложность: O(W·H·log(W·H)), где W и H — ширина и высота поля.
// Посещение узлов: В худшем случае алгоритм может посетить все узлы графа
// Для сетки W×H: O(W·H) узлов
// Обработка каждого узла: Для каждого узла проверяются соседи (до 8)
// Работа с очередью с приоритетом (PriorityQueue):
// Добавление/удаление: O(log n), где n — количество элементов в очереди
// В худшем случае n = W·H
// Итого: O(W·H × log(W·H))

public class UnitTargetPathFinderImpl implements UnitTargetPathFinder {

    // Размеры игрового поля (фиксированные)
    private static final int WIDTH = 27;
    private static final int HEIGHT = 21;

    @Override
    public List<Edge> getTargetPath(Unit attackUnit, Unit targetUnit, List<Unit> existingUnitList) {
        // Собираем координаты всех занятых клеток
        Set<String> occupiedCells = new HashSet<>();
        for (Unit unit : existingUnitList) {
            occupiedCells.add(unit.getxCoordinate() + "," + unit.getyCoordinate());
        }

        // Определяем стартовую и целевую позиции
        int startX = attackUnit.getxCoordinate();
        int startY = attackUnit.getyCoordinate();
        int endX = targetUnit.getxCoordinate();
        int endY = targetUnit.getyCoordinate();

        occupiedCells.remove(startX + "," + startY);
        occupiedCells.remove(endX + "," + endY);

        Map<String, Integer> gScore = new HashMap<>();
        Map<String, Integer> fScore = new HashMap<>();
        Map<String, String> cameFrom = new HashMap<>();

        String startKey = startX + "," + startY;
        gScore.put(startKey, 0);
        fScore.put(startKey, heuristic(startX, startY, endX, endY));

        PriorityQueue<int[]> openSet = new PriorityQueue<>((a, b) -> {
            String keyA = a[0] + "," + a[1];
            String keyB = b[0] + "," + b[1];
            return fScore.getOrDefault(keyA, Integer.MAX_VALUE)
                    - fScore.getOrDefault(keyB, Integer.MAX_VALUE);
        });

        openSet.offer(new int[]{startX, startY});

        Set<String> closedSet = new HashSet<>();

        while (!openSet.isEmpty()) {
            // Извлекаем узел с наименьшей оценкой f
            int[] current = openSet.poll();
            int x = current[0], y = current[1];
            String currentKey = x + "," + y;

            // Если узел уже был обработан — пропускаем
            if (closedSet.contains(currentKey)) {
                continue;
            }

            // Проверяем, достигли ли цели
            if (x == endX && y == endY) {
                return reconstructPath(cameFrom, currentKey); // Возвращаем найденный путь
            }

            // Помечаем текущий узел как обработанный
            closedSet.add(currentKey);

            // Проверяем всех 8 соседей
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue; // Пропускаем самого себя

                    int nx = x + dx;
                    int ny = y + dy;

                    // Проверка выхода за границы
                    if (nx < 0 || nx >= WIDTH || ny < 0 || ny >= HEIGHT) continue;

                    String neighborKey = nx + "," + ny;

                    // Пропускаем, если сосед уже обработан или занят другим юнитом
                    if (closedSet.contains(neighborKey)) continue;
                    if (occupiedCells.contains(neighborKey)) continue;

                    // Стоимость перемещения: 1 — по прямой, 2 — по диагонали
                    int moveCost = (dx != 0 && dy != 0) ? 2 : 1;
                    int tentativeG = gScore.get(currentKey) + moveCost;

                    // Если найден более короткий путь до соседа — обновляем данные
                    if (tentativeG < gScore.getOrDefault(neighborKey, Integer.MAX_VALUE)) {
                        cameFrom.put(neighborKey, currentKey);
                        gScore.put(neighborKey, tentativeG);
                        int h = heuristic(nx, ny, endX, endY);
                        fScore.put(neighborKey, tentativeG + h);

                        // Добавляем соседа в очередь для дальнейшего рассмотрения
                        openSet.offer(new int[]{nx, ny});
                    }
                }
            }
        }

        // Если очередь опустела, а цель не достигнута — путь не существует
        return new ArrayList<>();
    }

    private int heuristic(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private List<Edge> reconstructPath(Map<String, String> cameFrom, String currentKey) {
        List<Edge> path = new ArrayList<>();
        String current = currentKey;

        // Идём от цели к старту
        while (current != null) {
            String[] parts = current.split(",");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            path.add(new Edge(x, y));
            current = cameFrom.get(current);
        }

        Collections.reverse(path);
        return path;
    }
}