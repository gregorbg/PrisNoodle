package com.puzzletimer.managers;

import com.puzzletimer.models.Solution;

import java.util.*;

public class SessionManager {
    public static class Listener {
        public void solutionsUpdated(Solution[] solutions) {
        }
    }

    private ArrayList<Listener> listeners;
    private HashMap<UUID, Solution> solutions;
    private UUID sessionID;

    public SessionManager() {
        this.listeners = new ArrayList<>();
        this.solutions = new HashMap<>();
        this.sessionID = UUID.randomUUID();
    }

    public Solution[] getSolutions() {
        ArrayList<Solution> solutions =
                new ArrayList<>(this.solutions.values());

        Collections.sort(solutions, (solution1, solution2) -> {
            Date start1 = solution1.getTiming().getStart();
            Date start2 = solution2.getTiming().getStart();
            return start2.compareTo(start1);
        });

        Solution[] solutionsArray = new Solution[solutions.size()];
        solutions.toArray(solutionsArray);

        return solutionsArray;
    }

    public void addSolution(Solution solution) {
        this.solutions.put(solution.getSolutionId(), solution);
        notifyListeners();
    }

    public void updateSolution(Solution solution) {
        if (this.solutions.containsKey(solution.getSolutionId())) {
            this.solutions.put(solution.getSolutionId(), solution);
            notifyListeners();
        }
    }

    public void removeSolution(Solution solution) {
        this.solutions.remove(solution.getSolutionId());
        notifyListeners();
    }

    public UUID getSessionID() {
        return this.sessionID;
    }

    public void clearSession(boolean updateSessionId) {
        this.solutions.clear();
        if (updateSessionId) this.sessionID = UUID.randomUUID();
        notifyListeners();
    }

    public void notifyListeners() {
        ArrayList<Solution> solutions =
                new ArrayList<>(this.solutions.values());

        Collections.sort(solutions, (solution1, solution2) -> {
            Date start1 = solution1.getTiming().getStart();
            Date start2 = solution2.getTiming().getStart();
            return start2.compareTo(start1);
        });

        Solution[] solutionsArray = new Solution[solutions.size()];
        solutions.toArray(solutionsArray);

        for (Listener listener : this.listeners) listener.solutionsUpdated(solutionsArray);
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }
}
