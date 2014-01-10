#!/bin/bash

#export EXT_DIR=../../src/main/protoc/extend
export XINQI_DIR=../../src/main/protoc/stat
export Handler=Handler
export HANDLER_DIR=com/xinqihd/sns/gameserver/statserver/handler
export TEST_DIR=com/xinqihd/sns/gameserver/proto
export STAT_DIR=../../../../../statserver/src/main/java
#export LUA_DIR=../../../../client/game/Build/Script

#Generate protoc java code 
genjavacode() {
	mkdir -p ../gensrc/java
	for file in $XINQI_DIR/*.proto
	do
		echo "generate java code for $file"
		protoc --java_out=../gensrc/java --proto_path=$XINQI_DIR $file
	done
}

genhandler() {
	echo "package com.xinqihd.sns.gameserver.statserver.handler;"
	echo ""
	echo "import org.apache.commons.logging.Log;"
    echo "import org.slf4j.Logger;"
    echo "import org.slf4j.LoggerFactory;"
	echo ""
	echo "import org.apache.mina.core.session.IoSession;"
	echo ""
	echo "import com.xinqihd.sns.gameserver.proto.*;"
	echo "import com.xinqihd.sns.gameserver.proto.stat.*;"
	echo "import com.xinqihd.sns.gameserver.proto.stat.*;"
	echo "import com.xinqihd.sns.gameserver.transport.*;"
	echo "import com.xinqihd.sns.gameserver.transport.stat.*;"
	echo ""
	echo "/**"
	printf " * The CStat%sHandler is used for protocol %s \n" $class $class
	echo " * @author wangqi"
	echo " *"
	echo " */"
	printf "public class CStat%sHandler extends StatChannelHandler {\n" $class
	echo "	"
	printf "	private Logger logger = LoggerFactory.getLogger(CStat%sHandler.class);\n" $class
	echo "	"
	printf "	private static final CStat%sHandler instance = new CStat%sHandler();\n" $class $class
	echo "	"
	printf "	private CStat%sHandler() {\n" $class
	echo "		super();"
	echo "	}"
	echo ""
	printf "	public static CStat%sHandler getInstance() {\n" $class
	echo "		return instance;"
	echo "	}"
	echo ""
	echo "	@Override"
	echo "	public void messageProcess(IoSession session, Object message)"
	echo "			throws Exception {"
	echo "		if ( logger.isDebugEnabled() ) {"
	printf "			logger.debug(\"->CStat%s\");\n" $class
	echo "		}"
	printf "    JavaCStat%s.CStat%s request = (JavaCStat%s.CStat%s)message;\n" $class $class $class $class
	echo "	}"
	echo "	"
	echo "	"
	echo "}"
}

genhandlerandtests() {
	cd ../main/protoc/stat/
	let num=0;
	for file in CStat*.proto
	do
		class=`echo $file | sed -e 's/.proto//' -e 's/CStat//'`
		echo "Generating $class"
		genhandler > $STAT_DIR/$HANDLER_DIR/CStat$class$Handler.java
		let num+=1
	done
	cd -
}

genjavacode
mkdir -p ../main/java/$HANDLER_DIR
genhandlerandtests

java -cp ../../target/classes/ com.xinqihd.sns.gameserver.util.StatProtocolBufUtil ../..
