package net.onestorm.plugins.stormutils.api.user;

import net.onestorm.library.user.User;
import net.onestorm.plugins.stormutils.api.storage.UserData;


public interface UtilUser extends User {

    String getLastKnownUsername();

    void setLastKnownUsername(String username);

    long getFirstJoinTime();

    void setFirstJoinTime(long time);

    long getLastJoinTime();

    void setLastJoinTime(long time);

    long getLastLogoutTime();

    void setLastLogoutTime(long time);

    UserData getUserData();

    void setUserData(UserData userData);



}
