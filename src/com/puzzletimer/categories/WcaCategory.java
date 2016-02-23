package com.puzzletimer.categories;

import java.util.UUID;

public class WcaCategory implements Category {
    private final UUID categoryId;
    private final String scramblerId;
    private final String description;
    private final String[] tipIds;
    private final char mnemonic, accelerator;
    private final boolean alt;
    private final boolean bld;
    private final boolean isCustom;

    public WcaCategory(UUID categoryId, String scramblerId, String description, String[] tipIds, char mnemonic, char accelerator, boolean isBld, boolean isCustom) {
        this(categoryId, scramblerId, description, tipIds, mnemonic, accelerator, false, isBld, isCustom);
    }

    public WcaCategory(UUID categoryId, String scramblerId, String description, String[] tipIds, char mnemonic, char accelerator, boolean hasAlt, boolean isBld, boolean isCustom) {
        this.categoryId = categoryId;
        this.scramblerId = scramblerId;
        this.description = description;
        this.tipIds = tipIds;
        this.mnemonic = mnemonic;
        this.accelerator = accelerator;
        this.alt = hasAlt;
        this.bld = isBld;
        this.isCustom = isCustom;
    }

    public UUID getCategoryId() {
        return this.categoryId;
    }

    public String getScramblerId() {
        return this.scramblerId;
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public String[] getTipIds() {
        return this.tipIds;
    }

    @Override
    public Category setScramblerId(String scramblerId) {
        return new WcaCategory(this.categoryId, scramblerId, this.description, this.tipIds, this.mnemonic, this.accelerator, this.alt, this.bld, this.isCustom);
    }

    @Override
    public Category setDescription(String description) {
        return new WcaCategory(this.categoryId, this.scramblerId, description, this.tipIds, this.mnemonic, this.accelerator, this.alt, this.bld, this.isCustom);
    }

    @Override
    public Category setTipIds(String[] tipIds) {
        return new WcaCategory(this.categoryId, this.scramblerId, this.description, tipIds, this.mnemonic, this.accelerator, this.alt, this.bld, this.isCustom);
    }

    @Override
    public Category setBldMode(boolean bldMode) {
        return new WcaCategory(this.categoryId, this.scramblerId, this.description, this.tipIds, this.mnemonic, this.accelerator, this.alt, bldMode, this.isCustom);
    }

    public char getAccelerator() {
        return this.accelerator;
    }

    public char getMnemonic() {
        return this.mnemonic;
    }

    @Override
    public boolean hasAlt() {
        return this.alt;
    }

    @Override
    public boolean isBldMode() {
        return this.bld;
    }

    @Override
    public boolean isCustom() {
        return this.isCustom;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WcaCategory) {
            WcaCategory other = (WcaCategory) obj;
            return other.getCategoryId().equals(this.categoryId);
        } else return false;
    }
}
