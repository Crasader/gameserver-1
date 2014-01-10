#!/bin/sh

for file in ../test/config/*.dat
do
	echo $file
	java -cp ../../target/bootstrap-0.1.12.20111017_184010-jar-with-dependencies.jar com.xinqihd.sns.gameserver.config.ZooKeeperUtil -c 192.168.0.201:2181/snsgame/babywar importdat $file
done
