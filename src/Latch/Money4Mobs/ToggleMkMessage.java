package Latch.Money4Mobs;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ToggleMkMessage implements CommandExecutor {
    private static List<MobModel> mobListFromConfig = new ArrayList<MobModel>();
    private static MobConfigManager cfgm;

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        List<Mobs4MoneyPlayer> playerList = Money4Mobs.getPlayerList();
        org.bukkit.entity.Player player = (org.bukkit.entity.Player) commandSender;
        List<MobModel> mm = cfgm.getMobModelFromConfig();
        for (int i = 0; i < playerList.size(); i++){
            if (player.getName().equals(playerList.get(i).getPlayerName())){
                if (args.length == 1) {
                    if(args[0].toLowerCase().equals("on")){
                        playerList.get(i).setKillerMessage(true);
                        player.sendMessage(ChatColor.GREEN + "MobKiller message on");
                    }
                    else if (args[0].toLowerCase().equals("off")){
                        playerList.get(i).setKillerMessage(false);
                        player.sendMessage(ChatColor.GREEN + "MobKiller message off");
                    }
                } else if(args.length == 2) {
                    if(args[0].equals("worth")){
                        for (int j = 0; j < mm.size(); j++){
                            if(args[1].equalsIgnoreCase(mm.get(j).mobName)){
                                String mobName = mm.get(j).mobName;
                                Integer lowWorth = mm.get(j).lowWorth;
                                Integer highWorth = mm.get(j).highWorth;
                                if(lowWorth == highWorth){
                                    player.sendMessage(mobName + "s are worth $" + lowWorth.toString());
                                } else {
                                    player.sendMessage(mobName + "s are worth between $" + lowWorth.toString() + " and $" + highWorth.toString());
                                }
                            }
                        }
                    }
                }
                else {
                    player.sendMessage(ChatColor.RED +("Error: ") + ChatColor.GRAY + ("Please use command like this -> " +ChatColor.DARK_GRAY + "/mk [on/off/worth]"));
                }
            }
        }
        return true;
    }
}
