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

    Category setBldMode(boolean bldMode);

    char getAccelerator();

    char getMnemonic();

    boolean hasAlt();

    boolean isBldMode();

    boolean isCustom();
}
