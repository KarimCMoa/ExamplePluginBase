package me.karim;

import lombok.Getter;
import me.karim.chat.ChatManager;
import me.karim.command.CommandHandler;
import me.karim.handlers.ListenerHandler;
import me.karim.task.MenuUpdateTask;
import me.karim.utilities.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class CoreExample extends JavaPlugin {

    private static CoreExample INSTANCE;
    private CoreMode coreMode;

    @Override
    public void onEnable() {
        INSTANCE = this;
        this.setCoreMode(CoreMode.SETUP);
        System.out.println("The core is currently in " + this.coreMode.getName());
        System.out.println("The core is currently in " + this.coreMode.getName());
        System.out.println("The core is currently in " + this.coreMode.getName());
        System.out.println("The core is currently in " + this.coreMode.getName());


        ItemUtil.load();
        CommandHandler.init();

        CommandHandler.loadCommandsFromPackage(this, "me.karim.command.commands");
        ListenerHandler.loadListenersFromPackage(this, "me.karim.menu");

        this.getServer().getScheduler().runTaskTimer(this, new MenuUpdateTask(), 20L, 20L);

        ChatManager chatManager = new ChatManager();

        this.setCoreMode(CoreMode.DEV);
        System.out.println("The core is currently in " + this.coreMode.getName());
        System.out.println("The core is currently in " + this.coreMode.getName());
        System.out.println("The core is currently in " + this.coreMode.getName());
        System.out.println("The core is currently in " + this.coreMode.getName());
    }

    @Override
    public void onDisable() {
        System.out.println("The core switched from the " + this.coreMode.getName() + " to " + CoreMode.SETUP);
        System.out.println("The core switched from the " + this.coreMode.getName() + " to " + CoreMode.SETUP);
        System.out.println("The core switched from the " + this.coreMode.getName() + " to " + CoreMode.SETUP);
        System.out.println("The core switched from the " + this.coreMode.getName() + " to " + CoreMode.SETUP);
        this.setCoreMode(CoreMode.SETUP);

        Bukkit.shutdown();

        this.setCoreMode(CoreMode.DISABLED);
        System.out.println("The core is now " + this.coreMode.getName());
        System.out.println("The core is now " + this.coreMode.getName());
        System.out.println("The core is now " + this.coreMode.getName());
        System.out.println("The core is now " + this.coreMode.getName());

    }

    public static CoreExample INSTANCE() {
        return INSTANCE;
    }

    public void setCoreMode(CoreMode coreMode) {
        this.coreMode = coreMode;
    }

}
