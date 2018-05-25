// What have I done?
package rs.expand.pixelupgrade.commands;

// Remote imports.
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
import rs.expand.pixelupgrade.utilities.PrintingMethods;
import static rs.expand.pixelupgrade.PixelUpgrade.*;

// TODO: Turn /dittofusion into a generic /fuse that works on everything?
public class DittoFusion implements CommandExecutor
{
    // Declare some variables. We'll load stuff into these when we call the config loader.
    // Other config variables are loaded in from their respective classes. Check the imports.
    public static String commandAlias;
    public static Integer stat0to5, stat6to10, stat11to15, stat16to20, stat21to25, stat26to30, stat31plus, regularCap;
    public static Integer shinyCap, pointMultiplierForCost, previouslyUpgradedMultiplier, addFlatFee;
    public static Boolean passOnShinyStatus;

    // Pass any debug messages onto final printing, where we will decide whether to show or swallow them.
    private void printToLog (final int debugNum, final String inputString)
    { PrintingMethods.printDebugMessage("DittoFusion", debugNum, inputString); }

    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        if (economyEnabled && src instanceof Player)
        {
            // Validate the data we get from the command's main config.
            final List<String> nativeErrorArray = new ArrayList<>();
            if (commandAlias == null)
                nativeErrorArray.add("commandAlias");
            if (stat0to5 == null)
                nativeErrorArray.add("stat0to5");
            if (stat6to10 == null)
                nativeErrorArray.add("stat6to10");
            if (stat11to15 == null)
                nativeErrorArray.add("stat11to15");
            if (stat16to20 == null)
                nativeErrorArray.add("stat16to20");
            if (stat21to25 == null)
                nativeErrorArray.add("stat21to25");
            if (stat26to30 == null)
                nativeErrorArray.add("stat26to30");
            if (stat31plus == null)
                nativeErrorArray.add("stat31plus");
            if (regularCap == null)
                nativeErrorArray.add("regularCap");
            if (shinyCap == null)
                nativeErrorArray.add("shinyCap");
            if (passOnShinyStatus == null)
                nativeErrorArray.add("passOnShinyStatus");
            if (pointMultiplierForCost == null)
                nativeErrorArray.add("pointMultiplierForCost");
            if (previouslyUpgradedMultiplier == null)
                nativeErrorArray.add("previouslyUpgradedMultiplier");
            if (addFlatFee == null)
                nativeErrorArray.add("addFlatFee");

            if (!nativeErrorArray.isEmpty())
            {
                PrintingMethods.printCommandNodeError("DittoFusion", nativeErrorArray);
                src.sendMessage(Text.of("§4Error: §cThis command's config is invalid! Please report to staff."));
            }
            else if (useBritishSpelling == null)
            {
                printToLog(0, "Could not read remote node \"§4useBritishSpelling§c\".");
                printToLog(0, "The main config contains invalid variables. Exiting.");
                src.sendMessage(Text.of("§4Error: §cCould not parse main config. Please report to staff."));
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
                int slot1 = 0, slot2 = 0;
                boolean commandConfirmed = false, canContinue = false;
                String errorString = "§4There's an error message missing, please report this!";

                if (!args.<String>getOne("main slot").isPresent())
                {
                    printToLog(1, "No arguments provided. Exit.");
                    errorString = "§4Error: §cNo slots were provided. Please provide two valid slots.";
                }
                else
                {
                    final String slotString = args.<String>getOne("main slot").get();

                    if (slotString.matches("^[1-6]"))
                    {
                        printToLog(2, "Target slot was a valid slot number. Let's move on!");
                        slot1 = Integer.parseInt(args.<String>getOne("main slot").get());
                        canContinue = true;
                    }
                    else
                    {
                        printToLog(1, "Invalid slot for target Pokémon. Exit.");
                        errorString = "§4Error: §cInvalid value on target slot. Valid values are 1-6.";
                    }
                }

                if (canContinue)
                {
                    canContinue = false; // Reset the flag.

                    if (!args.<String>getOne("sacrifice slot").isPresent())
                    {
                        printToLog(1, "No sacrifice Pokémon slot provided. Exit.");
                        errorString = "§4Error: §cNo sacrifice provided. Please provide two valid slots.";
                    }
                    else
                    {
                        final String slotString = args.<String>getOne("sacrifice slot").get();

                        if (slotString.matches("^[1-6]"))
                        {
                            printToLog(2, "Valid slot found on arg 2. Checking against arg 1...");
                            slot2 = Integer.parseInt(args.<String>getOne("sacrifice slot").get());

                            if (slot2 == slot1)
                            {
                                printToLog(1, "Player tried to fuse a Pokémon with itself. Abort, abort!");
                                errorString = "§4Error: §cYou can't fuse a Pokémon with itself.";
                            }
                            else
                                canContinue = true;
                        }
                        else
                        {
                            printToLog(1, "Invalid slot for sacrifice Pokémon. Exit.");
                            errorString = "§4Error: §cInvalid value on sacrifice slot. Valid values are 1-6.";
                        }
                    }
                }

                if (args.hasAny("c"))
                    commandConfirmed = true;

                if (!canContinue)
                {
                    src.sendMessage(Text.of("§5-----------------------------------------------------"));
                    src.sendMessage(Text.of(errorString));
                    src.sendMessage(Text.of("§4Usage: §c/" + commandAlias + " <target> <sacrifice> {-c to confirm}"));
                    src.sendMessage(Text.EMPTY);
                    src.sendMessage(Text.of("§6Warning: §eAdd the -c flag only if you're ready to spend money!"));
                    src.sendMessage(Text.of("§5-----------------------------------------------------"));
                }
                else
                {
                    final Optional<?> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) src));

                    if (!storage.isPresent())
                    {
                        printToLog(0, "§4" + src.getName() + "§c does not have a Pixelmon storage, aborting. Bug?");
                        src.sendMessage(Text.of("§4Error: §cNo Pixelmon storage found. Please contact staff!"));
                    }
                    else
                    {
                        final PlayerStorage storageCompleted = (PlayerStorage) storage.get();
                        final NBTTagCompound nbt1 = storageCompleted.partyPokemon[slot1 - 1];
                        final NBTTagCompound nbt2 = storageCompleted.partyPokemon[slot2 - 1];

                        if (nbt1 == null && nbt2 != null)
                        {
                            printToLog(1, "No NBT data found for target Pokémon, slot empty? Exit.");
                            src.sendMessage(Text.of("§4Error: §cThe target Pokémon does not seem to exist."));
                        }
                        else if (nbt1 != null && nbt2 == null)
                        {
                            printToLog(1, "No NBT data found for sacrifice Pokémon, slot empty? Exit.");
                            src.sendMessage(Text.of("§4Error: §cThe sacrifice Pokémon does not seem to exist."));
                        }
                        else if (nbt1 == null)
                        {
                            printToLog(1, "No NBT data found for target not sacrifice, slots empty? Exit.");
                            src.sendMessage(Text.of("§4Error: §cNeither the target nor the sacrifice seem to exist."));
                        }
                        else
                        {
                            final Optional<UniqueAccount> optionalAccount = economyService.getOrCreateAccount(player.getUniqueId());

                            if (optionalAccount.isPresent())
                            {
                                if (!nbt1.getString("Name").equals("Ditto") && nbt2.getString("Name").equals("Ditto"))
                                {
                                    printToLog(1, "Target was not a Ditto. Abort, abort!");
                                    src.sendMessage(Text.of("§4Error: §cYour target Pokémon is not a Ditto."));
                                }
                                else if (nbt1.getString("Name").equals("Ditto") && !nbt2.getString("Name").equals("Ditto"))
                                {
                                    printToLog(1, "Sacrifice was not a Ditto. Abort, abort!");
                                    src.sendMessage(Text.of("§4Error: §cSorry, but the sacrifice needs to be a Ditto."));
                                }
                                else if (!nbt1.getString("Name").equals("Ditto") && !nbt2.getString("Name").equals("Ditto"))
                                {
                                    printToLog(1, "No Dittos found. Let's not create some unholy abomination -- abort!");
                                    src.sendMessage(Text.of("§4Error: §cThis command only works on Dittos."));
                                }
                                else
                                {
                                    final World currentWorld = (World) player.getWorld();
                                    final EntityPixelmon targetPokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt1, currentWorld);
                                    final EntityPixelmon sacrificePokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt2, currentWorld);
                                    final int targetFuseCount = targetPokemon.getEntityData().getInteger("fuseCount");
                                    final int sacrificeFuseCount = sacrificePokemon.getEntityData().getInteger("fuseCount");

                                    if (targetFuseCount >= shinyCap && nbt1.getInteger(NbtKeys.IS_SHINY) == 1)
                                    {
                                        printToLog(1, "Hit the shiny cap on target Ditto. Exit.");

                                        src.sendMessage(Text.of("§4Error: §cYour target shiny Ditto cannot grow any further."));
                                        src.sendMessage(Text.of("§6Tip: §eYou could still sacrifice §othis§r§e Ditto... You monster."));
                                    }
                                    else if (targetFuseCount >= regularCap && nbt1.getInteger(NbtKeys.IS_SHINY) != 1)
                                    {
                                        printToLog(1, "Hit the non-shiny cap on target Ditto. Exit.");

                                        src.sendMessage(Text.of("§4Error: §cYour target Ditto cannot grow any further."));
                                        src.sendMessage(Text.of("§6Tip: §eYou could still sacrifice §othis§r§e Ditto... You monster."));
                                    }
                                    else
                                    {
                                        printToLog(2, "Passed most of the checks, moving on to execution.");

                                        final UniqueAccount uniqueAccount = optionalAccount.get();
                                        int HPPlusNum = 0, ATKPlusNum = 0, DEFPlusNum = 0, SPATKPlusNum = 0;
                                        int SPDEFPlusNum = 0, SPDPlusNum = 0, statToCheck = 0, statToUpgrade;

                                        final int targetHP = nbt1.getInteger(NbtKeys.IV_HP);
                                        final int targetATK = nbt1.getInteger(NbtKeys.IV_ATTACK);
                                        final int targetDEF = nbt1.getInteger(NbtKeys.IV_DEFENCE);
                                        final int targetSPATK = nbt1.getInteger(NbtKeys.IV_SP_ATT);
                                        final int targetSPDEF = nbt1.getInteger(NbtKeys.IV_SP_DEF);
                                        final int targetSPD = nbt1.getInteger(NbtKeys.IV_SPEED);

                                        final int sacrificeHP = nbt2.getInteger(NbtKeys.IV_HP);
                                        final int sacrificeATK = nbt2.getInteger(NbtKeys.IV_ATTACK);
                                        final int sacrificeDEF = nbt2.getInteger(NbtKeys.IV_DEFENCE);
                                        final int sacrificeSPATK = nbt2.getInteger(NbtKeys.IV_SP_ATT);
                                        final int sacrificeSPDEF = nbt2.getInteger(NbtKeys.IV_SP_DEF);
                                        final int sacrificeSPD = nbt2.getInteger(NbtKeys.IV_SPEED);

                                        for (int i = 0; i <= 5; i++)
                                        {
                                            switch (i)
                                            {
                                                case 0: statToCheck = sacrificeHP; break;
                                                case 1: statToCheck = sacrificeATK; break;
                                                case 2: statToCheck = sacrificeDEF; break;
                                                case 3: statToCheck = sacrificeSPATK; break;
                                                case 4: statToCheck = sacrificeSPDEF; break;
                                                case 5: statToCheck = sacrificeSPD; break;
                                            }

                                            if (statToCheck < 6) // my fancy old switched range check didn't work right... how embarrassing!
                                                statToUpgrade = stat0to5;
                                            else if (statToCheck < 11)
                                                statToUpgrade = stat6to10;
                                            else if (statToCheck < 16)
                                                statToUpgrade = stat11to15;
                                            else if (statToCheck < 21)
                                                statToUpgrade = stat16to20;
                                            else if (statToCheck < 26)
                                                statToUpgrade = stat21to25;
                                            else if (statToCheck < 31)
                                                statToUpgrade = stat26to30;
                                            else
                                                statToUpgrade = stat31plus;

                                            switch (i)
                                            {
                                                case 0: HPPlusNum = statToUpgrade; break;
                                                case 1: ATKPlusNum = statToUpgrade; break;
                                                case 2: DEFPlusNum = statToUpgrade; break;
                                                case 3: SPATKPlusNum = statToUpgrade; break;
                                                case 4: SPDEFPlusNum = statToUpgrade; break;
                                                case 5: SPDPlusNum = statToUpgrade; break;
                                            }
                                        }

                                        if (targetHP >= 31)
                                            HPPlusNum = 0;
                                        else if (HPPlusNum + targetHP >= 31)
                                            HPPlusNum = 31 - targetHP;

                                        if (targetATK >= 31)
                                            ATKPlusNum = 0;
                                        else if (ATKPlusNum + targetATK >= 31)
                                            ATKPlusNum = 31 - targetATK;

                                        if (targetDEF >= 31)
                                            DEFPlusNum = 0;
                                        else if (DEFPlusNum + targetDEF >= 31)
                                            DEFPlusNum = 31 - targetDEF;

                                        if (targetSPATK >= 31)
                                            SPATKPlusNum = 0;
                                        else if (SPATKPlusNum + targetSPATK >= 31)
                                            SPATKPlusNum = 31 - targetSPATK;

                                        if (targetSPDEF >= 31)
                                            SPDEFPlusNum = 0;
                                        else if (SPDEFPlusNum + targetSPDEF >= 31)
                                            SPDEFPlusNum = 31 - targetSPDEF;

                                        if (targetSPD >= 31)
                                            SPDPlusNum = 0;
                                        else if (SPDPlusNum + targetSPD >= 31)
                                            SPDPlusNum = 31 - targetSPD;

                                        int totalUpgradeCount = HPPlusNum + ATKPlusNum + DEFPlusNum + SPATKPlusNum;
                                        totalUpgradeCount = totalUpgradeCount + SPDEFPlusNum + SPDPlusNum;
                                        BigDecimal costToConfirm = BigDecimal.valueOf((totalUpgradeCount * pointMultiplierForCost) + addFlatFee);
                                        final BigDecimal nonMultipliedCost = costToConfirm;
                                        BigDecimal extraCost = new BigDecimal(0);

                                        if (sacrificeFuseCount > 0)
                                        {
                                            costToConfirm = costToConfirm.multiply(new BigDecimal(previouslyUpgradedMultiplier));
                                            extraCost = costToConfirm.subtract(nonMultipliedCost);
                                        }

                                        if (totalUpgradeCount == 0)
                                        {
                                            printToLog(1, "Sacrifice was too weak to add any stats, apparently. Wow. Exit.");
                                            src.sendMessage(Text.of("§4Error: §cYour sacrificial Ditto is too weak to make a difference."));
                                        }
                                        else if (commandConfirmed)
                                        {
                                            final TransactionResult transactionResult = uniqueAccount.withdraw(economyService.getDefaultCurrency(),
                                                costToConfirm, Sponge.getCauseStackManager().getCurrentCause());

                                            if (transactionResult.getResult() == ResultType.SUCCESS)
                                            {
                                                src.sendMessage(Text.of("§7-----------------------------------------------------"));
                                                src.sendMessage(Text.of("§eThe §6Ditto §ein slot §6" + slot2 +
                                                    "§e was eaten, taking §6" + costToConfirm + "§e coins with it."));
                                                src.sendMessage(Text.EMPTY);

                                                if (HPPlusNum != 0)
                                                {
                                                    src.sendMessage(Text.of("§bUpgraded HP!"));
                                                    src.sendMessage(Text.of("§7" + targetHP + " §f-> §a" + (targetHP + HPPlusNum)));
                                                    nbt1.setInteger(NbtKeys.IV_HP, nbt1.getInteger(NbtKeys.IV_HP) + HPPlusNum);
                                                }
                                                if (ATKPlusNum != 0)
                                                {
                                                    src.sendMessage(Text.of("§bUpgraded Attack!"));
                                                    src.sendMessage(Text.of("§7" + targetATK + " §f-> §a" + (targetATK + ATKPlusNum)));
                                                    nbt1.setInteger(NbtKeys.IV_ATTACK, nbt1.getInteger(NbtKeys.IV_ATTACK) + ATKPlusNum);
                                                }
                                                if (DEFPlusNum != 0)
                                                {
                                                    if (useBritishSpelling)
                                                        src.sendMessage(Text.of("§bUpgraded Defence!"));
                                                    else
                                                        src.sendMessage(Text.of("§bUpgraded Defense!"));

                                                    src.sendMessage(Text.of("§7" + targetDEF + " §f-> §a" + (targetDEF + DEFPlusNum)));
                                                    nbt1.setInteger(NbtKeys.IV_DEFENCE, nbt1.getInteger(NbtKeys.IV_DEFENCE) + DEFPlusNum);
                                                }
                                                if (SPATKPlusNum != 0)
                                                {
                                                    src.sendMessage(Text.of("§bUpgraded Special Attack!"));
                                                    src.sendMessage(Text.of("§7" + targetSPATK + " §f-> §a" + (targetSPATK + SPATKPlusNum)));
                                                    nbt1.setInteger(NbtKeys.IV_SP_ATT, nbt1.getInteger(NbtKeys.IV_SP_ATT) + SPATKPlusNum);
                                                }
                                                if (SPDEFPlusNum != 0)
                                                {
                                                    if (useBritishSpelling)
                                                        src.sendMessage(Text.of("§bUpgraded Special Defence!"));
                                                    else
                                                        src.sendMessage(Text.of("§bUpgraded Special Defense!"));

                                                    src.sendMessage(Text.of("§7" + targetSPDEF + " §f-> §a" + (targetSPDEF + SPDEFPlusNum)));
                                                    nbt1.setInteger(NbtKeys.IV_SP_DEF, nbt1.getInteger(NbtKeys.IV_SP_DEF) + SPDEFPlusNum);
                                                }
                                                if (SPDPlusNum != 0)
                                                {
                                                    src.sendMessage(Text.of("§bUpgraded Speed!"));
                                                    src.sendMessage(Text.of("§7" + targetSPD + " §f-> §a" + (targetSPD + SPDPlusNum)));
                                                    nbt1.setInteger(NbtKeys.IV_SPEED, nbt1.getInteger(NbtKeys.IV_SPEED) + SPDPlusNum);
                                                }

                                                if (sacrificeFuseCount > 0)
                                                {
                                                    src.sendMessage(Text.EMPTY);
                                                    src.sendMessage(Text.of("§dSacrifice had prior upgrades. You paid an extra §5" + extraCost + "§d coins."));
                                                }

                                                if (nbt2.getInteger(NbtKeys.IS_SHINY) == 1 && passOnShinyStatus)
                                                {
                                                    printToLog(2, "Passing on shinyness is enabled, and sacrifice is shiny. Do it!");

                                                    // Not sure which one I need, so I set both. Doesn't seem to matter much.
                                                    nbt1.setInteger(NbtKeys.IS_SHINY, 1);
                                                    nbt1.setInteger(NbtKeys.SHINY, 1);

                                                    // Force the client to update.
                                                    storageCompleted.sendUpdatedList();
                                                }

                                                src.sendMessage(Text.of("§7-----------------------------------------------------"));

                                                targetPokemon.getEntityData().setInteger("fuseCount", targetFuseCount + 1);
                                                storageCompleted.changePokemonAndAssignID(slot2 - 1, null);
                                                //storageCompleted.update(targetPokemon, EnumUpdateType.Status);

                                                printToLog(1, "Transaction successful. Took §3" + costToConfirm +
                                                        "§b coins (+ §3Ditto§b), remainder is §3" + uniqueAccount.getBalance(economyService.getDefaultCurrency()));
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
                                            printToLog(1, "Got cost but no confirmation; end of the line.");

                                            src.sendMessage(Text.of("§7-----------------------------------------------------"));
                                            if (nbt2.getInteger(NbtKeys.IS_SHINY) == 1 && passOnShinyStatus)
                                                src.sendMessage(Text.of("§eThe Ditto in slot §6" + slot1 + "§e will be upgraded. Passing on shiny status!"));
                                            else
                                                src.sendMessage(Text.of("§eYou are about to upgrade the Ditto in slot §6" + slot1 + "§e."));

                                            src.sendMessage(Text.EMPTY);

                                            if (HPPlusNum != 0)
                                            {
                                                src.sendMessage(Text.of("§bHP will be upgraded."));
                                                src.sendMessage(Text.of("§7" + targetHP + " §f-> §a" + (targetHP + HPPlusNum)));
                                            }
                                            if (ATKPlusNum != 0)
                                            {
                                                src.sendMessage(Text.of("§bAttack will be upgraded."));
                                                src.sendMessage(Text.of("§7" + targetATK + " §f-> §a" + (targetATK + ATKPlusNum)));
                                            }
                                            if (DEFPlusNum != 0)
                                            {
                                                if (useBritishSpelling)
                                                    src.sendMessage(Text.of("§bDefence will be upgraded."));
                                                else
                                                    src.sendMessage(Text.of("§bDefense will be upgraded."));

                                                src.sendMessage(Text.of("§7" + targetDEF + " §f-> §a" + (targetDEF + DEFPlusNum)));
                                            }
                                            if (SPATKPlusNum != 0)
                                            {
                                                src.sendMessage(Text.of("§bSpecial Attack will be upgraded."));
                                                src.sendMessage(Text.of("§7" + targetSPATK + " §f-> §a" + (targetSPATK + SPATKPlusNum)));
                                            }
                                            if (SPDEFPlusNum != 0)
                                            {
                                                if (useBritishSpelling)
                                                    src.sendMessage(Text.of("§bSpecial Defence will be upgraded."));
                                                else
                                                    src.sendMessage(Text.of("§bSpecial Defense will be upgraded."));

                                                src.sendMessage(Text.of("§7" + targetSPDEF + " §f-> §a" + (targetSPDEF + SPDEFPlusNum)));
                                            }
                                            if (SPDPlusNum != 0)
                                            {
                                                src.sendMessage(Text.of("§bSpeed will be upgraded."));
                                                src.sendMessage(Text.of("§7" + targetSPD + " §f-> §a" + (targetSPD + SPDPlusNum)));
                                            }

                                            src.sendMessage(Text.EMPTY);

                                            if (sacrificeFuseCount > 0)
                                            {
                                                src.sendMessage(Text.of("§eFusing costs §6" + costToConfirm + "§e coins plus §6"
                                                        + extraCost + "§e coins for prior upgrades."));
                                            }
                                            else
                                                src.sendMessage(Text.of("§eFusing will cost you §6" + costToConfirm + "§e coins."));

                                            src.sendMessage(Text.of("§2Ready? Use: §a/" + commandAlias + " " + slot1 + " " + slot2 + " -c"));
                                            src.sendMessage(Text.of("§4Warning: §cThe Ditto in slot §4" + slot2 + "§c will be §ldeleted§r§c!"));
                                            src.sendMessage(Text.of("§7-----------------------------------------------------"));
                                        }
                                    }
                                }
                            }
                            else
                            {
                                src.sendMessage(Text.of("§4Error: §cNo economy account found. Please contact staff!"));
                                printToLog(0, "§4" + src.getName() + "§c does not have an economy account, Exit. Bug?");
                            }
                        }
                    }
                }
            }
        }
        else if (!economyEnabled)
            src.sendMessage(Text.of("§4Error: §cThis server does not have an economy plugin installed."));
        else
            printToLog(0, "This command cannot run from the console or command blocks.");

        return CommandResult.success();
    }
}
