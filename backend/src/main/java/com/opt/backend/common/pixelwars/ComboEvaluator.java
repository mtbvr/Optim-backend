package com.opt.backend.common.pixelwars;

import com.opt.backend.common.config.PixelWarsProperties;
import com.opt.backend.entity.Team;
import org.springframework.stereotype.Component;

@Component
public class ComboEvaluator {

    private static final int[][] LINE_DIRECTIONS = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};

    private final PixelWarsProperties properties;

    public ComboEvaluator(PixelWarsProperties properties) {
        this.properties = properties;
    }

    public int evaluate(BoardGrid grid, int x, int y, Team team) {
        int bonus = lineBonus(grid, x, y, team);
        if (hasSquare(grid, x, y, team)) {
            bonus += properties.getSquareComboBonus();
        }
        return bonus;
    }

    private int lineBonus(BoardGrid grid, int x, int y, Team team) {
        int bonus = 0;
        for (int[] direction : LINE_DIRECTIONS) {
            int length = 1
                    + countDirection(grid, x, y, team, direction[0], direction[1])
                    + countDirection(grid, x, y, team, -direction[0], -direction[1]);
            if (length >= properties.getLineComboLength()) {
                bonus += properties.getLineComboBonus();
            }
        }
        return bonus;
    }

    private int countDirection(BoardGrid grid, int x, int y, Team team, int dx, int dy) {
        int count = 0;
        int cx = x + dx;
        int cy = y + dy;
        while (grid.isInBounds(cx, cy) && grid.get(cx, cy) == team) {
            count++;
            cx += dx;
            cy += dy;
        }
        return count;
    }

    private boolean hasSquare(BoardGrid grid, int x, int y, Team team) {
        int size = properties.getSquareComboSize();
        for (int oy = y - size + 1; oy <= y; oy++) {
            for (int ox = x - size + 1; ox <= x; ox++) {
                if (isFullSquare(grid, ox, oy, size, team)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isFullSquare(BoardGrid grid, int originX, int originY, int size, Team team) {
        for (int dy = 0; dy < size; dy++) {
            for (int dx = 0; dx < size; dx++) {
                int cx = originX + dx;
                int cy = originY + dy;
                if (!grid.isInBounds(cx, cy) || grid.get(cx, cy) != team) {
                    return false;
                }
            }
        }
        return true;
    }
}
