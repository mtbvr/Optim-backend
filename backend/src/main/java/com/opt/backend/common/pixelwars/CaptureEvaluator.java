package com.opt.backend.common.pixelwars;

import com.opt.backend.entity.Team;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CaptureEvaluator {

    public List<Coord> evaluate(BoardGrid grid, List<Coord> placedCells, Team placingTeam) {
        Team opposing = placingTeam.opposite();
        Set<Coord> visited = new HashSet<>();
        List<Coord> captured = new ArrayList<>();

        for (Coord placed : placedCells) {
            for (Coord neighbor : grid.neighbors(placed.x(), placed.y())) {
                if (visited.contains(neighbor) || grid.get(neighbor.x(), neighbor.y()) != opposing) {
                    continue;
                }
                Component component = floodFill(grid, neighbor, opposing, placingTeam);
                visited.addAll(component.cells());
                if (component.enclosed()) {
                    captured.addAll(component.cells());
                }
            }
        }
        return captured;
    }

    private Component floodFill(BoardGrid grid, Coord start, Team opposing, Team placingTeam) {
        Set<Coord> cells = new HashSet<>();
        Deque<Coord> queue = new ArrayDeque<>();
        cells.add(start);
        queue.add(start);
        boolean enclosed = true;

        while (!queue.isEmpty()) {
            Coord current = queue.poll();
            List<Coord> neighbors = grid.neighbors(current.x(), current.y());
            if (neighbors.size() < 4) {
                enclosed = false;
            }
            for (Coord neighbor : neighbors) {
                Team team = grid.get(neighbor.x(), neighbor.y());
                if (team == opposing) {
                    if (cells.add(neighbor)) {
                        queue.add(neighbor);
                    }
                } else if (team != placingTeam) {
                    enclosed = false;
                }
            }
        }
        return new Component(cells, enclosed);
    }

    private record Component(Set<Coord> cells, boolean enclosed) {
    }
}
