package com.opt.backend.common.pixelwars;

import com.opt.backend.dto.LeaderboardEntryDto;
import com.opt.backend.dto.LeaderboardSnapshot;
import com.opt.backend.entity.Pixel;
import com.opt.backend.entity.Team;
import com.opt.backend.repository.PixelRepository;
import com.opt.backend.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LeaderboardTracker {

    private static final int TOP_PLAYERS_LIMIT = 10;

    private final PixelRepository pixelRepository;
    private final UserRepository userRepository;
    private final Map<UUID, Integer> countByUser = new ConcurrentHashMap<>();
    private final Map<Team, AtomicInteger> countByTeam = new ConcurrentHashMap<>();

    public LeaderboardTracker(PixelRepository pixelRepository, UserRepository userRepository) {
        this.pixelRepository = pixelRepository;
        this.userRepository = userRepository;
    }

    @PostConstruct
    void init() {
        for (Team team : Team.values()) {
            countByTeam.put(team, new AtomicInteger());
        }
        for (Pixel pixel : pixelRepository.findAll()) {
            countByUser.merge(pixel.getUpdatedBy(), 1, Integer::sum);
            countByTeam.get(pixel.getTeam()).incrementAndGet();
        }
    }

    public void recordChange(UUID previousOwner, Team previousTeam, UUID newOwner, Team newTeam) {
        if (previousOwner != null) {
            countByUser.computeIfPresent(previousOwner, (id, count) -> count <= 1 ? null : count - 1);
            countByTeam.get(previousTeam).decrementAndGet();
        }
        countByUser.merge(newOwner, 1, Integer::sum);
        countByTeam.get(newTeam).incrementAndGet();
    }

    public LeaderboardSnapshot snapshot() {
        List<Map.Entry<UUID, Integer>> sorted = countByUser.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(TOP_PLAYERS_LIMIT)
                .toList();

        List<LeaderboardEntryDto> topPlayers = new ArrayList<>();
        for (Map.Entry<UUID, Integer> entry : sorted) {
            userRepository.findById(entry.getKey()).ifPresent(user ->
                    topPlayers.add(new LeaderboardEntryDto(user.getId(), user.getFullName(), user.getTeam(), entry.getValue())));
        }

        Map<Team, Integer> teamTotals = new EnumMap<>(Team.class);
        countByTeam.forEach((team, count) -> teamTotals.put(team, count.get()));

        return new LeaderboardSnapshot(topPlayers, teamTotals);
    }
}
