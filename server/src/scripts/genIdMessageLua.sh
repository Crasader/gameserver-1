#!/bin/bash
# Generate Id message mapping for Lua script.
genidtomessage() {
	echo "require(\"com/xinqihd/common/base.lua\")"
	echo "require(\"com/xinqihd/common/PkgMap.lua\")" 

	cd $path
	for file in *.proto
	do
		class=`echo $file | sed -e 's/.proto//'`
		printf "require(\"com/xinqihd/bombbaby/protocol/%s_pb.lua\")\n" $class
	done
	echo ""

	#Generate ID mapping
	let num=512;
	for file in Bse*.proto
	do
		class=`echo $file | sed -e 's/.proto//'`
		printf "ID_%s = %4d\n" $class $num 
		let num+=1
	done
	let num=1024;
	for file in Bce*.proto
	do
		class=`echo $file | sed -e 's/.proto//'`
		printf "ID_%s = %4d\n" $class $num 
		let num+=1
	done

	echo "ProtocolMgr = class(\"ProtocolMgr\")"
	echo "function ProtocolMgr:InitPkgMap()"
	let num=512;
	for file in Bse*.proto
	do
		class=`echo $file | sed -e 's/.proto//'`
		printf "    self._pkgMap:AddPkg(%4d, %s_pb.%s());\n" $num $class $class
		let num+=1
	done
	let num=1024;
	for file in Bce*.proto
	do
		class=`echo $file | sed -e 's/.proto//'`
		printf "    self._pkgMap:AddPkg(%4d, %s_pb.%s());\n" $num $class $class
		let num+=1
	done

	echo "end"
	echo

	echo "function ProtocolMgr:initialize()"
	echo "  self._pkgMap = PkgMap:new()"
	echo "  self:InitPkgMap()"
	echo "end"
	echo ""
	echo "function ProtocolMgr:GetPkgMap()"
	echo "  return self._pkgMap"
	echo "end"
}

#Note: It should only be called after the Google PB compiler generated all the scripts
modifyrequirepath() {
	cd ../gensrc/lua/
	for file in *.lua
	do
		if [ $file == ProtocolMgr.lua ]; then
			echo $file
		else
			sed -i '' -e 's/require("/require(\"com\/xinqihd\/bombbaby\/protocol\//' $file
			sed -i '' -e 's/_pb")/_pb.lua")/' $file
		fi
	done
	echo ""
}

#Create the tmp dir
export path=../../target/proto
export luapath=../../../../client/game/Build/Script
cp -pr ../main/protoc/xinqihd/*.proto $path/
cp -pr ../main/protoc/extend/*.proto $path/
mkdir -p $path
mkdir -p $luapath
genidtomessage > ../gensrc/lua/ProtocolMgr.lua
cd -
#This method should only be called after Google pb compiler generated all lua scripts
modifyrequirepath
cd -
cp -p ../gensrc/lua/ProtocolMgr.lua $luapath
cp -p ../gensrc/lua/*.lua $luapath/com/xinqihd/bombbaby/protocol

