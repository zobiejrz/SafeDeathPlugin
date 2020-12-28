# SafeDeath

> A minecraft plugin built for TheFuturesMind servers

## Features

* Puts player items in double or single chest on death depending on the number of items in the inventory
* Gives player a piece of paper with coordinates pointing to their death
* Grave protections to keep other players from opening/destroying others graves
* Chest farming protections
* Configuration file

## Configurations

> Found in config.yml

|           Value          |                                                                      Description                                                                      |
| ------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------  |
| shouldSendWelcome        | Whether or not ```welcomeNewPlayerMessage``` and ```welcomeMessage``` are sent on player connection                                                   |
| welcomeNewPlayerMessage  | The string broadcast when a new player joins (**NOTE**: the joining player's name will be broadcast immediately proceeding with no additional spaces) |
| welcomeMessage           | The string broacast when a returning player joins (**NOTE**: see previous note)                                                                       |
| shouldPayRespects        | Whether or not ```payRespectMessage``` is sent on player death                                                                                        |
| payRespectMessage        | The string broadcast if ```shouldPayRespects``` is set to true                                                                                        |
| shouldLogDeathInv        | Whether or not player inventories are logged on death (Useful in case of inventory loss due to bugs or server errors)                                 |
| shouldSendPaper          | Whether or not death coordinates are sent to players on respawn                                                                                       |
| shouldMakeGrave          | Whether or not graves are made on player death                                                                                                        |
| protectedGraves          | Whether or not graves can only be broken/opened by the owner                                                                                          |
| disappearingGraves       | Whether or not graves will disappear when empty or destroyed (**NOTE**: Chest items will drop if this is disabled)                                    |
