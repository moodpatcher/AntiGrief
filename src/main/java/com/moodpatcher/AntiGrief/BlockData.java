package com.moodpatcher.AntiGrief;

import java.io.Serializable;
import java.sql.ResultSet;

public class BlockData implements Serializable  {
    public int id;
    public String world;
    public int x, y, z;
    public String uuid;
    public String name;
    public int timestamp;
    public String material;
    public boolean guest;
    public String guestOf;
    public boolean op;

    public BlockData(ResultSet rs) {
        try {
            this.id = rs.getInt("id");
            this.world = rs.getString("world");
            this.x = rs.getInt("x");
            this.y = rs.getInt("y");
            this.z = rs.getInt("z");
            this.uuid = rs.getString("uuid");
            this.name = rs.getString("name");
            this.timestamp = rs.getInt("timestamp");
            this.material = rs.getString("material");
            this.guest = rs.getBoolean("guest");
            this.guestOf = rs.getString("guestOf");
            this.op = rs.getBoolean("op");
        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "BlockDBData{" +
            "id='" + id + '\'' +
            "world='" + world + '\'' +
            ", x=" + x +
            ", y=" + y +
            ", z=" + z +
            ", uuid='" + uuid + '\'' +
            ", name='" + name + '\'' +
            ", timestamp='" + timestamp + '\'' +
            ", material='" + material + '\'' +
            ", guest='" + guest + '\'' +
            ", guestOf='" + guestOf + '\'' +
            ", op='" + op + '\'' +
            '}';
    }
}
