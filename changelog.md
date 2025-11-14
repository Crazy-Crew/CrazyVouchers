## Bugs Fixed 🐛
- Fixed multiple issues with `random-commands`
- Fixed multiple issues with the migration types like `VouchersDeprecated`
- Fixed an issue where the old `random-commands` list would be copied as is during migration into one command pool which caused all commands to be run, instead of spread out.
  - This will perform the previous behavior as it was intended now.
  - If you have a backup, load it... and re-run  /crazyvouchers migrate -mt VouchersDeprecated

As always, Report 🐛 to https://github.com/Crazy-Crew/CrazyVouchers/issues