# Wyspia Express

This is a custom mod designed to overwrite other Harpy Express/Wathe addon roles in game shop. It replaces the shop those roles 
had with a custom one thats highly configurable. 

Right now it supports Noelle's Role 1.6.3-h1.3, Stupid Express 0.7.1, Kin's Wathe 1.5.2, Starry Express 1.3.1. Of course 
you need to have those mod installed both on the server and on the client with this mod.

## Development observation

- Right now this mod heavily depends on other mod to give civilian roles coin. They achieve that by adding a check on PlayerMoodComponent.setMood()
    via mixin so when a player completes a task they get coins. This means that taskIncome cannot be configurable on
    those mods role.
- The same happens with passive income
