package backend;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Credentials {
    private String token;
    private String username;
    private String link;

    public Credentials() {
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            prop.load(fis);
            token = prop.getProperty("GH_TOKEN");
            username = prop.getProperty("USERNAME");
            link = prop.getProperty("REPO_LINK");
        } catch (IOException e) {
            e.printStackTrace();
            // Optionally, handle missing file or values
        }
    }

    public String getToken() { return token; }
    public String getLink() { return link; }
    public String getUsername() { return username; }
}
