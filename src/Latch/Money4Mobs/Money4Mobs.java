package Latch.Money4Mobs;

import Latch.Money4Mobs.Metrics;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Money4Mobs extends JavaPlugin implements Listener {

    private static final Logger log = Logger.getLogger("Minecraft");
    private static Economy econ = null;
    private static final List<Mobs4MoneyPlayer> playerList = new ArrayList<>();
    private static final List<UserModel> userList = new ArrayList<>();
    private static final SetMobList sml = new SetMobList();
    private static MobConfigManager MobCfgm;
    private static ItemListManager ItemCfgm;
    private static UserManager UserCfgm;
    private static int entityId;

    @Override
    public void onEnable() {
        try {
            loadMobConfigManager();
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadItemConfigManager();
        loadUserConfigManager();
        getServer().getPluginManager().registerEvents(this, this);
        setupEconomy();
        ItemCfgm.createItemsConfig();
        if (!UserManager.usersCfg.getBoolean("users.user-1.showMessage")) {
            UserCfgm.createUsersConfig();
        }
        try {
            MobConfigManager.setMobListFromConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(OfflinePlayer p : getServer().getOfflinePlayers()) {
            userList.add(new UserModel(p.getName(), p.getUniqueId().toString(),true, "English"));
            playerList.add(new Mobs4MoneyPlayer(p.getName(), true ));
        }

        int pluginId = 9484; // <-- Replace with the id of your plugin!
        Metrics metrics = new Metrics(this, pluginId);

        // Optional: Add custom charts
        metrics.addCustomChart(new Metrics.SimplePie("chart_id", () -> "My value"));

        Objects.requireNonNull(this.getCommand("mk")).setExecutor(new MkCommand());
        Objects.requireNonNull(this.getCommand("mk")).setTabCompleter(new MobWorthTabComplete());
        Objects.requireNonNull(this.getCommand("enc")).setExecutor(new EnchantCommand());
        Objects.requireNonNull(this.getCommand("enc")).setTabCompleter(new EnchantTabComplete());

        sml.getMobModel();
    }


    @Override
    public void onDisable() {
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event){
        try {
            MobKiller.setEvent(event);
            callRewardMobKiller(event);
        } catch (RuntimeException | NoClassDefFoundError ignore){
        }
    }

    @EventHandler
    public void onLogin(PlayerJoinEvent event) throws IOException {
        int count = 0;
        for (Mobs4MoneyPlayer mobs4MoneyPlayer : playerList) {
            if (event.getPlayer().getName().equals(mobs4MoneyPlayer.getPlayerName())) {
                count = 1;
            }
        }
        UserModel um = new UserModel(event.getPlayer().getName(), event.getPlayer().getUniqueId().toString(), true,"English");
        UserManager.addUserToList(um);
        if (count == 0){
            playerList.add(new Mobs4MoneyPlayer((event.getPlayer().getName()), true ));
        }
    }

    @EventHandler
    public void onEntitySpawn(CreatureSpawnEvent event) {
        if(!event.getSpawnReason().toString().equals("NATURAL")){
            try {
                MobKiller.getSpawnReason(event);
            } catch (NoClassDefFoundError e) {
                System.out.println(ChatColor.YELLOW + "Warning: " + ChatColor.WHITE + "Couldn't get the spawn reason for the entity killed.");
            }
        }
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent event){
        removePlayerOnLeave(event);
    }

    public void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        econ = rsp.getProvider();
        setEconomy(econ);
    }

    public void setEconomy(Economy value) {
        econ = value;
    }

    public void removePlayerOnLeave(PlayerQuitEvent event){
        Player pa = event.getPlayer();
        for (int i = 0; i < playerList.size(); i++) {
            if (pa.toString().equals(playerList.get(i).getPlayerName())) {
                playerList.remove(i);
            }
        }
    }

    @EventHandler
    public void test(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Wolf) {
            setIsTamedWolf(event.getEntity().getEntityId());
        }
    }

    public int getIsTamedWolf() {
        return entityId;
    }

    public void setIsTamedWolf(int entityId){
        Money4Mobs.entityId = entityId;
    }

    public void callRewardMobKiller(EntityDeathEvent event){
        Player pa = event.getEntity().getKiller();
        Entity e = event.getEntity();
        if (pa != null && pa.hasPermission("m4m.rewardMoney") || pa.isOp() || pa.hasPermission("m4m.rewardmoney")) {
            boolean tamedWolvesGiveMoney = MobConfigManager.mobsCfg.getBoolean("tamedWolvesGiveMoney");
            if (getIsTamedWolf() == 0) {
                MobKiller.rewardPlayerMoney(pa, e, econ);
            }
            else {
                if (!Boolean.FALSE.equals(tamedWolvesGiveMoney)) {
                    MobKiller.rewardPlayerMoney(pa, e, econ);
                }
            }
        }
        setIsTamedWolf(0);
    }

    public static void loadMobConfigManager() throws IOException {
        MobCfgm = new MobConfigManager();
        MobCfgm.setup();
    }

    public static void loadItemConfigManager(){
        ItemCfgm = new ItemListManager();
        ItemCfgm.setup();
    }

    static void loadUserConfigManager() {
        UserCfgm = new UserManager();
        UserCfgm.setup();
    }

    public static List<Mobs4MoneyPlayer> getPlayerList(){
        return playerList;
    }

    public static List<UserModel> getUserList() {
        return userList;
    }

    public static void reloadConfigFiles() throws IOException {
        loadMobConfigManager();
        loadItemConfigManager();
        loadUserConfigManager();
    }
}
