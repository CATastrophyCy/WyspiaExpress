# Server configuration

This branch contains the real server settings in the WyspiaExpress 0.8.1 configuration format. It is a configuration checkout, separate from the main IntelliJ project.

WyspiaExpress shared settings are in `wyspiaexpress/`: `general.json5`, `server.json5`, `rules.json5`, `tasks.json5`, `roles.json5`, `items.json5`, `modifiers.json5` and `gamemodes.json5`. The original 0.8.0 settings have been retained; new settings use the current defaults.

Mode overrides are under `wyspiaexpress/gamemodes/<namespace>/<mode>/`. Files are sparse: omitted fields inherit the shared config, and omitted mode settings use their Java defaults.

| Mode | Main role dividend | Killer-aligned neutral dividend | Hide mode in guidebook |
| --- | --- | --- | --- |
| `wyspiaexpress:killer_modded` | 6 players per killer | 9 players | true |
| `wyspiaexpress:true_neutral_modded` | 6 players per true neutral | 9 players | true |

These dividends set separate slot quotas using whole-number division. At 12 players, either mode has two main slots and one killer-aligned neutral slot; at 18, three and two; at 24, four and two. Enabled roles, player requirements, role copy limits and the existing assignment/fallback rules still determine which slots can be filled. Both modes inherit the saved Wathe vigilante dividend (`vigilanteDividend: -1`). The shared `gamemodes.neutralDividend` is for the legacy modded mode, not either of these two modes.

Start `wyspiaexpress:modded_alternate` to choose between the two modes. At 12 or more players, the configured true-neutral chance is 50%; smaller eligible lobbies use killer mode. The existing horn/autostart routing returns to the alternate selector for subsequent rounds.

The true-neutral role overrides keep Lich/Hacker out, allow up to three Licensed Villains or Thieves with an additional copy allowed per six players above their minimum, and apply the authored Thief shop/loadout settings. Cult Leader retains its shared copy limit. Other rule/item/task/modifier overrides remain empty. `hideGamemode` only suppresses the guidebook's mode-specific role-pool filtering; explicit server disables, configured spawn requirements and hidden/obsolete exclusions still apply.

Stop the server before updating its configuration checkout and use the matching mod build. Replace the old WyspiaExpress config directory with this directory rather than mixing old and new layouts. Keep the other mods' config files in their normal locations. Dedicated servers load these files on startup and send the resolved settings to matching clients during login; editing the files requires a restart.

The `.schema` marker belongs to disposable client configuration and is not tracked here. This branch contains no testing overrides.

The config is validated against the current 0.8.1 schema, including resolved mode overrides and client synchronization encode/decode. The assignment-credit system uses `baseWeight` and `shareKillerNeutralWeights`; shared killer/true-neutral history is off. The real server's `baseWeight` remains `0.0004`. These are server balance settings, with no testing overrides.

The shared files retain the field documentation declared in the Java config models. After successful validation, the loader saves these shared files with complete defaults and existing values; supplied field comments are retained. Existing gamemode override files are not rewritten or expanded.
