package com.puzzletimer.categories;

import java.util.UUID;

public interface Category {
    UUID getCategoryId();

    String getScramblerId();

    String getDescription();

    String[] getTipIds();

    Category setScramblerId(String scramblerId);

    Category setDescription(String description);

    Category setTipIds(String[] tipIds);

    Category setForceStart(boolean force);

    Category setPhases(int phases);

    char getAccelerator();

    char getMnemonic();

    int getPhases();

    boolean hasAlt();

    boolean isForceStart();

    boolean isCustom();
}
