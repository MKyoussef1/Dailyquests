package dev.valid.daily_quests;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.logging.Level;

public class Utils {
    public Utils(){}
    public String f(String input){
        return ChatColor.translateAlternateColorCodes('&',input);
    }
    public void Msg(CommandSender target, String input){
        //Player p =null;p= Bukkit.getPlayer(target);
        if(target!=null)target.sendMessage(f(input));
    }
    public void Terminal(String input){
        DailyQuests.getInstance().getLogger().log(Level.WARNING,f(input));
    }
    public void b(String input){Bukkit.broadcastMessage(f(input));}
    public Player p(String name){
        return Bukkit.getPlayer(name);
    }
    public Player p(UUID uuid){
        return Bukkit.getPlayer(uuid);
    }
}
