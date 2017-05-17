package rs.expand.pixelupgrade.commands;

import java.util.Arrays;
import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public class Force implements CommandExecutor
{
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
	{
		Player player = (Player) src;
		if (!args.getOne("value").isPresent() || !args.getOne("stat").isPresent() || !args.getOne("slot").isPresent())
		{
			player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
			player.sendMessage(Text.of("\u00A74Error: \u00A7cNot all arguments were provided!"));
			player.sendMessage(Text.of("\u00A74Usage: \u00A7c/upgrade force <slot> <type> <value> (-f)"));
			player.sendMessage(Text.of(""));
			player.sendMessage(Text.of("\u00A75Please note: \u00A7dPassing the -f flag will disable safety checks."));
			player.sendMessage(Text.of("\u00A7dThis may lead to crashes or even corruption. Handle with care!"));
			player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
		}
		else
		{
			String stat = null, value = null;
			Integer slot = null, intValue = null;
			Boolean canContinue = false, valueIsInt = false;
			try
			{
				slot = Integer.parseInt(args.<String>getOne("slot").get());
				stat = args.<String>getOne("stat").get();
				value = args.<String>getOne("value").get();
				if (value.matches("^-?\\d+$"))
				{ 
					intValue = Integer.parseInt(value); 
					valueIsInt = true;
				}
				
				canContinue = true;
			}
			catch (NumberFormatException e) 
			{
				player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
				player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid arguments! Format is #, text, #."));
				player.sendMessage(Text.of("\u00A74Usage: \u00A7c/upgrade force <slot> <type> <value> (-f)"));
				player.sendMessage(Text.of(""));
				player.sendMessage(Text.of("\u00A75Please note: \u00A7dPassing the -f flag will disable safety checks."));
				player.sendMessage(Text.of("\u00A7dThis may lead to crashes or even corruption. Handle with care!"));
				player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
			}
			
			if (canContinue)
			{
				Boolean forceValue = false;
				if (args.hasAny("f"))
					forceValue = true;

			    if (slot > 6 || slot < 1)
			    	player.sendMessage(Text.of("\u00A74Error: \u00A7cSlot number must be between 1 and 6."));
			    else
			    {
		        	Optional<?> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) player));
		        	String username = player.getName();

		        	if (!forceValue && valueIsInt)
			        {
		            	PlayerStorage storageCompleted = (PlayerStorage)storage.get();
		                NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];
		                
			            if (nbt == null)
			            	player.sendMessage(Text.of("\u00A74Error: \u00A7cYou don't have anything in that slot!"));
			            else
			            {
					    	String[] validIVs = new String[] {"IVHP", "IVAttack", "IVDefence", "IVSpAtt", "IVSpDef", "IVSpeed"};
					    	String[] validEVs = new String[] {"EVHP", "EVAttack", "EVDefence", "EVSpecialAttack", "EVSpecialDefence", "EVSpAtt", "EVSpAtk", "EVSpDef", "EVSpeed"};
					    	String[] validGrowth = new String[] {"Growth"};
					    	String[] validNature = new String[] {"Nature"};
					    	String[] validBools = new String[] {"IsShiny", "Is_Shiny"};

					    	String fixedStat = stat;
					    	switch (fixedStat.toUpperCase())
					    	{
								case "IVHP":
									fixedStat = "IVHP";
									break;
								case "IVATTACK":
									fixedStat = "IVAttack";
									break;
								case "IVDEFENCE": case "IVDEFENSE":
									fixedStat = "IVDefence";
									break;
								case "IVSPATT": case "IVSPATK":
									fixedStat = "IVSpAtt";
									break;
								case "IVSPDEF":
									fixedStat = "IVSpDef";
									break;
								case "IVSPEED":
									fixedStat = "IVSpeed";
									break;
								case "EVHP":
									fixedStat = "EVHP";
									break;
								case "EVATTACK":
									fixedStat = "EVAttack";
									break;
								case "EVDEFENCE": case "EVDEFENSE":
									fixedStat = "EVDefence";
									break;
								case "EVSPECIALATTACK": case "EVSPATT": case "EVSPATK":
									fixedStat = "EVSpecialAttack";
									break;
								case "EVSPECIALDEFENCE": case "EVSPDEF":
									fixedStat = "EVSpecialDefence";
									break;
								case "EVSPEED":
									fixedStat = "EVSpeed";
									break;
								case "GROWTH": case "SIZE":
									fixedStat = "Growth";
									break;
								case "NATURE":
									fixedStat = "Nature";
									break;
								case "ISSHINY": case "IS_SHINY":
									fixedStat = "IsShiny";
									break;
					    	}
					    	
					    	System.out.println("\u00A72Params: \u00A7c" + stat + " " + fixedStat);
					    	
					    	if (Arrays.asList(validIVs).contains(fixedStat) && intValue > 32767 || Arrays.asList(validIVs).contains(fixedStat) && intValue < -32768)
					    		player.sendMessage(Text.of("\u00A74Error: \u00A7cIV value out of bounds. Valid range: -32768 ~ 32767"));
					    	else if (Arrays.asList(validEVs).contains(fixedStat) && intValue > 32767 || Arrays.asList(validEVs).contains(fixedStat) && intValue < -32768)
					    		player.sendMessage(Text.of("\u00A74Error: \u00A7cEV value out of bounds. Valid range: -32768 ~ 32767"));
					    	else if (Arrays.asList(validGrowth).contains(fixedStat) && intValue > 8 || Arrays.asList(validGrowth).contains(fixedStat) && intValue < 0)
					    		player.sendMessage(Text.of("\u00A74Error: \u00A7cSize value out of bounds. Valid range: 0 ~ 8"));
					    	else if (Arrays.asList(validNature).contains(fixedStat) && intValue > 24 || Arrays.asList(validNature).contains(fixedStat) && intValue < 0)
					    		player.sendMessage(Text.of("\u00A74Error: \u00A7cNature value out of bounds. Valid range: 0 ~ 24"));
                            else if (Arrays.asList(validBools).contains(fixedStat) && intValue > 1 || Arrays.asList(validBools).contains(fixedStat) && intValue < 0)
                                player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid boolean value. Valid values: 0 (=false) or 1 (=true)"));
					    	else if (Arrays.asList(validIVs).contains(fixedStat) || Arrays.asList(validEVs).contains(fixedStat) || Arrays.asList(validGrowth).contains(fixedStat) || Arrays.asList(validNature).contains(fixedStat) || Arrays.asList(validBools).contains(fixedStat))
					    	{				    	
						                nbt.setInteger(fixedStat, intValue);
						                player.sendMessage(Text.of("\u00A7aValue changed! Not showing? Reconnect to update your client."));
					    	}
					    	else
					    	{
					    		player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
					    		player.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid parameter. See below!"));
								player.sendMessage(Text.of(""));
					    		player.sendMessage(Text.of("\u00A76IVs: \u00A7eIVHP, IVAttack, IVDefence, IVSpAtt, IVSpDef, IVSpeed"));
					    		player.sendMessage(Text.of("\u00A76EVs: \u00A7eEVHP, EVAttack, EVDefence, EVSpAtt, EVSpDef, EVSpeed"));
					    		player.sendMessage(Text.of("\u00A76Others: \u00A7eGrowth, Nature, IsShiny"));
                                player.sendMessage(Text.of(""));
                                player.sendMessage(Text.of("\u00A75Please note: \u00A7dThese are sanitized, and may not work on -f."));
					    		player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
					    	}
			            }
			        }
			        else if (!forceValue && !valueIsInt)
			        {
			        	player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
						player.sendMessage(Text.of("\u00A74Error: \u00A7c<value> should be a number, not text."));
						player.sendMessage(Text.of("\u00A74Usage: \u00A7c/upgrade force <slot> <type> <value> (-f)"));
						player.sendMessage(Text.of(""));
						player.sendMessage(Text.of("\u00A75Please note: \u00A7dPassing the -f flag will disable safety checks."));
						player.sendMessage(Text.of("\u00A7dThis may lead to crashes or even corruption. Handle with care!"));
						player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
			        }
			        else
			        {
		            	PlayerStorage storageCompleted = (PlayerStorage)storage.get();
		                NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];
		                
			            if (nbt == null)
			            	player.sendMessage(Text.of("\u00A74Error: \u00A7cYou don't have anything in that slot!"));
			            else
			            {
					    	String fixedStat = stat;
					    	Boolean statWasFixed = true;
					    	switch (fixedStat.toUpperCase())
					    	{
					    		case "IVHP":
					    		    fixedStat = "IVHP";
					    		    break;
					    		case "IVATTACK":
					    		    fixedStat = "IVAttack";
					    		    break;
					    		case "IVDEFENCE": case "IVDEFENSE":
					    		    fixedStat = "IVDefence";
					    		    break;
					    		case "IVSPATT": case "IVSPATK":
					    		    fixedStat = "IVSpAtt";
					    		    break;
					    		case "IVSPDEF":
					    		    fixedStat = "IVSpDef";
					    		    break;
					    		case "IVSPEED":
					    		    fixedStat = "IVSpeed";
					    		    break;
					    		case "EVHP":
					    		    fixedStat = "EVHP";
					    		    break;
					    		case "EVATTACK":
					    		    fixedStat = "EVAttack";
					    		    break;
					    		case "EVDEFENCE": case "EVDEFENSE":
					    		    fixedStat = "EVDefence";
					    		    break;
                                case "EVSPECIALATTACK": case "EVSPATT": case "EVSPATK":
                                    fixedStat = "EVSpecialAttack";
                                    break;
                                case "EVSPECIALDEFENCE": case "EVSPDEF":
                                    fixedStat = "EVSpecialDefence";
                                    break;
					    		case "EVSPEED":
					    		    fixedStat = "EVSpeed";
					    		    break;
					    		case "GROWTH": case "SIZE":
					    		    fixedStat = "Growth";
					    		    break;
                                case "ISSHINY": case "IS_SHINY":
                                    fixedStat = "IsShiny";
                                    break;
                                default: statWasFixed = false;
					    	}
					    	
			            	player.sendMessage(Text.of("\u00A7eForcing value..."));

					    	if (statWasFixed)
                            {
                                player.sendMessage(Text.of("\u00A75Note: \u00AdAn invalid but known stat was found, and was auto-corrected."));
                                player.sendMessage(Text.of("\u00A75Provided stat: \u00Ad" + stat + "\u00A75, corrected to: \u00A7d" + fixedStat));

                                stat = fixedStat;
                            }

			            	if (valueIsInt)
			            		nbt.setInteger(stat, intValue);
			            	else
			            		nbt.setString(stat, value);

			            	player.sendMessage(Text.of("\u00A7aThe new value was written, a reconnect may be necessary."));
			            }
			        }
			    }
			}
	    }
	    return CommandResult.success();
	}
}