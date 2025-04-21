package com.moodpatcher.AntiGrief;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class Logger {
    static JavaPlugin plugin;

    public static void start(JavaPlugin javaPlugin) {
        plugin = javaPlugin;
    }

    public static void log(Object... messages) {
        StringBuilder builder = new StringBuilder();

        for (Object message : messages) {
            builder.append(String.valueOf(message)).append(" ");
        }

        plugin.getLogger().info(builder.toString().trim());
    }
    
}
