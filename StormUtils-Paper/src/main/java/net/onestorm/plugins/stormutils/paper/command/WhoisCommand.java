package net.onestorm.plugins.stormutils.paper.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.onestorm.plugins.stormutils.api.user.UtilUser;
import net.onestorm.plugins.stormutils.paper.StormUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

public class WhoisCommand extends BukkitCommand {

    private static final String COMMAND_NAME = "whois";
    private static final String COMMAND_PERMISSION = "stormutils.admin";

    private final StormUtils plugin;

    public WhoisCommand(StormUtils plugin) {
        super(COMMAND_NAME);
        this.plugin = plugin;
        setPermission(COMMAND_PERMISSION);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] arguments) {

        if (arguments.length != 1 ) {
            sender.sendMessage(Component.text("[!] Invalid command usage: /info <username>", NamedTextColor.RED));
            return true;
        }

        plugin.getUserManager().getUser(arguments[0]).thenAccept(optionalUser -> {
            if (optionalUser.isEmpty()) {
                sender.sendMessage(Component.text("[!] Could not find user.", NamedTextColor.RED));
                return;
            }

            UtilUser user = optionalUser.get();

            try {
                plugin.getLogger().info("TEST: " + user.getLastKnownUsername() + " " + user.getFirstJoinTime() + " " + user.getLastJoinTime() + " " + user.getLastLogoutTime());

                sender.sendMessage(Component.text("User Info:", NamedTextColor.DARK_AQUA).decoration(TextDecoration.BOLD, true));
                sender.sendMessage(Component.text(" Last known username: ", NamedTextColor.AQUA)
                        .append(Component.text(user.getLastKnownUsername(), NamedTextColor.WHITE)));
                sender.sendMessage(Component.text(" First join time: ", NamedTextColor.AQUA)
                        .append(Component.text(format(user.getFirstJoinTime()), NamedTextColor.WHITE)));
                sender.sendMessage(Component.text(" Last join time: ", NamedTextColor.AQUA)
                        .append(Component.text(format(user.getLastJoinTime()), NamedTextColor.WHITE)));
                sender.sendMessage(Component.text(" Last logout time: ", NamedTextColor.AQUA)
                        .append(Component.text(format(user.getLastLogoutTime()), NamedTextColor.WHITE)));
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Uncaught exception in WhoisCommand#execute(...)", e);
            }


        });

        return true;
    }

    private String format(long time) {

        Instant instant = Instant.ofEpochMilli(time);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z").withZone(ZoneId.systemDefault());

        return formatter.format(instant);
    }
}
