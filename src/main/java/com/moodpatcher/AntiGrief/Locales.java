package com.moodpatcher.AntiGrief;

import java.io.File;
import java.io.FileWriter;
import java.text.MessageFormat;
import java.time.LocalDateTime;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Locales {
    static JavaPlugin plugin;
    private static String language;

    public static void start(JavaPlugin javaPlugin) {
        plugin = javaPlugin;
        language = Config.read("language");

        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            File configFile = new File(dataFolder, "locales.yaml");
            if (!configFile.exists()) {
                configFile.createNewFile();

                FileWriter writer = new FileWriter(configFile);
                
                writer.write("# Generated by " + (Config.class.getCanonicalName()) + " at " + LocalDateTime.now() + "\n");
                writer.write("\n");
                writer.write("EN:\n");
                writer.write("  OP_NO_RAID: \"This block cannot be raided.\"\n");
                writer.write("  INSUFFICIENT_FIRECHARGES: \"You need at least {0} Fire Charges\"\n");
                writer.write("  BLOCK_CONFLICT_MSG: \"You need to ask for {0}'s permission to interact with this zone.\"\n");
                writer.write("  ZONE_ENUM_OWNER: \"Owner\"\n");
                writer.write("  ZONE_ENUM_ERROR: \"None\"\n");
                writer.write("  ZONE_ENUM_GUEST: \"Guest\"\n");
                writer.write("  ZONE_ENUM_NEUTRAL: \"Neutral\"\n");
                writer.write("  ZONE_INFO_YES: \"Yes\"\n");
                writer.write("  ZONE_INFO_NO: \"No\"\n");
                writer.write("  ZONE_INFO: \"Zone info\"\n");
                writer.write("  ZONE_INFO_INT: \"Zone interaction:\"\n");
                writer.write("  ZONE_INFO_BLK_INT: \"Block interaction:\"\n");
                writer.write("  ZONE_INFO_GUEST_BLK: \"Guest block:\"\n");
                writer.write("  ZONE_INFO_PERMISSION: \"Permission:\"\n");
                writer.write("  CMD_USAGE: \"Usage: /antigrief <playername>\"\n");
                writer.write("  CMD_PLAYER_NOT_FOUND: \"Player not found.\"\n");
                writer.write("  CMD_PLAYER_ADDED: \"{0} can now access your zones.\"\n");
                writer.write("  CMD_PLAYER_ADDED_GUEST: \"{0} has added you to their zones as a guest.\"\n");
                writer.write("  CMD_PLAYER_REMOVED: \"{0} can't access your zones anymore.\"\n");
                writer.write("\n");
                writer.write("HU:\n");
                writer.write("  OP_NO_RAID: \"Ez a blokk nem raidelhető.\"\n");
                writer.write("  INSUFFICIENT_FIRECHARGES: \"Legalább {0} Fire Charge-ra van szükséged.\"\n");
                writer.write("  BLOCK_CONFLICT_MSG: \"{0} engedélyére van szükséged hogy interaktálhass ezzel a zónával.\"\n");
                writer.write("  ZONE_ENUM_OWNER: \"Tulaj\"\n");
                writer.write("  ZONE_ENUM_ERROR: \"Nincs\"\n");
                writer.write("  ZONE_ENUM_GUEST: \"Vendég\"\n");
                writer.write("  ZONE_ENUM_NEUTRAL: \"Semleges\"\n");
                writer.write("  ZONE_INFO_YES: \"Igen\"\n");
                writer.write("  ZONE_INFO_NO: \"Nem\"\n");
                writer.write("  ZONE_INFO: \"Zóna infó\"\n");
                writer.write("  ZONE_INFO_INT: \"Zóna interakció:\"\n");
                writer.write("  ZONE_INFO_BLK_INT: \"Blokk interakció:\"\n");
                writer.write("  ZONE_INFO_GUEST_BLK: \"Vendég blokk:\"\n");
                writer.write("  ZONE_INFO_PERMISSION: \"Jogosultság:\"\n");
                writer.write("  CMD_USAGE: \"Használat: /antigrief <játékos>\"\n");
                writer.write("  CMD_PLAYER_NOT_FOUND: \"A játékos nem található.\"\n");
                writer.write("  CMD_PLAYER_ADDED: \"{0} mostantól hozzáfér a zónáidhoz.\"\n");
                writer.write("  CMD_PLAYER_ADDED_GUEST: \"{0} hozzáadott a zónáihoz, mint vendég.\"\n");
                writer.write("  CMD_PLAYER_REMOVED: \"{0} nem fér mostantól hozzá a zónáidhoz.\"\n");

                writer.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getLocale(String localeName, Object... args) {
        File configFile = new File(plugin.getDataFolder(), "locales.yaml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        String message = config.getString(language + "." + localeName);

        if (args != null)
            for (int i = 0; i < args.length; i++) {
                message = message.replace("{" + i + "}", String.valueOf(args[i]));
            }
    
        return message;
    }
}
