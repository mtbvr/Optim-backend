package com.opt.backend.common.pixelwars;

import com.opt.backend.common.config.PixelWarsProperties;
import com.opt.backend.dto.PixelDto;
import com.opt.backend.entity.Pixel;
import com.opt.backend.entity.Team;
import com.opt.backend.repository.PixelRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BoardGrid {

    private final PixelWarsProperties properties;
    private final PixelRepository pixelRepository;
    private Team[][] grid;
    private final Set<Coord> occupiedCells = ConcurrentHashMap.newKeySet();

    public BoardGrid(PixelWarsProperties properties, PixelRepository pixelRepository) {
        this.properties = properties;
        this.pixelRepository = pixelRepository;
    }

    @PostConstruct
    void init() {
        grid = new Team[properties.getHeight()][properties.getWidth()];
        for (Pixel pixel : pixelRepository.findAll()) {
            int x = pixel.getId().getX();
            int y = pixel.getId().getY();
            if (isInBounds(x, y)) {
                grid[y][x] = pixel.getTeam();
                occupiedCells.add(new Coord(x, y));
            }
        }
    }

    public int getWidth() {
        return properties.getWidth();
    }

    public int getHeight() {
        return properties.getHeight();
    }

    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < properties.getWidth() && y >= 0 && y < properties.getHeight();
    }

    public Team get(int x, int y) {
        return grid[y][x];
    }

    public void set(int x, int y, Team team) {
        grid[y][x] = team;
        occupiedCells.add(new Coord(x, y));
    }

    public List<PixelDto> snapshot() {
        List<PixelDto> result = new ArrayList<>(occupiedCells.size());
        for (Coord cell : occupiedCells) {
            result.add(new PixelDto(cell.x(), cell.y(), grid[cell.y()][cell.x()]));
        }
        return result;
    }

    public List<Coord> neighbors(int x, int y) {
        List<Coord> result = new ArrayList<>(4);
        addIfInBounds(result, x + 1, y);
        addIfInBounds(result, x - 1, y);
        addIfInBounds(result, x, y + 1);
        addIfInBounds(result, x, y - 1);
        return result;
    }

    private void addIfInBounds(List<Coord> result, int x, int y) {
        if (isInBounds(x, y)) {
            result.add(new Coord(x, y));
        }
    }
}
