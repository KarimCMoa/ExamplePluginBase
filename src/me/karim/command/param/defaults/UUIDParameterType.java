package me.karim.command.param.defaults;

import me.karim.CoreExample;
import me.karim.command.param.ParameterType;
import me.karim.utilities.design.CC;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class UUIDParameterType implements ParameterType<UUID> {

    public UUID transform(CommandSender sender, String source) {
        if (sender instanceof Player && (source.equalsIgnoreCase("self") || source.equals(""))) {
            return (((Player) sender).getUniqueId());
        }

        try {
            return UUID.fromString(source);
        } catch (Exception e) {
            sender.sendMessage(CC.RED + "That UUID could not be parsed.");
        }

        return null;
    }

    public List<String> tabComplete(Player sender, Set<String> flags, String source) {
        List<String> completions = new ArrayList<>();

        for (Player player : CoreExample.INSTANCE().getServer().getOnlinePlayers()) {
            if (StringUtils.startsWithIgnoreCase(player.getName(), source)) {
                completions.add(player.getName());
            }
        }

        return (completions);
    }

}