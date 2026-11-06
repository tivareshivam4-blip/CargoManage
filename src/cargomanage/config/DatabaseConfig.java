package cargomanage.config;

import java.io.*;
import java.util.Properties;

public class DatabaseConfig {
    private static final String CONFIG_FILE = "db.properties";
    private static Properties props = new Properties();

    static {
        load();
    }

    public static synchronized void load() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("Warning: Could not read " + CONFIG_FILE + ", using defaults: " + e.getMessage());
            }
        } else {
            setDefaults();
            save();
        }
    }

    private static void setDefaults() {
        props.setProperty("db.host", "localhost");
        props.setProperty("db.port", "3306");
        props.setProperty("db.name", "cargomanage");
        props.setProperty("db.user", "root");
        props.setProperty("db.password", "");
        props.setProperty("db.useSSL", "false");
        props.setProperty("db.allowPublicKeyRetrieval", "true");
        props.setProperty("db.serverTimezone", "UTC");
    }

    public static synchronized void save() {
        try (OutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "CargoManage Database Configuration");
        } catch (IOException e) {
            System.err.println("Warning: Could not save " + CONFIG_FILE + ": " + e.getMessage());
        }
    }

    public static String getHost() { return props.getProperty("db.host", "localhost"); }
    public static void setHost(String val) { props.setProperty("db.host", val); }

    public static String getPort() { return props.getProperty("db.port", "3306"); }
    public static void setPort(String val) { props.setProperty("db.port", val); }

    public static String getDbName() { return props.getProperty("db.name", "cargomanage"); }
    public static void setDbName(String val) { props.setProperty("db.name", val); }

    public static String getUser() { return props.getProperty("db.user", "root"); }
    public static void setUser(String val) { props.setProperty("db.user", val); }

    public static String getPassword() { return props.getProperty("db.password", ""); }
    public static void setPassword(String val) { props.setProperty("db.password", val); }

    public static String getServerUrl() {
        return "jdbc:mysql://" + getHost() + ":" + getPort() + "/?useSSL=" +
                props.getProperty("db.useSSL", "false") +
                "&allowPublicKeyRetrieval=" + props.getProperty("db.allowPublicKeyRetrieval", "true") +
                "&serverTimezone=" + props.getProperty("db.serverTimezone", "UTC") +
                "&characterEncoding=UTF-8";
    }

    public static String getDatabaseUrl() {
        return "jdbc:mysql://" + getHost() + ":" + getPort() + "/" + getDbName() + "?useSSL=" +
                props.getProperty("db.useSSL", "false") +
                "&allowPublicKeyRetrieval=" + props.getProperty("db.allowPublicKeyRetrieval", "true") +
                "&serverTimezone=" + props.getProperty("db.serverTimezone", "UTC") +
                "&characterEncoding=UTF-8";
    }
}

