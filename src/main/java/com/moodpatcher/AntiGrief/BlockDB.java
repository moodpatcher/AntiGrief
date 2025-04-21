package com.moodpatcher.AntiGrief;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockDB {
    private Connection connection;
    private JavaPlugin plugin;

    public void connect(JavaPlugin plugin) {
        try {
            File dataFolder = new File(plugin.getDataFolder(), "data.db");
            if (!dataFolder.exists()) {
                plugin.getDataFolder().mkdirs();
            }

            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder.getPath());
            this.plugin = plugin;

            createTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        try (Statement stmt = connection.createStatement()) {
            String queries[] = {
                "CREATE TABLE IF NOT EXISTS blockdb (id INTEGER PRIMARY KEY AUTOINCREMENT, world TEXT, x INTEGER, y INTEGER, z INTEGER, uuid TEXT, name TEXT, timestamp INTIGER, material TEXT, guest BOOL, guestOf TEXT, op BOOL)",
                "CREATE TABLE IF NOT EXISTS permissions (ownerName TEXT, ownerUUID TEXT, guestName TEXT, guestUUID TEXT)"
            };

            for (String query: queries) {
                stmt.executeUpdate(query);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertBlock(String world, int x, int y, int z, UUID uuid, String playerName, String material, boolean guest, String guestOf, boolean op) {
        String sql = "INSERT INTO blockdb (world, x, y, z, uuid, name, timestamp, material, guest, guestOf, op) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            long currentTimeMillis = System.currentTimeMillis();
    
            pstmt.setString(1, world);
            pstmt.setInt(2, x);
            pstmt.setInt(3, y);
            pstmt.setInt(4, z);
            pstmt.setString(5, uuid.toString());
            pstmt.setString(6, playerName);
            pstmt.setLong(7, currentTimeMillis);
            pstmt.setString(8, material);
            pstmt.setBoolean(9, guest);
            pstmt.setString(10, guestOf);
            pstmt.setBoolean(11, op);
    
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeBlock(double id) {
        String sql = "DELETE FROM blockdb WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public BlockData getBlockFromCoords(String world, int x, int y, int z) {
        String sql = "SELECT * FROM blockdb WHERE world = ? AND x = ? AND y = ? AND z = ? LIMIT 1";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            pstmt.setString(1, world);
            pstmt.setInt(2, x);
            pstmt.setInt(3, y);
            pstmt.setInt(4, z);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new BlockData(rs);
            } else {
                return null;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<BlockData> getBlocks(String world) {
        List<BlockData> blocksInRange = new ArrayList<>();

        String sql = "SELECT * FROM blockdb WHERE world = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, world);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                blocksInRange.add(new BlockData(rs));
            }

            return blocksInRange;

        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<BlockData> getBlocksInRange(String world, int centerX, int centerY, int centerZ, @Nullable Integer range) {
        if (range == null) range = Integer.MAX_VALUE;
    
        List<BlockData> blocksInRange = new ArrayList<>();
    
        String sql = "SELECT * FROM blockdb WHERE world = ? " +
                     "AND x BETWEEN ? AND ? " +
                     "AND z BETWEEN ? AND ?";
    
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, world);
            pstmt.setInt(2, centerX - range);
            pstmt.setInt(3, centerX + range);
            pstmt.setInt(4, centerZ - range);
            pstmt.setInt(5, centerZ + range);

            ResultSet rs = pstmt.executeQuery();
    
            while (rs.next()) {
                BlockData bd = new BlockData(rs);
                blocksInRange.add(bd);
            }
    
            return blocksInRange;
    
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void checkForFakeBlocks(String world, Location loc, Integer range) {
        if (range == null) range = 20;
        List<BlockData> blocks = getBlocksInRange(world, (int)loc.getX(), (int)loc.getY(), (int)loc.getZ(), range);

        for (BlockData blockData : blocks) {
            Location location = new Location(Bukkit.getWorld(blockData.world), (double)blockData.x, (double)blockData.y, (double)blockData.z);

            Block block = location.getBlock();

            if (block.getType() == Material.AIR) {
                removeBlock(blockData.id);
                block.setType(Material.AIR);
            }
        }
    }

    public boolean permissionCheck(String ownerUUID, String guestUUID) {
        String sql = "SELECT * FROM permissions WHERE ownerUUID = ? AND guestUUID = ? LIMIT 1";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, ownerUUID);
            pstmt.setString(2, guestUUID);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) { return true; }

        } catch (SQLException e) { e.printStackTrace(); return false; }

        return false;
    }

    public BlockData getGuestRoot(String world, int x, int y, int z) {
        List<BlockData> blocks = getBlocksInRange(world, x, y, z, BlockEvents.BLOCK_RADIUS * 2);

        for (BlockData blockData : blocks) {
            if (blockData.guest) continue;
            return blockData;
        }

        return null;
    }

    public boolean togglePermissions(String ownerUUID, String guestUUID) {
        String sql;
        boolean insert = false;
        
        if (permissionCheck(ownerUUID, guestUUID)) {
            sql = "DELETE FROM permissions WHERE ownerUUID = ? AND guestUUID = ?";
            insert = false;
        }

        else {
            sql = "INSERT INTO permissions (ownerName, ownerUUID, guestName, guestUUID) VALUES (?, ?, ?, ?)";
            insert = true;
        }

        Player owner = Bukkit.getPlayer(UUID.fromString(ownerUUID));
        Player guest = Bukkit.getPlayer(UUID.fromString(guestUUID));

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            if (insert) {
                pstmt.setString(1, owner.getName()); pstmt.setString(2, ownerUUID);
                pstmt.setString(3, guest.getName()); pstmt.setString(4, guestUUID);
            }
            else {
                pstmt.setString(1, ownerUUID); pstmt.setString(2, guestUUID);
            }

            pstmt.executeUpdate();

        } catch (SQLException e) { e.printStackTrace(); return false; }

        return insert;
    }
}