# Advancement-Nametag

A Minecraft plugin for Paper/Folia that lets players choose a name tag from their completed advancements.

## Features

- **Advancement-Based Selection:** Players pick name tags by completing advancements.
- **Paginated GUI:** Supports players with many advancements via a multi-page inventory interface.
- **Persistent Storage:** Player selections are saved in a local SQLite database.
- **PlaceholderAPI Support:** Expose a player's selected tag to other plugins via PAPI placeholders.
- **Folia Compatible:** Works on both Paper and Folia servers.

## Requirements

- Java 21+
- Paper or Folia 1.21.1+
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) *(optional)*

## Installation

1. Download the latest JAR from [Releases](https://github.com/xydesu/Advancement-Nametag/releases).
2. Place the JAR in your server's `plugins/` folder.
3. Restart the server.

## Commands

| Command | Description                          | Permission |
|---------|--------------------------------------|------------|
| `/tags` | Open the name tag selection GUI      | *(none)*   |

## Configuration

The `config.yml` file is generated automatically on first run:

```yaml
message:
  reset: "&cYour nametag has been reset."
  set: "&aYour nametag has been set to %tag%."
  not-player: "&cYou must be a player to use this command."
```

Color codes use the `&` prefix (e.g. `&a` for green, `&c` for red).

## PlaceholderAPI

When PlaceholderAPI is installed the following placeholders are available:

| Placeholder                        | Description                                         |
|------------------------------------|-----------------------------------------------------|
| `%advancementnametag_tag%`         | The plain-text name of the player's selected tag.   |
| `%advancementnametag_colored%`     | The color-formatted name of the player's selected tag. |
| `%advancementnametag_hastag%`      | `true` if the player has a tag selected, `false` otherwise. |

## Building from Source

Requires Maven and Java 21+.

```bash
git clone https://github.com/xydesu/Advancement-Nametag.git
cd Advancement-Nametag
mvn clean package
```

The compiled JAR will be located at `target/AdvancementNametag-<version>.jar`.

## Tested Versions

| Platform | Version |
|----------|---------|
| Paper    | 1.21.1  |
| Folia    | 1.21.1  |

> ⚠️ Custom advancements may not display correctly in the GUI.
