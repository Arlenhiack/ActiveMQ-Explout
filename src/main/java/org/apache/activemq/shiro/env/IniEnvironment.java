package org.apache.activemq.shiro.env;
public class IniEnvironment extends Throwable{
    private String message;

    public IniEnvironment(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
