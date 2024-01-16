# Potioncraft



## Data Driven Cauldroning

This mod has fully customizable recipes!  
This is achieved via json datapacks.  

### Inversions?
Inversions specify how effects change when "inverted".  
The field is named `"inversions"` and contains a json array of objects.
These objects have three fields:

* `"from"` - Identifier of a potion effect
* `"to"` - Identifier of a potion effect
* `"mutual"` - (Optional = false) boolean

`"from"` specifies what the start effect is, and `"to"` specifies the result.  
`"mutual"` is used to specify that `"to"` -> `"from"` also applies.

#### Example
```json
{
  "inversions" : [
    {
      "from": "minecraft:speed",
      "to": "slowness",
      "mutual": true
    },
    {
      "from": "instant_health",
      "to": "instant_damage"
    }
  ]
}
```

When inverted, Speed becomes Slowness and vice versa. Instant Health becomes Instant Damage, but not vice versa

### Heat!
Heats allow you to place a block under a cauldron and change it's heat level!
The field is named `"heats"` and contains a json object with blocks and their heats.  
Blocks that are not present have a heat of zero.

#### Example
```json
{
  "heats": {
    "lava": 10000000,
    "stone": 0,
    "blue_ice": -100
  }
}
```

When placed atop lava, a cauldron will have a heat of `10000000`, `0` with stone, and `-100` with blue ice.

### Recipes and You

Recipes are more complex, they have several components.
The top-level field is named `"recipes"` which may contains several cauldron block identifiers.  
It is very important that these blocks are considered cauldrons in some way.
Then inside these are several item identifiers which then have an `"effects"` json array which contains the behavior.

An effect contains json objects with the following layout:

* `"id"`: What behavior to use
* `"quickfail"`: (Optional) On what event should the behavior fail to occur
* `"params"`: (Optional?) Parameters for that behavior

`quickfail` refers to the result of the previous behavior, for the first behavior this is always `"SUCCESS"`.  
These can also be any of the other Minecraft "action results", but Potioncraft will only ever return `"SUCCESS"` or `"PASS"`.  
As implied above, the last behavior gives it's result to minecraft to determine if the player's hand should swing.

#### Example
```json
{
  "recipes": {
    "potioncraft:potion_cauldron_block": {
      "minecraft:glass_bottle": {
        "effects": [
          {
            "id": "USE_ITEM",
            "params": {
              "id": "potion",
              "sound": "item.bottle.fill",
              "applyPotion": true
            }
          },
          {
            "id": "REMOVE_LEVEL"
          }
        ]
      }
    }
  }
}
```

When a glass bottle is used on a potion cauldron (so there is some liquid with potion effects), the bottle is consumed and a potion is given out with the effects. Also a sound plays and the cauldron decreases in fluid level.

#### Behaviors

| Identifier             | Description                                                                            | Parameters                                                                                                                                                                                                                                                      |
|------------------------|----------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `"FORCE_SWING_HAND"`   | Always returns `"SUCCESS"`                                                             | `quickfail` can be used to cause failure still                                                                                                                                                                                                                  |
| `"INVERT_COND"`        | Inverts the previous return value, `"SUCCESS"` becomes `"PASS"` and vice versa         | `quickfail` can be used still                                                                                                                                                                                                                                   |
| `"IS_FROM_VANILLA"`    | Returns `"SUCCESS"` when the block is from vanilla                                     | `quickfail` can be used still                                                                                                                                                                                                                                   |
| `"HAS_LEVEL"`          | Returns `"SUCCESS"` when the cauldron contains fluid                                   | `"level"`: (Optional) integer - only returns `"SUCCESS"` if the fluid level is equal to this parameter                                                                                                                                                          |
| `"HAS_HEAT"`           | Returns `"SUCCESS"` when the cauldron has a heat level more extreme then the parameter | `"heat"`: (Optional = 1) integer - heat level to compare against. When 0, cauldron heat must also be 0                                                                                                                                                          |
| `"IS_FULL"`            | Returns `"SUCCESS"` when the cauldron is full                                          | `quickfail` can be used still                                                                                                                                                                                                                                   |
| `"ITEM_HAS_EFFECTS"`   | Returns `"SUCCESS"` when the used item has potion effects                              | `quickfail` can be used still                                                                                                                                                                                                                                   |
| `"USE_ITEM"`           | Uses the item in the player's hand                                                     | `"id"`: (Optional) Identifier - item to replace with. `"applyPotion"`: (Optional = false) Boolean - should the replacement item have effects. `"sound"`: (Optional) Identifer - sound to play. `"count"`: (Optional = 1) integer - amount of items to take/swap |
| `"PLAY_SOUND"`         | Plays a sound                                                                          | `"id"`: Identifier - sound to play                                                                                                                                                                                                                              |
| `"CLEAR_EFFECTS"`      | Clears all effects from a cauldron                                                     |                                                                                                                                                                                                                                                                 |
| `"INVERT_EFFECTS"`     | Inverts all effects in a cauldron                                                      |                                                                                                                                                                                                                                                                 |
| `"ADD_STATUS_EFFECT"`  | Adds a status effect to a cauldron                                                     | `"id"`: Identifier - potion effect to add. `"duration"`: (Optional = 1.0) Decimal - duration of effect. `"amplifier"`: (Optional = 0.0) Decimal - effect level. `"showParticles"`: (Optional = true) Boolean. `"showIcon"`: (Optional = true) Boolean           |
| `"ADD_POTION_EFFECT"`  | Adds a potion's first effect to a cauldron                                             | `"id"`: Identifier - potion to add first effect of                                                                                                                                                                                                              |
| `"APPLY_ITEM_EFFECTS"` | Adds the effects of the used item to a cauldron                                        |                                                                                                                                                                                                                                                                 |
| `"ADD_LEVEL"`          | Adds a fluid level to the cauldron, will return `"PASS"` when cauldron is full         | `"dilute"`: (Optional = true) Boolean - If dilution should occur                                                                                                                                                                                                |
| `"REMOVE_LEVEL"`       | Removes a fluid level from the cauldron, will return `"PASS"` when cauldron is empty   |                                                                                                                                                                                                                                                                 |
| `"AMPLIFY"`            | Evenly distributes effect level to all the effects in a cauldron                       | `"amplifier"`: (Optional = 3.0) Decimal - amplifier to distribute                                                                                                                                                                                               |
| `"EXTEND"`             | Evenly distributes effect duration to all the effects in a cauldron                    | `"duration"`: (Optional = 6000.0) Decimal - duration to distribute                                                                                                                                                                                              |

Mods can even add more if they want!  
Using `quickfail` you can do basic boolean logic, `&&` is fairly simple! `||` can be done using DeMorgan's Laws.