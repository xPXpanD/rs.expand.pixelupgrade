// It's like a berry, but... not a berry!
package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
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
import rs.expand.pixelupgrade.utilities.CommonMethods;
import static rs.expand.pixelupgrade.PixelUpgrade.*;

public class FixEVs implements CommandExecutor
{
    // Initialize some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;
    public static Integer commandCost;

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (int debugNum, String inputString)
    { CommonMethods.printDebugMessage("FixEVs", debugNum, inputString); }

	@SuppressWarnings("NullableProblems")
    public CommandResult execute(CommandSource src, CommandContext args)
	{
	    if (src instanceof Player)
        {
            // Validate the data we get from the command's main config.
            ArrayList<String> nativeErrorArray = new ArrayList<>();
            if (commandAlias == null)
                nativeErrorArray.add("commandAlias");
            if (commandCost == null)
                nativeErrorArray.add("commandCost");

            if (!nativeErrorArray.isEmpty())
            {
                CommonMethods.printCommandNodeError("FixEVs", nativeErrorArray);
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
            }
            else if (useBritishSpelling == null)
            {
                printToLog(0, "Could not read remote node \"§4useBritishSpelling§c\".");
                printToLog(0, "The main config contains invalid variables. Exiting.");
                src.sendMessage(Text.of("§4Error: §cCould not parse main config. Please report to staff."));
            }
            else
            {
                printToLog(1, "Called by player §3" + src.getName() + "§b. Starting!");

                Player player = (Player) src;
                boolean canContinue = false, commandConfirmed = false;
                int slot = 0;

                if (!args.<String>getOne("slot").isPresent())
                {
                    printToLog(1, "No arguments provided. Exit.");

                    if (commandCost > 0)
                        src.sendMessage(Text.of("§5-----------------------------------------------------"));

                    src.sendMessage(Text.of("§4Error: §cNo parameters found. Please provide a slot."));
                    printSyntaxHelper(src);
                    CommonMethods.checkAndAddFooter(commandCost, src);
                }
                else
                {
                    String slotString = args.<String>getOne("slot").get();

                    if (slotString.matches("^[1-6]"))
                    {
                        printToLog(2, "Slot was a valid slot number. Let's move on!");
                        slot = Integer.parseInt(args.<String>getOne("slot").get());
                        canContinue = true;
                    }
                    else
                    {
                        printToLog(1, "Invalid slot provided. Exit.");

                        if (commandCost > 0)
                            src.sendMessage(Text.of("§5-----------------------------------------------------"));

                        src.sendMessage(Text.of("§4Error: §cInvalid slot value. Valid values are 1-6."));
                        printSyntaxHelper(src);
                        CommonMethods.checkAndAddFooter(commandCost, src);
                    }
                }

                if (args.hasAny("c"))
                    commandConfirmed = true;

                if (canContinue)
                {
                    printToLog(2, "No errors encountered, input should be valid. Continuing!");
                    Optional<?> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) src));

                    if (!storage.isPresent())
                    {
                        printToLog(0, "§4" + src.getName() + "§c does not have a Pixelmon storage, aborting. Bug?");
                        src.sendMessage(Text.of("§4Error: §cNo Pixelmon storage found. Please contact staff!"));
                    }
                    else
                    {
                        PlayerStorage storageCompleted = (PlayerStorage) storage.get();
                        NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                        if (nbt == null)
                        {
                            printToLog(1, "No NBT data found in slot, probably empty. Exit.");
                            src.sendMessage(Text.of("§4Error: §cYou don't have anything in that slot!"));
                        }
                        else if (nbt.getBoolean("isEgg"))
                        {
                            printToLog(1, "Tried to fix EVs on an egg. Exit.");
                            src.sendMessage(Text.of("§4Error: §cThat's an egg! Go hatch it, first."));
                        }
                        else
                        {
                            EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());
                            int HPEV = pokemon.stats.EVs.HP;
                            int attackEV = pokemon.stats.EVs.Attack;
                            int defenceEV = pokemon.stats.EVs.Defence;
                            int spAttackEV = pokemon.stats.EVs.SpecialAttack;
                            int spDefenceEV = pokemon.stats.EVs.SpecialDefence;
                            int speedEV = pokemon.stats.EVs.Speed;
                            int totalEVs = HPEV + attackEV + defenceEV + spAttackEV + spDefenceEV + speedEV;
                            boolean allEVsGood = false;

                            if (HPEV < 253 && attackEV < 253 && defenceEV < 253 && spAttackEV < 253 && spDefenceEV < 253 && speedEV < 253)
                                allEVsGood = true;

                            if (HPEV == 0 && attackEV == 0 && defenceEV == 0 && spAttackEV == 0 && spDefenceEV == 0 && speedEV == 0)
                            {
                                printToLog(1, "All EVs were at zero, no upgrades needed to be done. Exit.");
                                src.sendMessage(Text.of("§dNo EVs were found. Go faint some wild Pokémon!"));
                            }
                            else if (HPEV > 255 || attackEV > 255 || defenceEV > 255 || spAttackEV > 255 || spDefenceEV > 255 || speedEV > 255)
                            {
                                printToLog(1, "Found one or more EVs above 255. Probably set by staff, so exit.");
                                src.sendMessage(Text.of("§4Error: §cOne or more EVs are above the limit. Contact staff."));
                            }
                            else if (HPEV < 0 || attackEV < 0 || defenceEV < 0 || spAttackEV < 0 || spDefenceEV < 0 || speedEV < 0)
                            {
                                printToLog(1, "Found one or more negative EVs. Let's let staff handle this -- exit.");
                                src.sendMessage(Text.of("§4Error: §cOne or more EVs are negative. Please contact staff."));
                            }
                            else if (allEVsGood)
                            {
                                if (totalEVs < 510)
                                {
                                    printToLog(1, "No wasted stats were detected. Exit.");
                                    src.sendMessage(Text.of("§dNo issues found! Your Pokémon is coming along nicely."));
                                }
                                else if (totalEVs > 510)
                                {
                                    printToLog(1, "Found an EV total above the limit. Exit.");
                                    src.sendMessage(Text.of("§dPokémon is above the limits! Contact staff if this is unintended."));
                                }
                                else // EV total is exactly 510.
                                {
                                    printToLog(1, "EV total of 510 hit, but no overleveled EVs found. Exit.");
                                    src.sendMessage(Text.of("§dNo issues found! Not happy? Get some EV-reducing berries!"));
                                }
                            }
                            else if (commandCost > 0)
                            {
                                BigDecimal costToConfirm = new BigDecimal(commandCost);

                                if (commandConfirmed)
                                {
                                    Optional<UniqueAccount> optionalAccount = economyService.getOrCreateAccount(player.getUniqueId());

                                    if (optionalAccount.isPresent())
                                    {
                                        UniqueAccount uniqueAccount = optionalAccount.get();
                                        TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(),
                                                costToConfirm, Sponge.getCauseStackManager().getCurrentCause());

                                        if (transactionResult.getResult() == ResultType.SUCCESS)
                                        {
                                            printToLog(1, "Fixed EVs for slot §3" + slot +
                                                    "§b, and took §3" + costToConfirm + "§b coins.");
                                            fixPlayerEVs(nbt, src, HPEV, attackEV, defenceEV, spAttackEV, spDefenceEV, speedEV);
                                        }
                                        else
                                        {
                                            BigDecimal balanceNeeded = uniqueAccount.getBalance(economyService.getDefaultCurrency()).subtract(costToConfirm).abs();
                                            printToLog(1, "Not enough coins! Cost is §3" + costToConfirm +
                                                    "§b, and we're lacking §3" + balanceNeeded);

                                            src.sendMessage(Text.of("§4Error: §cYou need §4" + balanceNeeded +
                                                    "§c more coins to do this."));
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
                                    printToLog(1, "Got cost but no confirmation; end of the line.");

                                    // Is cost to confirm exactly one coin?
                                    if (costToConfirm.compareTo(BigDecimal.ONE) == 0)
                                        src.sendMessage(Text.of("§6Warning: §eFixing EVs will cost §6one §ecoin."));
                                    else
                                    {
                                        src.sendMessage(Text.of("§6Warning: §eFixing EVs will cost §6" +
                                                costToConfirm + "§e coins."));
                                    }

                                    src.sendMessage(Text.of("§2Ready? Type: §a/" + commandAlias + " " + slot + " -c"));
                                }
                            }
                            else
                            {
                                printToLog(1, "Fixed EVs for slot §3" + slot + "§b. Config price is §30§b, taking nothing.");
                                fixPlayerEVs(nbt, src, HPEV, attackEV, defenceEV, spAttackEV, spDefenceEV, speedEV);
                            }
                        }
                    }
                }
            }
        }
	    else
            printToLog(0,"This command cannot run from the console or command blocks.");

        return CommandResult.success();
	}

	private void printSyntaxHelper(CommandSource src)
    {
        if (commandCost != 0)
            src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <slot, 1-6> {-c to confirm}"));
        else
            src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <slot, 1-6>"));
    }

	private void fixPlayerEVs(NBTTagCompound nbt, CommandSource src,
                              int HPEV, int attackEV, int defenceEV, int spAttackEV, int spDefenceEV, int speedEV)
    {
        String defenseName = "Defense";
        if (useBritishSpelling)
            defenseName = "Defence";

        if (HPEV > 252)
        {
            src.sendMessage(Text.of("§aThe §2HP §aEV was above 252 and has been fixed!"));
            nbt.setInteger(NbtKeys.EV_HP, 252);
        }

        if (attackEV > 252)
        {
            src.sendMessage(Text.of("§aThe §2Attack §aEV was above 252 and has been fixed!"));
            nbt.setInteger(NbtKeys.EV_ATTACK, 252);
        }

        if (defenceEV > 252)
        {
            src.sendMessage(Text.of("§aThe §2" + defenseName + " §aEV was above 252 and has been fixed!"));
            nbt.setInteger(NbtKeys.EV_DEFENCE, 252);
        }

        if (spAttackEV > 252)
        {
            src.sendMessage(Text.of("§aThe §2Special Attack §aEV was above 252 and has been fixed!"));
            nbt.setInteger(NbtKeys.EV_SPECIAL_ATTACK, 252);
        }

        if (spDefenceEV > 252)
        {
            src.sendMessage(Text.of("§aThe §2Special" + defenseName + " §aEV was above 252 and has been fixed!"));
            nbt.setInteger(NbtKeys.EV_SPECIAL_DEFENCE, 252);
        }

        if (speedEV > 252)
        {
            src.sendMessage(Text.of("§aThe §2Speed §aEV was above 252 and has been fixed!"));
            nbt.setInteger(NbtKeys.EV_SPEED, 252);
        }

        if (nbt.getString("Nickname").equals(""))
            src.sendMessage(Text.of("§6" + nbt.getString("Name") + "§e has been checked and optimized!"));
        else
            src.sendMessage(Text.of("§eYour §6" + nbt.getString("Nickname") + "§e has been checked and optimized!"));
    }
}