package org.example;

import org.apache.activemq.command.ControlCommand;
import org.apache.activemq.transport.TransportListener;

import java.io.IOException;

public class MyTransportListener implements TransportListener {

    @Override
    public void onCommand(Object command) {

        if (command instanceof ControlCommand){
            ControlCommand res = (ControlCommand)command;
            System.out.println("[+] cmd result："+res.getCommand());
        }
    }

    @Override
    public void onException(IOException error) {

    }

    @Override
    public void transportInterupted() {

    }

    @Override
    public void transportResumed() {

    }
}
