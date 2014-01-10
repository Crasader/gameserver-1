#!/bin/sh
FILES=../main/java/com/xinqihd/sns/gameserver/handler/*.java
for f in $FILES
do 
	echo "Processing $f file..."
	filename=`echo "$f" | cut -d/ -f9`
	echo $filename
	#sed -f ./mina.sed < $f > ../main/java/com/xinqihd/sns/gameserver/handler/$filename.txt
	sed -i "" -f ./mina.sed  $f 
done

