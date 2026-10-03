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
- Wathe Blood 1.1.4.
- Wathe 1.3.2

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

## Guidebook

Press **L** to open the role/modifier guide in the **Roles** tab; the key is rebindable. **All** lists non-hidden/non-obsolete entries; **Current** shows the possible round roster; **Unavailable** shows enabled roles that missed this round’s spawn requirements; **Disabled** shows explicit server-config exclusions. These snapshots refresh only at round start and remain stable between rounds. Discovery Civilian, Loose End, base Civilian/Killer and obsolete entries never appear. **Me** shows your role/modifiers and **Pick** your Copycat choices. First use defaults to Pick for Copycat and Current otherwise; later openings restore your cached choice. Background (transparent by default), folds, search and selected page are saved locally. The top Roles tab leaves the full interior for the guide. Current roles/modifiers are highlighted when visible. Press its configured key again to close it, unless a text input has focus; clicking anywhere outside the input clears that focus. Role rows have no hover messages, and detail pages omit roster state messages. Guide content refreshes on opening and interaction, including BG, rather than polling each tick. Saved preferences reset when their format version differs.

The top **BG** button toggles background transparency. **SFX** beside it toggles all guidebook click sounds; the choice is saved locally. Guide text uses Minecraft’s default font. Pages show inclusive spawn requirements, a summary, optional artwork (falling back to the mod icon), config-linked abilities, starting items, shop prices with item tooltips, and lore. Values follow the existing server configuration synchronization. Resource packs can supply the new JSON format while old StarryExpress name/title/description keys remain compatible. See [Guidebook system reference](doc/guidebook.md) for authoring and extension details.

`suppressOldGuidebook` in the synchronized general config defaults to **true**, removing StarryExpress's old top-left inventory guidebook button. Set it false and reopen the inventory to restore it.

## Miscellaneous

- Players crawl faster (configurable)
- Players that receive fatal damage but blocked it will be stunned (configurable)
- Phantom will lose invisibility after killing a player in any way that isn't poison (configurable)
- Spectators can use "/sv join [0-5]" to enter different spectator voicechat group. They can also use "/sv leave" to leave the voicechat group as default voicechat group function is disabled
- Spectators receive special instinct that allows them to see more information
- One killer each game will always have the guesser modifier (configurable)

## Operator commands

| Command | Behavior |
| --- | --- |
| `/sv drag <player> <index>` | Move a player into spectator voice group `0` through the configured maximum. Group `0` is the default Wathe spectator group. |
| `/sv drag <player>` | Force the player out of their voice group. |
| `/revive <player> [x y z]` | Switch the player to Adventure and remove them from spectator voice chat. With coordinates, teleport into the command source's dimension; without coordinates, retain their position and dimension. Relative (`~`) and local (`^`) coordinates are supported. Clears the death record and resets freezing, depression and mood. |
| `/setMood <player> <amount>` | Set mood to a value from `0.0` through `1.0`, without granting task coins. The player's role must use real mood. |
| `/setRole <player> <role>` | Assign a role during an active round. |

## New Items & Item rework
- (Starexpress)Tape rework: tape is made much more stable and Muzzler and Morphling will receive passive income for each muzzled players; muzzled players will passively lose mood (configurable)
- (Kinswathe) Dream Imprint: made the teleport function configurable
- (Kinswathe) Wrench: added a new functionality to wrench that allows it to restore nearby lights
- Megaphone: a new item that can be used on a dead body to report it and make it temporally visible to all players. A body can only be reported once
- Smoke bomb: a grenade that doesn't kill nearby players, but instead slows them and create a large area of smoke for a few seconds. Any player that are inside the smoke will also receive lingering effects (configurable)
## New roles

### Outlaw

A killer that can purchase a special revolver that doesn't drop when they shoot a civilian, instead it will go to a much higher cooldown
They also have an ability that allows them to refresh the cooldown of every gun items

### Lich

A reimagining of the killer role Necromancer introduced by Stupid Express
The Lich always revives dead players into a weaker killer role named Ghoul

### Gambler

A civilian role that can purchase a *Fun box*, which will be them a random item out of a selected pool

### The Insane Dammed Paranoid Civilian of Just Waffles

A true neutral role that wants to be the last one standing to win. They can hear dead players at their will

### The Nightshade Cult Leader

A true neutral role that wants to eliminate the train of non believers
The Cult Leader will slowly convert players near them. Once fully converted, the player can be seen by ANY cult member on passive instinct
When a fully converted player fall, their body will be visible to cult members, the Cult Leader can then go and revive the fallen player into a Cultist

The Cult Leader can also purchase Ritual Dagger, which works like a knife can it turns the player into a Cultist instead of killing them. It is worth noting that the Dagger conversion does not clear
the converted player's inventory, unlike the revive

## New modifiers

### Employee

Player receives a special "Employee key" that allows them to open several doors on the map

### Professional Vent Crawler

Player crawls much faster

### Elusive

Player is hidden from active instinct when they are within a certain range of the player
