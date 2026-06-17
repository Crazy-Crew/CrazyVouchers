## Changes 🔨
### New Commands
- Added /crazyvouchers version which includes the git commit, previous git commit, and current version.
    - If any value is not found, it will return "N/A"

### Plugin Support
**Note: replace `<item_id>` with the correct identifier from the custom item plugins.**
**Second Note: Any place you can input `diamond_axe` allows you to use this format.**
**Third Note: You can still use `emerald_helmet` as in direct ids as well**

- Added support for HMCWraps.
  - `item: hmcwraps@<item_id>`
- This introduces new formats for other plugins like Oraxen, Nexo, CraftEngine, and ItemsAdder
  - `item: nexo@<item_id>`
  - `item: oraxen@<item_id>`
  - `item: itemsadder@<item_id>`
  - `item: craftengine@<item_id>`

### Configuration Changes
#### Glowing
- Glowing no longer is true/false, however existing configurations using true/false will still work as we look for that internally as well.
```yaml
  # Should the item glow?
  # Available Types: add_glow, remove_glow, none
  Glowing: "none"
```
#### Two-Factor Authentication
- Added the ability to customize the two-factor message *per* voucher/code
  - If the message in the voucher/code file is not found, it falls back to the default message in messages.yml
```yaml
    # Enables the ability to require a player to confirm claiming the voucher.
    two-factor:
      # If this should be enabled.
      toggle: false
      # The messages to send to confirm.
      message:
        - "{prefix}<gray>Right click again to confirm that you want to use this voucher." 
```

## Bugs Fixed 🐛
- Vouchers could be used in recipes due to using return instead of continue.
- world#dropItem was not wrapped in Folia Scheduler which led to incompatibility on Folia.
- Vouchers/Codes not refreshing when running the migrators.
- The plugin jar being in a directory with a space in it would cause startup errors.
- Custom Heads with HeadDatabase were not working.
- Weighted random commands were not working properly to invalid filters.
- Commands with no weights were not running due to an invalid filter.
- {random}:minimum-maximum placeholder now works properly, and allows for multiple replacements in a command.

As always, Report 🐛 to https://github.com/Crazy-Crew/CrazyVouchers/issues