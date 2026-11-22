### Database Changes
- Added 3 new database types with SQLite replacing users/data.yml as the default database type, You can configure this in the config.yml!
  - Postgres
  - MariaDB
  - SQLite (default)

*You will need to migrate using the /crazyvouchers migrate -mt DataBaseMigration command!*

Do not delete your data.yml or users.yml!

If you have any issues with migrating, or issues with the new database system. Submit a bug report!

### Changes
- Added message for when you reach the voucher/code limit to messages.yml.
- Fixed multiple bugs with the limiter feature.
- Updated to 26.3