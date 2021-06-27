package Latch.Money4Mobs;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

import Latch.Money4Mobs.Managers.MessagesConfigManager;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.apache.logging.log4j.core.util.SystemNanoClock;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public abstract class MobKiller implements CommandExecutor {

    private static final List<MobModel> mm = MobConfigManager.getMobModelFromConfig();
    private static EntityDeathEvent ede;
    private static final Random rand = new Random();
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private static double money = 0;
    private static final List<MobSpawnedReason> msr = new ArrayList<>();
    private static Boolean giveMoney = false;
    private static String language = "";
    private static Boolean showMessage = true;
    private static final Random r = new Random();
    private static double percentageLost = 0;

    public static void rewardPlayerMoney(CommandSender pa, Entity e, Economy econ) throws IOException {
        setLanguage(pa);
        giveMoneyCheck(pa,e, econ);
        Player player = null;
        if (pa instanceof Player){
            player = (Player) pa;
        }
        assert player != null;
        boolean samePlayer = player.getUniqueId().toString().equals(e.getUniqueId().toString());
        if (Boolean.TRUE.equals(samePlayer)) {
            giveMoney = false;
        }
        if (Boolean.TRUE.equals(giveMoney)){
            setRange(e, pa);
            setDefaultDrops();
            setCustomDrops(e, pa);
            displayKillMessage(pa);
            sendKillMessage(pa, econ);
        }
    }

    public static void setEvent(EntityDeathEvent e) {
        ede = e;
    }

    public static void setLanguage(CommandSender pa){
        int counter = 1;
        for(String users : UserManager.usersCfg.getConfigurationSection("users").getKeys(false)) {
            String userId = UserManager.usersCfg.getString("users.user-" + counter + ".userId");
            assert userId != null;
            if (pa instanceof Player){
                Player player = (Player) pa;
                if(userId.equalsIgnoreCase(player.getUniqueId().toString())){
                    language = UserManager.usersCfg.getString("users.user-" + counter + ".language");
                }
            }
            counter++;
        }
    }

    public static void displayKillMessage(CommandSender pa){
        int counter = 1;
        for(String users : UserManager.usersCfg.getConfigurationSection("users").getKeys(false)) {
            String userId = UserManager.usersCfg.getString("users.user-" + counter + ".userId");
            assert userId != null;
            if (pa instanceof Player) {
                Player player = (Player) pa;
                if (userId.equalsIgnoreCase(player.getUniqueId().toString())) {
                    showMessage = UserManager.usersCfg.getBoolean("users.user-" + counter + ".showMessage");
                }
            }
            counter++;
        }
    }

    public static void sendKillMessage(CommandSender pa, Economy econ){
        EconomyResponse r = null;
        Player player = null;
        if (pa instanceof Player) {
            player = (Player) pa;
        }
        int counter = 1;

        for(String users : UserManager.usersCfg.getConfigurationSection("users").getKeys(false)) {
            String userId = UserManager.usersCfg.getString("users.user-" + counter + ".userId");
            assert userId != null;
            assert player != null;
            if (player.getUniqueId().toString().equals(userId)) {
                showMessage = UserManager.usersCfg.getBoolean("users.user-" + counter + ".showMessage");
                language = UserManager.usersCfg.getString("users.user-" + counter + ".language");
                if (Boolean.TRUE.equals(showMessage)) {
                    if (money != 0) {
                        Double balance = econ.getBalance(player);
                        df.format(balance);
                        if (Double.compare(money, 0.0) > 0.0) {
                            econ.depositPlayer(player, money);
                            balance = econ.getBalance(player);
                            String moneyRewardedMessage = MessagesConfigManager.messagesCfg.getString("language." + language + ".moneyRewardedMessage" + ".message");
                            String moneyRewardedMessageLocation = MessagesConfigManager.messagesCfg.getString("language." + language + ".moneyRewardedMessage" + ".location");
                            assert moneyRewardedMessage != null;
                            MkCommand.convertMessage(moneyRewardedMessage, pa, null, null, null, Math.round(money * 100.0) / 100.0, null, null, Math.round(balance * 100.0) / 100.0, moneyRewardedMessageLocation);
                        } else {
                            econ.withdrawPlayer(player, Math.abs(money));
                            String moneySubtractedMessage = MessagesConfigManager.messagesCfg.getString("language." + language + ".moneySubtractedMessage" + ".message");
                            String moneySubtractedMessageLocation = MessagesConfigManager.messagesCfg.getString("language." + language + ".moneySubtractedMessage" + ".location");
                            assert moneySubtractedMessage != null;
                            MkCommand.convertMessage(moneySubtractedMessage, pa, null, null, null, Math.round(r.amount * 100.0) / 100.0, null, null, Math.round(balance * 100.0) / 100.0, moneySubtractedMessageLocation);

                        }

                    }
                }
            }
            counter++;
        }

    }

    public static void setCustomDrops(Entity e, CommandSender p){
        getLootingLevel();
        int randomNumber = rand.nextInt(100);
        for(String mobObject : MobConfigManager.mobsCfg.getConfigurationSection("mobs").getKeys(false)) {
            if (mobObject.equalsIgnoreCase(e.getName())) {
                String mobName = mobObject.substring(0, 1).toUpperCase() + mobObject.substring(1);
                boolean customDrops = MobConfigManager.mobsCfg.getBoolean("mobs." + mobName + ".customDrops");
                if (Boolean.TRUE.equals(customDrops)){
                    int numberOfDrops = 0;
                    for(String drop : MobConfigManager.mobsCfg.getConfigurationSection("mobs." + mobName + ".drops").getKeys(false)) {
                        numberOfDrops++;
                    }
                    int counter = 1;
                    for (int l = 0; l < numberOfDrops; l++) {
                        String itemName = MobConfigManager.mobsCfg.getString("mobs." + mobName + ".drops.item-" + counter + ".name");
                        int amount = MobConfigManager.mobsCfg.getInt("mobs." + mobName + ".drops.item-" + counter + ".amount");
                        double chance = MobConfigManager.mobsCfg.getDouble("mobs." + mobName + ".drops.item-" + counter + ".chance");
                        counter++;
                        if (chance == 0){
                            chance = 100;
                        }
                        if (randomNumber <= chance) {
                            Material m = Material.valueOf(itemName);
                            ede.getDrops().add(new ItemStack(m, amount));
                        }
                    }
                }
            }
        }
    }

    public static void setDefaultDrops() {
        for(String mobObject : MobConfigManager.mobsCfg.getConfigurationSection("mobs").getKeys(false)) {
            if (mobObject.equalsIgnoreCase(ede.getEntity().getName())) {
                boolean defaultDrops = MobConfigManager.mobsCfg.getBoolean("mobs." + mobObject + ".keepDefaultDrops/");
                if (!Boolean.TRUE.equals(defaultDrops)) {
                     ede.getDrops().clear();
                }
            }
        }
    }

    public static void getSpawnReason(CreatureSpawnEvent e) {
        msr.add(new MobSpawnedReason(e.getSpawnReason().toString(), e.getEntity().getUniqueId().toString()));
    }

    public static void getLootingLevel(){
        Map<Enchantment, Integer> s = Objects.requireNonNull(ede.getEntity().getKiller()).getInventory().getItemInMainHand().getEnchantments();
        for (Map.Entry<Enchantment,Integer> entry : s.entrySet()){
            if (entry.getKey().toString().contains("looting")){
                Integer lootingLevel = entry.getValue();
            }
        }
    }

    public static void giveMoneyCheck(CommandSender pa, Entity e, Economy econ){
        int counter = 0;
        Player predator = null;
        if (pa instanceof Player) {
            predator = (Player) pa;
        }
        assert predator != null;
        String killerIP = predator.getAddress().getAddress().toString();
        if (pa.hasPermission("m4m.rewardMoney") || pa.isOp() || pa.hasPermission("m4m.rewardmoney")) {
            for (MobSpawnedReason mobSpawnedReason : msr) {
                if (mobSpawnedReason.getUuid().equals(e.getUniqueId().toString())) {
                    counter = 1;
                    if (mobSpawnedReason.getMobSpawnReason().equalsIgnoreCase("SPAWNER_EGG")) {
                        Boolean spawnEggs = MobConfigManager.mobsCfg.getBoolean("spawneggs");
                        if (Boolean.TRUE.equals(spawnEggs)) {
                            giveMoney = true;
                        } else {
                            giveMoney = false;
                        }
                    } else if (mobSpawnedReason.getMobSpawnReason().equalsIgnoreCase("SPAWNER")) {
                        Boolean spawners = MobConfigManager.mobsCfg.getBoolean("spawners");
                        giveMoney = Boolean.TRUE.equals(spawners);
                    }
                }
            }
            if(counter == 0){
                giveMoney = true;
            }
            List<Mobs4MoneyPlayer> pl = Money4Mobs.getPlayerList();
            for (int i = 0; i < Money4Mobs.getPlayerList().size(); i++){
                if (e.getName().equalsIgnoreCase(pl.get(i).playerName)) {
                    giveMoney = false;
                }
            }

            MobConfigManager.mobsCfg.getBoolean("mobs.Player.ipBan");
            if(e instanceof Player) {
                giveMoney = true;
                if (!pa.hasPermission("m4m.bypass.ipban")) {
                    if(MobConfigManager.mobsCfg.getBoolean("mobs.Player.ipBanFarming")) {
                        String entityIP = ((Player) e).getAddress().getAddress().toString();
                        if (killerIP.equals(entityIP)) {
                            giveMoney = false;
                        }
                    }
                } else {
                    if(MobConfigManager.mobsCfg.getBoolean("mobs.Player.enablePercentageDrop")){
                        giveMoney = false;
                        percentageLost = MobConfigManager.mobsCfg.getDouble("mobs.Player.percentageDropAmount");
                        Player prey = ((Player) e).getPlayer();
                        double preyBalance = econ.getBalance(prey);
                        double amountToSubtract = (preyBalance / 100) * percentageLost;
                        econ.withdrawPlayer(prey, amountToSubtract);
                        econ.depositPlayer(predator, amountToSubtract);
                        String playerKilledPlayerMessage = MessagesConfigManager.messagesCfg.getString("language." + language + ".playerKilledPlayerMessage" + ".message");
                        String playerKilledPlayerMessageLocation = MessagesConfigManager.messagesCfg.getString("language." + language + ".playerKilledPlayerMessage" + ".location");
                        assert playerKilledPlayerMessage != null;
                        MkCommand.convertMessage(playerKilledPlayerMessage, predator, null, null, null, Math.round(amountToSubtract * 100.0) / 100.0, null, null, Math.round(econ.getBalance(predator) * 100.0) / 100.0, playerKilledPlayerMessageLocation);
                        String playerKilledByPlayerMessage = MessagesConfigManager.messagesCfg.getString("language." + language + ".playerKilledByPlayerMessage" + ".message");
                        String playerKilledByPlayerMessageLocation = MessagesConfigManager.messagesCfg.getString("language." + language + ".playerKilledByPlayerMessage" + ".location");
                        assert playerKilledByPlayerMessage != null;
                        MkCommand.convertMessage(playerKilledByPlayerMessage, prey, null, null, null, Math.round(amountToSubtract * 100.0) / 100.0, null, null, Math.round(econ.getBalance(prey) * 100.0) / 100.0, playerKilledByPlayerMessageLocation);
                    }
                }
            }

        } else {
            giveMoney = false;
        }
    }

    public static void setRange(Entity e, CommandSender pa) throws IOException {
        double levelMultiplier = 1;
        List<String> levelList = new ArrayList<>();
        Money4Mobs.loadMobConfigManager();
        levelList.addAll(MobConfigManager.mobsCfg.getConfigurationSection("group-multiplier").getKeys(false));
        for (String level : levelList) {
            if ( pa.hasPermission("m4m.multiplier." + level)){
                levelMultiplier = MobConfigManager.mobsCfg.getDouble("group-multiplier."+level);
            }
        }
        double operator = MobConfigManager.mobsCfg.getDouble("group-multiplier.operator");

        if (pa.isOp()){
            levelMultiplier = operator;
        }
        for(String mobObject : MobConfigManager.mobsCfg.getConfigurationSection("mobs").getKeys(false)) {
            if (mobObject.equalsIgnoreCase(e.getName())){
                double lowWorth = MobConfigManager.mobsCfg.getDouble("mobs." + mobObject + ".worth.low");
                double highWorth = MobConfigManager.mobsCfg.getDouble("mobs." + mobObject + ".worth.high");
                if (lowWorth == highWorth){
                    money = lowWorth;
                } else {
                    money = lowWorth + (highWorth - lowWorth) * r.nextDouble();
                    money = money * levelMultiplier;
                    money = Math.round(money * 100.0) / 100.0;
                }
            } else if (e instanceof Player) {
                double lowWorth = MobConfigManager.mobsCfg.getDouble("mobs.Player.worth.low");
                double highWorth = MobConfigManager.mobsCfg.getDouble("mobs.Player.worth.high");
                if (lowWorth == highWorth){
                    money = lowWorth;
                } else {
                    money = lowWorth + (highWorth - lowWorth) * r.nextDouble();
                    money = money * levelMultiplier;
                    money = Math.round(money * 100.0) / 100.0;
                }
            }
        }
        isRidingCheck(pa);
    }

    private static void isRidingCheck(CommandSender pa) {
        double ridingHorseMultiplier = MobConfigManager.mobsCfg.getDouble("actions-multipliers.riding-horse.multiplier");
        Boolean isRidingHorseActive = MobConfigManager.mobsCfg.getBoolean("actions-multipliers.riding-horse.isActive");
        double ridingMuleMultiplier = MobConfigManager.mobsCfg.getDouble("actions-multipliers.riding-mule.multiplier");
        Boolean isRidingMuleActive = MobConfigManager.mobsCfg.getBoolean("actions-multipliers.riding-mule.isActive");
        double ridingDonkeyMultiplier = MobConfigManager.mobsCfg.getDouble("actions-multipliers.riding-donkey.multiplier");
        Boolean isRidingDonkeyActive = MobConfigManager.mobsCfg.getBoolean("actions-multipliers.riding-donkey.isActive");
        double ridingStriderMultiplier = MobConfigManager.mobsCfg.getDouble("actions-multipliers.riding-strider.multiplier");
        Boolean isRidingStriderActive = MobConfigManager.mobsCfg.getBoolean("actions-multipliers.riding-strider.isActive");
        double ridingPigMultiplier = MobConfigManager.mobsCfg.getDouble("actions-multipliers.riding-pig.multiplier");
        Boolean isRidingPigActive = MobConfigManager.mobsCfg.getBoolean("actions-multipliers.riding-pig.isActive");

        if (pa instanceof Player){
            Player player = (Player) pa;
            if (player.getVehicle() != null) {
                isRidingMob(ridingHorseMultiplier, isRidingHorseActive, player, "Horse");
                isRidingMob(ridingMuleMultiplier, isRidingMuleActive, player, "Mule");
                isRidingMob(ridingDonkeyMultiplier, isRidingDonkeyActive, player, "Donkey");
                isRidingMob(ridingStriderMultiplier, isRidingStriderActive, player, "Strider");
                isRidingMob(ridingPigMultiplier, isRidingPigActive, player, "Pig");
                money = Math.round(money * 100.0) / 100.0;
            }
        }
    }

    private static void isRidingMob(double ridingHorseMultiplier, Boolean isRidingHorseActive, Player player, String horse) {
        if (Objects.requireNonNull(player.getVehicle()).getName().equalsIgnoreCase(horse) && Boolean.TRUE.equals(isRidingHorseActive)) {
            money = money * ridingHorseMultiplier;
        }
    }


}
