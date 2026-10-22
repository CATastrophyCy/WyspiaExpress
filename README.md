# WyspiaExpress server configuration

This is the config-only `server-config` branch of the WyspiaExpress repository. Its root corresponds to a Minecraft server's `config/` directory.

## Initial state

The current files were supplied from the real server running **WyspiaExpress 0.8.0**. Keep this baseline unchanged until the 0.8.1 implementation is finished and tested; update the configuration for 0.8.1 afterward. No source code, client preferences, logs, world data or server statistics are included.

Currently supplied configs:

- `wyspiaexpress/general.json5`
- `wyspiaexpress/roles.json5`
- `wyspiaexpress/items.json5`
- `wyspiaexpress/modifiers.json5`

The ignore file allows these files, this documentation, and selected optional configs for other server mods. Those optional files are not currently included. To manage another config outside that allowlist, explicitly add its path to `.gitignore` and then add the file.

## Local editing with IntelliJ

The code checkout remains `/home/cat/github/WyspiaExpress` on `main`. The separate config checkout is `/home/cat/github/WyspiaExpress-server-config` on `server-config`.

Open the config folder in IntelliJ using **File > Open > New Window**. Keep the main project window on `main`; there is no need to switch its branch. The two checkouts share the repository's remote, but have independent working files and staging areas.

Edit actual JSON/JSON5 values in the config window. Java defaults edited in the main project are not automatically exported here. Changes to the development instance's `run/config` directory are also not automatically copied here. If you want to test these configs locally, copy the selected files into the local instance while it is stopped.

## Branch history

This branch has independent config-only history. Its initial commit contains the supplied 0.8.0 server configuration and these setup instructions. Future config changes can be committed and pushed from this checkout without committing the main project's code changes.

## One-time setup on an existing Linux server

Do this with the Minecraft server stopped. The server machine needs Git and read access to the repository. The example uses the normal GitHub SSH hostname; the local machine's `CAT-github.com` SSH alias may not exist on the server.

These steps assume `config/` already exists, is not already a Git checkout, and `config-from-git/` does not exist:

```sh
cd /path/to/minecraft-server
git clone --single-branch --branch server-config git@github.com:CATastrophyCy/WyspiaExpress.git config-from-git
cp -an config/. config-from-git/
mv config "config.backup.$(date +%Y%m%d-%H%M%S)"
mv config-from-git config
git -C config status --short
```

The clone supplies the selected versioned configs. `cp -an` carries over other existing configs without replacing the versioned files. The original config directory is retained as a timestamped backup. Inspect the prepared files and status before starting Minecraft again.

## Normal updates

On the local machine, from the config checkout:

```sh
git diff
git add .
git commit -m "Adjust server balance"
git push
```

On the server, stop Minecraft, then run:

```sh
cd /path/to/minecraft-server/config
git status --short
git pull --ff-only
```

Start Minecraft again after a successful pull. There is no extra copy step once the checkout occupies the actual `config/` directory.

If a mod rewrites a tracked file, inspect `git diff` on the server before updating. Do not use a forced reset to hide a real edit. Restore a file from Git only after deciding its server-side changes can be discarded; otherwise save/capture them first. Git updates disk files; do not assume every running mod supports hot reload.

Keep the mod jar compatible with the versioned configuration. This branch does not deploy jars, server properties, worlds or player statistics.
