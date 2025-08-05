### Please report any bugs to our GitHub issues tab!
https://github.com/Crazy-Crew/CrazyVouchers/issues

## New Items section
I've made a new `Items` section which is currently in use in CrazyCrates, I am simply bringing it to CrazyVouchers.

You can experiment with this option by enabling it in your `config.yml`

- The command /crazyvouchers migrate -mt NewItemFormat will update all `Items` section to the best it can.
  - The migration is one way, Please be wary of this and take backups.

## Legacy Color Codes
Legacy color codes are no longer supported, They have been replaced by MiniMessage, https://docs.advntr.dev/minimessage/format

### Migrate
- https://toolbox.helpch.at/converters/legacy/minimessage
- https://www.birdflop.com/resources/rgb/
  - Use the "Decode" option labeled experimental, I complained about legacy color code garbage about 2 years ago, they remembered and pinged me.
- /crazyvouchers migrate -mt VouchersColor which will try it's best to convert them.

### Other migration types
We offer migration types for the following

- VouchersDeprecated
  - Any deprecated option will be replaced/removed with this
- VouchersNbtApi
  - Migrates old vouchers to new vouchers
- VouchersSwitch
  - Switches between single and multiple files
- VouchersRename
  - Renames voucher-codes.yml to codes.yml, This takes a backup of the previous file.

## Permissions
Permissions are no longer the same, Permissions were renamed and the old nodes were always meant to be a temporary usage
This allows more control over what you can give a player.

`/vouchers` and `/voucher` are considered aliases for `/crazyvouchers`

### Commands
| New                     | Old               | Command                  | Aliases                |
|-------------------------|-------------------|--------------------------|------------------------|
| `crazyvouchers.giveall` | `vouchers.admin`  | `/crazyvouchers giveall` | `N/A`                  |
| `crazyvouchers.migrate` | `vouchers.admin`  | `/crazyvouchers migrate` | `N/A`                  |
| `crazyvouchers.reload`  | `vouchers.admin`  | `/crazyvouchers reload`  | `N/A`                  | 
| `crazyvouchers.redeem`  | `vouchers.redeem` | `/crazyvouchers redeem`  | `N/A`                  |
| `crazyvouchers.types`   | `vouchers.admin`  | `/crazyvouchers types`   | `/crazyvouchers list`  |
| `crazyvouchers.help`    | `vouchers.admin`  | `/crazyvouchers help`    | `N/A`                  |
| `crazyvouchers.open`    | `vouchers.admin`  | `/crazyvouchers open`    | `/crazyvouchers admin` |
| `crazyvouchers.give`    | `vouchers.admin`  | `/crazyvouchers give`    | `N/A`                  |
| `crazyvouchers.access`  | `vouchers.admin`  | `/crazyvouchers`         | `N/A`                  | 

### Other
| New                          | Old                     |
|------------------------------|-------------------------|
| `crazyvouchers.notify.duped` | `vouchers.notify.duped` |
| `crazyvouchers.bypass`       | `vouchers.bypass`       |

## Other Changes
### Trim Material
#### Before
```yml
voucher:
  # The items to give when claiming the voucher.
  items:
    - 'Item:DIAMOND_HELMET, Trim-Pattern:SENTRY, Trim-Material:QUARTZ, Amount:1'
    - 'Item:DIAMOND_CHESTPLATE, Trim-Pattern:DUNE, Trim-Material:REDSTONE, Amount:1'
```

#### After
```yml
voucher:
  # The items to give when claiming the voucher.
  items:
    - 'Item:DIAMOND_HELMET, Trim:SENTRY!QUARTZ, Amount:1'
    - 'Item:DIAMOND_CHESTPLATE, Trim:DUNE!REDSTONE, Amount:1'
```
### Enchantments
| New                     | Old                        |
|-------------------------|----------------------------|
| `protection`            | `PROTECTION_ENVIRONMENTAL` |
| `fire_protection`       | `PROTECTION_FIRE`          |
| `feather_falling`       | `PROTECTION_FALL`          |
| `blast_protection`      | `PROTECTION_EXPLOSIONS`    |
| `projectile_protection` | `PROTECTION_PROJECTILE`    |
| `respiration`           | `OXYGEN`                   |
| `aqua_affinity`         | `WATER_WORKER`             |
| `sharpness`             | `DAMAGE_ALL`               |
| `smite`                 | `DAMAGE_UNDEAD`            |
| `bane_of_arthropods`    | `DAMAGE_ARTHROPODS`        |
| `looting`               | `LOOT_BONUS_MOBS`          |
| `sweeping`              | `SWEEPING_EDGE`            |
| `efficiency`            | `DIG_SPEED`                |
| `unbreaking`            | `DURABILITY`               |
| `fortune`               | `LOOT_BONUS_BLOCKS`        |
| `power`                 | `ARROW_DAMAGE`             |
| `punch`                 | `ARROW_KNOCKBACK`          |
| `flame`                 | `ARROW_FIRE`               |
| `infinity`              | `ARROW_INFINITE`           |
| `luck_of_the_sea`       | `LUCK`                     |

- No longer rely on ItemMeta anymore which means a large performance increase due to the nature of how ItemMeta works.
- Commands have been overhauled which will make adding new commands easier!
- Replaced plugin.yml with paper-plugin.yml.
- Added support for item models which serves as a replacement for `Custom Model Data`
- Fixed an issue with anti-craft not working.
- `Updated to 1.21.8`
```yml
voucher:
  # Any generic component for the Item.
  components:
    # Hides the entire tooltip
    hide-tooltip: false
    # Hides components defined in this list. It allows more control than above.
    # Leave the hide-tooltip false if you plan to use this.
    hide-tooltip-advanced:
      - "enchantments"
    # The item model, Mojang introduced this in 1.21.4... this replaces custom model data!
    # Set this to blank for it to do nothing.
    item-model:
      # The namespace i.e. nexo
      namespace: ""
      # The key i.e. emerald_helmet
      key: "" 
```