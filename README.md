# 🛡️ moodpatcher's AntiGrief plugin 
A simple plugin for base protection, that allows PvP and base raiding.

## Zone protection
![Default](https://i.imgur.com/5GnGOFE.png) <br/>
By default, every block placed by a player alongside a 4 block radius is protected by the plugin. Other players cannot open chests, doors or place blocks in this area.

## Guests:
![Default](https://i.imgur.com/QIwt35Z.png) <br/>
Players can add guests, which allows them to interact with protected blocks placed by another player. Guest permission on other players can be toggled using the `/antigrief` command.

## Raiding:
![Default](https://i.imgur.com/b2XkT7c.png) <br/>
You can destroy blocks placed by other players, if raiding is enabled in the config file. By default, 32 Fire Charges are required to do so. Blocks placed by server OPs can't be destroyed by default, but you can change this in the plugin config. 

## Misc:
English and Hungarian localizations are available. Server OPs can bypass zone protection by enabling the option it in the config file. 
Blocks are stored in the plugin folder, in data.db (SQLite) alongside with localization and the configuration file.
