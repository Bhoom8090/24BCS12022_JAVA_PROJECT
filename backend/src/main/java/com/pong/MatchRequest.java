package com.pong;

public record MatchRequest(int player1Score, int player2Score, String winner) {
}
