package it.heron4gf.hpluginmessanger.commons;

import it.heron4gf.hpluginmessanger.commons.socket.GeneralListener;
import it.heron4gf.hpluginmessanger.commons.socket.ReceiveListener;
import lombok.Setter;

import java.util.Map;

public class HPluginMessageAPI {

    @Setter
    private static HPlugin plugin;
    /**

     Sends a message to a specified server.
     @param message the message to send (if using channels the format is CHANNEL:MESSAGE)
     @param server the server to send the message to (caps sensible)
     */
    public static void sendMessage(String message, String server) {
        plugin.getSocket().sendMessage(message, server);
    }
    /**

     Updates the servers for the BukkitHPluginMessenger instance.
     This method should be called whenever the server list needs to be updated.
     */
    public static void updateServers() {
        plugin.updateServers();
    }
    /**

     Registers a new ReceiveListener to receive messages from the server socket.
     @param listener the ReceiveListener to register
     */
    public static void registerNewListener(ReceiveListener listener) {
        plugin.getSocket().registerNewListener(listener);
    }
    /**

     Registers a new GeneralListener to receive channel-specific messages from the server socket.
     @param listener the GeneralListener to register
     @param channel the channel to listen on
     */
    public static void registerNewChanneledListener(GeneralListener listener, String channel) {
        listener.setChannel(channel);
        plugin.getSocket().registerNewListener(listener);
    }
    /**

     Returns a map of server sockets list.
     @return the map of server sockets list
     */
    public static Map<String, Integer> getServerSocketsList() {
        return plugin.getServers();
    }
    /**

     Unregisters a ReceiveListener from the server socket.
     @param listener the ReceiveListener to unregister
     */
    public static void unregisterListener(ReceiveListener listener) {
        plugin.getSocket().unregisterListener(listener);
    }
}
