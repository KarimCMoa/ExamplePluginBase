package me.karim.player;

import lombok.Getter;
import lombok.Setter;
import me.karim.CoreExample;
import me.karim.utilities.reflection.BukkitReflection;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
public class PlayerInfo {

    private final UUID uuid;
    @Setter
    private String name;

    public PlayerInfo(Player player) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
    }

    public PlayerInfo(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public Player toPlayer() {
        Player player = CoreExample.INSTANCE().getServer().getPlayer(this.getUuid());

        if (player != null && player.isOnline()) {
            return player;
        } else {
            return null;
        }
    }

    public String getDisplayName() {
        Player player = this.toPlayer();

        return player == null ? this.getName() : player.getDisplayName();
    }

    public int getPing() {
        Player player = CoreExample.INSTANCE().getServer().getPlayer(this.getUuid());

        if (player == null) {
            return 0;
        } else {
            return BukkitReflection.getPing(player);
        }
    }

}
