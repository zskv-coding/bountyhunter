package org.zskv.bountyhunter;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BountyCommand implements CommandExecutor {

    private final Bountyhunter plugin;

    public BountyCommand(Bountyhunter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                int bounty = plugin.getBounty(player.getUniqueId());
                player.sendMessage("§bYour bounty: §e" + bounty);
            } else {
                sender.sendMessage("Only players can run this command.");
            }
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found.");
            return true;
        }

        int bounty = plugin.getBounty(target.getUniqueId());
        sender.sendMessage("§b" + target.getName() + "'s bounty: §e" + bounty);
        return true;
    }
}
