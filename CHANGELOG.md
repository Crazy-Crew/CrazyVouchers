### Notice:
- Once 1.21.2 drops, NBT-API will be stripped from the plugin.
  - This is your second warning to migrate old vouchers players have.
- All old vouchers will stop working once NBT-API is removed!
- The migration for old configurations has been removed.
  - If you need to migrate, use 3.7.4 as the migration works there.

### Future Changes:
- MiniMessage Support
  - There will be a migration from Legacy Color Codes -> MiniMessage
- Updates to configurations with materials, sounds, and how items work
  - Internally, all materials, and trims will be handled internally, so need not worry about that.
  - Sounds will have to be migrated manually, not much I can do about that.

### Changes:
- Allowed switching between file systems on `/crazyvouchers reload`
  - There will not be a migration to switch between.
- Updated vital api

### Fixes:
- Fixed an issue on Folia [#73](https://github.com/Crazy-Crew/CrazyVouchers/issues/73)