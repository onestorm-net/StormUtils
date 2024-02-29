package net.onestorm.plugins.stormutils.api.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserManager {

    Optional<OnlineUtilUser> getOnlineUser(UUID uuid);

    Optional<OnlineUtilUser> getOnlineUser(String username);

    List<OnlineUtilUser> getOnlineUsers();

    CompletableFuture<Optional<UtilUser>> getUser(UUID uuid);

    CompletableFuture<Optional<UtilUser>> getUser(String username);

    void close();

}
