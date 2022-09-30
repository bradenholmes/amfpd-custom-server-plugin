# disMCord
### What
Gamers everywhere have been itching to play the best game ever made again, and I thought it'd be cool to try to build a place where they could do so together. This is a spigot plugin which connects a spigot minecraft server running in the cloud to a separate private Discord server. 

The idea was to restrict the minecraft world to a private whitelist of players, but allow any of those players to add to that list from inside the discord. This adds security through the verifications needed to link a discord account with a minecraft account, while also providing myself with much better control over that information. 

The plugin communicates with a MySQL database in the cloud where records are kept on players and whitelist events.
### Why
I started out using DiscordWhitelister (https://www.spigotmc.org/resources/discord-whitelister.69929/)
It's a good plugin in its own right. It didn't do what I needed it to do, and it did do what I didn't need it to do.

So I ravaged it, but my thanks and credit to https://github.com/Shimeo98/DiscordWhitelisterSpigot
