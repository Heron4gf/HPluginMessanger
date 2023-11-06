package it.heron4gf.hpluginmessanger.commons.socket;

import lombok.Getter;
import lombok.Setter;

public abstract class GeneralListener implements ReceiveListener {

    @Setter @Getter
    protected String channel;

    public void receivedMessage(String message) {
        if(!message.startsWith(channel)) return;
        receivedChannelMessage(message.replace(channel+":",""));
    }

    public abstract void receivedChannelMessage(String message);

}
