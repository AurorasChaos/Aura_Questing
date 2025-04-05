# 🗺️ Quest System for Paper 1.21.4

An advanced, extensible quest system for Minecraft servers (Paper 1.21.4), built with full support for:
- Multi-tier quests (Daily, Weekly, Global)
- Multi-stage progression logic (AND/OR)
- Modular quest listeners
- PlaceholderAPI, Vault, and AuraSkills integration
- Full Bedrock support via Geyser and Bedrock Forms (WIP)

![Quest GUI Example](https://your.image.hosting/quest-gui.png)

---

## 🔧 Features

✅ Daily, Weekly, and Global quest tiers  
✅ Dynamic GUI with animated shimmer borders  
✅ Paginated quest interface with filters and quest rarity icons  
✅ Global quest progress shared server-wide  
✅ Leaderboard with DecentHolograms integration  
✅ Reward system supporting:  
• Vault (economy)  
• AuraSkills XP (per skill)  
• Skill point bonuses  
✅ Reset system with configurable intervals  
✅ YAML-based configuration for quests and rewards  
✅ Debug logging mode (`Debug: true` in config.yml)  
✅ Bedrock-compatible via Geyser (with optional Bedrock UI)

---

## 📦 Features

### ✅ Core Quest Engine
- **Over 300 handcrafted quests** across all tiers and types
- Types include: `KILL_MOB`, `MINE_BLOCK`, `FISH`, `TAME_ENTITY`, `CONSUME_ITEM`, `EXPLORE_BIOME`, and more
- Quest rewards: in-game currency, skill XP, items, and cosmetic flair

### 🧠 Multi-Stage Quest Logic (NEW)
> Currently under implementation

Quests can now have multiple stages defined in **ordered** or **unordered** logic groups. These can include:
- Nested `AND`/`OR` logic for complex objectives
- Subquest references (like quest chains)
- Custom completion hooks (e.g., dialogue, cutscenes)

```yaml
stages:
  logic: AND
  groups:
    - logic: OR
      list:
        - type: MINE_BLOCK
          target: STONE
          amount: 100
        - type: MINE_BLOCK
          target: COBBLESTONE
          amount: 100
```

🖼️ _[Insert logic tree diagram image here]_

---

## 🧱 Architecture

```mermaid
flowchart TD
    A[Player Action] --> B[Listener (e.g. KillMobListenerV2)]
    B --> C[QuestManager]
    C --> D{Quest Type}
    D -->|Legacy| E[Direct Progress]
    D -->|Multi-Stage| F[QuestProgressEvaluator]
    F --> G[Stage Tree Evaluation]
```

---

## 🧩 Dependencies

- [Vault](https://www.spigotmc.org/resources/vault.34315/) (for economy support)
- [AuraSkills](https://www.spigotmc.org/resources/auraskills.92541/) (for RPG skill rewards)
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) (optional placeholders)
- [DecentHolograms](https://www.spigotmc.org/resources/decentholograms.96927/) (for leaderboard holograms)
- [Citizens](https://www.spigotmc.org/resources/citizens.13811/) (optional NPC support)

---

## 🎯 Supported Quest Types

| Type            | Description                            |
|-----------------|----------------------------------------|
| `KILL_MOB`      | Kill specific mobs                     |
| `GATHER_ITEM`   | Break or gather specific blocks/items  |
| `CRAFT_ITEM`    | Craft specific items                   |
| `WALK_DISTANCE` | Walk a certain distance                |
| `EXPLORE_BIOME` | Visit specific biomes                  |
| `CONSUME_ITEM`  | Eat or drink specific items            |
| `GAIN_SKILL_EXP`| Gain AuraSkills XP                    |
| `GAIN_SKILL_LEVEL` | Level up a skill in AuraSkills     |
| `FISH`          | Catch fish                             |
| `PLACE_BLOCK`   | Place specific blocks                  |
| `MINE_BLOCK`    | Mine specific blocks                   |
| `LOOT`          | Pickup items                           |
| `TRADE`         | Trade with villagers                   |
| `BREED_ANIMAL`  | Breed animals                          |
| `SMELT_ITEM`    | Smelt items in furnaces                |
| `TAME_ENTITY`   | Tame pets (wolves, cats, etc.)         |
| `CUSTOM`        | Reserved for custom event hooks        |

---

## 📊 Leaderboards

Top 5 players by total claimed quest completions (with support for holograms):  
- Integrated with DecentHolograms  
- Supports refresh/update cycles  
- Future-ready for expansion (per quest, per type, seasonal, etc.)

---

## 📁 Example Configuration

```yaml
Debug: true

ResetTimes:
  Daily: "06:00"
  Weekly: "Monday 06:00"

Currency:
  RewardAmount: 100

Hologram:
  Enabled: true
  Location: world,100.5,65,200.5

Skills:
  Enabled: true
```

---

## 🛒 Future Features

### 🗣️ Dialogue + Cutscenes
Quests will support stage-triggered:
- Text sequences via Citizens
- Cinematic cutscenes
- In-game commands via `onComplete`

### 🧾 Quest Shop System
Players can spend earned currency and reputation to:
- Purchase new quests
- Unlock quest chains
- Upgrade rewards

### 📱 Bedrock Support (via Geyser & Bedrock Forms)
All quest GUIs will offer fallback to Bedrock-native interfaces for Geyser-connected players.

🖼️ _[Insert mockup of Bedrock quest form]_

---

## 📈 Development Roadmap

| Feature                    | Status      |
|---------------------------|-------------|
| Daily/Weekly/Global quests| ✅ Complete |
| Multi-stage quest engine  | 🚧 In Progress |
| AND/OR stage logic        | ✅ Prototype implemented |
| Dialogue triggers         | 🔜 Planned |
| Bedrock Forms GUI         | 🔜 Planned |
| Quest Shop                | 🔜 Planned |
| Subquest references       | ✅ Design ready |
| Scoreboard API Integration| ✅ With UniversalScoreboard |
| Debug & Logging Tools     | ✅ Toggleable in config.yml |

---

## 🤝 Contributing

We welcome pull requests and issue discussions!  
To contribute:
1. Fork the repo
2. Create a feature branch
3. Submit your PR with a description of what you changed and why

---

## 🧠 Powered By

> Built from the ground up with extensibility and immersion in mind.  
> Designed to scale across player levels, gameplay styles, and modded environments.

---

### 📸 Screenshots (To Be Added)

- Quest GUI
- Bedrock Forms mockup
- Multi-stage progress tracker
- Debug logs with stage progression

---

### 📜 License
MIT License — see [LICENSE.md](LICENSE.md)