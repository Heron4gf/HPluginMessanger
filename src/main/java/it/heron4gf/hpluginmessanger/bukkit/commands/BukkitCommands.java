package it.heron4gf.hpluginmessanger.bukkit.commands;

import it.heron4gf.hpluginmessanger.commons.HPluginMessageAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class BukkitCommands implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!sender.hasPermission("hpluginmessanger.admin")) {
            sender.sendMessage(Component.text("You don't have the required permission").color(TextColor.fromHexString("#f7723d")));
            return false;
        }
        if(args.length < 1 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(Component.text("HPluginMessanger by Heron4gf").color(TextColor.fromHexString("#c5f73d")));
            sender.sendMessage(Component.text("* /hpm reload - reload serversockets list").color(TextColor.fromHexString("#f7cc3d")));
            sender.sendMessage(Component.text("* /hpm servers - shows serversockets list").color(TextColor.fromHexString("#f7cc3d")));
            return true;
        }
        if(args[0].equalsIgnoreCase("reload")) {
            HPluginMessageAPI.updateServers();
            sender.sendMessage(Component.text("Server sockets updated!").color(TextColor.fromHexString("#c5f73d")));
            return true;
        }
        if(args[0].equalsIgnoreCase("servers")) {
            for(String server : HPluginMessageAPI.getServerSocketsList().keySet()) {
                sender.sendMessage(Component.text("Server "+server+" port: "+HPluginMessageAPI.getServerSocketsList().get(server)).color(TextColor.fromHexString("#c5f73d")));
            }
            return true;
        }
        sender.sendMessage(Component.text("Unknown argument").color(TextColor.fromHexString("#f7723d")));
        return false;
    }
}
