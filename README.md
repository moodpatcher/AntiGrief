# 🛡️ moodpatcher's AntiGrief plugin 
A simple plugin for base protection, that allows PvP and base raiding.

## Zone protection
![Default](https://i.imgur.com/5GnGOFE.png) <br/>
By default, every block placed by a player - along with a 4-block radius around it - is protected. Other players cannot open chests, doors, or place blocks within this protected area.

## Guests:
![Default](https://i.imgur.com/QIwt35Z.png) <br/>
Players can add others as guests, allowing them to interact with their protected blocks. Guest permissions can be toggled on or off using the /antigrief command.

## Raiding:
![Default](https://i.imgur.com/b2XkT7c.png) <br/>
If raiding is enabled in the configuration file, players can destroy blocks placed by others. By default, this requires 32 Fire Charges.
Blocks placed by server OPs cannot be destroyed unless this setting is changed in the config.

## Misc:
English and Hungarian localizations are available. Server OPs can bypass zone protection by enabling the option for it in the config file. 
You can print the zone info into the chatbox by sneaking and right clicking on a block with a stick in your hand.
Blocks are stored in the plugin folder, in data.db (SQLite) alongside with localization and the configuration file.
