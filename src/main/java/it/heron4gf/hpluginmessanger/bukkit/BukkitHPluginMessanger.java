package it.heron4gf.hpluginmessanger.bukkit;

import it.heron4gf.hpluginmessanger.bukkit.commands.BukkitCommands;
import it.heron4gf.hpluginmessanger.commons.HPlugin;
import it.heron4gf.hpluginmessanger.commons.HPluginMessageAPI;
import it.heron4gf.hpluginmessanger.commons.socket.GeneralListener;
import it.heron4gf.hpluginmessanger.commons.socket.SimpleSocket;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public final class BukkitHPluginMessanger extends JavaPlugin implements HPlugin {

    @Getter
    private Map<String,Integer> servers = new HashMap<>();

    @Getter
    private SimpleSocket socket;

    @Getter
    private static BukkitHPluginMessanger instance;

    @Getter
    private String servername;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        HPluginMessageAPI.setPlugin(this);

        servername = getConfig().getString("servername","spawn");

        int start = getConfig().getInt("socket_pool.assign.start",4000);
        int end = getConfig().getInt("socket_pool.assign.end",5000);

        socket = assignSocket(start,end);
        Bukkit.getLogger().info("Datagram socket created on port: "+socket.getPort());

        socket.registerNewListener(new SocketUpdateListener());
        updateServers();

        getCommand("hpluginmessanger").setExecutor(new BukkitCommands());
        getCommand("hpm").setExecutor(new BukkitCommands());
        Bukkit.getLogger().info("Registered command hpluginmessanger [hpm]");
    }

    public void updateServers() {
        servers.clear();
        int start = getConfig().getInt("socket_pool.search.start",4000);
        int end = getConfig().getInt("socket_pool.search.end",5000);
        for(int port = start; port < end; port++) {
            if(port != socket.getPort()) {
                try {
                    socket.sendMessage("HSOCKETUPDATE:"+servername+":"+socket.getPort(), InetAddress.getLocalHost(), port);
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void onDisable() {
        socket.close();
    }

    private class SocketUpdateListener extends GeneralListener {

        public SocketUpdateListener() {
            channel = "HSOCKETUPDATE";
        }

        @Override
        public void receivedChannelMessage(String message) {
            String[] args = message.split(":");
            servers.put(args[0],Integer.parseInt(args[1]));
            if(args.length > 2 && args[2].equalsIgnoreCase("NOREPLY")) return;
            socket.sendMessage("HSOCKETUPDATE:"+servername+":"+socket.getPort()+":NOREPLY",args[0]);
        }
    }

}
