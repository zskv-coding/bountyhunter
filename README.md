# BountyHunter Plugin

BountyHunter is a Minecraft plugin that allows server administrators to set bounties on players. Players can earn rewards for hunting bountied players. The plugin allows you to customize and manage bounties easily through both in-game commands and configuration files.

## Features

- Set bounties on players manually through commands.
- Bounties are saved to a `bounties.yml` file for persistence across server restarts.
- Bounty leaderboard (UI) to display active bounties.
- Customizable bounty values for players.
- Supports custom events for bounty hunting with particles and sounds.

## Installation

1. **Download the plugin jar** from this repository's releases section.
2. **Place the jar file** into your server's `plugins` folder.
3. **Restart your server** to enable the plugin.

## Configuration

- The plugin automatically generates a `bounties.yml` file inside the plugin folder after the first run.
- Bounties for players are stored in this file using their **UUID** as the key.
- The plugin will read and write bounty data to this file every time a bounty is set or the server shuts down.

## Default Configuration (Example)

```yaml
# bounties.yml
uuid_of_player_1: 100
uuid_of_player_2: 200
uuid_of_player_3: 150

**THIS PLUGIN IS IN HEAVY BETA AND DOES NOT WORK AS INTENDED AT THIS TIME.**
