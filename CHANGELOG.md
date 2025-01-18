remorphed 6.0
================

- fix player blacklist handling
- **rework menu**
- change key to open the menu to "B"
- don't display variants by default

remorphed 5.0
================

- port to 1.21.2
- fix menu not loading in async mode
- add turkish by Feitan_Portor

remorphed 4.2
================

- add config option for loading menu asynchronously

remorphed 4.1
================

- fix crash on 1.20.5+ server

remorphed 4.0
================

- merge to new PreProcessor, CraftedCore 5 & Walkers 5
- add support for SkinShifter
- re-implement async menu loading

remorphed 3.6.3
================

- fix crash when morphing back and unlockFriendlyNormal is true
- use new walkers API Levels & use PlayerMorph.class

remorphed 3.6.2
================

- update to CraftedCore 4.2 & Walkers 4.4.2
- temporarily disable caching thread in menu to prevent wrong rendering

remorphed 3.6.1
================

- fix issues with entity caching in menu

remorphed 3.6
================

- selected entity is rendered first
- add compatibility for walkers 4.2 (and variants menu)
- skills can renderer multiple times per entity if required (e.g. the MobEffectSkill can render multiple icons now)

remorphed 3.5
================

- change entity orientation in menu
- fix sync error
- fix render scissor (no more entities in the menu)
- some skills render the icon (toggleable)

remorphed 3.4
================

- fix crash on startup for 1.20.2+
- player data will be properly restored on death now (required re-login before)
- fix only craftedcore 3.0 possible on Fabric

remorphed 3.3
================

- menu loads entities in the background
- menu only renders visible entities (fixes low FPS in the menu)
- fix every entity available in survival

remorphed 3.0
================
First of all, this update is required to make the mod work with Woodwalkers v3+.
Furthermore, this implements a feature which was originally introduced by identity- entity favourites! I hope you like
the implementation.
Of course, I've also fixed the search-algorithm, so you can search for toolbox infos, too.
There is also a brand new config option to change "killToUnlock" values per entity type.
Last but not least, the Special Shape implementation is way more advanced, you consider to donate to check it out ;D.
As always, there are some background improvements, too.