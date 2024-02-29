package net.onestorm.plugins.stormutils.paper.username;

import net.onestorm.library.username.UsernameManager;
import net.onestorm.plugins.stormutils.api.user.UtilUser;
import net.onestorm.plugins.stormutils.paper.StormUtils;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UsernameManagerImpl implements UsernameManager {

    private final StormUtils plugin;

    public UsernameManagerImpl(StormUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Optional<String>> getUsername(UUID uuid) {
        return plugin.getUserManager()
                .getUser(uuid)
                .thenApply(optionalUser -> optionalUser.map(UtilUser::getLastKnownUsername));
    }

    @Override
    public CompletableFuture<Optional<UUID>> getUuid(String username) {
        return plugin.getUserManager()
                .getUser(username)
                .thenApply(optionalUser -> optionalUser.map(UtilUser::getUuid));
    }
}
