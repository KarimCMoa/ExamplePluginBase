package me.karim.command.commands;

import me.karim.command.Command;
import me.karim.command.param.Parameter;
import me.karim.utilities.design.CC;
import me.karim.utilities.design.Style;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GeneralCommands {

    @Command(names = {"gamemode", "gm"}, permissionNode = "core.gamemode")
    public static void gamemodeSelf(Player player, @Parameter(name = "gamemode") GameMode gameMode) {

        player.setGameMode(gameMode);
        player.updateInventory();
        player.sendMessage(Style.FIRST_COLOR() + "You updated your game mode to " + CC.RESET + gameMode.name());

    }

    @Command(names = {"gamemodeother", "gmother"}, permissionNode = "core.gamemode.other")
    public static void gamemodeOther(CommandSender sender, @Parameter(name = "target") Player target, @Parameter(name = "gamemode") GameMode gameMode) {
        String senderName;

        if (sender instanceof Player) {

            Player player = (Player) sender;
            senderName = player.getName();
        } else {

            senderName = CC.DARK_RED + "Console";
        }

        target.setGameMode(gameMode);
        target.updateInventory();
        target.sendMessage(Style.FIRST_COLOR() + "Your game mode has been updated by " + CC.RESET + senderName);
        sender.sendMessage(Style.FIRST_COLOR() + "You updated " + target.getDisplayName() + Style.FIRST_COLOR() + "'s game mode to " + CC.RESET + gameMode.name());

    }

}
