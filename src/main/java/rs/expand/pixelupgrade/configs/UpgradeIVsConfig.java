package rs.expand.pixelupgrade.configs;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import rs.expand.pixelupgrade.PixelUpgrade;

import static rs.expand.pixelupgrade.utilities.ConfigOperations.printMessages;

public class UpgradeIVsConfig
{
    private CommentedConfigurationNode config;
    private static UpgradeIVsConfig instance = new UpgradeIVsConfig();
    public static UpgradeIVsConfig getInstance()
    {   return instance;    }

    // Called during initial setup, either when the server is booting up or when /pureload has been executed.
    public String loadOrCreateConfig(Path checkPath, ConfigurationLoader<CommentedConfigurationNode> configLoader)
    {
        String fallbackAlias = "upgrade";

        if (Files.notExists(checkPath))
        {
            try
            {
                printMessages(1, "upgradeivs", "");
                Path targetLocation = Paths.get(PixelUpgrade.getInstance().path, "UpgradeIVs.conf");
                Files.copy(getClass().getResourceAsStream("/assets/UpgradeIVs.conf"), targetLocation);
                config = configLoader.load();
            }
            catch (IOException F)
            {
                printMessages(2, "UpgradeIVs", "");
                F.printStackTrace();
            }

            return fallbackAlias;
        }
        else try
        {
            config = configLoader.load();
            String alias = getConfig().getNode("commandAlias").getString();

            if (!Objects.equals(alias, null))
            {
                printMessages(3, "upgradeivs", alias);
                return alias;
            }
            else
            {
                printMessages(4, "UpgradeIVs", "");
                return fallbackAlias;
            }
        }
        catch (IOException F)
        {
            printMessages(5, "upgradeivs", "");
            F.printStackTrace();
            return fallbackAlias;
        }
    }

    public CommentedConfigurationNode getConfig()
    {   return config;  }
}
