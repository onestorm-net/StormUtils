package net.onestorm.plugins.stormutils.paper.user;

import net.onestorm.plugins.stormutils.api.storage.UserData;
import net.onestorm.plugins.stormutils.api.user.UtilUser;

import java.util.UUID;

public class UtilUserImpl implements UtilUser {

    private final UUID uuid;
    private UserData userData;

    public UtilUserImpl(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public String getLastKnownUsername() {
        return userData.getLastKnownUsername();
    }

    @Override
    public void setLastKnownUsername(String username) {
        userData.setLastKnownUsername(username);
    }

    @Override
    public long getFirstJoinTime() {
        return userData.getFirstJoinTime();
    }

    @Override
    public void setFirstJoinTime(long time) {
        userData.setFirstJoinTime(time);
    }

    @Override
    public long getLastJoinTime() {
        return userData.getLastJoinTime();
    }

    @Override
    public void setLastJoinTime(long time) {
        userData.setLastJoinTime(time);
    }

    @Override
    public long getLastLogoutTime() {
        return userData.getLastLogoutTime();
    }

    @Override
    public void setLastLogoutTime(long time) {
        userData.setLastLogoutTime(time);
    }

    @Override
    public UserData getUserData() {
        return userData;
    }

    @Override
    public void setUserData(UserData userData) {
        this.userData = userData;
    }
}
