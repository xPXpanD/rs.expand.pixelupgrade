package rs.expand.pixelupgrade.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

public class Upgrade implements CommandExecutor
{
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
	{
		Player player = (Player) src;
		Boolean hasNoPermission = true;

		player.sendMessage(Text.of("\u00A75---------------- \u00A7dPixelUpgrade commands \u00A75----------------"));
		if (player.hasPermission("pixelupgrade.commands.fixevs"))
		{
			player.sendMessage(Text.of("\u00A76/fixevs <slot>\u00A7f -- \u00A7eFixes EVs above 252 (which are wasted)."));
			hasNoPermission = false;
		}
		if (player.hasPermission("pixelupgrade.commands.resetevs"))
		{
            player.sendMessage(Text.of("\u00A76/resetevs <slot> (confirm)\u00A7f -- \u00A7eResets all EVs to zero."));
            hasNoPermission = false;
        }
        if (player.hasPermission("pixelupgrade.commands.getstats"))
        {
            player.sendMessage(Text.of("\u00A76/getstats (player) <slot>\u00A7f -- \u00A7eLists many of a Pok\u00E9mon's stats."));
            hasNoPermission = false;
        }
		if (player.hasPermission("pixelupgrade.commands.ivs"))
        {
            player.sendMessage(Text.of("\u00A76/upgrade ivs <slot> <IV> (value) (confirm)\u00A7f -- \u00A7eRaise IVs for cash."));
            hasNoPermission = false;
        }
        if (player.hasPermission("pixelupgrade.commands.resize"))
        {
            player.sendMessage(Text.of("\u00A76/upgrade size <slot> <size> (confirm)\u00A7f -- \u00A7eBuy size changes on shinies/legendaries."));
            hasNoPermission = false;
        }
		if (player.hasPermission("pixelupgrade.commands.admin.force"))
        {
            player.sendMessage(Text.of("\u00A76/upgrade force <slot> <type> <value> (-f)\u00A7f -- \u00A7eChange anything!"));
            hasNoPermission = false;
        }

        if (hasNoPermission)
        {
            player.sendMessage(Text.of("\u00A7cYou do not have any of this plugin's permissions. Sorry!"));
        }
		player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

		return CommandResult.success();
	}
}