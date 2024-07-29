package me.RareHyperIon.ChatGames.commands;

import me.RareHyperIon.ChatGames.ChatGames;
import me.RareHyperIon.ChatGames.utility.Utility;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class ChatGameCommand implements CommandExecutor, TabCompleter {

    private final ChatGames plugin;

    public ChatGameCommand(final ChatGames plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {

        if(cmd.getName().equalsIgnoreCase("ChatGames")) {
            if(args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
                sender.sendMessage(Utility.color("&cUsage: /cg reload"));
                return true;
            }

            this.plugin.reload();
            sender.sendMessage(Utility.color("&aSuccessfully reloaded."));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command cnd, final String label, final String[] args) {
        return List.of("reload");
    }

}
