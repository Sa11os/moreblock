# MoreBlock English User Guide

This guide is written for three types of users:

- Resource creators who want to add custom decorative blocks to modpacks or maps
- Server owners who want to distribute custom blocks to clients or servers
- Mod developers who want to use the MoreBlock API in their own mods

If you only want to get one block working quickly, start with "Quick Start" and "Recommended Folder Structure". If you plan to build content for long-term use, read the whole guide.

## 1. What This Mod Does

MoreBlock reads block packs from `config/moreblock/block` at runtime and automatically registers their models, textures, display settings, and interaction settings as usable blocks.

It is mainly suitable for these scenarios:

- Adding decorative blocks to a modpack without writing a separate mod for each one
- Maintaining one shared set of building assets for a server
- Bringing GeckoLib models made in Blockbench into the game quickly
- Providing a dynamic block rendering and interaction API for other mods

## 2. Requirements

- Minecraft `1.20.1`
- Forge `47.x`
- GeckoLib `4.4.2+`
- Java `17`

In multiplayer, both the client and the server must have MoreBlock installed. If the block pack contents do not match, the player will be blocked from joining.

## 3. What You Need To Prepare

### 3.1 Basic Tools

- `Blockbench`
- The `GeckoLib` plugin for Blockbench
- The MoreBlock Blockbench tool script included in this repository: `tools/blockbench/moreblock_blockbench_tools.js`
- An image editor that can handle PNG files
- A text editor that can save UTF-8 files
- A zip tool

Here is one important note about the `GeckoLib` plugin in `Blockbench`:

- MoreBlock finally reads `*.geo.json` files exported in the `GeckoLib` format
- If your original project is not already a `GeckoLib` project, you usually need to convert it first
- Common cases include Bedrock model projects, regular Java model projects, or any other Blockbench project that cannot directly export a `GeckoLib geo.json`
- These project types cannot be used by MoreBlock directly. You should install the `GeckoLib Models & Animations` plugin in Blockbench, convert the project to `GeckoLib Animated Model`, and then export it through `Export GeckoLib Model`

You can think of it like this:

- The original project format is only your modeling project format
- MoreBlock needs the final `GeckoLib` output format
- The step in the middle is usually "convert the project first, then export"

### 3.2 Minimum Required Assets

To create the most basic imported block, you need at least:

- One `GeckoLib` model file: `*.geo.json`
- One texture file: `*.png`

It is also recommended to prepare:

- One config file: `*.json`
- One item display file: `*-display.json`

### 3.3 File Encoding Suggestions

- Save all JSON files as `UTF-8` without `BOM`
- Keep file names in lowercase English letters, digits, and underscores whenever possible
- It is recommended to use `UTF-8` encoding for zip file names

MoreBlock has some compatibility handling for zip file name encodings, but for long-term maintenance it is still best to keep everything in UTF-8 and avoid cross-platform filename issues.

## 4. Quick Start

### 4.1 First Launch

After launching the game for the first time, MoreBlock will automatically create:

```text
config/moreblock/block
```

It will also generate:

```text
config/moreblock/block/.keep
config/moreblock/block/README.txt
config/moreblock/block/example/example.json
config/moreblock/block/example/example.md
```

Notes:

- `README.txt` explains how the folder is used
- `example` is only an example and will not be loaded as a custom block
- `example.json` and `example.md` can be used as templates

### 4.2 Fastest Way To Test

Put a complete block pack folder into:

```text
config/moreblock/block/
```

For example:

```text
config/moreblock/block/
└─ blue_chair/
   ├─ blue_chair.json
   ├─ blue_chair.geo.json
   ├─ texture.png
   └─ blue_chair-display.json
```

Then restart the game, or restart the client or server, and enter a world to test it.

## 5. Recommended Folder Structure

A recommended block pack structure looks like this:

```text
blue_chair/
├─ blue_chair.json
├─ blue_chair.geo.json
├─ texture.png
└─ blue_chair-display.json
```

What these files mean:

- `blue_chair.json`: block config
- `blue_chair.geo.json`: GeckoLib model
- `texture.png`: texture
- `blue_chair-display.json`: item display settings for hand, GUI, and ground views, optional

## 6. Packaging Formats And Loading Rules

MoreBlock supports two direct input formats:

- Folder
- `zip` archive

### 6.1 Folder Format

The most common method is to place the folder directly in the config directory:

```text
config/moreblock/block/
└─ blue_chair/
   ├─ blue_chair.json
   ├─ blue_chair.geo.json
   └─ texture.png
```

### 6.2 zip Format

You can also place a zip file directly:

```text
config/moreblock/block/
└─ blue_chair_pack.zip
```

Inside the zip, the following layouts are supported:

- Put the config, model, and texture files directly in the archive root
- Put them inside one same-named folder
- Put multiple subfolders in one outer archive
- Mix multiple subfolders and nested zip files in one outer archive

For example, all of these are valid:

```text
blue_chair_pack.zip
├─ blue_chair.json
├─ blue_chair.geo.json
└─ texture.png
```

```text
blue_chair_pack.zip
└─ blue_chair/
   ├─ blue_chair.json
   ├─ blue_chair.geo.json
   └─ texture.png
```

```text
all_blocks.zip
├─ chair_pack/
│  ├─ chair.json
│  ├─ chair.geo.json
│  └─ texture.png
├─ lamp_pack/
│  ├─ lamp.json
│  ├─ lamp.geo.json
│  └─ texture.png
└─ bed_pack.zip
```

### 6.3 How The Mod Decides Whether A Folder Is A Block Pack

A folder is treated as a loadable pack if it matches either of these conditions:

- It contains a valid config JSON file
- It contains both a `*.geo.json` file and a `*.png` file

So the config file is not absolutely required, but it is still strongly recommended because it makes maintenance much easier later.

## 7. Config File Guide

### 7.1 Recommended Workflow

It is recommended that every block pack has its own config file.

If you are using the `Blockbench` tool script included in this project, it is recommended to generate your first JSON through the plugin instead of writing it by hand:

1. Click `File -> Export -> Export MoreBlock Config JSON`
2. In the popup form, fill in the block `id`, Chinese and English names, model file name, texture file name, `display` file name, and parameters such as sitting, lying, and light level
3. Confirm to export a usable MoreBlock config JSON directly

Benefits of this workflow:

- It reduces the chance of typing field names incorrectly
- File names and default values are easier to keep consistent
- It is a good way to generate a template first and then fine-tune it manually

If you write it by hand, a recommended structure looks like this:

```json
{
  "id": "blue_chair",
  "name": {
    "zh_cn": "蓝色椅子",
    "en_us": "Blue Chair"
  },
  "geo": "blue_chair.geo.json",
  "texture": "texture.png",
  "display": "blue_chair-display.json",
  "light_level": 0,
  "supports_sitting": true,
  "seat_height": 0.45,
  "supports_lying": false,
  "lying_height": 0.5,
  "lying_rotation_compensation": 0
}
```

### 7.2 Field Meanings

| Field | Recommended | Description |
| --- | --- | --- |
| `id` | Yes | Short block ID. It will be used when generating the registry name. Lowercase letters, digits, and underscores are recommended. |
| `name.zh_cn` | Yes | Chinese display name. |
| `name.en_us` | Yes | English display name. |
| `geo` | Yes | Model file name, pointing to the `*.geo.json` in the same folder. |
| `texture` | Yes | Texture file name, usually `texture.png`. |
| `display` | Optional | Item display file name. If omitted, the built-in default display settings are used. |
| `light_level` | Optional | Light level, from `0-15`. |
| `supports_sitting` | Optional | Whether right-click sitting is enabled. |
| `seat_height` | Optional | Sitting height. A `0-2` range is recommended. Default is `0.5`. |
| `supports_lying` | Optional | Whether right-click lying is enabled. |
| `lying_height` | Optional | Lying height. A `0-2` range is recommended. Default is `0.5`. |
| `lying_rotation_compensation` | Optional | Rotation compensation for the lying direction, in 90-degree steps. Allowed values are `-3` to `3`. |

### 7.3 Interaction Mutual Exclusion

`supports_sitting` and `supports_lying` are mutually exclusive.

That means:

- Only `supports_sitting` enabled: the block can be sat on
- Only `supports_lying` enabled: the block can be lied on
- Both enabled: neither effect will work

### 7.4 Lying Behavior

When `supports_lying` is enabled:

- At night, the behavior is similar to a bed and can start the sleep flow
- During the day, players can still lie down
- But daytime lying does not skip time or trigger the full sleep effect

### 7.5 Compatible Aliases

The current version also supports some old field names or aliases, for example:

- `block_id`
- `registry_name`
- `key`
- `lightLevel`
- `emission`
- `light`
- `supportsSitting`
- `can_sit`
- `canSit`
- `supportsLying`
- `can_lie`
- `canLie`
- `lyingRotationCompensation`
- `bed_rotation_compensation`

Even so, this guide still recommends the standard names above because they are easier to maintain.

## 8. Blockbench Workflow

### 8.1 Model Format Requirements

MoreBlock reads `GeckoLib` `*.geo.json` files, not regular vanilla block model JSON files.

So the correct workflow is:

1. Open your `.bbmodel` project
2. Click `File -> Plugins`, open the plugin marketplace, search for and install `GeckoLib Models & Animations`
3. Confirm that the plugin was installed successfully
4. Click `File -> Convert Project`, then choose `GeckoLib Animated Model` from the format list
5. Wait for the conversion to finish and confirm that the project is now in a `GeckoLib`-exportable state
6. Click `File -> Export -> Export GeckoLib Model`
7. Export the `*.geo.json` file
8. Prepare the matching texture and organize all files using the MoreBlock block pack format

If you try to import a vanilla model or a regular JSON model directly, MoreBlock will not work as expected.

In one short sentence, the process is:

`Open .bbmodel -> install GeckoLib Models & Animations -> convert the project to GeckoLib Animated Model -> Export GeckoLib Model`

### 8.2 hitbox Bone Rules

MoreBlock reads one bone from the model to build the collision box.

It is recommended that you explicitly prepare a bone group named `hitbox`.

If there is no bone named `hitbox`, MoreBlock falls back to a standard full-block collision box, which means a `1*1*1` collision volume.

### 8.3 Built-In Blockbench Tool Script

The repository already includes a MoreBlock-specific tool script:

```text
tools/blockbench/moreblock_blockbench_tools.js
```

It can do two very useful things:

- Export a MoreBlock config JSON from the current model
- Generate a MoreBlock-compatible `hitbox` bone automatically from the current model

### 8.4 Available Hitbox Generation Modes

The script provides four modes:

- `simple`: one large box, fastest
- `complex`: fast voxel merge
- `quality`: high-quality global largest-box cover
- `greedy`: sliced rectangle merge

Recommended usage:

- For quick prototypes: start with `simple`
- For common furniture and decorative blocks: try `quality` first
- For structured models with clean surfaces: try `greedy`
- For a balance between speed and detail: use `complex`

### 8.5 Naming Suggestions

It is recommended to keep one consistent naming style:

- Blockbench project name: `blue_chair`
- Exported geo file: `blue_chair.geo.json`
- Config file: `blue_chair.json`
- Display file: `blue_chair-display.json`

This makes searching, debugging, and batch maintenance much easier later.

## 9. Item Display File `*-display.json`

The `display` file controls item transform settings in different views, for example:

- In hand
- GUI
- On the ground
- Item frame

If you do not provide this file, MoreBlock will generate a built-in default display setup, so the lack of a display file does not cause the import to fail.

However, it is recommended to create a custom `display` file for models like these:

- Very tall or very flat furniture
- Models that only look good when rotated sideways
- Models that look awkward in GUI with the default angle

## 10. Full Workflow From Assets To In-Game

Here is one recommended practical workflow:

1. Finish the model in Blockbench
2. Install the GeckoLib plugin and export `*.geo.json`
3. Use the MoreBlock script to generate `hitbox`
4. Prepare `texture.png`
5. Export or write `*-display.json` as needed
6. Write or generate the block config JSON
7. Put the whole folder or zip file into `config/moreblock/block`
8. Restart the client or server
9. Enter the game and verify the result through the creative inventory or commands

## 11. What Gets Registered After Import

After a successful import, MoreBlock automatically generates at runtime:

- The block
- The corresponding item
- Runtime resource pack content
- Language file content
- Blockstates and empty model files

The actual registry name of an imported pack is not exactly the same as the `id` you wrote. It goes through MoreBlock's internal naming rules and usually looks like this:

```text
moreblock:config_block_blue_chair
```

If a name conflict occurs, a number is appended automatically.

So when you write docs, compatibility scripts, or debugging notes, it is best to keep "display name" and "registry name" separate.

## 12. Multiplayer And Sync Rules

MoreBlock verifies the imported block pack manifest between the client and the server.

The check is not based only on file names. It generates a fingerprint from:

- Registry name
- Display name
- Config file content
- Model file content
- Display file content
- Texture file content

If any of these differ, the pack may be considered mismatched.

### 12.1 Server Usage Suggestions

- Install the same MoreBlock version on both server and client
- Use exactly the same block packs on both sides
- The safest method is to distribute the exact same folder or zip file to every client

### 12.2 What Happens When Packs Do Not Match

If the client is missing packs, has extra packs, or has different pack contents:

- The connection may be blocked during login
- Or the player may be disconnected after joining when verification fails

This is a normal protection mechanism, not a bug.

## 13. In-Game Commands

The current version provides two commonly used commands:

- `/moreblock block list`
- `/moreblock block check`

### 13.1 `/moreblock block list`

Lists all imported blocks that are currently loaded.

This is useful for confirming:

- Whether the pack was detected
- How many blocks were imported
- What the registry names roughly look like

### 13.2 `/moreblock block check`

Checks whether the item in your main hand is an imported MoreBlock item and shows whether it comes from:

- A folder
- Or a zip archive

This is very useful when you want to know which pack a specific block was loaded from.

## 14. Common Troubleshooting

### 14.1 The Block Did Not Load

Check these first:

- Whether the files were placed in `config/moreblock/block`
- Whether the model is a `GeckoLib` `*.geo.json`
- Whether there is a usable `png` in the same folder
- Whether the `geo`, `texture`, and `display` file names in the config are correct
- Whether the JSON files are saved as `UTF-8` without `BOM`

### 14.2 I Got Kicked From A Server

Check these first:

- Whether the client and server are using the same MoreBlock version
- Whether the imported packs are exactly the same on the client and server
- Whether someone changed the JSON, texture, geo, or display files

### 14.3 The Block Display Name Is Wrong

Check these config fields:

- `name.zh_cn`
- `name.en_us`

If neither is set, MoreBlock falls back to another available name.

### 14.4 The Model Can Be Placed But The Collision Is Wrong

Check:

- Whether there is a bone named `hitbox`
- Whether the `hitbox` range matches what you want
- Whether you should regenerate it using `quality` or `greedy`

### 14.5 The Item Looks Wrong In Hand Or GUI

That usually means you should make a custom `display` file instead of relying on the default display settings.

## 15. Recommended Conventions For Content Creators

If you plan to maintain dozens or even hundreds of blocks, it is recommended to standardize these things:

- One folder per block
- A config file for every block
- Always use `texture.png`
- Keep the geo, json, and display names aligned with the block ID
- Always create an explicit `hitbox` for each model
- Package everything into zip files before release so it is easier to sync to clients and servers

This makes version updates, bug fixes, and server pack distribution much easier later.

## 16. API Notes

If you are not putting block packs into `config`, but instead want to register dynamic blocks through code in your own mod, you can use the MoreBlock API.

See the full developer guide here:

```text
docs/api.md
```

Here are the key points first.

### 16.1 What The API Is Good For

Suitable when:

- You are already writing your own Forge mod
- You want to reuse MoreBlock's dynamic model and rendering logic
- You want to register blocks through code instead of importing them from an external config folder

Not suitable when:

- You only want to add a few decorative blocks to a modpack and do not want to write Java code

### 16.2 Where API Block Assets Belong

In API mode, the assets are not placed in `config/moreblock/block`. They stay in your own mod's `resources` directory.

For example, if your `modid` is `examplemod`:

```text
src/main/resources/assets/examplemod/
├─ geo/block/blue_chair.geo.json
├─ textures/block/blue_chair.png
├─ models/item/blue_chair.json
└─ lang/zh_cn.json
```

### 16.3 Minimal Registration Example

```java
public static final RegisteredMoreBlock BLUE_CHAIR = MoreBlockApi.builder("examplemod", "blue_chair")
        .name("Blue Chair", "Blue Chair")
        .resourceBase("geo/block/blue_chair")
        .hitboxBoneName("hitbox")
        .showInMoreBlockTab(true)
        .sitting(0.45d)
        .register();
```

### 16.4 What `resourceBase(...)` Does

If you write:

```java
.resourceBase("geo/block/blue_chair")
```

It automatically resolves to:

- geo: `examplemod:geo/block/blue_chair.geo.json`
- texture: `examplemod:textures/block/blue_chair.png`
- display: `examplemod:models/item/blue_chair.json`

If your resources do not follow this path layout, use:

- `geo(...)`
- `texture(...)`
- `display(...)`

and specify them manually.

### 16.5 Common API Parameters

| Method | Purpose |
| --- | --- |
| `name(zhCn, enUs)` | Sets the Chinese and English display names |
| `resourceBase(path)` | Derives geo, texture, and display paths from one base path |
| `geo(...)` | Manually sets the GeckoLib model resource |
| `texture(...)` | Manually sets the texture resource |
| `display(...)` | Manually sets the item display resource |
| `hitboxBoneName(name)` | Sets the collision bone name |
| `showInMoreBlockTab(value)` | Controls whether the item appears in the MoreBlock creative tab |
| `translucent(value)` | Controls whether the block is rendered as translucent |
| `lightLevel(value)` | Sets light level |
| `sitting(height)` | Enables sitting and sets the sitting height |
| `lying(height, rotationCompensation)` | Enables lying and sets the height and rotation compensation |
| `register()` | Submits the registration |

### 16.6 Actual IDs Registered By The API

Blocks registered through the API use IDs like:

```text
moreblock:examplemod_blue_chair
```

Note that this naming rule is different from config-imported blocks.

### 16.7 What Events The API Can Listen To

The MoreBlock API provides three callback types:

- `MoreBlockEvents.onUseBlock(...)`
- `MoreBlockEvents.onPlaceBlock(...)`
- `MoreBlockEvents.onRemoveBlock(...)`

These correspond to:

- Right-click interaction
- Block placement
- Block removal

If you return one of these values in `onUseBlock`:

- `PASS`: continue MoreBlock's default logic
- `SUCCESS`, `CONSUME`, or `FAIL`: block the default logic

So if you only want to listen without changing behavior, return `PASS`.

### 16.8 Who Handles Languages And Creative Tabs In API Mode

The MoreBlock API registers the dynamic block and its base item for you, but it does not manage all surrounding content for your mod.

You still need to decide:

- How to write your language files
- Whether to put the item into your own creative tab
- Whether to register an extra custom item class

## 17. Recommended Release Workflow

If you want to release a batch of blocks to players or servers, this is a good workflow:

1. Finish the model, texture, config, and display locally
2. Test it in `config/moreblock/block`
3. Use `/moreblock block list` to check whether everything was loaded
4. Use `/moreblock block check` to verify the source
5. Make sure the client and server are using the exact same files
6. Package the final content into zip files for release

## 18. One-Sentence Summary

If you are a content creator, these four points are the most important:

1. The model must be a `GeckoLib` `*.geo.json`
2. There must be at least one `png` texture
3. It is strongly recommended to give every block a config JSON and save it as `UTF-8` without `BOM`
4. In multiplayer, the client and server must use exactly the same block packs

If you follow these four points, the MoreBlock import workflow is usually very smooth.

## Document Version

`v1.0.0`
