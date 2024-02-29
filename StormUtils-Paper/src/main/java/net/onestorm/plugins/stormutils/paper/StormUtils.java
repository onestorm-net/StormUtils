package net.onestorm.plugins.stormutils.paper;

import net.onestorm.library.action.ActionManager;
import net.onestorm.library.action.ActionManagerImpl;
import net.onestorm.library.action.implementation.MessageActionBuilder;
import net.onestorm.library.configuration.Configuration;
import net.onestorm.library.configuration.file.FileConfiguration;
import net.onestorm.library.configuration.file.json.JsonConfiguration;
import net.onestorm.library.paper.action.KillPlayerActionBuilder;
import net.onestorm.library.paper.action.PlaySoundActionBuilder;
import net.onestorm.library.requirement.RequirementManager;
import net.onestorm.library.requirement.RequirementManagerImpl;
import net.onestorm.library.requirement.implementation.PermissionRequirementBuilder;
import net.onestorm.library.username.UsernameManager;
import net.onestorm.plugins.stormutils.api.UtilPlugin;
import net.onestorm.plugins.stormutils.api.storage.Storage;
import net.onestorm.plugins.stormutils.api.user.UserManager;
import net.onestorm.plugins.stormutils.core.storage.MySqlStorage;
import net.onestorm.plugins.stormutils.paper.command.WhoisCommand;
import net.onestorm.plugins.stormutils.paper.user.UserManagerImpl;
import net.onestorm.plugins.stormutils.paper.username.UsernameManagerImpl;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;

public class StormUtils extends JavaPlugin implements UtilPlugin {

    private File configurationFile = new File(getDataFolder(), "configuration.json");
    private FileConfiguration configuration = new JsonConfiguration();
    private Storage storage;
    private UserManager userManager;
    private UsernameManager usernameManager;
    private ActionManager actionManager;
    private RequirementManager requirementManager;

    private boolean isDisabling = false;

    @Override
    public void onEnable() {
        isDisabling = false; // just to be sure

        reload();

        userManager = new UserManagerImpl(this);

        usernameManager = new UsernameManagerImpl(this);

        actionManager = new ActionManagerImpl(getLogger());
        actionManager.registerBuilder(new KillPlayerActionBuilder());
        actionManager.registerBuilder(new PlaySoundActionBuilder(getServer()));
        actionManager.registerBuilder(new MessageActionBuilder());

        requirementManager = new RequirementManagerImpl(getLogger());
        requirementManager.registerBuilder(new PermissionRequirementBuilder());

        getServer().getCommandMap().register("whois", getName().toLowerCase(Locale.ENGLISH), new WhoisCommand(this));
    }

    @Override
    public void onDisable() {
        isDisabling = true;
        userManager.close();
        storage.close();
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public Storage getStorage() {
        return storage;
    }

    @Override
    public UserManager getUserManager() {
        return userManager;
    }

    @Override
    public UsernameManager getUsernameManager() {
        return usernameManager;
    }

    @Override
    public ActionManager getActionManager() {
        return actionManager;
    }

    @Override
    public RequirementManager getRequirementManager() {
        return requirementManager;
    }

    @Override
    public boolean isDisabling() {
        return isDisabling;
    }

    public void reload() {
        if (!configurationFile.exists()) {
            saveResource("configuration.json", false);
        }

        try {
            configuration.load(configurationFile);
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Could not load configuration file", e);
            return;
        }

        if (storage != null) {
            storage.close();
        }
        storage = new MySqlStorage(this);
    }
}
