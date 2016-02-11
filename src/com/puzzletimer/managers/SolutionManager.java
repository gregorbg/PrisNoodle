package com.puzzletimer.managers;

import com.puzzletimer.models.Solution;

import java.util.*;

public class SolutionManager {
    public static class Listener {
        public void solutionAdded(Solution solution) {
        }

        public void solutionRemoved(Solution solution) {
        }

        public void solutionUpdated(Solution solution) {
        }

        public void solutionsUpdated(Solution[] solutions) {
        }
    }

    private ArrayList<Listener> listeners;
    private HashMap<UUID, Solution> solutions;

    public SolutionManager() {
        this.listeners = new ArrayList<>();
        this.solutions = new HashMap<>();
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

    public void loadSolutions(Solution[] solutions) {
        this.solutions.clear();
        for (Solution solution : solutions) this.solutions.put(solution.getSolutionId(), solution);

        notifyListeners();
    }

    public void addSolution(Solution solution) {
        this.solutions.put(solution.getSolutionId(), solution);

        for (Listener listener : this.listeners) listener.solutionAdded(solution);

        notifyListeners();
    }

    public void removeSolution(Solution solution) {
        this.solutions.remove(solution.getSolutionId());

        for (Listener listener : this.listeners) listener.solutionRemoved(solution);

        notifyListeners();
    }

    public void updateSolution(Solution solution) {
        this.solutions.put(solution.getSolutionId(), solution);

        for (Listener listener : this.listeners) listener.solutionUpdated(solution);

        notifyListeners();
    }

    public void notifyListeners() {
        Solution[] solutions = getSolutions();

        for (Listener listener : this.listeners) listener.solutionsUpdated(solutions);
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }
}
