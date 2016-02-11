package com.puzzletimer.util;

import com.puzzletimer.models.Solution;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class SolutionUtils {
    public static String formatSeconds(long time, boolean round) {
        String result;

        if (time == Long.MAX_VALUE) return "DNF";

        String sign = "";
        if (time < 0) {
            sign = "-";
            time = -time;
        }

        if (round) time = time + 5;
        time = time / 10;

        long seconds = time / 100;
        long centiseconds = time % 100;
        result = sign +
                seconds + "." +
                (centiseconds < 10 ? "0" + centiseconds : centiseconds);

        return result;
    }

    public static String formatMinutes(long time, boolean round) {
        String result;

        if (time == Long.MAX_VALUE) return "DNF";

        String sign = "";
        if (time < 0) {
            sign = "-";
            time = -time;
        }

        if (round) time = time + 5;
        time = time / 10;

        long minutes = time / 6000;
        long seconds = (time / 100) % 60;
        long centiseconds = time % 100;
        result = sign +
                (minutes < 10 ? "0" + minutes : minutes) + ":" +
                (seconds < 10 ? "0" + seconds : seconds) + "." +
                (centiseconds < 10 ? "0" + centiseconds : centiseconds);

        return result;
    }

    public static String format(long time, boolean round) {
        if (-60000 < time && time < 60000) return formatSeconds(time, round);

        String result;

        if (time == Long.MAX_VALUE) return "DNF";

        String sign = "";
        if (time < 0) {
            sign = "-";
            time = -time;
        }

        if (round) time = time + 5;
        time = time / 10;

        long minutes = time / 6000;
        long seconds = (time / 100) % 60;
        long centiseconds = time % 100;
        result = sign +
                minutes + ":" +
                (seconds < 10 ? "0" + seconds : seconds) + "." +
                (centiseconds < 10 ? "0" + centiseconds : centiseconds);

        return result;
    }

    public static long parseTime(String input) {
        Scanner scanner = new Scanner(input.trim());
        scanner.useLocale(Locale.ENGLISH);

        long time;

        // 00:00.000
        if (input.contains(":")) {
            scanner.useDelimiter(":");

            if (!scanner.hasNextLong()) return 0;

            long minutes = scanner.nextLong();
            if (minutes < 0 || !scanner.hasNextDouble()) return 0;

            double seconds = scanner.nextDouble();
            if (seconds < 0.0 || seconds >= 60.0) return 0;

            time = (long) (60000 * minutes + 1000 * seconds);
        }

        // 00.000
        else {
            if (!scanner.hasNextDouble()) return 0;

            double seconds = scanner.nextDouble();
            if (seconds < 0.0) return 0;

            time = (long) (1000 * seconds);
        }

        return time;
    }

    public static long realTime(Solution solution) {
        if (solution.getPenalty().equals("DNF")) return Long.MAX_VALUE;

        if (solution.getPenalty().equals("+2"))
            return (((solution.getTiming().getElapsedTime() + 2000) / 10) * 10);

        if (solution.getPenalty().equals("+2")) return solution.getTiming().getElapsedTime() + 2000;

        return ((solution.getTiming().getElapsedTime() / 10) * 10);
    }

    public static long[] realTimes(Solution[] solutions, boolean filterDNF) {
        ArrayList<Long> realTimes = new ArrayList<>();
        for (Solution solution : solutions) {
            long actualTime = realTime(solution);
            if (!filterDNF || actualTime != Long.MAX_VALUE) realTimes.add(actualTime);
        }

        long[] realTimesArray = new long[realTimes.size()];
        for (int i = 0; i < realTimesArray.length; i++) realTimesArray[i] = realTimes.get(i);

        return realTimesArray;
    }

    public static int dnfCount(Solution[] solutions) {
        int count = 0;
        for (Solution s : solutions) if ("DNF".equals(s.getPenalty())) count++;
        return count;
    }
}
