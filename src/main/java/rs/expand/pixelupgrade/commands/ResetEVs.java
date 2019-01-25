// Forget berries!
package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVsStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.pixelupgrade.utilities.PrintingMethods;
import static rs.expand.pixelupgrade.PixelUpgrade.*;

// TODO: Update the economy setup to be in line with most other economy-using commands.
public class ResetEVs implements CommandExecutor
{
    // Declare some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;
    public static Integer commandCost;

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (final int debugNum, final String inputString)
    { PrintingMethods.printDebugMessage("ResetEVs", debugNum, inputString); }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        if (src instanceof Player)
        {
            // Validate the data we get from the command's main config.
            final List<String> nativeErrorArray = new ArrayList<>();
            if (commandAlias == null)
                nativeErrorArray.add("commandAlias");
            if (commandCost == null)
                nativeErrorArray.add("commandCost");

            if (!nativeErrorArray.isEmpty())
            {
                PrintingMethods.printCommandNodeError("ResetEVs", nativeErrorArray);
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
            }
            else if (BattleRegistry.getBattle((EntityPlayerMP) src) != null)
            {
                printToLog(0, "Called by player §4" + src.getName() + "§c, but in a battle. Exit.");
                src.sendMessage(Text.of("§4Error: §cYou can't use this command while in a battle!"));
            }
            else
            {
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

                final Player player = (Player) src;
                boolean canContinue = true, commandConfirmed = false;
                int slot = 0;

                if (!args.<String>getOne("slot").isPresent())
                {
                    printToLog(1, "No arguments provided. Exit.");

                    src.sendMessage(Text.of("§5-----------------------------------------------------"));
                    src.sendMessage(Text.of("§4Error: §cNo arguments found. Please provide a slot."));

                    printSyntaxHelper(src);
                    PrintingMethods.checkAndAddFooter(true, commandCost, src);

                    canContinue = false;
                }
                else
                {
                    final String slotString = args.<String>getOne("slot").get();

                    if (slotString.matches("^[1-6]"))
                    {
                        printToLog(2, "Slot was a valid slot number. Let's move on!");
                        slot = Integer.parseInt(args.<String>getOne("slot").get());
                    }
                    else
                    {
                        printToLog(1, "Invalid slot provided. Exit.");

                        src.sendMessage(Text.of("§5-----------------------------------------------------"));
                        src.sendMessage(Text.of("§4Error: §cInvalid slot value. Valid values are 1-6."));

                        printSyntaxHelper(src);
                        PrintingMethods.checkAndAddFooter(true, commandCost, src);

                        canContinue = false;
                    }
                }

                if (args.hasAny("c"))
                    commandConfirmed = true;

                if (canContinue)
                {
                    // Get the player's party, and then get the Pokémon in the targeted slot.
                    final Pokemon pokemon = Pixelmon.storageManager.getParty((EntityPlayerMP) src).get(slot - 1);

                    if (pokemon == null)
                    {
                        printToLog(1, "No Pokémon data found in slot, probably empty. Exit.");
                        src.sendMessage(Text.of("§4Error: §cYou don't have anything in that slot!"));
                    }
                    else if (pokemon.isEgg())
                    {
                        printToLog(1, "Tried to reset EVs on an egg. Exit.");
                        src.sendMessage(Text.of("§4Error: §cThat's an egg! Go hatch it, first."));
                    }
                    else if (commandConfirmed)
                    {
                        printToLog(2, "Command was confirmed, checking balances.");

                        if (economyEnabled && commandCost > 0)
                        {
                            final BigDecimal costToConfirm = new BigDecimal(commandCost);
                            final Optional<UniqueAccount> optionalAccount = economyService.getOrCreateAccount(player.getUniqueId());

                            if (optionalAccount.isPresent())
                            {
                                final UniqueAccount uniqueAccount = optionalAccount.get();
                                final TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(),
                                            costToConfirm, Sponge.getCauseStackManager().getCurrentCause());

                                if (transactionResult.getResult() == ResultType.SUCCESS)
                                {
                                    resetPlayerEVs(pokemon, src);
                                    printToLog(1, "Reset EVs for slot §3" + slot +
                                            "§b, taking §3" + costToConfirm + "§b coins.");
                                }
                                else
                                {
                                    final BigDecimal balanceNeeded = uniqueAccount.getBalance(economyService.getDefaultCurrency()).subtract(costToConfirm).abs();
                                    printToLog(1, "Not enough coins! Cost is §3" + costToConfirm +
                                            "§b, and we're lacking §3" + balanceNeeded);

                                    src.sendMessage(Text.of("§4Error: §cYou need §4" + balanceNeeded + "§c more coins to do this."));
                                }
                            }
                            else
                            {
                                printToLog(0, "§4" + src.getName() + "§c does not have an economy account, aborting. Bug?");
                                src.sendMessage(Text.of("§4Error: §cNo economy account found. Please contact staff!"));
                            }
                        }
                        else
                        {
                            if (economyEnabled)
                            {
                                printToLog(1, "Resetting EVs for slot §3" + slot +
                                        "§b. Config price is §30§b, taking nothing.");
                            }
                            else
                            {
                                printToLog(1, "Resetting EVs for slot §3" + slot +
                                        "§b. No economy, so we skipped eco checks.");
                            }

                            resetPlayerEVs(pokemon, src);
                        }
                    }
                    else
                    {
                        printToLog(1, "No confirmation provided, printing warning and aborting.");

                        src.sendMessage(Text.of("§5-----------------------------------------------------"));
                        src.sendMessage(Text.of("§6Warning: §eYou are about to reset this Pokémon's EVs to zero!"));
                        src.sendMessage(Text.EMPTY);

                        if (economyEnabled && commandCost > 0)
                            src.sendMessage(Text.of("§eResetting will cost §6" + commandCost + "§e coins!"));

                        src.sendMessage(Text.of("§2Ready? Type: §a/" + commandAlias + " " + slot + " -c"));
                        src.sendMessage(Text.of("§5-----------------------------------------------------"));
                    }
                }
            }
        }
        else
            printToLog(0,"This command cannot run from the console or command blocks.");

        return CommandResult.success();
	}

    // Called when it's necessary to figure out the right perm message, or when it's just convenient. Saves typing!
    private void printSyntaxHelper(final CommandSource src)
    {
        src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <slot, 1-6> {-c to confirm}"));
    }

	private void resetPlayerEVs(final Pokemon pokemon, final CommandSource src)
    {
        final EVsStore EVs = pokemon.getEVs();

        printToLog(1, "Old EVS -- §3" + EVs.hp + "§b, §3" + EVs.attack + "§b, §3" + EVs.defence +
                "§b, §3" + EVs.specialAttack + "§b, §3" + EVs.specialDefence + "§b, §3" + EVs.speed);

        EVs.set(StatsType.HP, 0);
        EVs.set(StatsType.Attack, 0);
        EVs.set(StatsType.Defence, 0);
        EVs.set(StatsType.SpecialAttack, 0);
        EVs.set(StatsType.SpecialDefence, 0);
        EVs.set(StatsType.Speed, 0);

        src.sendMessage(Text.of(
                "§aYour §2" + pokemon.getSpecies().getLocalizedName() + "§a had its EVs wiped!"));
    }
}