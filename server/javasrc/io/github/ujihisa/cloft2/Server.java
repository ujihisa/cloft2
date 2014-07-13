package io.github.ujihisa.cloft2;
import org.bukkit.plugin.java.JavaPlugin;
import clojure.lang.RT;

public final class Server extends JavaPlugin {
  ClassLoader previous = Thread.currentThread().getContextClassLoader();

  @Override
  public void onEnable() {
    try {
      Thread.currentThread().setContextClassLoader(Server.class.getClassLoader());
      eval("(use '[clojure.tools.nrepl.server :only (start-server stop-server)])" +
          "(defonce server (start-server :port 7888))" +
          "(prn 'nrepl-server server)");
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1); // TODO
    } finally {
      Thread.currentThread().setContextClassLoader(previous);
    }
  }

  @Override
  public void onDisable() {
    try {
      Thread.currentThread().setContextClassLoader(Server.class.getClassLoader());
      eval("(stop-server server)");
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1); // TODO
    } finally {
      Thread.currentThread().setContextClassLoader(previous);
    }
  }

  private void eval(String cljcode) throws Exception {
    RT.var("clojure.core", "eval").invoke(
        RT.var("clojure.core","read-string").invoke("(do " + cljcode + ")"));
  }
}