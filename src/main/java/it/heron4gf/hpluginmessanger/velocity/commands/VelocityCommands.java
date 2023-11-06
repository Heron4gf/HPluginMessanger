package it.heron4gf.hpluginmessanger.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import it.heron4gf.hpluginmessanger.commons.HPluginMessageAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class VelocityCommands extends GeneralCommand {
    public VelocityCommands(String command, String... aliases) {
        super(command, aliases);
    }

    @Override
    public void handleCommand(CommandSource sender, String[] args) {
        if(!sender.hasPermission("hpluginmessanger.admin")) {
            sender.sendMessage(Component.text("You don't have the required permission").color(TextColor.fromHexString("#f7723d")));
            return;
        }
        if(args.length < 1 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(Component.text("HPluginMessanger by Heron4gf").color(TextColor.fromHexString("#c5f73d")));
            sender.sendMessage(Component.text("* /vhpm reload - reload serversockets list").color(TextColor.fromHexString("#f7cc3d")));
            sender.sendMessage(Component.text("* /vhpm servers - shows serversockets list").color(TextColor.fromHexString("#f7cc3d")));
            return;
        }
        if(args[0].equalsIgnoreCase("reload")) {
            HPluginMessageAPI.updateServers();
            sender.sendMessage(Component.text("Server sockets updated!").color(TextColor.fromHexString("#c5f73d")));
            return;
        }
        if(args[0].equalsIgnoreCase("servers")) {
            for(String server : HPluginMessageAPI.getServerSocketsList().keySet()) {
                sender.sendMessage(Component.text("Server "+server+" port: "+HPluginMessageAPI.getServerSocketsList().get(server)).color(TextColor.fromHexString("#c5f73d")));
            }
            return;
        }
        sender.sendMessage(Component.text("Unknown argument").color(TextColor.fromHexString("#f7723d")));
    }
}
