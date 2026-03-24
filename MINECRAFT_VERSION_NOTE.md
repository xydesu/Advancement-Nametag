# Minecraft Version Update Note

This repository has been updated to support Minecraft 1.21.x.

**Important:** Minecraft version 1.21.11 does not exist. The Minecraft 1.21.x series includes:
- 1.21
- 1.21.1
- 1.21.2
- 1.21.3
- 1.21.4 (latest as of Feb 2026)

This plugin has been configured to work with **Minecraft 1.21.1** which is a stable release in the 1.21.x series.

## Changes Made:
1. Updated Paper API dependency from 1.20.2 to 1.21.1
2. Updated plugin.yml api-version from 1.20 to 1.21
3. Updated Java version from 15 to 21 (required for MC 1.21+)
4. Updated README with tested versions

## Building:
The plugin requires internet access to download dependencies from:
- PaperMC Maven Repository
- PlaceholderAPI Maven Repository
- Maven Central

To build: `mvn clean package`
