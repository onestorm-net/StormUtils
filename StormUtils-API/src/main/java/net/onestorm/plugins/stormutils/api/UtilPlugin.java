package net.onestorm.plugins.stormutils.api;

import net.onestorm.library.action.ActionManager;
import net.onestorm.library.configuration.Configuration;
import net.onestorm.library.requirement.RequirementManager;
import net.onestorm.library.username.UsernameManager;
import net.onestorm.plugins.stormutils.api.storage.Storage;
import net.onestorm.plugins.stormutils.api.user.UserManager;

public interface UtilPlugin {

    Configuration getConfiguration();

    Storage getStorage();

    UserManager getUserManager();

    UsernameManager getUsernameManager();

    ActionManager getActionManager();

    RequirementManager getRequirementManager();

    boolean isDisabling();

}
