package net.onestorm.plugins.stormutils.api.storage;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Storage {

    CompletableFuture<Optional<UserData>> getUserData(UUID uuid);

    CompletableFuture<Optional<UserData>> getUserData(String username);

    CompletableFuture<Void> setUserData(UUID uuid, UserData data);

    void close();

}
