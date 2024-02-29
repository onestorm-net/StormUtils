package net.onestorm.plugins.stormutils.api.storage;

import java.util.UUID;

public interface UserData {

    UUID getUuid();

    String getLastKnownUsername();

    void setLastKnownUsername(String username);

    long getFirstJoinTime();

    void setFirstJoinTime(long time);

    long getLastJoinTime();

    void setLastJoinTime(long time);

    long getLastLogoutTime();

    void setLastLogoutTime(long time);

    void setSaved();

    boolean needsSaving();

}
