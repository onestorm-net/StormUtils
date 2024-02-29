package net.onestorm.plugins.stormutils.paper.user;

import net.kyori.adventure.text.Component;
import net.onestorm.library.paper.user.PaperOnlineUser;
import net.onestorm.plugins.stormutils.api.storage.UserData;
import net.onestorm.plugins.stormutils.api.user.OnlineUtilUser;
import org.bukkit.entity.Player;

import java.sql.Date;
import java.util.UUID;

public class OnlineUtilUserImpl extends UtilUserImpl implements OnlineUtilUser, PaperOnlineUser {

    private final Player player;
    private UserData userData;

    public OnlineUtilUserImpl(Player player) {
        super(player.getUniqueId());
        this.player = player;
    }

    @Override
    public String getUsername() {
        return player.getName();
    }

    @Override
    public void sendMessage(Component component) {
        player.sendMessage(component);
    }

    @Override
    public boolean hasPermission(String node) {
        return player.hasPermission(node);
    }

    @Override
    public UUID getUuid() {
        return player.getUniqueId();
    }

    @Override
    public Player asPlayer() {
        return player;
    }
}
