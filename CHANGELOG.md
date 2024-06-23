### Added:
- Added support for HeadDatabaseAPI
```yaml
voucher:
  # The material.
  item: 'PLAYER_HEAD'
  # The name of the item.
  name: '&cA player head example'
  # The lore of the item.
  lore:
    - '&7Use this voucher to get fancy trims.'
    - '&7&l(&6&l!&7&l) &7Right click to redeem.'
  # Only uncomment this if using HeadDatabase by Arcaniax
  skull: '61151'
  # The items to give when claiming the voucher.
  items:
    # Only uncomment this is using HeadDatabase by Arcaniax
    - 'Item:PLAYER_HEAD, Skull:61151, Name:&cA fancy head, Lore:&eA fancy lore,&7with lines, Amount:3, Glowing:true'
```