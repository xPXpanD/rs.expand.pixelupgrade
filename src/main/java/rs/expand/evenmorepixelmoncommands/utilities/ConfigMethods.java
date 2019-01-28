// The enormous and rather complicated config handler, version two. Could use some work still, but it'll do for now.
// Version one was just n config classes, where n was the number of commands there were minus main. We've come a long way.
package rs.expand.evenmorepixelmoncommands.utilities;

// Remote imports.
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import java.util.List;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import static org.apache.commons.lang3.BooleanUtils.toBooleanObject;

// Local imports.
import rs.expand.evenmorepixelmoncommands.EMPC;
import rs.expand.evenmorepixelmoncommands.commands.*;
import static rs.expand.evenmorepixelmoncommands.EMPC.*;
import static rs.expand.evenmorepixelmoncommands.utilities.PrintingMethods.printUnformattedMessage;

// Note: printUnformattedMessage is a static import for a method from PrintingMethods, for convenience.
// Also, EMPC class variables are loaded in the same way. Used in loadConfig and registerCommands.
public class ConfigMethods
{
    // If we find a config that's broken during reloads, we set this flag and print an error.
    private static boolean gotConfigError;

    // Make a little converter for safely handling Strings that might have an integer value inside.
    private static Integer interpretInteger(final String input)
    {
        if (input != null && input.matches("-?[1-9]\\d*|0"))
            return Integer.parseInt(input);
        else
            return null;
    }

    /*// Do the same for doubles.
    private static Double interpretDouble(final String input)
    {
        if (input != null)
        {
            final Scanner readDouble = new Scanner(input);
            if (readDouble.hasNextDouble())
                return readDouble.nextDouble();
        }

        // Was the input null, or could we not find a double? Return null and let our commands show an error.
        return null;
    }*/

    // Registers all known EMPC commands and their aliases.
    public static boolean registerCommands()
    {
        // Get the container for our current plugin, I think? Return "null" if it can't be retrieved.
        final PluginContainer empcContainer =
                Sponge.getPluginManager().getPlugin("evenmorepixelmoncommands").orElse(null);

        // Contains base commands and common (?) mistakes, as well as interchangeable alternatives.
        if (empcContainer != null)
        {
            // Release all old mappings. This lets us dynamically load/unload aliases for commands without a restart!
            final Game game = Sponge.getGame();
            game.getCommandManager().getOwnedBy(empcContainer).forEach(game.getCommandManager()::removeMapping);

            // Register the main command.
            if (EMPC.commandAlias != null && !EMPC.commandAlias.matches("evenmorepixelmoncommands|empc"))
                Sponge.getCommandManager().register(empcContainer, basecommand, "evenmorepixelmoncommands", "empc", EMPC.commandAlias);
            else
                Sponge.getCommandManager().register(empcContainer, basecommand, "evenmorepixelmoncommands", "empc");

            // Register all other commands.
            if (CheckStats.commandAlias != null && !CheckStats.commandAlias.matches("checkstats|getstats|checkegg"))
                Sponge.getCommandManager().register(empcContainer, checkstats, "checkstats", "getstats", "checkegg", CheckStats.commandAlias);
            else
                Sponge.getCommandManager().register(empcContainer, checkstats, "checkstats", "getstats", "checkegg");

            if (CheckTypes.commandAlias != null && !CheckTypes.commandAlias.matches("checktypes|checktype"))
                Sponge.getCommandManager().register(empcContainer, checktypes, "checktypes", "checktype", CheckTypes.commandAlias);
            else
                Sponge.getCommandManager().register(empcContainer, checktypes, "checktypes", "checktype");

            /*if (DittoFusion.commandAlias != null && !DittoFusion.commandAlias.equals("dittofusion"))
                Sponge.getCommandManager().register(empcContainer, dittofusion, "dittofusion", DittoFusion.commandAlias);
            else
                Sponge.getCommandManager().register(empcContainer, dittofusion, "dittofusion");*/

            if (FixGenders.commandAlias != null && !FixGenders.commandAlias.matches("fixgenders|fixgender"))
                Sponge.getCommandManager().register(empcContainer, fixgenders, "fixgenders", "fixgender", FixGenders.commandAlias);
            else
                Sponge.getCommandManager().register(empcContainer, fixgenders, "fixgenders", "fixgender");

            if (ForceHatch.commandAlias != null && !ForceHatch.commandAlias.equals("forcehatch"))
                Sponge.getCommandManager().register(empcContainer, forcehatch, "forcehatch", ForceHatch.commandAlias);
            else
                Sponge.getCommandManager().register(empcContainer, forcehatch, "forcehatch");

            if (ForceStats.commandAlias != null && !ForceStats.commandAlias.matches("forcestats|forcestat"))
                Sponge.getCommandManager().register(empcContainer, forcestats, "forcestats", "forcestat", ForceStats.commandAlias);
            else
                Sponge.getCommandManager().register(empcContainer, forcestats, "forcestats", "forcestat");

            /*if (ResetCount.commandAlias != null && !ResetCount.commandAlias.matches("resetcount|resetcounts"))
                Sponge.getCommandManager().register(empcContainer, resetcount, "resetcount", "resetcounts", ResetCount.commandAlias);
            else
                Sponge.getCommandManager().register(empcContainer, resetcount, "resetcount", "resetcounts");*/

            if (ResetEVs.commandAlias != null && !ResetEVs.commandAlias.matches("resetevs|resetev"))
                Sponge.getCommandManager().register(empcContainer, resetevs, "resetevs", "resetev", ResetEVs.commandAlias);
            else
                Sponge.getCommandManager().register(empcContainer, resetevs, "resetevs", "resetev");

            if (ShowStats.commandAlias != null && !ShowStats.commandAlias.matches("showstats|showstat"))
                Sponge.getCommandManager().register(empcContainer, showstats, "showstats", "showstat", ShowStats.commandAlias);
            else
                Sponge.getCommandManager().register(empcContainer, showstats, "showstats", "showstat");

            if (SpawnDex.commandAlias != null && !SpawnDex.commandAlias.equals("spawndex"))
                Sponge.getCommandManager().register(empcContainer, spawndex, "spawndex", SpawnDex.commandAlias);
            else
                Sponge.getCommandManager().register(empcContainer, spawndex, "spawndex");

            if (SwitchGender.commandAlias != null && !SwitchGender.commandAlias.matches("switchgender|switchgenders"))
                Sponge.getCommandManager().register(empcContainer, switchgender, "switchgender", "switchgenders", SwitchGender.commandAlias);
            else
                Sponge.getCommandManager().register(empcContainer, switchgender, "switchgender", "switchgenders");

            if (TimedHatch.commandAlias != null && !TimedHatch.commandAlias.matches("timedhatch|timerhatch"))
                Sponge.getCommandManager().register(empcContainer, timedhatch, "timedhatch", "timerhatch", TimedHatch.commandAlias);
            else
                Sponge.getCommandManager().register(empcContainer, timedhatch, "timedhatch", "timerhatch");

            if (TimedHeal.commandAlias != null && !TimedHeal.commandAlias.matches("timedheal|timerheal"))
                Sponge.getCommandManager().register(empcContainer, timedheal, "timedheal", "timerheal", TimedHeal.commandAlias);
            else
                Sponge.getCommandManager().register(empcContainer, timedheal, "timedheal", "timerheal");

            /*if (UpgradeIVs.commandAlias != null && !UpgradeIVs.commandAlias.matches("upgradeivs|upgradeiv"))
                Sponge.getCommandManager().register(empcContainer, upgradeivs, "upgradeivs", "upgradeiv", UpgradeIVs.commandAlias);
            else
                Sponge.getCommandManager().register(empcContainer, upgradeivs, "upgradeivs", "upgradeiv");*/

            return true;
        }
        else
        {
            printUnformattedMessage("    §cCommand (re-)initialization failed. Please report this, this is a bug.");
            printUnformattedMessage("    §cSidemod commands are likely dead. A reboot or reload may work.");

            return false;
        }
    }

    // Called during initial load, and when a command is reloaded. Load configs, and print a pretty list.
    public static void printCommandsAndAliases()
    {
        // Do some initial setup for our formatted messages later on. We'll show three commands per line.
        final List<String> commandList = new ArrayList<>();
        final StringBuilder formattedCommand = new StringBuilder();
        final StringBuilder printableList = new StringBuilder();
        String commandAlias = "§4There's an error message missing, please report this!", commandString = null;

        // Format our commands and aliases and add them to the lists that we'll print in a bit.
        // TODO: If you add/remove a command, update this list and the numEntries counter!
        final int numEntries = 12;
        for (int i = 1; i <= numEntries; i++)
        {
            switch (i)
            {
                // Normal commands. If the alias is null (error returned), we pass the base command again instead.
                // This prevents NPEs while also letting us hide commands by checking whether they've returned null.
                case 1:
                {
                    commandAlias = CheckStats.commandAlias;
                    commandString = "/checkstats";
                    break;
                }
                case 2:
                {
                    commandAlias = CheckTypes.commandAlias;
                    commandString = "/checktypes";
                    break;
                }
                /*case 4:
                {
                    commandAlias = DittoFusion.commandAlias;
                    commandString = "/dittofusion";
                    break;
                }*/
                case 3:
                {
                    commandAlias = EMPC.commandAlias;
                    commandString = "/empc";
                    break;
                }
                case 4:
                {
                    commandAlias = FixGenders.commandAlias;
                    commandString = "/fixgenders";
                    break;
                }
                case 5:
                {
                    commandAlias = ForceHatch.commandAlias;
                    commandString = "/forcehatch";
                    break;
                }
                case 6:
                {
                    commandAlias = ForceStats.commandAlias;
                    commandString = "/forcestats";
                    break;
                }
                /*case 10:
                {
                    commandAlias = ResetCount.commandAlias;
                    commandString = "/resetcount";
                    break;
                }*/
                case 7:
                {
                    commandAlias = ResetEVs.commandAlias;
                    commandString = "/resetevs";
                    break;
                }
                case 8:
                {
                    commandAlias = ShowStats.commandAlias;
                    commandString = "/showstats";
                    break;
                }
                case 9:
                {
                    commandAlias = SpawnDex.commandAlias;
                    commandString = "/spawndex";
                    break;
                }
                case 10:
                {
                    commandAlias = SwitchGender.commandAlias;
                    commandString = "/switchgender";
                    break;
                }
                case 11:
                {
                    commandAlias = TimedHatch.commandAlias;
                    commandString = "/timedhatch";
                    break;
                }
                case 12:
                {
                    commandAlias = TimedHeal.commandAlias;
                    commandString = "/timedheal";
                    break;
                }
                /*case 17:
                {
                    commandAlias = UpgradeIVs.commandAlias;
                    commandString = "/upgradeivs";
                    break;
                }*/
            }

            if (commandAlias != null)
            {
                // Format the command's shown text.
                formattedCommand.append("§2").append(commandString);

                if (commandString.equals("/" + commandAlias))
                    formattedCommand.append("§a§f, ");
                else
                {
                    formattedCommand.append("§a (§2/");
                    formattedCommand.append(commandAlias.toLowerCase());
                    formattedCommand.append("§a)§f, ");
                }
            }
            else
            {
                // Alias loading went very very wrong, do some special red error formatting.
                formattedCommand.append("§4").append(commandString);
                formattedCommand.append("§c (§4");
                formattedCommand.append("ERROR!");
                formattedCommand.append("§c)§f, ");

                gotConfigError = true;
            }

            // If we're at the last command, shank the trailing formatting code, comma and space and for a clean end.
            if (i == numEntries)
                formattedCommand.setLength(formattedCommand.length() - 4);

            // Add the formatted command to the list, and then clear the StringBuilder so we can re-use it.
            commandList.add(formattedCommand.toString());
            formattedCommand.setLength(0);
        }

        // If we got a config error, warn here.
        if (gotConfigError)
            printUnformattedMessage("--> §eIssues found. Check for stray/missing characters, or recreate configs.");

        // Print the formatted commands + aliases.
        printUnformattedMessage("--> §aLoaded a bunch of commands, see below.");

        for (int q = 1; q < numEntries + 1; q++)
        {
            printableList.append(commandList.get(q - 1));

            if (q == numEntries) // Are we on the last entry of the list? Print and exit.
                printUnformattedMessage("    " + printableList);
            else if (q % 3 == 0) // Can the loop number be divided by 3? If so, we have three commands stocked up. Print!
            {
                printUnformattedMessage("    " + printableList);
                printableList.setLength(0); // Wipe the list so we can re-use it for the next three commands.
            }
        }
    }

    // Creates a config of the passed type if none exists. Does nothing otherwise.
    private static void checkOrCreateConfig(final String callSource, final Path checkPath)
    {
        if (Files.notExists(checkPath))
        {
            if (callSource.equals("EMPC"))
            {
                try
                {
                    // Create a new config since the file wasn't found. Add spaces to match startup/reload message spacing.
                    printUnformattedMessage("    §eNo primary configuration file found, creating...");

                    Files.copy(ConfigMethods.class.getResourceAsStream("/assets/EvenMorePixelmonCommands.conf"),
                            Paths.get(EMPC.primaryPath, "EvenMorePixelmonCommands.conf"));
                }
                catch (final IOException F)
                {
                    printUnformattedMessage("§cPrimary config setup has failed! Please report this.");
                    printUnformattedMessage("§cAdd any useful info you may have (operating system?). Stack trace:");

                    F.printStackTrace();
                }
            }
            else
            {
                try
                {
                    // Spaces added so it falls in line with startup/reload message spacing.
                    printUnformattedMessage("    §eNo §6/" + callSource.toLowerCase() +
                            "§e configuration file found, creating...");

                    Files.copy(ConfigMethods.class.getResourceAsStream("/assets/" + callSource + ".conf"),
                            Paths.get(EMPC.commandConfigPath, callSource + ".conf"));
                }
                catch (final IOException F)
                {
                    printUnformattedMessage("§cConfig setup for command \"§4/" + callSource.toLowerCase()
                            + "§c\" failed! Please report this.");
                    printUnformattedMessage("§cAdd any useful info you may have (operating system?). Stack trace:");
                    F.printStackTrace();
                }
            }
        }
    }

    // Called during initial setup, either when the server is booting up or when /empc reload has been executed.
    public static boolean tryCreateAndLoadConfigs()
    {
        // Print a message to squeeze between the messages of whatever called the (re-)load.
        printUnformattedMessage("--> §aLoading and validating EMPC settings...");

        // Create a config directory if it doesn't exist. Silently swallow an error if it does. I/O is awkward.
        try
        {
            Files.createDirectory(Paths.get(EMPC.commandConfigPath));
            printUnformattedMessage("--> §aEMPC folder not found, making a new one for command configs...");
        }
        catch (final IOException ignored) {}

        // Set up a String, and fill it with the command we're currently loading. Pass to error logic if something dies.
        String currentCommand = null;

        // Start creating/loading configs and committing config variables to memory.
        try
        {
            // Main config.
            currentCommand = "EMPC";
            checkOrCreateConfig(currentCommand, primaryConfigPath);
            final CommentedConfigurationNode mainConfig = EMPC.primaryConfigLoader.load();

            EMPC.commandAlias =
                    mainConfig.getNode("commandAlias").getString();
            EMPC.configVersion =
                    interpretInteger(mainConfig.getNode("configVersion").getString());
            EMPC.logImportantInfo =
                    toBooleanObject(mainConfig.getNode("logImportantInfo").getString());
            EMPC.numLinesPerPage =
                    interpretInteger(mainConfig.getNode("numLinesPerPage").getString());
            EMPC.shortenedHP =
                    mainConfig.getNode("shortenedHealth").getString();
            EMPC.shortenedAttack =
                    mainConfig.getNode("shortenedAttack").getString();
            EMPC.shortenedDefense =
                    mainConfig.getNode("shortenedDefense").getString();
            EMPC.shortenedSpecialAttack =
                    mainConfig.getNode("shortenedSpecialAttack").getString();
            EMPC.shortenedSpecialDefense =
                    mainConfig.getNode("shortenedSpecialDefense").getString();
            EMPC.shortenedSpeed =
                    mainConfig.getNode("shortenedSpeed").getString();

            // /checkstats
            currentCommand = "CheckStats";
            checkOrCreateConfig(currentCommand, checkStatsPath);
            final CommentedConfigurationNode checkStatsConfig = EMPC.checkStatsLoader.load();

            CheckStats.commandAlias =
                    checkStatsConfig.getNode("commandAlias").getString();
            CheckStats.showTeamWhenSlotEmpty =
                    toBooleanObject(checkStatsConfig.getNode("showTeamWhenSlotEmpty").getString());
            CheckStats.showEVs =
                    toBooleanObject(checkStatsConfig.getNode("showEVs").getString());
            CheckStats.allowCheckingEggs =
                    toBooleanObject(checkStatsConfig.getNode("allowCheckingEggs").getString());
            CheckStats.revealEggStats =
                    toBooleanObject(checkStatsConfig.getNode("revealEggStats").getString());
            CheckStats.babyHintPercentage =
                    interpretInteger(checkStatsConfig.getNode("babyHintPercentage").getString());
            CheckStats.commandCost =
                    interpretInteger(checkStatsConfig.getNode("commandCost").getString());
            CheckStats.recheckIsFree =
                    toBooleanObject(checkStatsConfig.getNode("recheckIsFree").getString());

            // /checktypes
            currentCommand = "CheckTypes";
            checkOrCreateConfig(currentCommand, checkTypesPath);
            final CommentedConfigurationNode checkTypesConfig = EMPC.checkTypesLoader.load();

            CheckTypes.commandAlias =
                    checkTypesConfig.getNode("commandAlias").getString();
            CheckTypes.showFormMessage =
                    toBooleanObject(checkTypesConfig.getNode("showFormMessage").getString());
            CheckTypes.showAlolanMessage =
                    toBooleanObject(checkTypesConfig.getNode("showAlolanMessage").getString());

            /*case "DittoFusion":
            {
                checkOrCreateConfig("DittoFusion", dittoFusionPath);
                final CommentedConfigurationNode commandConfig = PixelUpgrade.dittoFusionLoader.load();

                DittoFusion.commandAlias =
                        commandConfig.getNode("commandAlias").getString();
                DittoFusion.stat0to5 =
                        interpretInteger(commandConfig.getNode("stat0to5").getString());
                DittoFusion.stat6to10 =
                        interpretInteger(commandConfig.getNode("stat6to10").getString());
                DittoFusion.stat11to15 =
                        interpretInteger(commandConfig.getNode("stat11to15").getString());
                DittoFusion.stat16to20 =
                        interpretInteger(commandConfig.getNode("stat16to20").getString());
                DittoFusion.stat21to25 =
                        interpretInteger(commandConfig.getNode("stat21to25").getString());
                DittoFusion.stat26to30 =
                        interpretInteger(commandConfig.getNode("stat26to30").getString());
                DittoFusion.stat31plus =
                        interpretInteger(commandConfig.getNode("stat31plus").getString());
                DittoFusion.regularCap =
                        interpretInteger(commandConfig.getNode("regularCap").getString());
                DittoFusion.shinyCap =
                        interpretInteger(commandConfig.getNode("shinyCap").getString());
                DittoFusion.passOnShinyStatus =
                        toBooleanObject(commandConfig.getNode("passOnShinyStatus").getString());
                DittoFusion.pointMultiplierForCost =
                        interpretInteger(commandConfig.getNode("pointMultiplierForCost").getString());
                DittoFusion.previouslyUpgradedMultiplier =
                        interpretInteger(commandConfig.getNode("previouslyUpgradedMultiplier").getString());
                DittoFusion.addFlatFee =
                        interpretInteger(commandConfig.getNode("addFlatFee").getString());

                return DittoFusion.commandAlias;
            }*/

            // /fixgenders
            currentCommand = "FixGenders";
            checkOrCreateConfig(currentCommand, fixGendersPath);
            final CommentedConfigurationNode fixGendersConfig = EMPC.fixGendersLoader.load();

            FixGenders.commandAlias =
                    fixGendersConfig.getNode("commandAlias").getString();
            FixGenders.sneakyMode =
                    toBooleanObject(fixGendersConfig.getNode("sneakyMode").getString());
            FixGenders.requireConfirmation =
                    toBooleanObject(fixGendersConfig.getNode("requireConfirmation").getString());

            // /forcehatch
            currentCommand = "ForceHatch";
            checkOrCreateConfig(currentCommand, forceHatchPath);
            ForceHatch.commandAlias =
                    EMPC.forceHatchLoader.load().getNode("commandAlias").getString();

            // /forcestats
            currentCommand = "ForceStats";
            checkOrCreateConfig(currentCommand, forceStatsPath);
            ForceStats.commandAlias =
                    EMPC.forceStatsLoader.load().getNode("commandAlias").getString();

            /*case "ResetCount":
            {
                checkOrCreateConfig("ResetCount", resetCountPath);
                final CommentedConfigurationNode commandConfig = PixelUpgrade.resetCountLoader.load();

                ResetCount.commandAlias =
                        commandConfig.getNode("commandAlias").getString();

                return ResetCount.commandAlias;
            }*/

            // /resetevs
            currentCommand = "ResetEVs";
            checkOrCreateConfig(currentCommand, resetEVsPath);
            final CommentedConfigurationNode resetEVsConfig = EMPC.resetEVsLoader.load();

            ResetEVs.commandAlias =
                    resetEVsConfig.getNode("commandAlias").getString();
            ResetEVs.commandCost =
                    interpretInteger(resetEVsConfig.getNode("commandCost").getString());

            // /showstats
            currentCommand = "ShowStats";
            checkOrCreateConfig(currentCommand, showStatsPath);
            final CommentedConfigurationNode showStatsConfig = EMPC.showStatsLoader.load();

            ShowStats.commandAlias =
                    showStatsConfig.getNode("commandAlias").getString();
            ShowStats.cooldownInSeconds =
                    interpretInteger(showStatsConfig.getNode("cooldownInSeconds").getString());
            ShowStats.altCooldownInSeconds =
                    interpretInteger(showStatsConfig.getNode("altCooldownInSeconds").getString());
            ShowStats.showNicknames =
                    toBooleanObject(showStatsConfig.getNode("showNicknames").getString());
            ShowStats.showEVs =
                    toBooleanObject(showStatsConfig.getNode("showEVs").getString());
            ShowStats.showExtraInfo =
                    toBooleanObject(showStatsConfig.getNode("showExtraInfo").getString());
            ShowStats.showCounts =
                    toBooleanObject(showStatsConfig.getNode("showCounts").getString());
            ShowStats.clampBadNicknames =
                    toBooleanObject(showStatsConfig.getNode("clampBadNicknames").getString());
            ShowStats.notifyBadNicknames =
                    toBooleanObject(showStatsConfig.getNode("notifyBadNicknames").getString());
            ShowStats.commandCost =
                    interpretInteger(showStatsConfig.getNode("commandCost").getString());

            // /spawndex
            currentCommand = "SpawnDex";
            checkOrCreateConfig(currentCommand, spawnDexPath);
            final CommentedConfigurationNode spawnDexConfig = EMPC.spawnDexLoader.load();

            SpawnDex.commandAlias =
                    spawnDexConfig.getNode("commandAlias").getString();
            SpawnDex.fakeMessage =
                    spawnDexConfig.getNode("fakeMessage").getString();

            // /switchgender
            currentCommand = "SwitchGender";
            checkOrCreateConfig(currentCommand, switchGenderPath);
            final CommentedConfigurationNode switchGenderConfig = EMPC.switchGenderLoader.load();

            SwitchGender.commandAlias =
                    switchGenderConfig.getNode("commandAlias").getString();
            SwitchGender.commandCost =
                    interpretInteger(switchGenderConfig.getNode("commandCost").getString());

            // /timedhatch
            currentCommand = "TimedHatch";
            checkOrCreateConfig(currentCommand, timedHatchPath);
            final CommentedConfigurationNode timedHatchConfig = EMPC.timedHatchLoader.load();

            TimedHatch.commandAlias =
                    timedHatchConfig.getNode("commandAlias").getString();
            TimedHatch.cooldownInSeconds =
                    interpretInteger(timedHatchConfig.getNode("cooldownInSeconds").getString());
            TimedHatch.altCooldownInSeconds =
                    interpretInteger(timedHatchConfig.getNode("altCooldownInSeconds").getString());
            TimedHatch.hatchParty  =
                    toBooleanObject(timedHatchConfig.getNode("hatchParty").getString());
            TimedHatch.sneakyMode  =
                    toBooleanObject(timedHatchConfig.getNode("sneakyMode").getString());
            TimedHatch.commandCost =
                    interpretInteger(timedHatchConfig.getNode("commandCost").getString());

            // /timedheal
            currentCommand = "TimedHeal";
            checkOrCreateConfig(currentCommand, timedHealPath);
            final CommentedConfigurationNode timedHealConfig = EMPC.timedHealLoader.load();

            TimedHeal.commandAlias =
                    timedHealConfig.getNode("commandAlias").getString();
            TimedHeal.cooldownInSeconds =
                    interpretInteger(timedHealConfig.getNode("cooldownInSeconds").getString());
            TimedHeal.altCooldownInSeconds =
                    interpretInteger(timedHealConfig.getNode("altCooldownInSeconds").getString());
            TimedHeal.healParty  =
                    toBooleanObject(timedHealConfig.getNode("healParty").getString());
            TimedHeal.sneakyMode  =
                    toBooleanObject(timedHealConfig.getNode("sneakyMode").getString());
            TimedHeal.commandCost =
                    interpretInteger(timedHealConfig.getNode("commandCost").getString());

            /*case "UpgradeIVs":
            {
                checkOrCreateConfig("UpgradeIVs", upgradeIVsPath);
                final CommentedConfigurationNode commandConfig = PixelUpgrade.upgradeIVsLoader.load();

                UpgradeIVs.commandAlias =
                        commandConfig.getNode("commandAlias").getString();
                UpgradeIVs.legendaryAndShinyCap =
                        interpretInteger(commandConfig.getNode("legendaryAndShinyCap").getString());
                UpgradeIVs.legendaryCap =
                        interpretInteger(commandConfig.getNode("legendaryCap").getString());
                UpgradeIVs.shinyCap =
                        interpretInteger(commandConfig.getNode("shinyCap").getString());
                UpgradeIVs.regularCap =
                        interpretInteger(commandConfig.getNode("regularCap").getString());
                UpgradeIVs.mathMultiplier =
                        interpretDouble(commandConfig.getNode("mathMultiplier").getString());
                UpgradeIVs.fixedUpgradeCost =
                        interpretInteger(commandConfig.getNode("fixedUpgradeCost").getString());
                UpgradeIVs.legendaryAndShinyMult =
                        interpretDouble(commandConfig.getNode("legendaryAndShinyMult").getString());
                UpgradeIVs.legendaryMult =
                        interpretDouble(commandConfig.getNode("legendaryMult").getString());
                UpgradeIVs.shinyMult =
                        interpretDouble(commandConfig.getNode("shinyMult").getString());
                UpgradeIVs.regularMult =
                        interpretDouble(commandConfig.getNode("regularMult").getString());
                UpgradeIVs.upgradesFreeBelow =
                        interpretInteger(commandConfig.getNode("upgradesFreeBelow").getString());
                UpgradeIVs.addFlatFee =
                        interpretInteger(commandConfig.getNode("addFlatFee").getString());

                return UpgradeIVs.commandAlias;
            }*/

            return true;
        }
        catch (final Exception F)
        {
            // Spaces added so it falls in line with startup/reload message spacing.
            printUnformattedMessage("    §cCould not read config for §4/" + currentCommand.toLowerCase() + "§c.");
            printUnformattedMessage("    §cPlease check your config for any missing or invalid entries.");

            gotConfigError = true;
            return false;
        }
    }
}
