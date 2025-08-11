- Fixed another issue with the LegacyColorMigrator.
- Added new config option to vouchers.yml / vouchers folder
```yml
voucher:
  # The material.
  item: 'diamond_helmet'
  # An example of how to use custom model data
  custom-model-data: -1 
```
```yml
vouchers: # Where you make your vouchers.
  # The name of the voucher.
  money:
    # The material.
    item: 'diamond_helmet'
    # An example of how to use custom model data
    custom-model-data: -1
```
- Added a new migrator type called `VouchersDeprecated` which at the moment only migrates old custom model data usage to the format above.
  - The condition to migrate is that `item` must follow the format `diamond_helmet#1347`
- Fixed startup issue with old custom model data format i.e. `diamond_helmet#1347`
- Added more verbose logging to all existing migrators.
- Fixed issues with whitelist/blacklist checks.
