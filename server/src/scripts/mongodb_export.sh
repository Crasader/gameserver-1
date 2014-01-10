#!/bin/sh
export PREFIX=server0001
echo "Export configuration data from MongoDB"
mongoexport --db babywar --collection $PREFIX.gamedata > ../mongo/gamedata.json
mongoexport --db babywar --collection $PREFIX.battletools > ../mongo/battletools.json
mongoexport --db babywar --collection $PREFIX.equipments > ../mongo/equipments.json
mongoexport --db babywar --collection $PREFIX.maps > ../mongo/maps.json
mongoexport --db babywar --collection $PREFIX.items > ../mongo/items.json
mongoexport --db babywar --collection $PREFIX.shops > ../mongo/shops.json
mongoexport --db babywar --collection $PREFIX.tasks > ../mongo/tasks.json
mongoexport --db babywar --collection $PREFIX.dailymarks > ../mongo/dailymarks.json
mongoexport --db babywar --collection $PREFIX.loginlotteries > ../mongo/loginlotteries.json
mongoexport --db babywar --collection $PREFIX.tips > ../mongo/tips.json
