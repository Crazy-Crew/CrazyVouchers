## Legacy Color Codes
Legacy color codes are no longer supported, they have been replaced by MiniMessage, https://docs.advntr.dev/minimessage/format

You can run /crazyvouchers migrate -mt VouchersColor which should convert existing configs to MiniMessage as best as possible

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
- Replaced plugin.yml with paper-plugin.yml