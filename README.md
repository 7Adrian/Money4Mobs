# Money4Mobs
## Give money to players and custom drops when they kill a mob
### Permissions for M4M
Link to default mobs.yml file ---> https://github.com/lakeboy93/Money4Mobs/blob/master/src/Latch/Money4Mobs/mobs.yml
Command | Permission | Description
------ | ------- | -------
Not Applicable | m4m.rewardMoney | Needed permission for players to receive money.
/mk toggleKM | m4m.command.mk.toggleKM | Turn on/off mob kill message for player. (this is for each individual player to toggle message.
mk worth [mobName] | m4m.command.mk.worth | Gets worth of mob.
/mk setHighWorth [mobName] [amount] | m4m.command.mk.setHighWorth | Sets the high worth amount for the given mob.
/mk setLowWorth [mobName] [amount] | m4m.command.mk.setLowWorth | Sets the low worth amount for the given mob.
/mk drops [mobName] | m4m.command.mk.drops | Displays custom drops, if any, and chance of custom mob dropping the custom item.
/mk addCustomDrop [mobName] [itemName] [amount] [chance] | m4m.command.mk.addCustomDrop | Sets a custom item to drop for the given mob. Chance is represent in percent from 0 to 100%.
/mk removeCustomDrop [mobName] [itemName] | m4m.command.mk.removeCustomDrop | Removes a custom item set for the given mob.
/mk toggleCustomDrops [mobName] | m4m.command.mk.toggleCustomDrops | Toggles if the given mob will drop custom items set.
/mk toggleDefaultDrops [mobName] | m4m.command.mk.toggleDefaultDrops | Toggles if the given mob will keep their default drops. If false for Cows, they will no longer drop leather and raw beef.
/mk toggleMoneyFromSpawnEggs | m4m.command.mk.spawneggs | Toggles if players get money from mobs spawned in with eggs. Defaults to false on first time load.
/mk toggleMoneyFromSpawners | m4m.command.mk.spawners | Toggles if players get money from mobs spawned in from spawners. Defaults to false on first time load.
/mk language [language] | m4m.command.mk.language | Changes language of messages for M4M per player.
/mk reload | m4m.command.mk.reload | Reloads M4M data with manually changed values in config files without needing to reload or restart the whole server
/mk toggleMoneyFromTamedWolves | m4m.command.mk.toggleMoneyFromTamedWolves | Toggles if players receive money when their tamed wolf kills a mob
Not Applicable| m4m.bypass.ipBan | Allows a user to bypass the ipBan option for players on the same IP

## Permissions for group specific multipliers - 15 levels plus Operator
### If group/player has multiple level permissions set, the highest level take precedent
Permission | Description
------------ | ------------
m4m.multiplier.level-1 | Permission for level-1 multiplier
m4m.multiplier.level-2 | Permission for level-2 multiplier
m4m.multiplier.level-3 | Permission for level-3 multiplier
m4m.multiplier.level-4 | Permission for level-4 multiplier
m4m.multiplier.level-5 | Permission for level-5 multiplier
m4m.multiplier.level-6 | Permission for level-6 multiplier
m4m.multiplier.level-7 | Permission for level-7 multiplier
m4m.multiplier.level-8 | Permission for level-8 multiplier
m4m.multiplier.level-9 | Permission for level-9 multiplier
m4m.multiplier.level-10 | Permission for level-10 multiplier
m4m.multiplier.level-11 | Permission for level-11 multiplier
m4m.multiplier.level-12 | Permission for level-12 multiplier
m4m.multiplier.level-13 | Permission for level-13 multiplier
m4m.multiplier.level-14 | Permission for level-14 multiplier
m4m.multiplier.level-15 | Permission for level-15 multiplier

Link to default mobs.yml file ---> https://github.com/lakeboy93/Money4Mobs/blob/master/src/Latch/Money4Mobs/messages.yml
## How to set up a custom message
* Messages go off of the language set in users.language and corresponds to the language.[language] 
* * If the users language is english, then in the messages.yml file, the message layout needs to be language.english, not language.English. This option is case sensitive.
* Colors are the the default Minecraft color codes, "&7, &a, &f"
* In order to display the amount rewarded to the player upon mob kill, use '%amount%'
* In order to display the balance of the player after being rewarded the money, use '%balance%'
* In order to display the low worth of a mob, use '%lowWorth%'
* In order to display the high worth of a mob, use '%highWorth'
* In order to display the mob name, use '%mobName%'
* In order to display the item name, use '%itemName%'
* In order to display the chance that an item has to drop, use '%chance%'
* If the default message does not already contain one of the placeholders, then the message will not display properly
Each word or value needs to be separated by a space. The color code should be before the word or group of words, i.e., '&6Hello'
#### Examples
 * 1.) 
         customDropsTrueMessage:
            message: '&aCustom drops for &6%mobName% &amobs set to &6true'
            location: ChatMenu
            
        * 1- Result) "Custom drops for Ghast mobs set to true"
 * 2.)
         customDropsFalseMessage:
            message: '&aCustom drops for &6%mobName% &amobs set to &6false'
            location: ChatMenu
            
        * 2 - Result) "Custom drops for Ghast mobs set to false"
 * 3.)
         languageChangeSuccessMessage:
           message: '&aChanged Money4Mobs messages to &6English'
           location: ChatMenu
           
        * 3 - Result) "Changed Money4Mobs messages to English"
 * 4.)
         moneyRewardedMessage:
           message: '&aYou were given &6$ %amount%  &aand now have &6$ %balance%'
           location: ActionBar
           
       * 4 - Result) "Rewarded $10 and now have $100"
 * 5.) 
         moneySubtractedMessage:
           message: '&6$ %amount%  &awas taken and you now have &6$ %balance%'
           location: ActionBar
           
        * 5 - Result) "$10 was taken and you now have $100"
        
 
