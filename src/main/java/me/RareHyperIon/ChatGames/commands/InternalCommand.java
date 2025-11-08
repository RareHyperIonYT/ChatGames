package me.RareHyperIon.ChatGames.commands;

import me.RareHyperIon.ChatGames.ChatGames;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class InternalCommand implements CommandExecutor {

    private final ChatGames plugin;

    public InternalCommand(final ChatGames plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command,
                             @NotNull final String label, @NotNull final String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (this.plugin.getGameHandler().getGame() != null) {
            // If arguments are provided, validate the answer
            if (args.length > 0) {
                final String clickedText = String.join(" ", args);
                this.plugin.getGameHandler().attemptWin(player, clickedText);
            } else {
                // No validation needed (old behavior for backwards compatibility)
                this.plugin.getGameHandler().win(player);
            }
        }

        return true;
    }

}
