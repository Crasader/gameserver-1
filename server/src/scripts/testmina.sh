#!/bin/sh
FILES=../test/java/com/xinqihd/sns/gameserver/proto/Test*.java
for f in $FILES
do 
	echo "Processing $f file..."
	filename=`echo "$f" | cut -d/ -f9`
	echo $filename
	#sed -f ./mina.sed < $f > ../main/java/com/xinqihd/sns/gameserver/handler/$filename.txt
	sed -i "" -f ./testmina.sed $f 
	#mv  ../main/java/com/xinqihd/sns/gameserver/handler/$filename.txt  ../main/java/com/xinqihd/sns/gameserver/handler/$filename
done

