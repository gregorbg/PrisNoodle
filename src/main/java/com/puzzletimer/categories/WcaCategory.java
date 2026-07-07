package com.puzzletimer.categories;

import java.util.UUID;

public class WcaCategory implements Category {
    private final UUID categoryId;
    private final String scramblerId;
    private final String description;
    private final String[] tipIds;
    private final char mnemonic, accelerator;
    private final int phases;
    private final boolean alt;
    private final boolean forceStart;
    private final boolean isCustom;

	public WcaCategory(UUID categoryId, String scramblerId, String description, String[] tipIds, char mnemonic, char accelerator, boolean isCustom) {
		this(categoryId, scramblerId, description, tipIds, mnemonic, accelerator, 1, false, false, isCustom);
	}

    public WcaCategory(UUID categoryId, String scramblerId, String description, String[] tipIds, char mnemonic, char accelerator, int phases, boolean isCustom) {
        this(categoryId, scramblerId, description, tipIds, mnemonic, accelerator, phases, false, false, isCustom);
    }

	public WcaCategory(UUID categoryId, String scramblerId, String description, String[] tipIds, char mnemonic, char accelerator, boolean isForceStart, boolean isCustom) {
		this(categoryId, scramblerId, description, tipIds, mnemonic, accelerator, 1, false, isForceStart, isCustom);
	}

    public WcaCategory(UUID categoryId, String scramblerId, String description, String[] tipIds, char mnemonic, char accelerator, int phases, boolean isForceStart, boolean isCustom) {
        this(categoryId, scramblerId, description, tipIds, mnemonic, accelerator, phases, false, isForceStart, isCustom);
    }

	public WcaCategory(UUID categoryId, String scramblerId, String description, String[] tipIds, char mnemonic, char accelerator, boolean hasAlt, boolean isForceStart, boolean isCustom) {
		this(categoryId, scramblerId, description, tipIds, mnemonic, accelerator, 1, hasAlt, isForceStart, isCustom);
	}

    public WcaCategory(UUID categoryId, String scramblerId, String description, String[] tipIds, char mnemonic, char accelerator, int phases, boolean hasAlt, boolean isForceStart, boolean isCustom) {
        this.categoryId = categoryId;
        this.scramblerId = scramblerId;
        this.description = description;
        this.tipIds = tipIds;
        this.mnemonic = mnemonic;
        this.accelerator = accelerator;
        this.phases = phases;
        this.alt = hasAlt;
        this.forceStart = isForceStart;
        this.isCustom = isCustom;
    }

    @Override
	public UUID getCategoryId() {
        return this.categoryId;
    }

    @Override
	public String getScramblerId() {
        return this.scramblerId;
    }

    @Override
	public String getDescription() {
        return this.description;
    }

    @Override
    public String[] getTipIds() {
        return this.tipIds;
    }

    @Override
    public Category setScramblerId(String scramblerId) {
        return new WcaCategory(this.categoryId, scramblerId, this.description, this.tipIds, this.mnemonic, this.accelerator, this.phases, this.alt, this.forceStart, this.isCustom);
    }

    @Override
    public Category setDescription(String description) {
        return new WcaCategory(this.categoryId, this.scramblerId, description, this.tipIds, this.mnemonic, this.accelerator, this.phases, this.alt, this.forceStart, this.isCustom);
    }

    @Override
    public Category setTipIds(String[] tipIds) {
        return new WcaCategory(this.categoryId, this.scramblerId, this.description, tipIds, this.mnemonic, this.accelerator, this.phases, this.alt, this.forceStart, this.isCustom);
    }

    @Override
    public Category setForceStart(boolean force) {
        return new WcaCategory(this.categoryId, this.scramblerId, this.description, this.tipIds, this.mnemonic, this.accelerator, this.phases, this.alt, force, this.isCustom);
    }

    @Override
    public Category setPhases(int phases) {
        return new WcaCategory(this.categoryId, this.scramblerId, this.description, this.tipIds, this.mnemonic, this.accelerator, phases, this.alt, this.forceStart, this.isCustom);
    }

    @Override
	public char getAccelerator() {
        return this.accelerator;
    }

    @Override
	public char getMnemonic() {
        return this.mnemonic;
    }

    @Override
	public int getPhases() {
		return this.phases;
	}

    @Override
    public boolean hasAlt() {
        return this.alt;
    }

    @Override
    public boolean isForceStart() {
        return this.forceStart;
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
