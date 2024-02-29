package net.onestorm.plugins.stormutils.paper.user;

import net.onestorm.plugins.stormutils.api.storage.UserData;
import net.onestorm.plugins.stormutils.api.user.OnlineUtilUser;
import net.onestorm.plugins.stormutils.api.user.UserManager;
import net.onestorm.plugins.stormutils.api.user.UtilUser;
import net.onestorm.plugins.stormutils.core.storage.UserDataImpl;
import net.onestorm.plugins.stormutils.paper.StormUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class UserManagerImpl implements UserManager, Listener {

    private final Map<UUID, OnlineUtilUser> UuidToUserMap = new ConcurrentHashMap<>();
    private final Map<String, OnlineUtilUser> UsernameToUserMap = new ConcurrentHashMap<>();

    private final StormUtils plugin;

    public UserManagerImpl(StormUtils plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public Optional<OnlineUtilUser> getOnlineUser(UUID uuid) {
        return Optional.ofNullable(UuidToUserMap.get(uuid));
    }

    @Override
    public Optional<OnlineUtilUser> getOnlineUser(String username) {
        return Optional.ofNullable(UsernameToUserMap.get(username.toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public List<OnlineUtilUser> getOnlineUsers() {
        return List.copyOf(UuidToUserMap.values());
    }

    @Override
    public CompletableFuture<Optional<UtilUser>> getUser(UUID uuid) {
        UtilUser onlineUser = UuidToUserMap.get(uuid);

        if (onlineUser != null) {
            return CompletableFuture.completedFuture(Optional.of(onlineUser));
        }

        CompletableFuture<Optional<UtilUser>> futureUser = new CompletableFuture<>();

        plugin.getStorage().getUserData(uuid).thenAccept(optionalUserData -> {
            if (optionalUserData.isEmpty()) {
                futureUser.complete(Optional.empty());
                return;
            }

            UtilUser user = new UtilUserImpl(uuid);
            user.setUserData(optionalUserData.get());

            futureUser.complete(Optional.of(user));
        });

        return futureUser;
    }

    @Override
    public CompletableFuture<Optional<UtilUser>> getUser(String username) {
        UtilUser onlineUser = UsernameToUserMap.get(username.toLowerCase(Locale.ENGLISH));

        if (onlineUser != null) {
            return CompletableFuture.completedFuture(Optional.of(onlineUser));
        }

        CompletableFuture<Optional<UtilUser>> futureUser = new CompletableFuture<>();

        plugin.getStorage().getUserData(username).thenAccept(optionalUserData -> {
            if (optionalUserData.isEmpty()) {
                futureUser.complete(Optional.empty());
                return;
            }

            UserData userData = optionalUserData.get();

            UtilUser user = new UtilUserImpl(userData.getUuid());
            user.setUserData(userData);

            futureUser.complete(Optional.of(user));
        });

        return futureUser;
    }

    @Override
    public void close() {
        long lastLogoutTime = System.currentTimeMillis();

        UuidToUserMap.forEach((uuid, user) -> {
            UserData userData = user.getUserData();

            if (userData == null) {
                return;
            }

            userData.setLastLogoutTime(lastLogoutTime);

            plugin.getStorage()
                    .setUserData(uuid, userData)
                    .thenAccept(unused -> userData.setSaved());
        });

        UuidToUserMap.clear();
        UsernameToUserMap.clear();
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        OnlineUtilUser user = new OnlineUtilUserImpl(player);

        UUID uuid = user.getUuid();
        String username = user.getUsername().toLowerCase(Locale.ENGLISH);
        UuidToUserMap.put(uuid, user);
        UsernameToUserMap.put(username, user);

        CompletableFuture<Optional<UserData>> future = plugin.getStorage().getUserData(uuid);

        future.thenAccept(optionalUserData -> {

                boolean newUser = false;
                UserData data;
                if (optionalUserData.isPresent()) {
                    System.out.println("present");
                    data = optionalUserData.get();
                } else {
                    System.out.println("not present");
                    data = new UserDataImpl(uuid);
                    data.setFirstJoinTime(System.currentTimeMillis());
                    newUser = true;
                }
                data.setLastKnownUsername(player.getName());
                data.setLastJoinTime(System.currentTimeMillis());
                user.setUserData(data);

                System.out.println("before: " + newUser);
                if (newUser) {
                    System.out.println("before");

                    CompletableFuture<Void> saveFuture = plugin.getStorage().setUserData(uuid, data);

                    saveFuture.thenAccept(unused -> {
                        data.setSaved();
                        System.out.println("saved");
                    });
                }
        });

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        OnlineUtilUser user = UuidToUserMap.remove(player.getUniqueId());

        if (user == null || plugin.isDisabling()) {
            return;
        }

        UsernameToUserMap.remove(user.getUsername().toLowerCase(Locale.ENGLISH));

        UserData data = user.getUserData();

        if (data == null) {
            return;
        }

        data.setLastLogoutTime(System.currentTimeMillis());

        plugin.getStorage()
                .setUserData(user.getUuid(), data)
                .thenAccept(unused -> data.setSaved());
    }
}
