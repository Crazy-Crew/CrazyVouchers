## Changes
- Added a new configuration option `has-argument` to vouchers/codes which was a result of fixing `{arg}` not being replaced in commands.
- Added a new configuration option to the `config.yml` that allows you to choose whether to allow off-hand usage or not.
- Added a new permission `crazyvouchers.bypass.2fa` which lets you bypass 2fa when right-clicking.
- Added a new configuration option `override-anti-dupe` to vouchers which lets you override the anti dupe option in the config.yml
  - It defaults to false, which is the default behavior out of the box.
- Added a new configuration option `allow-vouchers-in-item-frames` to vouchers, which lets your players place vouchers in item frames if set to true.
- `chance-commands` and `random-commands` have been combined, They were not working properly anyway. We opted for a weight based system when picking random commands, The lower the number. The less likely the command(s) will be run.
- You can specify one command in the list, or multiple. It's up to you!
- If there is no weight option present, It will be considered how `random-commands` functioned before, which is run separately and has no relation to the weight based options.
- You will need to run `/crazyvouchers migrate -mt VouchersDeprecated` for the changes to take effect, You will see console nagging regarding outdated configurations until you do.
```yml
  random-commands:
    "1":
      # The chance for these to run
      weight: 45.0
      # The commands to run
      commands:
        - "eco give {player} 100"
    "2":
      # The chance for these to run
      weight: 25.0
      # The commands to run
      commands:
        - "eco give {player} 1000"
    # No chance is provided, thus we will pick random commands.
    "6":
      # The commands to run
      commands:
        - "give {player} diamond 5" 
```

## Technical Changes
- Cleaned up internals when creating a voucher, or a code.
  - This allows us to add newer features easier as it's more readable, and less prone to human error.
- `{random}:1-10000` has been improved, and has better logging when a problem happens i.e. due to invalid numbers being used.
- Updated the interaction internals to account for off-hand usage by using PlayerInventory & EquipmentSlots

## Bugs Fixed
- Voucher Codes when broken were being put in the wrong list which could lead to confusion when viewing what broke.
- `{arg}` was not being replaced when a command was being run.
  - We no longer check the name/lore if `{arg}` is present which in hindsight was a bad idea.
  - You can manually add this to your existing configurations that do use the `{arg}` variable.
      - [Example Config](https://github.com/Crazy-Crew/CrazyVouchers/blob/6ff270683a140c0e1b3b6d84cbee5bfac5408f3f/paper/src/main/resources/vouchers/Example-Arg.yml#L32)