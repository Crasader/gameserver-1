#!/bin/sh
FILES=../main/protoc/huaqing/*.proto
for f in $FILES
do 
	echo "Processing $f file..."
	filename=`echo "$f" | cut -d/ -f5`
	sed -f ./protob_sed.sed < $f > ../main/protoc/xinqihd/$filename
done
mv ../main/protoc/xinqihd/BseUserData.proto ../main/protoc/xinqihd/UserData.proto
mv ../main/protoc/xinqihd/BseUserExData.proto ../main/protoc/xinqihd/UserExData.proto
mv ../main/protoc/xinqihd/BseUserInfo.proto ../main/protoc/xinqihd/UserInfo.proto

