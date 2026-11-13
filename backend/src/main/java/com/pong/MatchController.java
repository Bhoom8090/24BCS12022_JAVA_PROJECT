package com.pong;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchRepository repo;

    public MatchController(MatchRepository repo) {
        this.repo = repo;
    }

    // Save a finished match
    @PostMapping
    public Match save(@RequestBody MatchRequest r) {
        return repo.save(new Match(r.player1Score(), r.player2Score(), r.winner()));
    }

    // Last 10 matches, newest first
    @GetMapping
    public List<Match> latest() {
        return repo.findTop10ByOrderByIdDesc();
    }
}
