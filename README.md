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
Heats allow you to place a block under a cauldron and change its heat level!
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

### Custom Fluids!

Custom fluid support lets you have leveled versions of any fluid right out of the box, you don't even have to make your own cauldron for it! 
You can also leverage the data driven recipes with that fluid with no code required!  
  
For mod developers, this just means you need to register your fluid and you're all set! 
But, if you have your own cauldron for it, then the following info is very important. 
  
The top-level field `"cauldrons"` 
This field consists of several cauldron identifiers which then have a fluid identifier. 
The fluids in this represents what fluid the cauldron holds. 
  
If the cauldron can hold multiple fluids, then `setBlockMapping` must be called with the appropriate arguments, unfortunately this is not data-driven.

#### Example
```json
{
  "cauldrons": {
    "my_custom_cauldron": "my_custom_fluid"
  }
}
```

There is also a top-level field `"fluids"` which contains fluid identifiers mapping to a Json object with two fields.
This is mainly used when using a fluid identifier instead of a specific cauldron in a recipe, but also allows Potioncraft to turn a potion cauldron into a normal cauldron when it makes sense to do so.  
  
If your cauldrons and fluids map one-to-one, this field may not be necessary since `"cauldrons"` can handle simple mappings.
  
The two fields are `"default"` and `"cauldrons"`. `"default"` describes the cauldron that the potion cauldron will try to transform into, if not specified it will be the first entry in `"cauldrons"` (hopefully).
`"cauldrons"` is an array consisting of cauldron identifers, these specify that the cauldron can hold this fluid for use in recipes.
  
The cauldron speficied by `"default"` does not need to be included in `"cauldrons"`, but it does not hurt to do so.

#### Example
```json
{
  "fluids": {
    "water": {
      "default": "water_cauldron",
      "cauldrons": [
        "water_cauldron",
        "potioncraft:potion_cauldron_block"
      ]
    }
  }
}
```

### Recipes and You

Recipes are more complex, they have several components.
The top-level field is named `"recipes"` which may contains several cauldron block identifiers.  
It is very important that these blocks are derived from AbstractCauldronBlock in some way.
Then inside these are several item identifiers which then have an `"effects"` json array which contains the behavior.  
  
Alternatively, a fluid identifier could be used to address multiple cauldrons at once. However, `Fluid.EMPTY` is not a valid fluid.
When doing this, it is important to keep in mind that the cauldron may not actually contain a different fluid, so `HAS_FLUID` should be used.
  
At the same level as `"effects"`, there can also be a `"potency"`. `"potency"` is Potioncraft's way of balancing recipes.
If a mixture's potency exeeds the maximum potency (determined by the config), the interaction will not occur.
If `"potency"` is not present, it defaults to 0. Setting `"max_potency"` in the config to a negative value will disable the mechanic.


An effect contains json objects with the following layout:

* `"id"`: What behavior to use
* `"quickfail"`: (Optional) On what event should the behavior fail to occur
* `"params"`: (Optional?) Parameters for that behavior

`quickfail` refers to the result of the previous behavior, for the first behavior this is always `"SUCCESS"`.  
These can also be any of the other Minecraft "action results", but Potioncraft will only ever return `"SUCCESS"` or `"PASS"`.  
As implied above, the last behavior gives its result to minecraft to determine if the player's hand should swing.

#### Example
```json
{
  "recipes": {
    "potioncraft:potion_cauldron_block": {
      "minecraft:glass_bottle": {
        "potency": 0,
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

When a glass bottle is used on a potion cauldron (so there is some liquid with potion effects), the bottle is consumed and a potion is given out with the effects. Also a sound plays and the cauldron decreases in fluid level. There is no change in potency.

#### Behaviors

Note: `"SUCCESS"` refers to `ActionResult.CONSUME` when running on the server and `ActionResult.SUCCESS` on the client.

| Identifier             | Description                                                                            | Parameters                                                                                                                                                                                                                                                                                             |
|------------------------|----------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `"FORCE_SWING_HAND"`   | Always returns `"SUCCESS"`                                                             | `quickfail` can be used to cause failure still                                                                                                                                                                                                                                                         |
| `"PASS"`               | Always returns `"PASS"`                                                                |                                                                                                                                                                                                                                                                                                        |
| `"INVERT_COND"`        | Inverts the previous return value, `"SUCCESS"` becomes `"PASS"` and vice versa         | `quickfail` can be used still                                                                                                                                                                                                                                                                          |
| `"AND"`                | Returns `"SUCCESS"` when ALL condition effect returns `"SUCCESS"`                      | `"conditions"`: JsonArray of effects to check the result of. `"short_circuit"`: (Optional = true) Boolean - if true, will stop evaluating conditions when any return `"PASS"`                                                                                                                          |                                                                                               |
| `"OR"`                 | Returns `"SUCCESS"` when ANY condition effect returns `"SUCCESS"`                      | `"conditions"`: JsonArray of effects to check the result of. `"short_circuit"`: (Optional = true) Boolean - if true, will stop evaluating conditions when any return `"SUCESS"`                                                                                                                        |    
| `"IS_FROM_VANILLA"`    | Returns `"SUCCESS"` when the block is from vanilla                                     | `quickfail` can be used still                                                                                                                                                                                                                                                                          |
| `"HAS_LEVEL"`          | Returns `"SUCCESS"` when the cauldron contains fluid                                   | `"level"`: (Optional) integer - only returns `"SUCCESS"` if the fluid level is equal to this parameter                                                                                                                                                                                                 |
| `"HAS_HEAT"`           | Returns `"SUCCESS"` when the cauldron has a heat level more extreme then the parameter | `"heat"`: (Optional = 1) integer - heat level to compare against. When 0, cauldron heat must also be 0                                                                                                                                                                                                 |
| `"IS_FULL"`            | Returns `"SUCCESS"` when the cauldron is full                                          | `quickfail` can be used still                                                                                                                                                                                                                                                                          |
| `"MIN_LEVEL"`          | Returns `"SUCCESS"` when the cauldron has at least `>=` `"level"` level                | `"level"`: integer - level to compare against                                                                                                                                                                                                                                                          |
| `"MAX_LEVEL"`          | Returns `"SUCCESS"` when the cauldron has at most `<=` `"level"` level                 | `"level"`: integer - level to compare against                                                                                                                                                                                                                                                          |
| `"MIN_HEAT"`           | Returns `"SUCCESS"` when the cauldron has at least `>=` `"heat"` heat                  | `"heat"`: integer - heat to compare against                                                                                                                                                                                                                                                            |
| `"MAX_HEAT"`           | Returns `"SUCCESS"` when the cauldron has at most `<=` `"heat"` heat                   | `"heat"`: integer - heat to compare against                                                                                                                                                                                                                                                            |
| `"HAS_FLUID"`          | Returns `"SUCCESS"` when the cauldrons contains any of the `"fluids"`                  | `"fluids"`: (Optional) JsonArray or an Identifier for fluids to check for the prescence of in the cauldron. When not present checks that the present fluid is not `FLuids.EMPTY` (regardless of level of the cauldron).                                                                                |                                                                                                                                       |                                                                                |                                                                                                                                                                                  |
| `"ITEM_HAS_EFFECTS"`   | Returns `"SUCCESS"` when the used item has potion effects                              | `quickfail` can be used still                                                                                                                                                                                                                                                                          |
| `"USE_ITEM"`           | Uses the item in the player's hand                                                     | `"id"`: (Optional) Identifier - item to replace with. `"applyPotion"`: (Optional = false) Boolean - should the replacement item have effects. `"sound"`: (Optional) Identifer - sound to play. `"count"`: (Optional = 1) integer - amount of items to take/swap                                        |
| `"PLAY_SOUND"`         | Plays a sound                                                                          | `"id"`: Identifier - sound to play                                                                                                                                                                                                                                                                     |
| `"CLEAR_EFFECTS"`      | Clears all effects from a cauldron                                                     |                                                                                                                                                                                                                                                                                                        |
| `"INVERT_EFFECTS"`     | Inverts all effects in a cauldron                                                      |                                                                                                                                                                                                                                                                                                        |
| `"ADD_STATUS_EFFECT"`  | Adds a status effect to a cauldron                                                     | `"id"`: Identifier - potion effect to add. `"duration"`: (Optional = 1.0) Decimal - duration of effect. `"amplifier"`: (Optional = 0.0) Decimal - effect level. `"showParticles"`: (Optional = true) Boolean. `"showIcon"`: (Optional = true) Boolean                                                  |
| `"ADD_POTION_EFFECT"`  | Adds a potion's first effect to a cauldron                                             | `"id"`: Identifier - potion to add first effect of                                                                                                                                                                                                                                                     |
| `"APPLY_ITEM_EFFECTS"` | Adds the effects of the used item to a cauldron                                        |                                                                                                                                                                                                                                                                                                        |
| `"ADD_LEVEL"`          | Adds a fluid level to the cauldron, will return `"PASS"` when cauldron is full         | `"dilute"`: (Optional = true) Boolean - If dilution should occur. `"fluid"`: (Optional) Identifier - fluid to try to add, will fail if the cauldron already contains a different fluid unless that fluid is `Fluids.EMPTY`                                                                             |                                             |
| `"REMOVE_LEVEL"`       | Removes a fluid level from the cauldron, will return `"PASS"` when cauldron is empty   |                                                                                                                                                                                                                                                                                                        |
| `"AMPLIFY"`            | Evenly distributes effect level to all the effects in a cauldron                       | `"amplifier"`: (Optional = 3.0) Decimal - amplifier to distribute                                                                                                                                                                                                                                      |
| `"EXTEND"`             | Evenly distributes effect duration to all the effects in a cauldron                    | `"duration"`: (Optional = 6000.0) Decimal - duration to distribute                                                                                                                                                                                                                                     |
| `"IF"`                 | Allows for condition execution of templates.                                           | `"condition"`: JsonObject that is an effect to check the result of. `"then"`: JsonArray of effects that occur when `"condition"` is `"SUCCESS"`. `"else"`: (Optional) JsonArray of effects that occur when condition is `"PASS"`. The result of this effect is always that of the last executed effect |

Mods can even add more if they want!  

### Templates!

Many behaviors are very common to see together, templates allow you group your effects together into a new behavior!
  
The top-level field `"templates"` is a Json object containing template identifiers and templates.
A template is comprised of two fields, `"defaults` and `"effects"`. `"effects"` is nearly identical to how it behaves when making a recipe.
However, it can also contain arguments.
  
Templates can be used like any other behavior, but their id is always `"${" + identifier + "}"`.
Since templates are lazily evaluated, templates can reference other templates! 
However, loops and recursion should be avoided as it will cause a error.

#### Arguments

Arguments allow for your templates to behave exactly like behaviors with `"params"`. A template can have (theoretically) infinite arguments!
Arguments are created with the syntax `"@{" + identifier + "}"`, these can appear as the value of ANY json field but cannot be a json field itself.  
  
`"defaults"` lets you specify the default value of an argument. 
`"defaults"` is a json object which has `identifier` as fields with the value being the default. These values can be ANY well formed json element.
If a argument does not have a default and is not set by `"params"` when the template is used a warning will be logged and the template will fail to apply.  

#### Example

```json
{
  "templates": {
    "use_if_fluid": {
      "defaults": {
        "fluids": "minecraft:water",
        "returned_item": 0
      },
      "effects": [
        {
          "id": "HAS_FLUID",
          "quickfail": "PASS",
          "params": {
            "fluids": "@{fluids}"
          }
        },
        {
          "id": "USE_ITEM",
          "quickfail": "PASS",
          "params": {
            "id": "@{returned_item}"
          }
        }
      ]
    }
  }
}
```
Note that setting `"returned_item"` to `0` as a default mimics having no returned item in this case.

```json
{
  "recipes": {
    "minecraft:water": {
      "milk_bucket": {
        "effects": [
          {
            "id": "${use_if_fluid}",
            "params": {
              "returned_item": "bucket"
            }
          },
          {
            "id": "CLEAR_EFFECTS",
            "quickfail": "PASS"
          }
        ]
      }
    }
  }
}
```