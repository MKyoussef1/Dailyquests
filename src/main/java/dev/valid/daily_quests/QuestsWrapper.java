package dev.valid.daily_quests;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.quests.Quest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class QuestsWrapper implements CommandExecutor, Listener {
    DailyQuests Api;
    QuestsAPI QApi;
    Utils u = new Utils();
    Inventory Gui;
    HashMap<String,Inventory> ProtectedGuis = new HashMap<>();
    final String PATH = "quests",SAVE = "timers";
    final int index = 40;
    List<Integer> Ids = new ArrayList<>();
    List<Quest> Quests = new ArrayList<>();
    public QuestsWrapper(){
        Api = DailyQuests.getInstance();
        QApi = Api.getQApi();
        ReadQuests();
        LoadQuests();
        Gui = CreateGui();
        ProtectedGuis.put("Global_Gui",Gui);
    }

    private Inventory CreateGui() {
        Inventory Gui = Bukkit.createInventory(null,9*6,u.f("&cDaily Quests"));
        if(Quests.isEmpty())return Gui;
        for (int i = 0; i < Gui.getSize(); i++) {
            if(i >= Quests.size())break;
            Quest quest = Quests.get(i);
            ItemStack is = quest.getQuestItem();
            ItemMeta meta = is.getItemMeta();
            meta.setDisplayName(u.f("&c"+quest.getName()));
            List<String> lore = new ArrayList<>();
            lore.add(u.f("&b Id : &5"+quest.getId()));
            //lore.add(u.f("&cRewards : "+ quest.getOptionValueOrDef()))
            lore.add(u.f("&eDescription: &7"+quest.getDescription()));
            meta.setLore(lore);
            is.setItemMeta(meta);
            Gui.setItem(i,is);
        }

        for (int i = 0; i < Gui.getSize(); i++) {
            if(Gui.getItem(i) == null || Gui.getItem(i).getType() == Material.AIR){Gui.setItem(i,new ItemStack(Material.BLACK_STAINED_GLASS_PANE));}
        }
        return Gui;
    }

    private void ReadQuests(){
        if(!Api.getConfig().getKeys(false).contains(PATH)){Api.getConfig().set(PATH,new ArrayList<>());Api.saveConfig();return;}
        Ids = Api.getConfig().getIntegerList(PATH);
    }

    public void LoadQuests(){
        if(Ids.isEmpty())return;
        for (int id : Ids) {
            try{
                Quest quest =  QApi.getQuestsManager().getQuest(id);
                Quests.add(quest);
                u.b("&a Loaded quest : "+quest.getName());
            }catch (Exception exp){
                Api.getLogger().severe(u.f("&c[Daily Quests]Cannot find quest of id : &6"+id));
                u.b("&c[Daily Quests]Cannot find quest of id : &6"+id);
                exp.printStackTrace();
            }
        }
    }
    public Quest getRandomQuest() {
        if (Quests.isEmpty()) {
            // Handle the case when the list is empty
            return null;
        }

        Random random = new Random();
        int randomIndex = random.nextInt(Quests.size());

        return Quests.get(randomIndex);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        try{
            if(args[0].equalsIgnoreCase("reload")){
                Api.reload();return true;
            }
            if(args[0].equalsIgnoreCase("add") && args.length == 2){
                Ids = Api.getConfig().getIntegerList(PATH);
                int id;
                try{
                    id = Integer.parseInt(args[1]);
                }catch (Exception e){u.Msg(sender,"&cCannot parse : "+args[1]+" into and integer"); return false;}
                if(Ids.contains(id)){
                    u.Msg(sender,"&6This quests is already registered as a daily quest");
                    return false;}
                Ids.add(id);
                Api.getConfig().set(PATH,Ids);
                Api.saveConfig();
                u.Msg(sender,"&eplease reload plugin to apply changes");
                return true;
            }
            if(args[0].equalsIgnoreCase("remove") && args.length == 2){
                Ids = Api.getConfig().getIntegerList(PATH);
                int id;
                try{
                    id = Integer.parseInt(args[1]);
                }catch (Exception e){u.Msg(sender,"&cCannot parse : "+args[1]+" into and integer"); return false;}
                if(!Ids.contains(id)){
                    u.Msg(sender,"&6This quests is not as a daily quest");
                    return false;}
                Ids.remove(id);
                Api.getConfig().set(PATH,Ids);
                Api.saveConfig();
                u.Msg(sender,"&eplease reload plugin to apply changes");
                return true;
            }
            if(args[0].equalsIgnoreCase("reset")){
                if(args[1].equalsIgnoreCase("*")){
                    //todo

                }
                String target = args[1];
                Api.getConfig().set(SAVE+"."+target,0);
                u.Msg(sender,"&aDone.");
                return true;
            }
            if(args[0].equalsIgnoreCase("list") && sender instanceof Player p){
                if(!p.hasPermission("dq.bypass")){
                    u.Msg(sender,"&c You cannot execute that command");
                    return false;
                }
                Inventory PersonalGui;
                if(!ProtectedGuis.containsKey(p.getName())){
                    PersonalGui =  CreateGui();
                    ProtectedGuis.put(p.getName(),PersonalGui);
                    PersonalGui.setItem(index,new ItemStack(Material.RED_STAINED_GLASS_PANE));
                    update(p);
                }
                else{PersonalGui = ProtectedGuis.get(p.getName());}
                p.openInventory(PersonalGui);
                return true;
            }
            if(args[0].equalsIgnoreCase("idlist")){
                if(!sender.hasPermission("dq.bypass")){
                    u.Msg(sender,"&c You cannot execute that command");
                    return false;
                }
                for (Integer id : Ids) {
                    u.Msg(sender,"&6Id :"+id);
                    try{
                        u.Msg(sender,"&c=>Quest name: &a"+QApi.getQuestsManager().getQuest(id).getName());
                }catch (Exception e){u.Msg(sender,"&cNot Found");}
            }
            if(Quests.isEmpty()){u.Msg(sender,"&cNo quests loaded...");}
            return true;
        }}catch (Exception ignore){}
        if(args.length != 0)return false;
        Player p = (Player)sender;
        return StartDailyQuest(p);
    }

    private void update(Player p) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Inventory PGui = ProtectedGuis.get(p.getName());
                if(CanStart(p.getName(),false)){
                    ItemStack is = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                    ItemMeta meta = is.getItemMeta();
                    meta.setDisplayName(u.f("&aGet Random Daily Quest !"));
                    is.setItemMeta(meta);
                    PGui.setItem(index,is);
                }else{
                    ItemStack is = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                    ItemMeta meta = is.getItemMeta();
                    meta.setDisplayName(u.f(getTimeLeft(p.getName())));
                    is.setItemMeta(meta);
                    PGui.setItem(index,is);
                }
            }
        }.runTaskTimer(Api,0,20);
    }

    private boolean StartDailyQuest(Player p) {
        PlayerAccount user = PlayersManager.getPlayerAccount(p);
        boolean proceed = true;
        for (Quest q : QApi.getQuestsManager().getQuestsStarted(user)) {
            if(Quests.contains(q)){
                proceed = false;
                break;
            }
        }
        if(!proceed){
            u.Msg(p,"&cComplete your current quest before starting a new one, please.");
            return false;
        }else{
            /*if everything goes right , the plugin will tell you that u have to wait x amount of hours*/
            Quest rd = getRandomQuest();
            int id = rd.getId();
            String name = p.getName();
            if(CanStart(p.getName(),true)){
                rd.start(p);
                Register(p.getName());
            }
            // Dispatch the command
        }
        return false;
    }

    private void Register(String name) {
        long current = System.currentTimeMillis() / 1000;
        Api.getConfig().set(SAVE+"."+name,current);
        Api.saveConfig();
    }

    private boolean CanStart(String name,boolean announce) {
        try{
            long save = Api.getConfig().getLong(SAVE+"."+name);
            long current = System.currentTimeMillis() / 1000;
            long passed = current - save;
            if(current - save < 1*24*60*60l){
                if(announce)
                Bukkit.getPlayer(name).sendMessage(u.f("&c You have to wait &6"+ formatTime(passed)+ "&c hours") );
                return false;
            }else{return true;}
        }catch (Exception exp){return true;}

    }
    private String getTimeLeft(String name){
        try{
            long save = Api.getConfig().getLong(SAVE+"."+name);
            long current = System.currentTimeMillis() / 1000;
            long passed = current - save;
            if(current - save < 1*24*60*60l){

                return "&6"+ formatTime(passed)+ "&c hours left";
            }else{return "0";}
        }catch (Exception exp){return "0";}
    }
    private String formatTime(long timePassedInSeconds) {
        // Calculate remaining time in seconds
        long remainingTimeInSeconds = 24L * 60L * 60L - timePassedInSeconds;

        // Convert remaining time to hours and minutes
        long hours = remainingTimeInSeconds / 3600L;
        long minutes = (remainingTimeInSeconds % 3600L) / 60L;

        // Format the time as "hh:mm"
        String formattedTime = String.format("%02d:%02d", hours, minutes);

        return formattedTime;
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void ProtectGui(InventoryClickEvent e){
        if(ProtectedGuis.containsValue(e.getInventory()))e.setCancelled(true);
        Inventory inv = e.getClickedInventory();
        Player p = Bukkit.getPlayer(e.getWhoClicked().getName());
        boolean proceed = false;
        try{if(ProtectedGuis.get(p.getName())==inv)proceed=true;}catch (Exception ignore){}
        if(!proceed){
             return;
        }
        if(e.getSlot() != index)return;
        if(CanStart(p.getName(),false)){
            StartDailyQuest(p);
            p.closeInventory();
        }

    }
}
