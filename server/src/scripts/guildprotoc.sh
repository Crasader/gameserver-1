#!/bin/bash

export EXT_DIR=../../src/main/protoc/guild
export XINQI_DIR=../../src/main/protoc/xinqihd
export Handler=Handler
export HANDLER_DIR=com/xinqihd/sns/gameserver/handler
export TEST_DIR=com/xinqihd/sns/gameserver/proto
export LUA_DIR=../../../../client/game/Build/Script

#Generate protoc java code 
genjavacode() {
	mkdir -p ../gensrc/java
	for file in $EXT_DIR/*.proto
	do
		echo "generate java code for $file"
		protoc --java_out=../gensrc/java --proto_path=$EXT_DIR --proto_path=$XINQI_DIR $file
	done
}

#Generate protoc lua code 
genluacode() {
	mkdir -p ../gensrc/lua
	for file in $EXT_DIR/*.proto
	do
		echo "generate lua code for $file"
		echo protoc --lua_out=../gensrc/lua -I$EXT_DIR $file
		protoc --lua_out=../gensrc/lua -I$EXT_DIR -I$XINQI_DIR $file
	done
	
	#This method should only be called after Google pb compiler generated all lua scripts
	modifyrequirepath
	cd -
	#cp -p ../gensrc/lua/ProtocolMgr.lua $LUA_DIR
	#cp -p ../gensrc/lua/*.lua $LUA_DIR
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

#Generate handler and test cases
genhandlerandtests() {
	mkdir -p ../gensrc/java/$HANDLER_DIR
	mkdir -p ../test/java/$TEST_DIR

	let num=0;
	for file in $EXT_DIR/Bce*.proto
	do
		class=`echo $file | sed -e 's/^.*Bce//' -e's/.proto//' `
		echo "Generating $class"
		genhandler > ../gensrc/java/$HANDLER_DIR/Bce$class$Handler.java
		#gentest >    ../test/java/$TEST_DIR/TestBce$class.java
		let num+=1
		#break;
	done
}

genhandler() {
	echo "package com.xinqihd.sns.gameserver.handler;"
	echo ""
	echo "import org.slf4j.Logger;"
	echo "import org.slf4j.LoggerFactory;"
	echo "import org.apache.mina.core.session.IoSession;"
	echo ""
	echo "import com.xinqihd.sns.gameserver.proto.*;"
	echo "import com.xinqihd.sns.gameserver.transport.*;"
	echo ""
	echo "/**"
	printf " * The Bce%sHandler is used for protocol %s \n" $class $class
	echo " * @author wangqi"
	echo " *"
	echo " */"
	printf "public class Bce%sHandler extends SimpleChannelHandler {\n" $class
	echo "	"
	printf "	private Log log = LogFactory.getLog(Bce%sHandler.class);\n" $class
	echo "	"
	printf "	private static final Bce%sHandler instance = new Bce%sHandler();\n" $class $class
	echo "	"
	printf "	private Bce%sHandler() {\n" $class
	echo "		super();"
	echo "	}"
	echo ""
	printf "	public static Bce%sHandler getInstance() {\n" $class
	echo "		return instance;"
	echo "	}"
	echo ""
	echo "	@Override"
	echo "	public void messageReceived(IoSession session, Object message)"
	echo "			throws Exception {"
	echo "		if ( logger.isDebugEnabled() ) {"
	printf "			logger.debug(\"->Bce%s\");\n" $class
	echo "		}"
	echo "		XinqiMessage request = (XinqiMessage)message;"
	echo "		XinqiMessage response = new XinqiMessage();"
	#if [ -f "Bse$class.proto" ]; then
	#	printf "		XinqiBse%s.Bse%s.Builder builder = XinqiBse%s.Bse%s.newBuilder();\n" $class $class $class $class
	#	echo "		response.payload = builder.build();"
	#else 
	#	printf "    //TODO Bse%s not exists\n" $class
	#	printf "		//XinqiBse%s.Bse%s.Builder builder = XinqiBse%s.Bse%s.newBuilder();\n" $class $class $class $class
	#	echo "		//response.payload = builder.build();"
	#fi
	#echo "		//TODO BEGIN add logic here."
	#printf "		//builder.setUid(((XinqiBce%s.Bce%s)request.payload).getUid());\n" $class $class
	#echo "		//END"
	#echo "		session.write(response);"
	echo "	}"
	echo "	"
	echo "	"
	echo "}"
}

gentest() {
    printf "package com.xinqihd.sns.gameserver.proto;\n"
    printf "\n"
    printf "import static org.junit.Assert.*;\n"
    printf "\n"
    printf "import org.apache.commons.logging.Log;\n"
    printf "import org.apache.commons.logging.LogFactory;\n"
    printf "import org.apache.mina.core.future.WriteFuture;\n"
    printf "import org.apache.mina.core.session.IoSession;\n"
    printf "import org.apache.commons.logging.LogFactory;\n"
    printf "import org.junit.After;\n"
    printf "import org.junit.Before;\n"
    printf "import org.junit.Test;\n"
    printf "\n"
    printf "import com.google.protobuf.MessageLite;\n"
    printf "import com.xinqihd.sns.gameserver.transport.MessageToId;\n"
    printf "import com.xinqihd.sns.gameserver.transport.XinqiMessage;\n"
    printf "\n"
    printf "public class TestBce%s extends ProtoBufSetup {\n" $class
    printf "	\n"
    printf "	private static final Log log = LogFactory.getLog(TestBce%s.class);\n" $class
    printf "	\n"
    printf "	@Before\n"
    printf "	public void setUp() throws Exception {\n"
    printf "		super.setUp(XinqiBce%s.Bce%s.getDefaultInstance(), this);\n" $class $class
    printf "	}\n"
    printf "\n"
    printf "	@After\n"
    printf "	public void tearDown() throws Exception {\n"
    printf "		super.tearDown();\n"
    printf "	}\n"
    printf "\n"
    printf "	@Test\n"
    printf "	public void testBce%s() {\n" $class
    printf "		XinqiBce%s.Bce%s.Builder msg = XinqiBce%s.Bce%s.getDefaultInstance().newBuilderForType();\n" $class $class $class $class
    printf "		//TODO add your test logic here\n"
	printf "        //XinqiBceLogin.BceLogin request = msg.setUsername("test").setPassword("pass").build(); \n"
	printf "		XinqiBce%s.Bce%s request = msg.build();\n" $class $class
    printf "		sendRequest(request);\n"
    printf "	}\n"
    printf "	\n"
    printf "  /**\n"
    printf "   * Invoked when a message object (e.g: {@link ChannelBuffer}) was received\n"
    printf "   * from a remote peer.\n"
    printf "   */\n"
    printf "  public void messageReceived(\n"
    printf "          IoSession session, Object message) throws Exception {\n"
    printf "      XinqiMessage response = (XinqiMessage)message;\n"
    printf "      assertEquals(1, response.index);\n"
    printf "      //TODO check your test result here\n"
	if [ -f "Bse$class.proto" ]; then
		printf "      assertEquals(null, ((XinqiBse%s.Bse%s)response.payload));\n" $class $class
	else 
		printf "    //assertEquals(null, ((XinqiBse%s.Bse%s)response.payload));\n" $class $class
	fi
    printf "  }\n"
    printf "	\n"
    printf "	/**\n"
    printf "	 * Send a message to server.\n"
    printf "	 * @param msg\n"
    printf "	 */\n"
    printf "	public void sendRequest(MessageLite msg) {\n"
    printf "		XinqiMessage request = new XinqiMessage();\n"
    printf "		request.index = 0;\n"
    printf "		request.payload = msg;\n"
    printf "		request.type = MessageToId.messageToId(msg);\n"
    printf "		WriteFuture future = session.write(request);\n"
    printf "		future.awaitUninterruptibly();\n"
    printf "		if ( !future.isWritten() ) {\n"
    printf "			future.getException().printStackTrace();\n"
    printf "			fail(\"testBce%s failed\");\n" $class
    printf "		}\n"
    printf "	}\n"
    printf "\n"
    printf "}\n"
}

genjavacode
genluacode

./genIdMessageClass.sh
#./genIdMessageLua.sh
export luapath=../../../../client/game/Build/Script
cp -p ../gensrc/lua/ProtocolMgr.lua $luapath
cp -p ../gensrc/lua/*.lua $luapath

#genhandlerandtests
