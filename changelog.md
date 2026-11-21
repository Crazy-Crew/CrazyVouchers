## Breaking Changes
*You will need to migrate using the /crazyvouchers migrate -mt DataBaseMigration command!*

Do not delete your data.yml or users.yml!

If you have any issues with migrating, or issues with the new database system. Submit a bug report!

### Changes
- Added a new message for when you reach the voucher/code limit to messages.yml.
- Improved internal configuration checks by switching to Configurate.
- Updated to 26.3

### Fixes
- The wrong configuration options were supplied for the limiter config options.