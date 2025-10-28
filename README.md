# ChatGames

A Minecraft plugin that adds interactive chat-based minigames to engage your server's players with trivia, math challenges, word scrambles, and reaction games.

## Features

- **Multiple Game Types:**
  - **Trivia** - Answer trivia questions correctly
  - **Math** - Solve math problems
  - **Unscramble** - Unscramble words
  - **Reaction** - Click buttons quickly (with customizable variants)

- **Automatic Game Scheduling** - Games start automatically at configurable intervals
- **Customizable Rewards** - Configure commands to run when players win (economy rewards, items, etc.)
- **Fully Configurable** - Customize questions, timeouts, rewards, and messages
- **PlaceholderAPI Support** - Use placeholders in messages
- **Multi-language Support** - Customize all messages in `language.yml`

## Requirements

- **Minecraft Server:** Paper/Spigot 1.16+
- **Optional Dependencies:**
  - VaultUnlocked (or Vault) - For economy rewards
  - PlaceholderAPI - For placeholder support

## Configuration

### Main Config (`config.yml`)

```yaml
# Game control settings
min-players: 1          # Minimum players online to start games
game-interval: 300      # Seconds between games (300 = 5 minutes)
logging: "FULL"         # Log level: FULL, PARTIAL, NONE
```

### Game Configuration

Each game has its own config file in `plugins/ChatGames/games/`:

- `trivia.yml` - Trivia questions and answers
- `math.yml` - Math problems
- `unscramble.yml` - Words to scramble
- `reaction.yml` - Clickable button variants

**Example game config structure:**

```yaml
name: "Math"
descriptor: "solve"
timeout: 30

reward-commands:
  - eco give %player% 250
  - give %player% diamond 1

questions:
  - ["What is 1+4", "5"]
  - ["What is 10 + 12", "22"]
```

**Placeholders in rewards:**
- `%player%` - Winner's username
- `{player}` - Winner's username (alternative format)

### Language Configuration (`language.yml`)

Customize all plugin messages. Available placeholders:
- `{prefix}` - Plugin prefix
- `{player}` - Player name
- `{name}` - Game name
- `{descriptor}` - Game descriptor (e.g., "solve", "unscramble")
- `{question}` - The question/challenge
- `{answer}` - The correct answer
- `{timeout}` - Timeout in seconds
- `\n` - New line

## Commands

| Command             | Permission         | Description               |
|---------------------|--------------------|---------------------------|
| `/chatgames reload` | `chatgames.reload` | Reload all configurations |
| `/chatgames`        | `chatgames.help`   | Show help information     |

## Permissions

- `chatgames.reload` - Allows reloading the plugin
- `chatgames.help` - Allows viewing help information

## Setting Up Rewards

### With VaultUnlocked (or Vault)

```yaml
reward-commands:
  - eco give %player% 250
```

### With Items

```yaml
reward-commands:
  - give %player% diamond 5
  - give %player% emerald 10
```

### Multiple Commands

You can run multiple commands per win:

```yaml
reward-commands:
  - eco give %player% 500
  - give %player% golden_apple 3
  - lp user %player% permission set special.perk true
```

## Adding Custom Questions

### Trivia Game

Edit `games/trivia.yml`:

```yaml
questions:
  - ["What is the capital of France?", "Paris"]
  - ["What year did WW2 end?", "1945"]
```

### Math Game

Edit `games/math.yml`:

```yaml
questions:
  - ["What is 15 + 27?", "42"]
  - ["If x + 5 = 12, what is x?", "7"]
```

### Unscramble Game

Edit `games/unscramble.yml`:

```yaml
words:
  - "minecraft"
  - "diamond"
  - "adventure"
```

### Reaction Game

Edit `games/reaction.yml`:

The Reaction game supports **multiple variants** - each with its own clickable button text. Players compete to click the button first!

```yaml
variants:
  - name: "Click Game"
    challenge: "&a&lClick Me!"
    answer: ""

  - name: "Color Game"
    challenge: "&c[Red] &a[Green] &c[Red] &7- Click the &aGreen &7button"
    answer: ""

  - name: "Fast Click"
    challenge: "&b&l⚡ &f&lFAST CLICK! &b&l⚡"
    answer: ""
```

**Variant fields:**
- `name` - Display name for the variant type
- `challenge` - The clickable text shown to players (supports color codes with `&`)
- `answer` - Leave empty `""` for click-based games

When a Reaction game starts, the plugin randomly selects one variant. Add as many custom variants as you want without modifying any code!

## Troubleshooting

**Games not starting:**
- Check `min-players` setting in `config.yml`
- Verify enough players are online
- Check console for errors

**Rewards not working:**
- Ensure VaultUnlocked (or Vault) is installed for economy commands
- Verify placeholder format is `%player%` or `{player}`
- Check console for command errors

**Questions not loading:**
- Verify YAML syntax in game config files
- Use `/chatgames reload` after making changes
- Check console for parsing errors
