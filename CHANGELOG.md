### Breaking Changes:
- Identifying a voucher has changed, new vouchers will use a much more lightweight implementation to check if it's a voucher or not.
  - In the next version of minecraft, the old way for checking vouchers **will** be removed, so please prepare for that by cleaning up old vouchers.

### Additions:
- Added a toggle for dupe protection which assigns uuids to new vouchers
  - Once a voucher is used, the uuid gets thrown in the data.yml
  - If a voucher is used with the same uuid again. it will tell the player no and notify all staff
  - Previous vouchers already given, won't be accounted with this, so you'll just have to phase those out.
- Added a new permission, crazyvouchers.notify.duped which notifies any player who has the permission of duped vouchers

### Fixed:
- Fixed the migrator on startup