package com.puzzletimer.managers;

import com.puzzletimer.models.ConfigurationEntry;

import java.util.ArrayList;
import java.util.HashMap;

public class ConfigurationManager {
    public static class Listener {
        public void configurationEntryUpdated(String key, String value) {
        }
    }

    private ArrayList<Listener> listeners;
    private HashMap<String, ConfigurationEntry> entryMap;

    public ConfigurationManager(ConfigurationEntry[] entries) {
        this.listeners = new ArrayList<>();

        this.entryMap = new HashMap<>();
        for (ConfigurationEntry entry : entries) this.entryMap.put(entry.getKey(), entry);
    }

    public String getConfiguration(String key) {
        ConfigurationEntry configurationEntry = this.entryMap.get(key);
        if (configurationEntry == null) return null;

        return configurationEntry.getValue();
    }

    public boolean getBooleanConfiguration(String key) {
        ConfigurationEntry configurationEntry = this.entryMap.get(key);
        return configurationEntry != null && Boolean.parseBoolean(configurationEntry.getValue().toLowerCase());
    }

    public void setConfiguration(String key, String value) {
        this.entryMap.put(key, new ConfigurationEntry(key, value));
        for (Listener listener : this.listeners) listener.configurationEntryUpdated(key, value);
    }

    public void setBooleanConfiguration(String key, boolean boolValue) {
        String value = String.valueOf(boolValue).toUpperCase();
        this.entryMap.put(key, new ConfigurationEntry(key, value));
        for (Listener listener : this.listeners) listener.configurationEntryUpdated(key, value);
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }
}
