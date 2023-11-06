package it.heron4gf.hpluginmessanger.commons;

import it.heron4gf.hpluginmessanger.commons.socket.SimpleSocket;

import java.util.Map;

public interface HPlugin {

    String getServername();
    SimpleSocket getSocket();
    Map<String,Integer> getServers();
    void updateServers();

    default SimpleSocket assignSocket(int start, int end) {
        boolean error = true;
        for(int port = start; port < end && error; port++) {
            try {
                return new SimpleSocket(port);
            } catch(Exception e) {
                error = true;
            }
        }
        return null;
    }

}
