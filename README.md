# Wyspia Express

Wyspia Express is the core mod developed for the modpack WyspiaExpress, a private modpack derived from The Extended
Voyage of the Harpy Express. 

# Dependencies
- owo config lib 0.12.15.4+1.21
- HarpyModLoader 1.2.4-h1.3
- Noelle's Role 1.6.4-h1.3
- Stupid Express 0.7.1
- Kin's Wathe 1.6.3
- Starry Express 1.3.2 
- Wathe Blood 1.1.4. Of course

You need to have those mod installed both on the server and on the client with this mod.

# Features
 
## Config
Wyspia Express allows the server owner to freely balance the roles, items and other mechanics:

For roles it allows editing:

- Ingame shop
- Starting item when round starts
- Whether the role has passive income
- Amount of coin gained per completing task
- Whether the role can see poison
- Minimum and Maximum amount of players in the lobby for the role to spawn
- Maximum amount of the role that can spawn
- Role specific ability

For items it allows:

- Whether the item should render when held
- Whether the item can punch players
- Whether the item should drop upon death
- Cooldown

## Freezing and Depression

Wyspia Express adds two toggleable mechanics, freezing and depression:

- A freezing meter will slowly go down when the player is outside of train. The meter is displayed via 
a bar on the left of the screen. Should the bar reach zero, the player will be eliminated. Players can recover the freezing meter by staying inside the train.
- When the player mood is 0, the depression meter will slowly empties out
- The meter is displayed via a bar below the mood bar. Should the bar reaches zero, the player will be eliminated. 
- Players can recover the depression bar by having more than 0 mood

## Choose your killer role - Copycat

Wyspia Express introduces a way for killer players to choose a killer role when the game starts.

When the round starts, every killer player will be assigned the Copycat role. By opening your inventory, you can see three buttons 
, and by clikcing the button you receive the corresponding role.

## Miscellaneous

- Players crawl faster
- Players that receive fatal damage but blocked it will be stunned (configurable)
- Phantom will lose invisibility after killing a player in any way that isn't poison (configurable)
- Tape rework. Muzzler will receive passive income for each muzzled players. Muzzled players will passively lose mood
- Spectators can use "/sv join [0-5]" to enter different spectator voicechat group. They can also use "/sv leave" to leave the voicechat group as default voicechat group function is disabled
- Spectators receive special instinct that allows them to see more
- One killer each game will always have the guesser modifier
## New roles

### Outlaw

A killer that can purchase a special revolver that doesn't drop when they shoot a civilian, instead it will go to a much higher cooldown.
They also have an ability that allows them to refresh the cooldown of every gun items.

### Lich

A reimagining of the killer role Necromancer introduced by Stupid Express. 
The Lich always revives dead players into a weaker killer role named Ghoul. 

### Gambler

A civilian role that can purchase a *Fun box*, which will be them a random item out of a selected pool.

### The Insane Dammed Paranoid Civilian of Just Waffles

A true neutral role that wants to be the last one standing to win. They can hear dead players at their will.

## New modifiers

### Employee

Player receives a special "Employee key" that allows them to open several doors on the map.

### Professional Vent Crawler

Player crawls much faster

### Elusive

Player is hidden from active instinct when they are within a certain range of the player.