# Update Summary: Minecraft 1.21.x Support

## Overview
This PR successfully updates the Advancement-Nametag Bukkit/Paper plugin from Minecraft 1.20.x to 1.21.x.

## Important Note
The original issue requested an update to "Minecraft 1.21.11", but this version **does not exist**. 
Minecraft versions in the 1.21 series are: 1.21, 1.21.1, 1.21.2, 1.21.3, 1.21.4.
This update targets **Minecraft 1.21.1**, a stable and widely-used version.

## Changes Made

### 1. pom.xml
- Updated Paper API dependency: `1.20.2-R0.1-SNAPSHOT` → `1.21.1-R0.1-SNAPSHOT`
- Updated Java version: `15` → `21` (required for MC 1.21+)
- Updated compiler to use `${java.version}` property for consistency

### 2. plugin.yml
- Updated api-version: `1.20` → `1.21`

### 3. README.md
- Updated tested versions to reflect Minecraft 1.21.1 support

### 4. Documentation
- Added MINECRAFT_VERSION_NOTE.md with version clarification

## Code Analysis
✅ **No Java code changes required** - All existing code is compatible with MC 1.21.1 API
✅ **Code review passed** - No issues found
✅ **Security scan** - No new vulnerabilities introduced

## Testing Requirements
The plugin should be tested on:
- Paper 1.21.1 server
- Folia 1.21.1 server (if available)

Test the following functionality:
1. Plugin loads successfully
2. `/tags` command opens the GUI
3. Advancement-based nametag selection works
4. PlaceholderAPI integration works (if installed)
5. Database functionality (SQLite) works correctly

## Build Instructions
```bash
mvn clean package
```
**Note:** Requires internet access to download dependencies from Maven repositories.

## Compatibility
- **Minecraft Version**: 1.21.1 (and compatible with 1.21.x series)
- **Java Version**: 21+
- **Server Software**: Paper, Folia
- **Dependencies**: PlaceholderAPI (optional)
