### Database Changes
- Added 3 new database types with SQLite replacing users/data.yml as the default database type, You can configure this in the config.yml!
  - Postgres
  - MariaDB
  - SQLite (default)

*You will need to migrate using the /crazyvouchers migrate -mt DataBaseMigration command!*

Do not delete your data.yml or users.yml!

If you have any issues with migrating, or issues with the new database system. Submit a bug report!

### Changes
- Added new command which is /crazyvouchers history <player> <type> <page>
- Added message for when you reach the voucher/code limit to messages.yml.
- Added ability to send offline player vouchers with the ability to get them on join.
  - This is disabled by default, and can be enabled in the `config.yml` via `settings.offline-player-support` (no this is not about online-mode=false)
  - Added message for when an offline player successfully gets added to the database.
  - If the player's inventory is full, it will add until it can no longer add, and simply wait until their inventory is empty, or they run /crazyvouchers load.
- Fixed multiple bugs with the limiter feature.
- Updated to 26.3