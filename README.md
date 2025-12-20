# ChatGames

A Minecraft plugin that adds interactive chat-based minigames to engage your server's players with trivia, math challenges, word scrambles, and reaction games.

## Why use ChatGames?

- Instant, lightweight chat minigames that keep your chat active.
- Multi-platform: same core works across Spigot, Paper, Folia, and Sponge via a platform abstraction layer.
- Fully configurable games, schedules, messages and reward commands.
- Small, well-documented, and easy to extend.

## Features

- **Multiple Game Types:**
  - **Trivia** - Answer trivia questions correctly
  - **Math** - Solve math problems
  - **Unscramble** - Unscramble words
  - **Reaction** - Click buttons quickly (with customizable variants)


- **Automatic Game Scheduling** - Games start automatically at configurable intervals
- **Customizable Rewards** - Configure commands to run when players win (economy rewards, items, etc.)

## Quick Install

1. Drop the plugin `.jar` into your server's `plugins/` folder.
2. Start the server once to generate default configurations.
3. Edit `config.yml`, `en-us.yml`, and the individual game files.

## Configuration

### Main Configuration

<details>
  <summary>config.yml</summary>

  ```yml
# ChatGames Configuration
# https://github.com/RareHyperIon/ChatGames

# Available: en-us
language: en-us

# Interval between automatic games (in seconds)
game-interval: 300

# Minimum players online to start automatic games
minimum-players: 1

# Whether to automatically start games at intervals
automatic-games: true

# Cooldown after wrong answer in multiple choice (in ticks, 20 ticks = 1 second)
answer-cooldown-ticks: 60

# Enable debug logging
debug: false
  ```
</details>

<details>
  <summary>language/en-us.yml</summary>

  ```yml
# ChatGames Language File - English (US)

# Message shown when a player tries to use a command they don't have access to
permission: "<red>You don't have permission to use this command.</red>"

# Message shown when ChatGames has been successfully reloaded
reload: "<green>Successfully reloaded ChatGames!</green>"

# Cooldown message when player tries to answer too quickly after wrong answer
cooldown: "<red>You cannot answer this question as you've already tried recently.</red>"
  ```
</details>

### Game Configuration

<details>
  <summary>games/math.yml</summary>

  ```yml
name: math
display-name: "<gold>Math Wizard</gold>"
type: math
timeout: 60

reward-commands:
  - "give {player} diamond {rand:1-3}"

messages:
  start: |

    <gold><bold>MATH WIZARD</bold></gold>
    <gray>Solve the equation below!</gray>

  win: |

    <green><bold>✓</bold> <yellow>{player}</yellow> solved it!</green>
    <gray>Answer: <white>{answer}</white></gray>

  timeout: |

    <red><bold>✗</bold> Time's up!</red>
    <gray>Answer: <white>{answer}</white></gray>


questions:
  - ["<yellow>12 + 8 = ?</yellow>", "20"]
  - ["<yellow>25 - 7 = ?</yellow>", "18"]
  - ["<yellow>6 × 7 = ?</yellow>", "42"]
  - ["<yellow>144 ÷ 12 = ?</yellow>", "12"]
  - ["<yellow>15 + 23 = ?</yellow>", "38"]
  - ["<yellow>50 - 18 = ?</yellow>", "32"]
  - ["<yellow>9 × 8 = ?</yellow>", "72"]
  - ["<yellow>100 ÷ 5 = ?</yellow>", "20"]
  ```
</details>

<details>
  <summary>games/multiple-choice.yml</summary>

  ```yml
name: multiple choice
display-name: "<green>Multiple Choice</green>"
type: multiple-choice
timeout: 45
cooldown: 60 # Ticks to wait after wrong answer (60 ticks = 3 seconds)

reward-commands:
  - "give {player} emerald {rand:1-3}"

messages:
  start: |

    <green><bold>MULTIPLE CHOICE</bold></green>
    <gray>Type the correct letter!</gray>

  win: |

    <green><bold>✓</bold> <yellow>{player}</yellow> chose correctly!</green>
    <gray>Answer: <white>{answer}</white></gray>

  timeout: |

    <red><bold>✗</bold> Nobody answered!</red>
    <gray>Correct: <white>{answer}</white></gray>


questions:
  q1:
    question: "<yellow>What is the largest planet in our solar system?</yellow>"
    answers:
      - "A. Earth"
      - "B. Jupiter"
      - "C. Saturn"
      - "D. Mars"
    correct-answer: "B"

  q2:
    question: "<yellow>Which programming language is Minecraft written in?</yellow>"
    answers:
      - "A. Python"
      - "B. C++"
      - "C. Java"
      - "D. JavaScript"
    correct-answer: "C"

  q3:
    question: "<yellow>What is 2 + 2 × 2?</yellow>"
    answers:
      - "A. 6"
      - "B. 8"
      - "C. 4"
      - "D. 10"
    correct-answer: "A"

  q4:
    question: "<yellow>Which mob drops blaze rods?</yellow>"
    answers:
      - "A. Ghast"
      - "B. Blaze"
      - "C. Wither Skeleton"
      - "D. Magma Cube"
    correct-answer: "B"
  ```
</details>

<details>
  <summary>games/reaction.yml</summary>

  ```yml
name: reaction
display-name: "<red>Reaction Test</red>"
type: reaction
timeout: 30

reward-commands:
  - "give {player} diamond {rand:1-3}"

messages:
  start: |
    
    <red><bold>REACTION TEST</bold></red>
    <gray>Be the first to respond!</gray>

  win: |

    <green><bold>✓</bold> <yellow>{player}</yellow> was fastest!</green>

  timeout: |

    <red><bold>✗</bold> Nobody reacted in time!</red>
    

# Variants allow different reaction challenges
# If answer is empty string "", any message wins (fastest typer)
# If answer is specified, must type that exact word
# If answer is "CLICK", then it will be clickable.
#   - Optional: Add hover="Your text" for custom hover message
#   - Example: <button hover='Click me!'>Press here</button>
#   - Without hover attribute, no hover text will be shown
#   - You can include colors in the hover attribute.
variants:
  - name: "Type Fast"
    challenge: "<gold><bold>Type: <yellow>MINECRAFT</yellow></bold></gold>"
    answer: "MINECRAFT"

  - name: "First Word"
    challenge: "<gold><bold>Type any word now!</bold></gold>"
    answer: ""

  - name: "Click Fast"
    challenge: "<button hover='Click to win!'><gold><bold>Click me to win!</bold></gold></button>"
    answer: "CLICK"

  - name: "Color"
    challenge: "<gold><bold>Type: <yellow>RED</yellow></bold></gold>"
    answer: "RED"
  ```
</details>

<details>
  <summary>games/trivia.yml</summary>

  ```yml
name: trivia
display-name: "<aqua>Trivia Time</aqua>"
type: trivia
timeout: 45

reward-commands:
  - "give {player} emerald {rand:1-3}"

messages:
  start: |

    <aqua><bold>TRIVIA TIME</bold></aqua>
    <gray>Answer the question below!</gray>

  win: |

    <green><bold>✓</bold> <yellow>{player}</yellow> got it right!</green>
    <gray>Answer: <white>{answer}</white></gray>

  timeout: |

    <red><bold>✗</bold> Nobody got it!</red>
    <gray>Answer: <white>{answer}</white></gray>


questions:
  - ["<yellow>What is the capital of France?</yellow>", "Paris"]
  - ["<yellow>How many continents are there?</yellow>", "7"]
  - ["<yellow>What year did the Titanic sink?</yellow>", "1912"]
  - ["<yellow>What is the largest ocean?</yellow>", "Pacific"]
  - ["<yellow>Who painted the Mona Lisa?</yellow>", "Leonardo da Vinci"]
  - ["<yellow>What is the speed of light?</yellow>", "299792458"]
  - ["<yellow>What is H2O commonly known as?</yellow>", "Water"]
  ```
</details>

<details>
  <summary>games/unscramble.yml</summary>

  ```yml
name: unscramble
display-name: "<light_purple>Word Scramble</light_purple>"
type: unscramble
timeout: 60

reward-commands:
  - "give {player} gold_ingot {rand:1-5}"

messages:
  start: |
    <light_purple><bold>WORD SCRAMBLE</bold></light_purple>
    <gray>Unscramble the word!</gray>

  win: |

    <green><bold>✓</bold> <yellow>{player}</yellow> unscrambled it!</green>
    <gray>Word: <white>{answer}</white></gray>

  timeout: |

    <red><bold>✗</bold> Time ran out!</red>
    <gray>Word: <white>{answer}</white></gray>

questions:
  - ["<yellow>TANESRIPHO</yellow>", "SMARTPHONE"]
  - ["<yellow>TMUPOECR</yellow>", "COMPUTER"]
  - ["<yellow>YGAMINL</yellow>", "GAMING"]
  - ["<yellow>EATKBOY</yellow>", "KEYBOARD"]
  - ["<yellow>IAMFRENTC</yellow>", "MINECRAFT"]
  - ["<yellow>DAMONDI</yellow>", "DIAMOND"]
  - ["<yellow>NTDAUEERV</yellow>", "ADVENTURE"]
  ```
</details>

## Supported Platforms & Versions

| Platform | Supported Versions |
|----------|--------------------|
| Spigot   | 1.13 – 1.21.x      |
| Paper    | 1.20.6 – 1.21.x    |
| Folia    | 1.20.6 – 1.21.x    |
| Sponge   | 1.21.x             |

## Commands

| Command                   | Permission         | Description                      |
|---------------------------|--------------------|----------------------------------|
| `/chatgames reload`       | `chatgames.reload` | Reload all configurations        |
| `/chatgames start <game>` | `chatgames.start`  | Manually start a game            |
| `/chatgames stop`         | `chatgames.stop`   | Stop the current game            |
| `/chatgames list`         | `chatgames.list`   | List all available games         |
| `/chatgames info`         | `chatgames.info`   | Show plugin information          |
| `/chatgames toggle`       | `chatgames.toggle` | Toggle automatic games on or off |


## Permissions

| Permission         | Description                        |
|--------------------|------------------------------------| 
| `chatgames.reload` | Allows reloading the plugin        |
| `chatgames.start`  | Allows manually starting a game    |
| `chatgames.stop`   | Allows stopping the current game   |
| `chatgames.list`   | Allows listing all available games |
| `chatgames.info`   | Allows viewing plugin information  |
| `chatgames.toggle` | Allows toggling automatic games    |


## Troubleshooting

**Games not starting:**
- Check `minimum-players` setting in `config.yml`
- Verify enough players are online
- Check console for errors

**Rewards not working:**
- Verify placeholder format is `{player}`
- Check console for command errors

**Questions not loading:**
- Verify YAML syntax in game config files
- Use `/chatgames reload` after making changes
- Check console for parsing errors
