package com.moodpatcher.AntiGrief;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;


public class Main extends JavaPlugin {
    private BlockDB blockDB;

    @Override
    public void onEnable() {
        Logger.start(this);
        com.moodpatcher.AntiGrief.Config.start(this);
        Locales.start(this);

        blockDB = new BlockDB();
        blockDB.connect(this);

        getServer().getPluginManager().registerEvents(new BlockEvents(this), this);
        getCommand("antigrief").setExecutor(this);

        int count = 0;
        int removedCount = 0;

        List<BlockData> blocks = blockDB.getBlocks("world");

        for (BlockData blockData : blocks) {
            ++count;
            Location location = new Location(Bukkit.getWorld(blockData.world), (double)blockData.x, (double)blockData.y, (double)blockData.z);

            Block block = location.getBlock();


            if (block.getType() == Material.AIR) {
                blockDB.removeBlock(blockData.id);
                block.setType(Material.AIR);
                ++removedCount;
            }
        }
        
    }

    @Override
    public void onDisable() {

    }

    public BlockDB getBlockDB() {
        return blockDB;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + Locales.getLocale("CMD_USAGE"));
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayerExact(targetName);

        if (target == null) {
            sender.sendMessage(ChatColor.RED + Locales.getLocale("CMD_PLAYER_NOT_FOUND"));
            return true;
        }

        Player p = (Player)sender;

        boolean inserted = blockDB.togglePermissions(p.getUniqueId().toString(), target.getUniqueId().toString());

        if (inserted) {
            sender.sendMessage(ChatColor.YELLOW + Locales.getLocale("CMD_PLAYER_ADDED", target.getName()));
            target.sendMessage(ChatColor.BLUE + Locales.getLocale("CMD_PLAYER_ADDED_GUEST", p.getName()));
        }

        else {
            sender.sendMessage(ChatColor.GREEN + Locales.getLocale("CMD_PLAYER_REMOVED", target.getName()));
        }

        return true;
    }
}
