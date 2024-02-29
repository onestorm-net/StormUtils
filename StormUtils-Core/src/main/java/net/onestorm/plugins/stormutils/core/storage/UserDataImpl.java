package net.onestorm.plugins.stormutils.core.storage;

import net.onestorm.plugins.stormutils.api.storage.UserData;

import java.util.UUID;

public class UserDataImpl implements UserData {

    private final UUID uuid;
    private boolean needsSaving = false;

    private String lastKnownUsername;
    private long firstJoinTime;
    private long lastJoinTime;
    private long lastLogoutTime;

    public UserDataImpl(UUID uuid) {
        this.uuid = uuid;
        this.lastKnownUsername = null;
        this.firstJoinTime = 0L; // 1970-01-01 00:00:01, as 1970-01-01 00:00:00, isn't allowed for some reason?
        this.lastJoinTime = 0L;
        this.lastLogoutTime = 0L;
    }

    public UserDataImpl(UUID uuid, String lastKnownUsername, long firstJoinTime, long lastJoinTime, long lastLogoutTime) {
        this.uuid = uuid;
        this.lastKnownUsername = lastKnownUsername;
        this.firstJoinTime = firstJoinTime;
        this.lastJoinTime = lastJoinTime;
        this.lastLogoutTime = lastLogoutTime;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public String getLastKnownUsername() {
        return lastKnownUsername;
    }

    @Override
    public void setLastKnownUsername(String username) {
        if (lastKnownUsername != null && lastKnownUsername.equals(username)) {
            return;
        }
        lastKnownUsername = username;
        needsSaving = true;
    }

    @Override
    public long getFirstJoinTime() {
        return firstJoinTime;
    }

    @Override
    public void setFirstJoinTime(long time) {
        if (firstJoinTime == time) {
            return;
        }
        firstJoinTime = time;
        needsSaving = true;
    }

    @Override
    public long getLastJoinTime() {
        return lastJoinTime;
    }

    @Override
    public void setLastJoinTime(long time) {
        if (lastJoinTime == time) {
            return;
        }
        lastJoinTime = time;
        needsSaving = true;
    }

    @Override
    public long getLastLogoutTime() {
        return lastLogoutTime;
    }

    @Override
    public void setLastLogoutTime(long time) {
        if (lastLogoutTime == time) {
            return;
        }
        lastLogoutTime = time;
        needsSaving = true;
    }

    @Override
    public void setSaved() {
        needsSaving = false;
    }

    @Override
    public boolean needsSaving() {
        return needsSaving;
    }
}
