package it.heron4gf.hpluginmessanger.velocity;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import it.heron4gf.hpluginmessanger.commons.HPlugin;
import it.heron4gf.hpluginmessanger.commons.HPluginMessageAPI;
import it.heron4gf.hpluginmessanger.commons.socket.GeneralListener;
import it.heron4gf.hpluginmessanger.commons.socket.SimpleSocket;
import it.heron4gf.hpluginmessanger.velocity.commands.VelocityCommands;
import lombok.Getter;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Plugin(id = "hpluginmessanger", name = "HPluginMessanger", version = "1.0",
        description = "Communicate easily between servers or plugins using Datagram Sockets", authors = {"Heron4gf"})
public class VelocityHPluginMessanger implements HPlugin {

    @Getter
    private ProxyServer proxyServer;

    @Getter
    private Logger logger;

    @Getter
    private final File dataDirectory;

    @Getter
    private static VelocityHPluginMessanger instance;

    @Getter
    private String servername;

    private static File CONFIG_FILE;

    @Getter
    private Map<String,Integer> servers = new HashMap<>();

    @Getter
    private Toml config;

    @Getter
    private SimpleSocket socket;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        HPluginMessageAPI.setPlugin(this);
        instance = this;
        saveDefaultConfig();
        config = loadConfig();

        servername = config.getString("servername","velocity");

        int start = config.getLong("socket_pool.assing.start",4000L).intValue();
        int end = config.getLong("socket_pool.assing.end",5000L).intValue();

        socket = assignSocket(start,end);
        logger.info("Datagram socket created on port: "+socket.getPort());

        socket.registerNewListener(new SocketUpdateListener());
        socket.registerNewListener(new ServerOnlinePlayers());
        if(config.getBoolean("enable_server_connect_listener")) {
            socket.registerNewListener(new ServerConnectListener());
        }
        updateServers();

        socket.registerNewListener(new OnlinePlayersMessanger());

        new VelocityCommands("vhpluginmessanger","vpluginmessanger","vhpm");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        socket.close();
    }

    private Toml loadConfig() {
        return new Toml().read(CONFIG_FILE);
    }

    public void saveDefaultConfig() {
        if (!dataDirectory.exists()) dataDirectory.mkdir();
        if (CONFIG_FILE.exists()) return;

        try (InputStream in = VelocityHPluginMessanger.class.getResourceAsStream("/config.toml")) {
            Files.copy(in, CONFIG_FILE.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Inject
    public VelocityHPluginMessanger(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory.toFile();
        CONFIG_FILE = new File(this.dataDirectory,"config.toml");
    }

    @Override
    public void updateServers() {
        servers.clear();
        int start = getConfig().getLong("socket_pool.search.start",4000L).intValue();
        int end = getConfig().getLong("socket_pool.search.end",5000L).intValue();
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

    private class ServerConnectListener extends GeneralListener {

        public ServerConnectListener() {
            channel = "SERVER_CONNECT";
        }

        @Override
        public void receivedChannelMessage(String message) {
            String[] args = message.split(":");
            String name = args[2];
            if(proxyServer.getServer(name).isPresent()) {
                RegisteredServer server = proxyServer.getServer(name).get();

                String playername = args[1];
                try {
                    Player player = proxyServer.getPlayer(playername).get();
                    player.createConnectionRequest(server).connect();
                } catch(Exception e) {
                    socket.sendMessage("SERVER_CONNECT:unable to connect "+playername+" to "+name+":NOREPLY",args[0]);
                }
            } else {
                socket.sendMessage("SERVER_CONNECT:server "+name+" not found:NOREPLY",args[0]);
            }
        }
    }

    private class OnlinePlayersMessanger extends GeneralListener {

        private JoinLeaveListener listener = new JoinLeaveListener();
        private boolean registered;

        public OnlinePlayersMessanger() {
            super();
            this.channel = "ONLINEPLAYER";
            proxyServer.getEventManager().register(VelocityHPluginMessanger.getInstance(),listener);
            registered = true;
        }
        @Override
        public void receivedChannelMessage(String message) {

            switch (message) {
                case "startup":
                    for(Player player : proxyServer.getAllPlayers()) {
                        HPluginMessageAPI.sendMessage("ONLINEPLAYER:ADD:"+player.getUniqueId()+":"+player.getUsername(),"survival");
                    }
                    if(registered) return;
                    proxyServer.getEventManager().register(VelocityHPluginMessanger.getInstance(),listener);
                    registered = true;
                    break;

                case "disable":
                    if(!registered) return;
                    proxyServer.getEventManager().unregisterListener(VelocityHPluginMessanger.getInstance(),listener);
                    registered = false;
                    break;
            }

        }
    }

    private class JoinLeaveListener {

        @Subscribe
        private void onJoin(ServerConnectedEvent event) {
            Player player = event.getPlayer();
            HPluginMessageAPI.sendMessage("ONLINEPLAYER:ADD:"+player.getUniqueId()+":"+player.getUsername(),"survival");
        }


        @Subscribe
        private void onLeave(DisconnectEvent event) {
            Player player = event.getPlayer();
            HPluginMessageAPI.sendMessage("ONLINEPLAYER:REMOVE:"+player.getUniqueId(),"survival");
        }
    }

    private class ServerOnlinePlayers extends GeneralListener {

        public ServerOnlinePlayers() {
            channel = "ONLINEPLAYERS";
        }

        @Override
        public void receivedChannelMessage(String message) {

            String online = "";
            for(Player player : proxyServer.getAllPlayers()) {
                online = online+player.getUsername()+":";
            }
            HPluginMessageAPI.sendMessage(online,message);

        }
    }

}
