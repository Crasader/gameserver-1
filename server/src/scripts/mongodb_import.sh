#!/bin/sh
export PREFIX=server0001
echo "Export configuration data from MongoDB"
mongoimport --host localhost --db babywar --collection $PREFIX.gamedata --upsert --file ../mongo/gamedata.json 
mongoimport --host localhost --db babywar --collection $PREFIX.battletools --upsert --file ../mongo/battletools.json 
mongoimport --host localhost --db babywar --collection $PREFIX.equipments --upsert --file ../mongo/equipments.json 
mongoimport --host localhost --db babywar --collection $PREFIX.maps --upsert --file ../mongo/maps.json 
mongoimport --host localhost --db babywar --collection $PREFIX.items --upsert --file ../mongo/items.json 
mongoimport --host localhost --db babywar --collection $PREFIX.shops --upsert --file ../mongo/shops.json 
mongoimport --host localhost --db babywar --collection $PREFIX.tasks --upsert --file ../mongo/tasks.json 
mongoimport --host localhost --db babywar --collection $PREFIX.dailymarks --upsert --file ../mongo/dailymarks.json 
mongoimport --host localhost --db babywar --collection $PREFIX.loginlotteries --upsert --file ../mongo/loginlotteries.json 
mongoimport --host localhost --db babywar --collection $PREFIX.tips --upsert --file ../mongo/tips.json 

