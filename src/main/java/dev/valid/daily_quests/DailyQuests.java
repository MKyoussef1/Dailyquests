package dev.valid.daily_quests;

import fr.skytasul.quests.api.QuestsAPI;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class DailyQuests extends JavaPlugin {
    private QuestsAPI QApi;
    private Utils u = new Utils();

    public static DailyQuests getInstance() {
        return getPlugin(DailyQuests.class);
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        u.b("&e[DailyQuests] Loading");
        final QuestsWrapper[] wrapper = new QuestsWrapper[1];
        new BukkitRunnable() {
            @Override
            public void run() {
                QApi = QuestsAPI.getAPI();
                wrapper[0] = new QuestsWrapper();
                getCommand("dailyquests").setExecutor(wrapper[0]);
                getCommand("dq").setExecutor(wrapper[0]);
                getServer().getPluginManager().registerEvents(wrapper[0],getInstance());
                u.b("&e[DailyQuests] Loaded "+ wrapper[0].Quests.size()+" quest(s)");
            }
        }.runTaskLater(this,20);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public QuestsAPI getQApi() {
        return QApi;
    }

    public void reload(){
        saveConfig();
        onDisable();
        onEnable();
    }
}
