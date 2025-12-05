package com.orbitalstriker.backend.modules.user.controller;

import com.orbitalstriker.backend.modules.user.dto.LeaderboardEntryDto;
import com.orbitalstriker.backend.modules.user.mapper.UserMapper;
import com.orbitalstriker.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class StatsController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @GetMapping("/leaderboard")
    public List<LeaderboardEntryDto> getLeaderboard() {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "eloRating"))
                .stream()
                .limit(10)
                .map(userMapper::toLeaderboardDto)
                .collect(Collectors.toList());
    }
}