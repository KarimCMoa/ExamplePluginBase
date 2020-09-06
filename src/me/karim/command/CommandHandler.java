package me.karim.command;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import me.karim.CoreExample;
import me.karim.CoreMode;
import me.karim.player.PlayerInfo;
import me.karim.command.param.Parameter;
import me.karim.command.param.ParameterData;
import me.karim.command.param.ParameterType;
import me.karim.command.param.defaults.*;
import me.karim.utilities.ClassUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class CommandHandler implements Listener {

    @Getter
    private static final List<CommandData> commands = new ArrayList<>();
    private static final Map<Class<?>, ParameterType> parameterTypes = new HashMap<>();
    private static boolean initiated = false;

    // Static class -- cannot be created.
    private CommandHandler() {
    }

    /**
     * Initiates the command handler. This can only be called once, and is called automatically when Core enables.
     */
    public static void init() {
        // Only allow the CommandHandler to be initiated once.
        // Note the '!' in the .checkState call.
        Preconditions.checkState(!initiated);
        initiated = true;

        CoreExample.INSTANCE().getServer().getPluginManager().registerEvents(new CommandHandler(), CoreExample.INSTANCE());

        // Run this on a delay so everything is registered.
        // Not really needed, but it's nice to play it safe.
        new BukkitRunnable() {
            public void run() {
                try {
                    // Command map field (we have to use reflection to get this)
                    Field commandMapField = CoreExample.INSTANCE().getServer().getClass().getDeclaredField("commandMap");
                    commandMapField.setAccessible(true);

                    Object oldCommandMap = commandMapField.get(CoreExample.INSTANCE().getServer());
                    CommandMap newCommandMap = new CommandMap(CoreExample.INSTANCE().getServer());

                    // Start copying the knownCommands field over
                    // (so any commands registered before we hook in are kept)
                    Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
                    knownCommandsField.setAccessible(true);

                    // The knownCommands field is,
                    // so to be able to set it in the new command map we have to remove it.
                    Field modifiersField = Field.class.getDeclaredField("modifiers");
                    modifiersField.setAccessible(true);
                    modifiersField.setInt(knownCommandsField, knownCommandsField.getModifiers() & ~Modifier.FINAL);

                    knownCommandsField.set(newCommandMap, knownCommandsField.get(oldCommandMap));
                    // End copying the knownCommands field over

                    commandMapField.set(CoreExample.INSTANCE().getServer(), newCommandMap);
                } catch (Exception e) {
                    // Shouldn't happen, so we can just
                    // printout the exception (and do nothing else)
                    e.printStackTrace();
                }
            }
        }.runTaskLater(CoreExample.INSTANCE(), 5L);

        // Register our default parameter types.
        // boolean.class is the same as Boolean.TYPE,
        // however using .class improves readability.
        registerParameterType(UUID.class, new UUIDParameterType());
        registerParameterType(boolean.class, new BooleanParameterType());
        registerParameterType(float.class, new FloatParameterType());
        registerParameterType(double.class, new DoubleParameterType());
        registerParameterType(long.class, new LongParameterType());
        registerParameterType(int.class, new IntegerParameterType());
        registerParameterType(Player.class, new PlayerParameterType());
        registerParameterType(World.class, new WorldParameterType());
        registerParameterType(ItemStack.class, new ItemStackParameterType());
        registerParameterType(PlayerInfo.class, new PlayerInfoParameterType());
        registerParameterType(GameMode.class, new GameModeParameterType());
    }

    public static void loadCommandsFromPackage(Plugin plugin, String packageName) {
        for (Class<?> clazz : ClassUtil.getClassesInPackage(plugin, packageName)) {
            registerClass(clazz);
        }
    }

    /**
     * Register a custom parameter adapter.
     *
     * @param transforms    The class this parameter type will return (IE KOTH.class, Player.class, etc.)
     * @param parameterType The ParameterType object which will perform the transformation.
     */
    public static void registerParameterType(Class<?> transforms, ParameterType parameterType) {
        parameterTypes.put(transforms, parameterType);
    }

    /**
     * Registers a single class with the command handler.
     *
     * @param registeredClass The class to scan/register.
     */
    protected static void registerClass(Class<?> registeredClass) {
        for (Method method : registeredClass.getMethods()) {
            if (method.getAnnotation(Command.class) != null) {
                registerMethod(method);
            }
        }
    }

    /**
     * Registers a single method with the command handler.
     *
     * @param method The method to register (if applicable)
     */
    protected static void registerMethod(Method method) {
        Command commandAnnotation = method.getAnnotation(Command.class);
        List<ParameterData> parameterData = new ArrayList<>();

        // Offset of 1 here for the sender parameter.
        for (int parameterIndex = 1; parameterIndex < method.getParameterTypes().length; parameterIndex++) {
            Parameter parameterAnnotation = null;

            for (Annotation annotation : method.getParameterAnnotations()[parameterIndex]) {
                if (annotation instanceof Parameter) {
                    parameterAnnotation = (Parameter) annotation;
                    break;
                }
            }

            if (parameterAnnotation != null) {
                parameterData.add(new ParameterData(parameterAnnotation, method.getParameterTypes()[parameterIndex]));
            } else {
                CoreExample.INSTANCE().getLogger()
                        .warning("Method '" + method.getName() + "' has a parameter without a @Parameter annotation.");
                return;
            }
        }

        commands.add(new CommandData(commandAnnotation, parameterData, method,
                method.getParameterTypes()[0].isAssignableFrom(Player.class)
        ));

        Collections.sort(commands, new Comparator<CommandData>() {
            @Override
            public int compare(CommandData o1, CommandData o2) {
                return (o2.getName().length() - o1.getName().length());
            }
        });
    }

    /**
     * @return the full command line input of a player before running or tab completing a Core command
     */
    public static String[] getParameters(Player player) {
        return CommandMap.parameters.get(player.getUniqueId());
    }

    /**
     * Process a command (permission checks, argument validation, etc.)
     *
     * @param sender  The CommandSender executing this command. It should be noted that any non-player sender is treated
     *                with full permissions.
     * @param command The command to process (without a prepended '/')
     * @return The Command executed
     */
    public static CommandData evalCommand(CommandSender sender, String command) {
        String[] args = new String[]{};
        CommandData found = null;

        CommandLoop:
        for (CommandData commandData : commands) {
            for (String alias : commandData.getNames()) {
                String messageString = command.toLowerCase() + " ";
                String aliasString = alias.toLowerCase() + " ";

                if (messageString.startsWith(aliasString)) {
                    found = commandData;

                    if (messageString.length() > aliasString.length()) {
                        if (found.getParameters().size() == 0) {
                            continue;
                        }
                    }

                    // If there's 'space' after the command, parse args.
                    // The +1 is there to account for a space after the command if there's parameters
                    if (command.length() > alias.length() + 1) {
                        // See above as to... why this works.
                        args = (command.substring(alias.length() + 1)).split(" ");
                    }

                    // We break to the command loop as we have 2 for loops here.
                    break CommandLoop;
                }
            }
        }

        if (found == null) {
            return (null);
        }
        // Test
        if (CoreExample.INSTANCE().getCoreMode() == CoreMode.SETUP) {
            sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "#C-001" + ChatColor.GRAY + " | " + ChatColor.RED + "You can't do that because the core is currently in setup mode!");
        }

        if (!(sender instanceof Player) && !found.isConsoleAllowed()) {
            sender.sendMessage(ChatColor.RED + "This command does not support execution from the console.");
            sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "#C-002" + ChatColor.GRAY + " | " + ChatColor.RED + "This command does not support execution from the console.");
            return (found);
        }

        if (!found.canAccess(sender)) {
            sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "#C-003" + ChatColor.GRAY + " | " + ChatColor.RED + "No Permission.");
            return (found);
        }

        if (found.isAsync()) {
            CommandData foundClone = found;
            String[] argsClone = args;

            new BukkitRunnable() {
                public void run() {
                    foundClone.execute(sender, argsClone);
                }
            }.runTaskAsynchronously(CoreExample.INSTANCE());
        } else {
            found.execute(sender, args);
        }

        return (found);
    }

    /**
     * Transforms a parameter.
     *
     * @param sender      The CommandSender executing the command (or whoever we should transform 'for')
     * @param parameter   The String to transform ('' if none)
     * @param transformTo The class we should use to fetch our ParameterType (which we delegate transforming down to)
     * @return The Object that we've transformed the parameter to.
     */
    protected static Object transformParameter(CommandSender sender, String parameter, Class<?> transformTo) {
        // Special-case Strings as they never need transforming.
        if (transformTo.equals(String.class)) {
            return (parameter);
        }

        // This will throw a NullPointerException if there's no registered
        // parameter type, but that's fine -- as that's what we'd do anyway.
        return (parameterTypes.get(transformTo).transform(sender, parameter));
    }

    /**
     * Tab completes a parameter.
     *
     * @param sender           The Player tab completing the command (not CommandSender as tab completion is for players
     *                         only)
     * @param parameter        The last thing the player typed in their style box before hitting tab ('' if none)
     * @param transformTo      The class we should use to fetch our ParameterType (which we delegate tab completing down
     *                         to)
     * @param tabCompleteFlags The list of custom flags to use when tab completing this parameter.
     * @return A List<String> of available tab completions. (empty if none)
     */
    protected static List<String> tabCompleteParameter(Player sender, String parameter, Class<?> transformTo,
                                                       String[] tabCompleteFlags) {
        if (!parameterTypes.containsKey(transformTo)) {
            return (new ArrayList<>());
        }

        return (parameterTypes.get(transformTo).tabComplete(sender, ImmutableSet.copyOf(tabCompleteFlags), parameter));
    }

    /**
     * Executes a command for the given player. Use this instead of Player#performCommand as that method does not call a
     * PlayerCommandPreprocess event.
     *
     * @param sender The player to execute the command for.
     */
    public static void executeCommand(Player sender, String commandLine) {
        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(sender, "/" + commandLine);
        Bukkit.getPluginManager().callEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommandPreProcess(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().substring(1);

        CommandMap.parameters.put(event.getPlayer().getUniqueId(), command.split(" "));

        if (evalCommand(event.getPlayer(), command) != null) {
            event.setCancelled(true);
        }
    }

}