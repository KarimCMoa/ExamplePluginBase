package me.karim.utilities.task;

import me.karim.CoreExample;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class TaskUtil {

    public TaskUtil() {
    }

    public static void run(Runnable runnable) {
        CoreExample.INSTANCE().getServer().getScheduler().runTask(CoreExample.INSTANCE(), runnable);
    }

    public static void runTimer(Runnable runnable, long delay, long timer) {
        CoreExample.INSTANCE().getServer().getScheduler().runTaskTimer(CoreExample.INSTANCE(), runnable, delay, timer);
    }

    public static void runTimer(BukkitRunnable runnable, long delay, long timer) {
        runnable.runTaskTimer(CoreExample.INSTANCE(), delay, timer);
    }

    public static void runLater(Runnable runnable, long delay) {
        CoreExample.INSTANCE().getServer().getScheduler().runTaskLater(CoreExample.INSTANCE(), runnable, delay);
    }

    public static void runSync(Runnable runnable) {
        if (Bukkit.isPrimaryThread())
            runnable.run();
        else
            Bukkit.getScheduler().runTask(CoreExample.INSTANCE(), runnable);
    }

    public static void runAsync(Runnable runnable) {
        if (Bukkit.isPrimaryThread())
            Bukkit.getScheduler().runTaskAsynchronously(CoreExample.INSTANCE(), runnable);
        else
            runnable.run();
    }
}
