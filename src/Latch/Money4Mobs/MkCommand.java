package Latch.Money4Mobs;

import Latch.Money4Mobs.Managers.ConfigFileManager;
import Latch.Money4Mobs.Managers.MessagesConfigManager;
import Latch.Money4Mobs.Managers.MobConfigManager;
import Latch.Money4Mobs.Managers.UserManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.apache.commons.codec.language.bm.Lang;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MkCommand implements CommandExecutor {
    private static final FileConfiguration mobsCfg = MobConfigManager.mobsCfg;
    private static final File mobsFile = MobConfigManager.mobsFile;
    private static final FileConfiguration configCfg = ConfigFileManager.configCfg;
    private static final File configFile = ConfigFileManager.configFile;
    private static final File userFile = UserManager.usersFile;
    private static final Material[] materials = Material.values();
    private static List<UserModel> um = UserManager.updateUsersOnReload();
    private static List<MobModel> mm = MobConfigManager.getMobModelFromConfig();
    private static Boolean showMessage = true;
    private static final Money4Mobs plugin = Money4Mobs.getPlugin(Money4Mobs.class);

    // Config file paths Constants
    private static final String SHOW_MESSAGE = ".showMessage";
    private static final String USERS_USER = "users.user-";
    private static final String ACCESS_DENIED_MESSAGE = ".accessDeniedMessage";
    private static final String SPAWN_EGGS = "spawneggs";
    private static final String SPAWNERS = "spawners";
    private static final String TAMED_WOLVES_GIVE_MONEY = "tamedWolvesGiveMoney";
    private static final String OVERRIDE_KILL_MESSAGE = "customMessageOption.overrideKillMessage";
    private static final String ADD_CUSTOM_DROP_INVALID_ERROR_MESSAGE = ".addCustomDropInvalidMobErrorMessage";
    private static final String MOBS = "mobs.";
    private static final String DROPS_ITEMS = ".drops.item-";
    private static final String CUSTOM_DROPS = ".customDrops";
    private static final String KEEP_DEFAULT_DROPS = ".keepDefaultDrops";
    private static final String LANGUAGE = "language.";
    private static final String LOCATION = ".location";
    private static final String MESSAGE = ".message";
    private static final String DEFAULT_LANGUAGE = configCfg.getString("defaultLanguage").toLowerCase();
    private static int userNumber;
    Logger logger = Logger.getLogger(MkCommand.class.getName());

    // Language Constants
    private static final String ENGLISH = "English";

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        String language = null;
        Player player2 = null;
        player2 = getPlayer(commandSender, null);
        List<MobModel> mm = MobConfigManager.getMobModelFromConfig();
        int firstCounter = 1;
        for(String firstUsers : UserManager.usersCfg.getConfigurationSection("users").getKeys(false)) {
            firstCounter++;
        }
        ArrayList<String> testList = new ArrayList<>();
        for (String items : MessagesConfigManager.messagesCfg.getConfigurationSection("language").getKeys(false)){
            testList.add(items);
        }
        if (player2 == null){
            firstCounter = 1;
        }

        for(int i = 1; i < firstCounter+1; i++) {
            String firstUserId = UserManager.usersCfg.getString(USERS_USER + i + ".userId");
            assert firstUserId != null;
            if (commandSender instanceof ConsoleCommandSender || player2.getUniqueId().toString().equals(firstUserId)) {
                if (player2 != null) {
                    if (firstUserId.equalsIgnoreCase(player2.getUniqueId().toString())) {
                        showMessage = UserManager.usersCfg.getBoolean(USERS_USER + i + SHOW_MESSAGE);
                        language = UserManager.usersCfg.getString(USERS_USER + i + ".language");
                        userNumber = i;
                    }
                }
            }
        }
        if (!(player2 instanceof Player)){
            language = DEFAULT_LANGUAGE;
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("toggleKM")) {
                if (player2 != null && (player2.hasPermission("m4m.command.mk.toggleKM") || commandSender.isOp())) {
                    if (language == null){
                        language = ENGLISH;
                    }
                    if (Boolean.TRUE.equals(showMessage)) {
                        String mobKillerOffMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language +  ".mobKillerOffMessage" + MESSAGE);
                        String mobKillerOffMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language +  ".mobKillerOffMessage" + LOCATION);
                        assert mobKillerOffMessage != null;
                        convertMessage(mobKillerOffMessage, commandSender, null, null, null, null, null, null, null, mobKillerOffMessageLocation);
                        UserManager.usersCfg.set(USERS_USER + userNumber + SHOW_MESSAGE, false);
                        try {
                            UserManager.usersCfg.save(userFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        String mobKillerOnMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".mobKillerOnMessage" + MESSAGE);
                        String mobKillerOnMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".mobKillerOnMessage" + LOCATION);
                        assert mobKillerOnMessage != null;
                        convertMessage(mobKillerOnMessage, commandSender, null, null, null, null, null, null, null, mobKillerOnMessageLocation);
                        UserManager.usersCfg.set(USERS_USER + userNumber + SHOW_MESSAGE, true);
                        try {
                            UserManager.usersCfg.save(userFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (player2 instanceof Player) {
                        String accessDeniedMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ACCESS_DENIED_MESSAGE + MESSAGE);
                        String accessDeniedMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ACCESS_DENIED_MESSAGE + LOCATION);
                        assert accessDeniedMessage != null;
                        convertMessage(accessDeniedMessage, commandSender, null, null, null, null, null, null, null, accessDeniedMessageLocation);
                    }
                }
            } else if (args[0].equalsIgnoreCase("toggleMoneyFromSpawnEggs")) {
                if (commandSender.hasPermission("m4m.command.mk.toggleMoneyFromSpawnEggs") || commandSender.isOp()) {
                    boolean spawnEgg = configCfg.getBoolean(SPAWN_EGGS);
                    if (Boolean.TRUE.equals(spawnEgg)) {
                        configCfg.set(SPAWN_EGGS, false);
                        String eggSpawnRewardFalseMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".eggSpawnRewardFalseMessage" + MESSAGE);
                        String eggSpawnRewardFalseMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".eggSpawnRewardFalseMessage" + LOCATION);
                        assert eggSpawnRewardFalseMessage != null;
                        convertMessage(eggSpawnRewardFalseMessage, commandSender, null, null, null, null, null, null, null, eggSpawnRewardFalseMessageLocation);
                    } else {
                        configCfg.set(SPAWN_EGGS, true);
                        String eggSpawnRewardTrueMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".eggSpawnRewardTrueMessage" + MESSAGE);
                        String eggSpawnRewardTrueMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".eggSpawnRewardTrueMessage" + LOCATION);
                        assert eggSpawnRewardTrueMessage != null;
                        convertMessage(eggSpawnRewardTrueMessage, commandSender, null, null, null, null, null, null, null, eggSpawnRewardTrueMessageLocation);
                    }
                    try {
                        configCfg.save(configFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    String accessDeniedMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ACCESS_DENIED_MESSAGE + MESSAGE);
                    String accessDeniedMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ACCESS_DENIED_MESSAGE + LOCATION);
                    assert accessDeniedMessage != null;
                    convertMessage(accessDeniedMessage, commandSender, null, null, null, null, null, null, null, accessDeniedMessageLocation);
                }
            } else if (args[0].equalsIgnoreCase("toggleMoneyFromSpawners")) {
                if (commandSender.hasPermission("m4m.command.mk.toggleMoneyFromSpawners") || commandSender.isOp()) {
                    boolean spawners = configCfg.getBoolean(SPAWNERS);
                    if (Boolean.TRUE.equals(spawners)) {
                        configCfg.set(SPAWNERS, false);
                        String spawnerSpawnRewardFalseMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".spawnerSpawnRewardFalseMessage" + MESSAGE);
                        String spawnerSpawnRewardFalseMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".spawnerSpawnRewardFalseMessage" + LOCATION);
                        assert spawnerSpawnRewardFalseMessage != null;
                        convertMessage(spawnerSpawnRewardFalseMessage, commandSender, null, null, null, null, null, null, null, spawnerSpawnRewardFalseMessageLocation);
                    } else {
                        configCfg.set(SPAWNERS, true);
                        String spawnerSpawnRewardTrueMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".spawnerSpawnRewardTrueMessage" + MESSAGE);
                        String spawnerSpawnRewardTrueMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".spawnerSpawnRewardTrueMessage" + LOCATION);
                        assert spawnerSpawnRewardTrueMessage != null;
                        convertMessage(spawnerSpawnRewardTrueMessage, commandSender, null, null, null, null, null, null, null, spawnerSpawnRewardTrueMessageLocation);
                    }
                    try {
                        configCfg.save(configFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    String accessDeniedMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ACCESS_DENIED_MESSAGE + MESSAGE);
                    String accessDeniedMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ACCESS_DENIED_MESSAGE + LOCATION);
                    assert accessDeniedMessage != null;
                    convertMessage(accessDeniedMessage, commandSender, null, null, null, null, null, null, null, accessDeniedMessageLocation);
                }
            }
            else if (args[0].equalsIgnoreCase("toggleMoneyFromTamedWolves")) {
                if (commandSender.hasPermission("m4m.command.mk.toggleMoneyFromTamedWolves") || commandSender.isOp()) {
                    boolean tamedWolvesGiveMoney = configCfg.getBoolean(TAMED_WOLVES_GIVE_MONEY);
                    if (Boolean.TRUE.equals(tamedWolvesGiveMoney)) {
                        configCfg.set(TAMED_WOLVES_GIVE_MONEY, false);
                        String tamedWolvesRewardFalseMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".tamedWolvesRewardFalseMessage" + MESSAGE);
                        String tamedWolvesRewardFalseMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".tamedWolvesRewardFalseMessage" + LOCATION);
                        assert tamedWolvesRewardFalseMessage != null;
                        convertMessage(tamedWolvesRewardFalseMessage, commandSender, null, null, null, null, null, null, null, tamedWolvesRewardFalseMessageLocation);
                    } else {
                        configCfg.set(TAMED_WOLVES_GIVE_MONEY, true);
                        String tamedWolvesRewardTrueMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".tamedWolvesRewardTrueMessage" + MESSAGE);
                        String tamedWolvesRewardTrueMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".tamedWolvesRewardTrueMessage" + LOCATION);
                        assert tamedWolvesRewardTrueMessage != null;
                        convertMessage(tamedWolvesRewardTrueMessage, commandSender, null, null, null, null, null, null, null, tamedWolvesRewardTrueMessageLocation);
                    }
                    try {
                        configCfg.save(configFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    String accessDeniedMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ACCESS_DENIED_MESSAGE + MESSAGE);
                    String accessDeniedMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ACCESS_DENIED_MESSAGE + LOCATION);
                    assert accessDeniedMessage != null;
                    convertMessage(accessDeniedMessage, commandSender, null, null, null, null, null, null, null, accessDeniedMessageLocation);
                }
            }
            else if (args[0].equalsIgnoreCase("reload")) {
                if (commandSender.hasPermission("m4m.command.mk.reload") || commandSender.isOp()) {
                    try {
                        Money4Mobs.reloadConfigFiles();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String reloadingMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".reloadingMessage" + MESSAGE);
                    String reloadingMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".reloadingMessage" + LOCATION);
                    assert reloadingMessage != null;
                    logger.log(Level.INFO, reloadingMessage.substring(2));
                    convertMessage(reloadingMessage, commandSender, null, null, null, null, null, null, null, reloadingMessageLocation);
                    mm.clear();
                    try {
                        Money4Mobs.loadMobConfigManager();
                        Money4Mobs.loadUserConfigManager();
                        Money4Mobs.loadLanguageConfigManager();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    um.clear();
                    um = UserManager.updateUsersOnReload();
                    String reloadConfirmMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".reloadConfirmMessage" + MESSAGE);
                    String reloadConfirmMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".reloadConfirmMessage" + LOCATION);
                    assert reloadConfirmMessage != null;
                    logger.log(Level.INFO, reloadConfirmMessage.substring(2));
                    convertMessage(reloadConfirmMessage, commandSender, null, null, null, null, null, null, null, reloadConfirmMessageLocation);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("worth")) {
                if (commandSender.hasPermission("m4m.command.mk.worth") || commandSender.isOp()) {
                    for(String mobObject : MobConfigManager.mobsCfg.getConfigurationSection("mobs").getKeys(false)) {
                        if (mobObject.equalsIgnoreCase(args[1])) {
                            String mobName = mobObject.substring(0, 1).toUpperCase() + mobObject.substring(1);
                            Double lowWorth = MobConfigManager.mobsCfg.getDouble(MOBS + mobName + ".worth.low");
                            Double highWorth = MobConfigManager.mobsCfg.getDouble(MOBS + mobName + ".worth.high");
                            assert language != null;
                            if (lowWorth.equals(highWorth)) {
                                String mobWorthMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".mobWorthMessage" + MESSAGE);
                                String mobWorthMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".mobWorthMessage" + LOCATION);
                                assert mobWorthMessage != null;
                                convertMessage(mobWorthMessage, commandSender, mobName, null, null, null, lowWorth.toString(), null, null, mobWorthMessageLocation);
                            } else {
                                String mobRangeWorthMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".mobRangeWorthMessage" + MESSAGE);
                                String mobRangeWorthMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".mobRangeWorthMessage" + LOCATION);
                                assert mobRangeWorthMessage != null;
                                convertMessage(mobRangeWorthMessage, commandSender, mobName, null, null, null, lowWorth.toString(), highWorth.toString(), null, mobRangeWorthMessageLocation);
                            }
                        }
                    }

                } else {
                    String accessDeniedMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ACCESS_DENIED_MESSAGE + MESSAGE);
                    String accessDeniedMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ACCESS_DENIED_MESSAGE + LOCATION);
                    assert accessDeniedMessage != null;
                    convertMessage(accessDeniedMessage, commandSender, null, null, null, null, null, null, null, accessDeniedMessageLocation);
                }
            } else if (args[0].equalsIgnoreCase("drops")) {
                if (commandSender.hasPermission("m4m.command.mk.drops") || commandSender.isOp()) {
                    boolean error = true;

                    for(String mobObject : MobConfigManager.mobsCfg.getConfigurationSection("mobs").getKeys(false)) {
                        if (mobObject.equalsIgnoreCase(args[1])) {
                            String mobName = mobObject.substring(0, 1).toUpperCase() + mobObject.substring(1);
                            error = false;
                            boolean customDrops = MobConfigManager.mobsCfg.getBoolean(MOBS + mobName + ".customDrops");
                            if (Boolean.TRUE.equals(customDrops)) {
                                int numberOfDrops = 0;
                                try {
                                    for(String drop : MobConfigManager.mobsCfg.getConfigurationSection(MOBS + mobName + ".drops").getKeys(false)) {
                                        numberOfDrops++;
                                    }
                                } catch (NullPointerException ignored) {

                                }
                                if (numberOfDrops == 0) {
                                    String customDropsNotSetMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".customDropsNotSetMessage" + MESSAGE);
                                    String customDropsNotSetMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".customDropsNotSetMessage" + LOCATION);
                                    assert customDropsNotSetMessage != null;
                                    convertMessage(customDropsNotSetMessage, commandSender, mobName, null, null, null, null, null, null, customDropsNotSetMessageLocation);
                                } else {
                                    int counter = 1;
                                    for (int l = 0; l < numberOfDrops; l++) {
                                        String itemName = MobConfigManager.mobsCfg.getString(MOBS + mobName + DROPS_ITEMS + counter + ".name");
                                        Double amount = MobConfigManager.mobsCfg.getDouble(MOBS + mobName + DROPS_ITEMS + counter + ".amount");
                                        double chance = MobConfigManager.mobsCfg.getDouble(MOBS + mobName + DROPS_ITEMS + counter + ".chance");
                                        String mobDropInfoMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".mobDropInfoMessage" + MESSAGE);
                                        String mobDropInfoMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".mobDropInfoMessage" + LOCATION);
                                        assert mobDropInfoMessage != null;
                                        convertMessage(mobDropInfoMessage, commandSender, mobName, itemName, (int) chance, amount, null, null, null, mobDropInfoMessageLocation);
                                        counter++;

                                    }
                                }
                            } else {
                                String customDropsNotEnabledMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".customDropsNotEnabledMessage" + MESSAGE);
                                String customDropsNotEnabledMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".customDropsNotEnabledMessage" + LOCATION);
                                assert customDropsNotEnabledMessage != null;
                                convertMessage(customDropsNotEnabledMessage, commandSender, mobName, null, null, null, null, null, null, customDropsNotEnabledMessageLocation);
                            }
                        }
                    }
                    if (Boolean.TRUE.equals(error)) {
                        String addCustomDropInvalidMobErrorMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ADD_CUSTOM_DROP_INVALID_ERROR_MESSAGE + MESSAGE);
                        String addCustomDropInvalidMobErrorMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ADD_CUSTOM_DROP_INVALID_ERROR_MESSAGE + LOCATION);
                        assert addCustomDropInvalidMobErrorMessage != null;
                        convertMessage(addCustomDropInvalidMobErrorMessage, commandSender, args[1], null, null, null, null, null, null, addCustomDropInvalidMobErrorMessageLocation);
                    }
                } else {
                    String accessDeniedMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ACCESS_DENIED_MESSAGE + MESSAGE);
                    String accessDeniedMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ACCESS_DENIED_MESSAGE + LOCATION);
                    assert accessDeniedMessage != null;
                    convertMessage(accessDeniedMessage, commandSender, null, null, null, null, null, null, null, accessDeniedMessageLocation);
                }
            } else if (args[0].equalsIgnoreCase("toggleCustomDrops")) {
                if (commandSender.hasPermission("m4m.command.mk.toggleCustomDrops") || commandSender.isOp()) {
                    boolean error = true;
                    for(String mobObject : MobConfigManager.mobsCfg.getConfigurationSection("mobs").getKeys(false)) {
                        if (mobObject.equalsIgnoreCase(args[1])) {
                            error = false;
                            String mobName = mobObject.substring(0, 1).toUpperCase() + mobObject.substring(1);
                            boolean customDrops = MobConfigManager.mobsCfg.getBoolean(MOBS + mobName + CUSTOM_DROPS);
                            if (Boolean.TRUE.equals(customDrops)) {
                                MobConfigManager.mobsCfg.set(MOBS + mobName + CUSTOM_DROPS, false);
                                String customDropsFalseMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".customDropsFalseMessage" + MESSAGE);
                                String customDropsFalseMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".customDropsFalseMessage" + LOCATION);
                                assert customDropsFalseMessage != null;
                                convertMessage(customDropsFalseMessage, commandSender, mobName, null, null, null, null, null, null, customDropsFalseMessageLocation);
                            } else {
                                MobConfigManager.mobsCfg.set(MOBS + mobName + CUSTOM_DROPS, true);
                                String customDropsTrueMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".customDropsTrueMessage" + MESSAGE);
                                String customDropsTrueMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".customDropsTrueMessage" + LOCATION);
                                assert customDropsTrueMessage != null;
                                convertMessage(customDropsTrueMessage, commandSender, mobName, null, null, null, null, null, null, customDropsTrueMessageLocation);
                            }
                            try {
                                MobConfigManager.mobsCfg.save(mobsFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if(Boolean.TRUE.equals(error)){
                        String addCustomDropInvalidMobErrorMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ADD_CUSTOM_DROP_INVALID_ERROR_MESSAGE + MESSAGE);
                        String addCustomDropInvalidMobErrorMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ADD_CUSTOM_DROP_INVALID_ERROR_MESSAGE + LOCATION);
                        assert addCustomDropInvalidMobErrorMessage != null;
                        convertMessage(addCustomDropInvalidMobErrorMessage, commandSender, args[1], null, null, null, null, null, null, addCustomDropInvalidMobErrorMessageLocation);
                    }
                } else {
                    String accessDeniedMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ACCESS_DENIED_MESSAGE + MESSAGE);
                    String accessDeniedMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ACCESS_DENIED_MESSAGE + LOCATION);
                    assert accessDeniedMessage != null;
                    convertMessage(accessDeniedMessage, commandSender, null, null, null, null, null, null, null, accessDeniedMessageLocation);
                }
            } else if (args[0].equalsIgnoreCase("toggleDefaultDrops")) {
                if (commandSender.hasPermission("m4m.command.mk.toggleDefaultDrops") || commandSender.isOp()) {
                    boolean error = true;
                    for(String mobObject : MobConfigManager.mobsCfg.getConfigurationSection("mobs").getKeys(false)) {
                        if (mobObject.equalsIgnoreCase(args[1])) {
                            error = false;
                            String mobName = mobObject.substring(0, 1).toUpperCase() + mobObject.substring(1);
                            boolean defaultDrops = MobConfigManager.mobsCfg.getBoolean(MOBS + mobName + KEEP_DEFAULT_DROPS);
                            if (Boolean.TRUE.equals(defaultDrops)) {
                                MobConfigManager.mobsCfg.set(MOBS + mobName + KEEP_DEFAULT_DROPS, false);
                                String defaultDropsFalseMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".defaultDropsFalseMessage" + MESSAGE);
                                String defaultDropsFalseMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".defaultDropsFalseMessage" + LOCATION);
                                assert defaultDropsFalseMessage != null;
                                convertMessage(defaultDropsFalseMessage, commandSender, mobName, null, null, null, null, null, null, defaultDropsFalseMessageLocation);
                            } else {
                                MobConfigManager.mobsCfg.set(MOBS + mobName + KEEP_DEFAULT_DROPS, true);
                                String defaultDropsTrueMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".defaultDropsTrueMessage" + MESSAGE);
                                String defaultDropsTrueMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".defaultDropsTrueMessage" + LOCATION);
                                assert defaultDropsTrueMessage != null;
                                convertMessage(defaultDropsTrueMessage, commandSender, mobName, null, null, null, null, null, null, defaultDropsTrueMessageLocation);
                            }
                            try {
                                MobConfigManager.mobsCfg.save(mobsFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (Boolean.TRUE.equals(error)) {
                        String addCustomDropInvalidMobErrorMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ADD_CUSTOM_DROP_INVALID_ERROR_MESSAGE + MESSAGE);
                        String addCustomDropInvalidMobErrorMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ADD_CUSTOM_DROP_INVALID_ERROR_MESSAGE + LOCATION);
                        assert addCustomDropInvalidMobErrorMessage != null;
                        convertMessage(addCustomDropInvalidMobErrorMessage, commandSender, args[1], null, null, null, null, null, null, addCustomDropInvalidMobErrorMessageLocation);
                    }
                } else {
                    String accessDeniedMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ACCESS_DENIED_MESSAGE + MESSAGE);
                    String accessDeniedMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ACCESS_DENIED_MESSAGE + LOCATION);
                    assert accessDeniedMessage != null;
                    convertMessage(accessDeniedMessage, commandSender, null, null, null, null, null, null, null, accessDeniedMessageLocation);
                }
            } else if (args[0].equalsIgnoreCase("language")) {
                if (player2 instanceof Player && (commandSender.hasPermission("m4m.command.mk.language") || commandSender.isOp())) {
                    boolean success = false;
                    for (String languageOption : testList) {
                        if (args[1].equalsIgnoreCase(languageOption)) {
                            int counter = 1;
                            for(String ignored : UserManager.usersCfg.getConfigurationSection("users").getKeys(false)) {
                                String userId = UserManager.usersCfg.getString(USERS_USER + counter + ".userId");
                                assert userId != null;
                                if (player2 != null){
                                    if(userId.equalsIgnoreCase(player2.getUniqueId().toString())){
                                        UserManager.usersCfg.set(USERS_USER + counter + ".language", args[1]);
                                        success = true;
                                        try {
                                            UserManager.usersCfg.save(userFile);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        String languageChangeSuccessMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + args[1] + ".languageChangeSuccessMessage" + MESSAGE);
                                        String languageChangeSuccessMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + args[1] + ".languageChangeSuccessMessage" + LOCATION);
                                        assert languageChangeSuccessMessage != null;
                                        convertMessage(languageChangeSuccessMessage, commandSender, args[1], null, null, null, null, null, null, languageChangeSuccessMessageLocation);
                                        for (UserModel user : um){
                                            if (user.getUserId().equalsIgnoreCase(player2.getUniqueId().toString())) {
                                                user.setLanguage(args[1]);
                                            }
                                        }
                                    }
                                    counter++;
                                }
                            }
                        }
                    }
                } else {
                    String accessDeniedMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ACCESS_DENIED_MESSAGE + MESSAGE);
                    String accessDeniedMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ACCESS_DENIED_MESSAGE + LOCATION);
                    assert accessDeniedMessage != null;
                    convertMessage(accessDeniedMessage, commandSender, null, null, null, null, null, null, null, accessDeniedMessageLocation);
                }
            }
        }
        else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("setLowWorth")) {
                if (commandSender.hasPermission("m4m.command.mk.setLowWorth") || commandSender.isOp()) {
                    boolean error = true;
                    for(String mobObject : MobConfigManager.mobsCfg.getConfigurationSection("mobs").getKeys(false)) {
                        if (mobObject.equalsIgnoreCase(args[1])) {
                            error = false;
                            String mobName = mobObject.substring(0, 1).toUpperCase() + mobObject.substring(1);
                            double highWorth = MobConfigManager.mobsCfg.getDouble(MOBS + mobName + ".worth.high");
                            try {
                                if (highWorth >= Double.parseDouble(args[2])) {
                                    MobConfigManager.mobsCfg.set(MOBS + mobName + ".worth.low", Double.parseDouble(args[2]));
                                    setLowWorthSuccessMessage(commandSender, args[2], mobName, "english");
                                    MobConfigManager.mobsCfg.save(mobsFile);
                                } else {
                                    String setHighWorthTooLowErrorMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".setHighWorthTooLowErrorMessage" + MESSAGE);
                                    String messageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".setHighWorthTooLowErrorMessage" + LOCATION);
                                    assert setHighWorthTooLowErrorMessage != null;
                                    convertMessage(setHighWorthTooLowErrorMessage, commandSender, mobName, null, null, null, null, null, null, messageLocation);
                                }
                            } catch (NumberFormatException | IOException e){
                                String setLowWorthCommandErrorMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".setLowWorthCommandErrorMessage" + MESSAGE );
                                assert setLowWorthCommandErrorMessage != null;
                                String messageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".setHighWorthTooLowErrorMessage" + LOCATION);
                                convertMessage(setLowWorthCommandErrorMessage, commandSender, null, null, null, null, null, null, null, messageLocation);
                            }
                        }
                    }
                    if (Boolean.TRUE.equals(error)) {
                        String addCustomDropInvalidMobErrorMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ADD_CUSTOM_DROP_INVALID_ERROR_MESSAGE + MESSAGE);
                        String addCustomDropInvalidMobErrorMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ADD_CUSTOM_DROP_INVALID_ERROR_MESSAGE + LOCATION);
                        assert addCustomDropInvalidMobErrorMessage != null;
                        convertMessage(addCustomDropInvalidMobErrorMessage, commandSender, args[1], null, null, null, null, null, null, addCustomDropInvalidMobErrorMessageLocation);
                    }
                } else {
                    String accessDeniedMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ACCESS_DENIED_MESSAGE + MESSAGE);
                    String accessDeniedMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ACCESS_DENIED_MESSAGE + LOCATION);
                    assert accessDeniedMessage != null;
                    convertMessage(accessDeniedMessage, commandSender, null, null, null, null, null, null, null, accessDeniedMessageLocation);
                }
            }
            if (args[0].equalsIgnoreCase("setHighWorth")) {
                if (commandSender.hasPermission("m4m.command.mk.setHighWorth") || commandSender.isOp()) {
                    boolean error = true;
                    for(String mobObject : MobConfigManager.mobsCfg.getConfigurationSection("mobs").getKeys(false)) {
                        if (mobObject.equalsIgnoreCase(args[1])) {
                            error = false;
                            String mobName = mobObject.substring(0, 1).toUpperCase() + mobObject.substring(1);
                            double lowWorth = MobConfigManager.mobsCfg.getDouble(MOBS + mobName + ".worth.low");
                            try {
                                if (lowWorth <= Double.parseDouble(args[2])) {
                                    MobConfigManager.mobsCfg.set(MOBS + mobName + ".worth.high", Double.parseDouble(args[2]));
                                    MobConfigManager.mobsCfg.save(mobsFile);
                                    setHighWorthSuccessMessage(commandSender, args[2], mobName, language);
                                } else {
                                    String setLowWorthTooHighErrorMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".setLowWorthTooHighErrorMessage" + MESSAGE);
                                    String setLowWorthTooHighErrorMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".setLowWorthTooHighErrorMessage" + LOCATION);
                                    assert setLowWorthTooHighErrorMessage != null;
                                    convertMessage(setLowWorthTooHighErrorMessage, commandSender, mobName, null, null, null, null, null, null, setLowWorthTooHighErrorMessageLocation);
                                }
                            } catch (NumberFormatException | IOException e){
                                String setHighWorthCommandErrorMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".setHighWorthCommandErrorMessage" + MESSAGE);
                                String setHighWorthCommandErrorMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".setHighWorthCommandErrorMessage" + LOCATION);
                                assert setHighWorthCommandErrorMessage != null;
                                convertMessage(setHighWorthCommandErrorMessage, commandSender, null, null, null, null, null, null, null, setHighWorthCommandErrorMessageLocation);
                            }
                        }
                    }
                    if (Boolean.TRUE.equals(error)) {
                        String addCustomDropInvalidMobErrorMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ADD_CUSTOM_DROP_INVALID_ERROR_MESSAGE + LOCATION);
                        String addCustomDropInvalidMobErrorMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ADD_CUSTOM_DROP_INVALID_ERROR_MESSAGE + LOCATION);
                        assert addCustomDropInvalidMobErrorMessage != null;
                        convertMessage(addCustomDropInvalidMobErrorMessage, commandSender, args[1], null, null, null, null, null, null, addCustomDropInvalidMobErrorMessageLocation);
                    }
                } else {
                    String accessDeniedMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ACCESS_DENIED_MESSAGE + MESSAGE);
                    String accessDeniedMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ACCESS_DENIED_MESSAGE + LOCATION);
                    assert accessDeniedMessage != null;
                    convertMessage(accessDeniedMessage, commandSender, null, null, null, null, null, null, null, accessDeniedMessageLocation);
                }
            }
            if (args[0].equalsIgnoreCase("removeCustomDrop")) {
                if (commandSender.hasPermission("m4m.command.mk.removeCustomDrop") || commandSender.isOp()) {
                    List<ItemModel> itemList = new ArrayList<>();
                    boolean itemError = true;
                    boolean mobError = true;
                    for(String mobObject : MobConfigManager.mobsCfg.getConfigurationSection("mobs").getKeys(false)) {
                        if (mobObject.equalsIgnoreCase(args[1])) {
                            String mobName = mobObject.substring(0, 1).toUpperCase() + mobObject.substring(1);
                            mobError = false;
                            for(String drop : MobConfigManager.mobsCfg.getConfigurationSection(MOBS + mobName + ".drops").getKeys(false)) {
                                if (MobConfigManager.mobsCfg.getString(MOBS + mobName + ".drops." +drop + ".name").equals(args[2])){
                                    itemError = false;
                                    MobConfigManager.mobsCfg.set(MOBS + mobName + ".drops." +drop, null);
                                    try {
                                        MobConfigManager.mobsCfg.save(mobsFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            if (Boolean.TRUE.equals(itemError)) {
                                String customDropsDoNotExistErrorMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".customDropsDoNotExistErrorMessage" + MESSAGE);
                                String customDropsDoNotExistErrorMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".customDropsDoNotExistErrorMessage" + LOCATION);
                                assert customDropsDoNotExistErrorMessage != null;
                                convertMessage(customDropsDoNotExistErrorMessage, commandSender, args[1], null, null, null, null, null, null, customDropsDoNotExistErrorMessageLocation);
                            } else {
                                try {
                                    String removeCustomDropSuccessMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".removeCustomDropSuccessMessage" + MESSAGE);
                                    String removeCustomDropSuccessMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".removeCustomDropSuccessMessage" + LOCATION);
                                    assert removeCustomDropSuccessMessage != null;
                                    convertMessage(removeCustomDropSuccessMessage, commandSender, args[1], args[2], null, null, null, null, null, removeCustomDropSuccessMessageLocation);
                                    MobConfigManager.mobsCfg.save(mobsFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    if (Boolean.TRUE.equals(mobError)) {
                        String addCustomDropInvalidMobErrorMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ADD_CUSTOM_DROP_INVALID_ERROR_MESSAGE + MESSAGE);
                        String addCustomDropInvalidMobErrorMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ADD_CUSTOM_DROP_INVALID_ERROR_MESSAGE + LOCATION);
                        assert addCustomDropInvalidMobErrorMessage != null;
                        convertMessage(addCustomDropInvalidMobErrorMessage, commandSender, args[1], null, null, null, null, null, null, addCustomDropInvalidMobErrorMessageLocation);
                    }
                } else {
                    String accessDeniedMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ACCESS_DENIED_MESSAGE + MESSAGE);
                    String accessDeniedMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ACCESS_DENIED_MESSAGE + LOCATION);
                    assert accessDeniedMessage != null;
                    convertMessage(accessDeniedMessage, commandSender, null, null, null, null, null, null, null, accessDeniedMessageLocation);
                }
            }
        } else if (args.length == 5) {
            if (args[0].equalsIgnoreCase("addCustomDrop")) {
                if (commandSender.hasPermission("m4m.command.mk.addCustomDrop") || commandSender.isOp()) {
                    if (args[1].equalsIgnoreCase("Player")){
                        String addCustomDropsErrorMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".addCustomDropsErrorMessage" + MESSAGE);
                        String addCustomDropsErrorMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".addCustomDropsErrorMessage" + LOCATION);
                        assert addCustomDropsErrorMessage != null;
                        convertMessage(addCustomDropsErrorMessage, commandSender, null, null, null, null, null, null, null, addCustomDropsErrorMessageLocation);
                    }
                    else {
                        for(String mobObject : MobConfigManager.mobsCfg.getConfigurationSection("mobs").getKeys(false)) {
                            if (mobObject.equalsIgnoreCase(args[1])) {
                                String mobName = mobObject.substring(0, 1).toUpperCase() + mobObject.substring(1);
                                    try {
                                        int itemPresent = 0;
                                        try {
                                            for(String drop : MobConfigManager.mobsCfg.getConfigurationSection(MOBS + mobName + ".drops").getKeys(false)) {
                                                if (MobConfigManager.mobsCfg.getString(MOBS + mobName + ".drops." + drop + ".name").equals(args[2])) {
                                                    itemPresent = 1;
                                                    break;

                                                }
                                            }
                                        } catch (NullPointerException ignored){

                                        }
                                        if (itemPresent == 0){
                                            Material m = Material.valueOf(args[2].toUpperCase());
                                            for (Material material : materials) {
                                                int counter = 0;
                                                if (material.equals(m)) {
                                                    counter++;
                                                }
                                                if (counter != 0) {
                                                    try {
                                                        int amount = Integer.parseInt(args[3]);
                                                        int chance = Integer.parseInt(args[4]);
                                                        if(chance > 100){
                                                            chance = 100;
                                                        }
                                                        int counter2 = 0;
                                                        counter2++;
                                                        MobConfigManager.mobsCfg.set(MOBS + mobName + DROPS_ITEMS + counter2 + ".name", args[2]);
                                                        MobConfigManager.mobsCfg.set(MOBS + mobName + DROPS_ITEMS + counter2 + ".amount", amount);
                                                        MobConfigManager.mobsCfg.set(MOBS + mobName + DROPS_ITEMS + counter2 + ".chance", chance);
                                                        String addCustomDropSuccessMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".addCustomDropSuccessMessage" + MESSAGE);
                                                        String addCustomDropSuccessMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".addCustomDropSuccessMessage" + LOCATION);
                                                        assert addCustomDropSuccessMessage != null;
                                                        convertMessage(addCustomDropSuccessMessage, commandSender, mobName, args[2], chance, (double) amount, null, null, null, addCustomDropSuccessMessageLocation);
                                                        MobConfigManager.mobsCfg.save(mobsFile);
                                                    } catch (NumberFormatException e) {
                                                        String addCustomDropsCommandErrorMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".addCustomDropsCommandErrorMessage" + MESSAGE);
                                                        String addCustomDropsCommandErrorMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".addCustomDropsCommandErrorMessage" + LOCATION);
                                                        assert addCustomDropsCommandErrorMessage != null;
                                                        convertMessage(addCustomDropsCommandErrorMessage, commandSender, null, null, null, null, null, null, null, addCustomDropsCommandErrorMessageLocation);
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        }
                                        else {
                                            String addCustomDropAlreadyPresentErrorMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".addCustomDropAlreadyPresentErrorMessage" + MESSAGE);
                                            String addCustomDropAlreadyPresentErrorMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".addCustomDropAlreadyPresentErrorMessage" + LOCATION);
                                            assert addCustomDropAlreadyPresentErrorMessage != null;
                                            convertMessage(addCustomDropAlreadyPresentErrorMessage, commandSender, null, args[2], null, null, null, null, null, addCustomDropAlreadyPresentErrorMessageLocation);
                                        }

                                    }
                                    catch (IllegalArgumentException e){
                                        String addCustomDropInvalidMobErrorMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ADD_CUSTOM_DROP_INVALID_ERROR_MESSAGE + MESSAGE);
                                        String addCustomDropInvalidMobErrorMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ADD_CUSTOM_DROP_INVALID_ERROR_MESSAGE + LOCATION);
                                        assert addCustomDropInvalidMobErrorMessage != null;
                                        convertMessage(addCustomDropInvalidMobErrorMessage, commandSender, args[1], null, null, null, null, null, null, addCustomDropInvalidMobErrorMessageLocation);
                                    }
                                }
                            }
                    }
                } else {
                    String accessDeniedMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ACCESS_DENIED_MESSAGE + MESSAGE);
                    String accessDeniedMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ACCESS_DENIED_MESSAGE + LOCATION);
                    assert accessDeniedMessage != null;
                    convertMessage(accessDeniedMessage, commandSender, null, null, null, null, null, null, null, accessDeniedMessageLocation);
                }
            }
        }
     return true;
}

    private void setLowWorthSuccessMessage(CommandSender commandSender, String arg, String mobName, String language) {
        try {
            String setLowWorthSuccessMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".setLowWorthSuccessMessage" + MESSAGE);
            String setLowWorthSuccessMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".setLowWorthSuccessMessage" + LOCATION);
            assert setLowWorthSuccessMessage != null;
            convertMessage(setLowWorthSuccessMessage, commandSender, mobName, null, null, null, arg, null, null, setLowWorthSuccessMessageLocation);
            MobConfigManager.mobsCfg.save(mobsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setHighWorthSuccessMessage(CommandSender commandSender, String arg, String mobName, String language) {
        try {
            String setHighWorthSuccessMessage = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".setHighWorthSuccessMessage" + MESSAGE);
            String setHighWorthSuccessMessageLocation = MessagesConfigManager.messagesCfg.getString(LANGUAGE + language + ".setHighWorthSuccessMessage" + LOCATION);
            assert setHighWorthSuccessMessage != null;
            convertMessage(setHighWorthSuccessMessage, commandSender, mobName, null, null, null, null, arg, null, setHighWorthSuccessMessageLocation);
            MobConfigManager.mobsCfg.save(mobsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Player getPlayer(CommandSender commandSender, Player player) {
        if (commandSender instanceof Player ){
            player = (Player) commandSender;
        }
        return player;
    }

    private static void ifWordIsNotAColor(List<Object> object, boolean test, String s) {
        if (Boolean.FALSE.equals(test)){
            object.add(s);
        }
    }

    private static boolean iterateColorArray(List<String> colorArray, List<Object> object, boolean test, String s) {
        for (String color : colorArray){
            test = isColor(object, test, s, color);
        }
        return test;
    }

    private static boolean isColor(List<Object> object, boolean test, String s, String color) {
        if (s.contains(color)){
            test = true;
            String colorCode = s.substring(0,2);
            object.add(colorConverter(colorCode));
            List<String> words = Arrays.asList(s.split(colorCode));
            object.add(words.get(1));
        }
        return test;
    }

    public static ChatColor colorConverter(String color){
        ChatColor holder = null;
        switch (color) {
            case "&4":
                holder = ChatColor.valueOf("DARK_RED");
                break;
            case "&c":
                holder = ChatColor.valueOf("RED");
                break;
            case "&6":
                holder = ChatColor.valueOf("GOLD");
                break;
            case "&e":
                holder = ChatColor.valueOf("YELLOW");
                break;
            case "&2":
                holder = ChatColor.valueOf("DARK_GREEN");
                break;
            case "&a":
                holder = ChatColor.valueOf("GREEN");
                break;
            case "&b":
                holder = ChatColor.valueOf("AQUA");
                break;
            case "&3":
                holder = ChatColor.valueOf("DARK_AQUA");
                break;
            case "&1":
                holder = ChatColor.valueOf("DARK_BLUE");
                break;
            case "&9":
                holder = ChatColor.valueOf("BLUE");
                break;
            case "&d":
                holder = ChatColor.valueOf("LIGHT_PURPLE");
                break;
            case "&5":
                holder = ChatColor.valueOf("DARK_PURPLE");
                break;
            case "&7":
                holder = ChatColor.valueOf("GRAY");
                break;
            case "&8":
                holder = ChatColor.valueOf("DARK_GRAY");
                break;
            case "&0":
                holder = ChatColor.valueOf("BLACK");
                break;
            default:
                holder = ChatColor.valueOf("WHITE");
                break;
        }
        return holder;
    }

    public static void convertMessage(String message, CommandSender pa, String mobName, String itemName, Integer chance, Double amount, String lowWorth, String highWorth, Double balance, String messageLocation) {
        String[] customArray = message.split(" ");
        List<String> colorArray = new ArrayList<>();
        colorArray.add("&4");
        colorArray.add("&c");
        colorArray.add("&6");
        colorArray.add("&e");
        colorArray.add("&2");
        colorArray.add("&a");
        colorArray.add("&b");
        colorArray.add("&3");
        colorArray.add("&1");
        colorArray.add("&9");
        colorArray.add("&d");
        colorArray.add("&5");
        colorArray.add("&f");
        colorArray.add("&7");
        colorArray.add("&8");
        colorArray.add("&0");
        List<Object> object = new ArrayList<>();
        splitStringIntoArrayAndConvert(customArray, colorArray, object);
        StringBuilder d = new StringBuilder(object.get(0).toString());
        int count = 1;
        for (Object word: object){
            if (count > 1){
                if (word.toString().equalsIgnoreCase("%mobName%")){
                    word = mobName;
                }
                if (word.toString().equalsIgnoreCase("%itemName%")){
                    word = itemName;
                }
                if (word.toString().equalsIgnoreCase("%chance%")){
                    word = chance;
                }
                if (word.toString().equalsIgnoreCase("%amount%")){
                    word = amount;
                }
                if (word.toString().equalsIgnoreCase("%lowWorth%") || word.toString().equalsIgnoreCase("%worth%")){
                    word = lowWorth;
                }
                if (word.toString().equalsIgnoreCase("%highWorth%")){
                    word = highWorth;
                }
                if (word.toString().equalsIgnoreCase("%balance%")){
                    word = balance;
                }
                if (word instanceof String && !word.toString().equals(".")) {
                    d.append(word).append(" ");
                }
                else {
                    d.append(word);
                }
            }
            count++;
        }
        String completedMessage = d.toString();
        completedMessage = completedMessage.replace("$ ", "$");

        if (messageLocation.equalsIgnoreCase("ActionBar") && pa instanceof Player){
            Player player = (Player) pa;
            sendActionBar(player, completedMessage);
        } else if (messageLocation.equalsIgnoreCase("ChatMenu")) {
            pa.sendMessage(completedMessage);
        }
    }

    public static void sendActionBar(Player player, String message) {
        CraftPlayer p = (CraftPlayer) player;
        IChatBaseComponent cbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + message + "\"}");
        PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte) 2);
        p.getHandle().playerConnection.sendPacket(ppoc);
    }

    private static void splitStringIntoArrayAndConvert(String[] customArray, List<String> colorArray, List<Object> object) {
        boolean test;
        for (String s: customArray){
            test = iterateColorArray(colorArray, object, false, s);
            ifWordIsNotAColor(object, test, s);
        }
    }
}
