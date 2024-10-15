package com.example.log4jtest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Log4jtestApplication {

  public static void main(String[] args) throws IOException {
    SpringApplication.run(Log4jtestApplication.class, args);


  }

  @GetMapping("/test")
  public byte[] getCodeBase() throws IOException {
    return Files.readAllBytes(ResourceUtils.getFile("classpath:static/HelloWorldExploit.class").toPath());
  }
  @GetMapping("/ldap")
  public String handleLdapRequest(@RequestParam String name) {
    try {
      // Эмуляция контекста LDAP
      Hashtable<String, String> env = new Hashtable<>();
      env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
      env.put(Context.PROVIDER_URL, "ldap://localhost:9090");

      Context ctx = new InitialContext(env);

      // Вызов lookup для вредоносного объекта
      Object obj = ctx.lookup(name);

      return "JNDI lookup result: " + obj.toString();
    } catch (NamingException e) {
      return "Error during JNDI lookup: " + e.getMessage();
    }
  }
}
