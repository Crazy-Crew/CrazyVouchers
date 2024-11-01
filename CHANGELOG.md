### Changes:
- Check if player is null on /voucher give, and return early.

### Fixed:
- The argument was not being applied to the voucher, due to checking if it was empty. We have to check if it's not empty.