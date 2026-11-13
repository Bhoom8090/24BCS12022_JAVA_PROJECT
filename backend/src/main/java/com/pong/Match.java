package com.pong;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player1_score", nullable = false)
    private int player1Score;

    @Column(name = "player2_score", nullable = false)
    private int player2Score;

    @Column(name = "winner", nullable = false, length = 10)
    private String winner;

    @Column(name = "played_at")
    private LocalDateTime playedAt;

    protected Match() {
    }

    public Match(int player1Score, int player2Score, String winner) {
        this.player1Score = player1Score;
        this.player2Score = player2Score;
        this.winner = winner;
        this.playedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public int getPlayer1Score() { return player1Score; }
    public int getPlayer2Score() { return player2Score; }
    public String getWinner() { return winner; }
    public LocalDateTime getPlayedAt() { return playedAt; }
}
