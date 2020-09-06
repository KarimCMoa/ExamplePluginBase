package me.karim.command;

import me.karim.command.param.ParameterData;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

class CommandData {
    private final String[] names;
    private final String permissionNode;
    private final boolean async;
    private final Method method;
    private final boolean consoleAllowed;
    private List<ParameterData> parameters;

    public CommandData(Command commandAnnotation, List<ParameterData> parameters, Method method, boolean consoleAllowed) {
        this.parameters = new ArrayList<>();
        this.names = commandAnnotation.names();
        this.permissionNode = commandAnnotation.permissionNode();
        this.async = commandAnnotation.async();
        this.parameters = parameters;
        this.method = method;
        this.consoleAllowed = consoleAllowed;
    }

    public static String toString(String[] args, int start) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int arg = start; arg < args.length; ++arg) {
            stringBuilder.append(args[arg]).append(" ");
        }
        return stringBuilder.toString().trim();
    }

    public String getName() {
        return this.names[0];
    }

    public boolean canAccess(CommandSender sender) {
        if (!(sender instanceof Player)) {
            return true;
        }
        String permissionNode = this.permissionNode;
        switch (permissionNode) {
            case "": {
                return true;
            }
            default: {
                return sender.hasPermission(this.permissionNode);
            }
        }
    }

    public String getUsageString() {
        return this.getUsageString(this.getName());
    }

    public String getUsageString(String aliasUsed) {
        StringBuilder stringBuilder = new StringBuilder();
        for (ParameterData paramHelp : this.getParameters()) {
            boolean needed = paramHelp.getDefaultValue().isEmpty();
            stringBuilder.append(needed ? "<" : "[").append(paramHelp.getName());
            stringBuilder.append(needed ? ">" : "]").append(" ");
        }
        return "/" + aliasUsed.toLowerCase() + " " + stringBuilder.toString().trim().toLowerCase();
    }

    public void execute(CommandSender sender, String[] params) {
        List<Object> transformedParameters = new ArrayList<>();
        transformedParameters.add(sender);
        for (int parameterIndex = 0; parameterIndex < this.getParameters().size(); ++parameterIndex) {
            ParameterData parameter = this.getParameters().get(parameterIndex);
            String passedParameter = ((parameterIndex < params.length) ? params[parameterIndex] : parameter.getDefaultValue()).trim();
            if (parameterIndex >= params.length && (parameter.getDefaultValue() == null || parameter.getDefaultValue().isEmpty())) {
                sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsageString());
                return;
            }
            if (parameter.isWildcard() && !passedParameter.trim().equals(parameter.getDefaultValue().trim())) {
                passedParameter = toString(params, parameterIndex);
            }
            Object result = CommandHandler.transformParameter(sender, passedParameter, parameter.getParameterClass());
            if (result == null) {
                return;
            }
            transformedParameters.add(result);
            if (parameter.isWildcard()) {
                break;
            }
        }
        try {
            this.method.invoke(null, transformedParameters.toArray());
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "#C-004" + ChatColor.GRAY + " | " + ChatColor.RED + "It appears there was some issues processing your command...");
            e.printStackTrace();
        }
    }

    public String[] getNames() {
        return this.names;
    }

    public String getPermissionNode() {
        return this.permissionNode;
    }

    public boolean isAsync() {
        return this.async;
    }

    public List<ParameterData> getParameters() {
        return this.parameters;
    }

    public Method getMethod() {
        return this.method;
    }

    public boolean isConsoleAllowed() {
        return this.consoleAllowed;
    }
}