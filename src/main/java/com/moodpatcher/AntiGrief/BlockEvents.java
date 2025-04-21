package com.moodpatcher.AntiGrief;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.moodpatcher.AntiGrief.BlockConflict.BlockConflictCase;

public class BlockEvents implements Listener {
    public static final int BLOCK_RADIUS = Integer.parseInt(Config.read("protection_radius"));
    public static final boolean RAIDING_ENABLED = Boolean.parseBoolean(Config.read("raiding_enabled"));
    public static final short INTERACT_TIMEOUT = 10;
    
    private static HashMap<String, Long> interactEventLastRun = new HashMap<String, Long>();
    private final Main plugin;

    public BlockEvents(Main plugin) { this.plugin = plugin; }

    //Block interaction event
    @EventHandler 
    public void PlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        long time = System.currentTimeMillis();

        //idk 😭😭
        if (interactEventLastRun.containsKey(uuid)) {
            long lastTime = interactEventLastRun.get(uuid);

            if (time - lastTime < INTERACT_TIMEOUT) {
                event.setCancelled(true);
                return;
            }
        }

        interactEventLastRun.put(uuid, time);

        Block block = event.getClickedBlock(); //Block the player is abt to interact with
        Block newBlock = block.getRelative(event.getBlockFace()); //Block thats about to be created

        BlockConflict baseConflict = canPlayerInteractWithBlock(block, player, true);
        BlockConflict thisConflict = canPlayerInteractWithBlock(block, player, false);

        boolean isGuest = baseConflict.conflictCase == BlockConflictCase.GUEST;

        boolean isItemInHand = event.getItem() != null;
        boolean building = false;
        boolean raiding = false; 

        ItemStack itemStack = event.getItem();
        
        Location loc = newBlock.getLocation();
        Location oldLoc = block.getLocation();

        if (isItemInHand) {
            building = event.getItem().getType().isBlock();
            raiding = event.getItem().getType() == Material.FIRE_CHARGE || event.getItem().getType() == Material.NETHER_STAR;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            
            if (event.getItem() != null && event.getItem().getType() == Material.STICK && player.isSneaking()) { printZoneInfo(player, block); event.setCancelled(true); return; }

            //No permissions
            if (baseConflict.conflict) {
                //Raid
                if (RAIDING_ENABLED) {
                    if (event.getItem() != null) {
                        Material item = event.getItem().getType();

                        if (raiding) {
                            if (item == Material.FIRE_CHARGE) {

                                //If the player wants to raid an OP block
                                if (thisConflict.blockData.op || baseConflict.blockData.op) {
                                    if (!Boolean.parseBoolean(Config.read("op_raid"))) {
                                        event.getPlayer().sendMessage(ChatColor.RED + Locales.getLocale("OP_NO_RAID", null));
                                        event.setCancelled(true);
                                        return;
                                    }
                                }

                                int fireCharges = Integer.parseInt(Config.read("fire_charges"));

                                //If the player doesn't have 32 Fire Charges as their main item
                                if (itemStack.getAmount() < fireCharges) {
                                    event.getPlayer().sendMessage(ChatColor.RED + Locales.getLocale("INSUFFICIENT_FIRECHARGES", fireCharges));
                                    event.setCancelled(true);
                                    return;
                                }

                                itemStack.setAmount(itemStack.getAmount() - fireCharges);

                                if (!block.getType().toString().equals("CHEST"))
                                    loc.getWorld().strikeLightning(block.getLocation());

                                block.getWorld().createExplosion(oldLoc, 1.3f);
                            }

                            else if (item == Material.NETHER_STAR && event.getPlayer().isOp()) {
                                block.getWorld().createExplosion(oldLoc, 20f);
                            }

                            plugin.getBlockDB().checkForFakeBlocks(block.getWorld().getName(), loc, 20);

                            loc.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, loc, 1);
                            loc.getWorld().spawnParticle(Particle.CRIT, loc, 1);

                            loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
                            loc.getWorld().playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0F, 1.0F);
                            loc.getWorld().playSound(loc, Sound.EVENT_RAID_HORN, 1.0F, 1.0F);

                            return;
                        }
                    }
                }

                conflict(baseConflict, event.getPlayer(), event, event.getClass().getSimpleName());
                return;
            }

            //Building
            if (building) {
                //Check if theres a block in DB for the coords
                BlockData blockOnCoords = plugin.getBlockDB().getBlockFromCoords(newBlock.getWorld().getName(), newBlock.getX(), newBlock.getY(), newBlock.getZ());

                //Workaround for the case, if the player is building as guest, to a zone that is out of the owner's blocks' protection range.
                //From that point on, the guest will own that zone. Weird logic, might change later, idk
                if (isGuest && baseConflict.blockData == null) {
                    isGuest = false;
                }

                //If there isn't any block on the coords, it gets inserted to DB
                if (blockOnCoords == null) {
                    plugin.getBlockDB().insertBlock(newBlock.getWorld().getName(), newBlock.getX(), newBlock.getY(), newBlock.getZ(), 
                        event.getPlayer().getUniqueId(), event.getPlayer().getName(), event.getItem().getType().toString(), 
                        isGuest, isGuest ? baseConflict.blockData.uuid : null, event.getPlayer().isOp()
                    );

                    //Post-insertion checking
                    BlockData newBlockOnCoords = plugin.getBlockDB().getBlockFromCoords(newBlock.getWorld().getName(), newBlock.getX(), newBlock.getY(), newBlock.getZ());
                    Location location = new Location(Bukkit.getWorld(newBlockOnCoords.world), newBlockOnCoords.x, newBlockOnCoords.y, newBlockOnCoords.z);

                    //Removing invalid block from the DB
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        Block newGameBlock = location.getBlock();

                        if (newGameBlock.getType() == Material.AIR) {
                            plugin.getBlockDB().removeBlock(newBlockOnCoords.id);
                        }

                    }, 40L);
                }
            }
        }
    }

    //Block breaking event
    @EventHandler 
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        BlockData blockOnCoords = plugin.getBlockDB().getBlockFromCoords(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
        BlockConflict conflict = canPlayerInteractWithBlock(block, event.getPlayer(), true);

        if (!conflict.conflict) {
            if (blockOnCoords != null) { 
                plugin.getBlockDB().removeBlock(blockOnCoords.id); 
            }
        }

        else {
            conflict(conflict, event.getPlayer(), event, event.getClass().getSimpleName());
        }
    }

    //Permission conflict
    void conflict(BlockConflict conflict, Player player, Event event, String eventType) {
        if (!conflict.blockData.guest) player.sendMessage(ChatColor.RED + Locales.getLocale("BLOCK_CONFLICT_MSG", conflict.blockData.name));
        else {
            UUID uuid = UUID.fromString(conflict.blockData.guestOf);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            String playerName = offlinePlayer.getName();

            player.sendMessage(ChatColor.RED + Locales.getLocale("BLOCK_CONFLICT_MSG", playerName));
        }

        if (event instanceof Cancellable) {
            Cancellable cEvent = (Cancellable) event;
            cEvent.setCancelled(true);
        }
    }

    void printZoneInfo(Player player, Block block) {
        BlockConflict baseConflict = canPlayerInteractWithBlock(block, player, true);
        BlockConflict thisConflict = canPlayerInteractWithBlock(block, player, false);
        BlockData blockData = plugin.getBlockDB().getBlockFromCoords(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());

        String guest = ChatColor.GRAY + Locales.getLocale("ZONE_INFO_NO");
        String raidable = ChatColor.GREEN + Locales.getLocale("ZONE_INFO_YES");

        if (blockData != null) {
            guest = blockData.guest ? ChatColor.YELLOW + Locales.getLocale("ZONE_INFO_YES") : ChatColor.GRAY + Locales.getLocale("ZONE_INFO_NO");
            raidable = blockData.op ? ChatColor.RED + Locales.getLocale("ZONE_INFO_NO") : ChatColor.GREEN + Locales.getLocale("ZONE_INFO_YES");
        }

        String zoneInteraction = baseConflict.conflict ? ChatColor.RED + Locales.getLocale("ZONE_INFO_NO") : ChatColor.GREEN + Locales.getLocale("ZONE_INFO_YES");
        String blockInteraction = thisConflict.conflict ? ChatColor.RED + Locales.getLocale("ZONE_INFO_NO") : ChatColor.GREEN + Locales.getLocale("ZONE_INFO_YES");
        String myPermissions = 
            baseConflict.conflictCase == BlockConflictCase.ERROR ? ChatColor.RED + Locales.getLocale("ZONE_ENUM_ERROR") :
            baseConflict.conflictCase == BlockConflictCase.OK ? ChatColor.GREEN + Locales.getLocale("ZONE_ENUM_OWNER") :
            baseConflict.conflictCase == BlockConflictCase.GUEST ? ChatColor.YELLOW + Locales.getLocale("ZONE_ENUM_GUEST") :
            baseConflict.conflictCase == BlockConflictCase.NEUTRAL ? ChatColor.GRAY + Locales.getLocale("ZONE_ENUM_NEUTRAL") :
            ChatColor.RED + "";

        player.sendMessage(ChatColor.BLUE + "/▔▔▔▔▔▔▔▔[" + Locales.getLocale("ZONE_INFO") + "]▔▔▔▔▔▔▔▔\\");
        player.sendMessage(ChatColor.WHITE + "• " + Locales.getLocale("ZONE_INFO_INT") + " " + zoneInteraction);
        player.sendMessage(ChatColor.WHITE + "• " + Locales.getLocale("ZONE_INFO_BLK_INT") + " " + blockInteraction);
        player.sendMessage(ChatColor.WHITE + "• " + Locales.getLocale("ZONE_INFO_GUEST_BLK") + " " + guest);
        player.sendMessage(ChatColor.WHITE + "• " + Locales.getLocale("ZONE_INFO_PERMISSION") + " " + myPermissions);
        player.sendMessage(ChatColor.GREEN + "☕ plugin by moodpatcher");
        player.sendMessage(ChatColor.BLUE + "\\_______[" + Locales.getLocale("ZONE_INFO") + "]_______/");
    }

    //Permission checking
    BlockConflict canPlayerInteractWithBlock(Block block, Player player, boolean includeProtectionRange) {
        if (Boolean.parseBoolean(Config.read("op_bypass"))) return new BlockConflict(null, false, BlockConflictCase.OK);

        List<BlockData> blocks = plugin.getBlockDB().getBlocksInRange(block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), 
            includeProtectionRange ? BLOCK_RADIUS * 2 : 0);

        String playerUUID = player.getUniqueId().toString();

        boolean conflict = false; BlockData conflictingBlock = null; BlockConflictCase conflictCase = BlockConflictCase.NEUTRAL;
        boolean error = false;

        for (BlockData blockData : blocks) {
            //If theres a collision, the loop will break on the next run, and the function will return the conflicting block
            if (error) break;

            //Ha guest blokk
            if (blockData.guest) {
                BlockData guestRoot = plugin.getBlockDB().getGuestRoot(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());

                //If its a guest block, and the player has permissions to it (for example if it was placed by another guest)
                if (plugin.getBlockDB().permissionCheck(blockData.guestOf, playerUUID)) { 
                    conflictCase = BlockConflictCase.GUEST;
                    conflictingBlock = guestRoot;
                }

                //If its a guest block, and the player is the owner of the zone
                else if (blockData.guestOf.equals(playerUUID)) {
                    conflictCase = BlockConflictCase.OK; 
                }

                //Guest block placed by guest (player)
                else if (blockData.uuid.equals(playerUUID)) {
                    conflictCase = BlockConflictCase.GUEST; 
                    conflictingBlock = guestRoot;
                }
                
                //Guest block with no permissions
                else { 
                    conflict = true; 
                    conflictingBlock = blockData;
                    conflictCase = BlockConflictCase.NOT_GUEST;
                    error = true;
                }

            }

            //UUID match
            else if (playerUUID.equals(blockData.uuid)) { 
                conflictCase = BlockConflictCase.OK; 
            }

            //Not a guest block, but the player has permissions
            else if (plugin.getBlockDB().permissionCheck(blockData.uuid, playerUUID)) { 
                conflictingBlock = blockData;
                conflictCase = BlockConflictCase.GUEST; 
            } 

            //No permissions, no UUID matches
            else { 
                conflict = true; 
                conflictingBlock = blockData; 
                conflictCase = BlockConflictCase.ERROR; 
                error = true; 
            }
        }

        return new BlockConflict(conflictingBlock, conflict, conflictCase);
    }
}
