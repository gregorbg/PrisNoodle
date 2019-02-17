package com.puzzletimer.models;

import java.util.UUID;

public class Solution {
    private final UUID solutionId;
    private final UUID categoryId;
    private final UUID sessionId;
    private final Scramble scramble;
    private final Timing timing;
    private final String penalty;

    public Solution(UUID solutionId, UUID categoryId, UUID sessionId, Scramble scramble, Timing timing, String penalty) {
        this.solutionId = solutionId;
        this.categoryId = categoryId;
        this.sessionId = sessionId;
        this.scramble = scramble;
        this.timing = timing;
        this.penalty = penalty;
    }

    public UUID getSolutionId() {
        return this.solutionId;
    }

    public UUID getCategoryId() {
        return this.categoryId;
    }

    public Scramble getScramble() {
        return this.scramble;
    }

    public Timing getTiming() {
        return this.timing;
    }

    public String getPenalty() {
        return this.penalty;
    }

    public Solution setPenalty(String penalty) {
        return new Solution(
                this.solutionId,
                this.categoryId,
                this.sessionId,
                this.scramble,
                this.timing,
                penalty);
    }

    public Solution setTiming(Timing timing) {
        return new Solution(
                this.solutionId,
                this.categoryId,
                this.sessionId,
                this.scramble,
                timing,
                this.penalty);
    }

    public UUID getSessionId() {
        return this.sessionId;
    }

}
