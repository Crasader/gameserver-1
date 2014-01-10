#!/bin/bash
Handler=Handler
genhandlerandtests() {
	cd ../../target/proto/
	let num=0;
	for file in Bce*.proto
	do
		class=`echo $file | sed -e 's/.proto//' -e 's/Bce//'`
		echo "Generating $class"
		genhandler > ../../src/gensrc/java/$HANDLER_DIR/Bce$class$Handler.java
		gentest > ../../src/test/java/$TEST_DIR/TestBce$class.java
		let num+=1
		#break;
	done
}

genhandler() {
	echo "package com.xinqihd.sns.gameserver.handler;"
	echo ""
	echo "import org.apache.commons.logging.Log;"
	echo "import org.apache.commons.logging.LogFactory;"
	echo "import org.jboss.netty.channel.Channel;"
	echo "import org.jboss.netty.channel.ChannelHandlerContext;"
	echo "import org.jboss.netty.channel.MessageEvent;"
	echo "import org.jboss.netty.channel.SimpleChannelHandler;"
	echo ""
	echo "import com.xinqihd.sns.gameserver.proto.*;"
	echo "import com.xinqihd.sns.gameserver.transport.XinqiMessage;"
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
	echo "	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)"
	echo "			throws Exception {"
	echo "		if ( log.isDebugEnabled() ) {"
	printf "			log.debug(\"->Bce%s\");\n" $class
	echo "		}"
	echo "		Channel channel = ctx.getChannel();"
	echo "		XinqiMessage request = (XinqiMessage)e.getMessage();"
	echo "		XinqiMessage response = new XinqiMessage();"
	if [ -f "Bse$class.proto" ]; then
		printf "		XinqiBse%s.Bse%s.Builder builder = XinqiBse%s.Bse%s.newBuilder();\n" $class $class $class $class
		echo "		response.payload = builder.build();"
	else 
		printf "    //TODO Bse%s not exists\n" $class
		printf "		//XinqiBse%s.Bse%s.Builder builder = XinqiBse%s.Bse%s.newBuilder();\n" $class $class $class $class
		echo "		//response.payload = builder.build();"
	fi
	echo "		//TODO BEGIN add logic here."
	printf "		//builder.setUid(((XinqiBce%s.Bce%s)request.payload).getUid());\n" $class $class
	echo "		//END"
	echo "		channel.write(response);"
	printf "		System.out.println(\"Bce%s: \" + response);\n" $class
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
    printf "import org.jboss.netty.buffer.ChannelBuffer;\n"
    printf "import org.jboss.netty.channel.ChannelFuture;\n"
    printf "import org.jboss.netty.channel.ChannelHandlerContext;\n"
    printf "import org.jboss.netty.channel.MessageEvent;\n"
    printf "import org.junit.After;\n"
    printf "import org.junit.Before;\n"
    printf "import org.junit.Test;\n"
    printf "\n"
    printf "import com.google.protobuf.MessageLite;\n"
    printf "import com.xinqihd.sns.gameserver.transport.XinqiMessage;\n"
    printf "import com.xinqihd.sns.gameserver.transport.client.MessageToId;\n"
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
	printf "		XinqiBce%s.Bce%s request = msg.build();\n" $class $class
    printf "		sendRequest(request);\n"
    printf "	}\n"
    printf "	\n"
    printf "  /**\n"
    printf "   * Invoked when a message object (e.g: {@link ChannelBuffer}) was received\n"
    printf "   * from a remote peer.\n"
    printf "   */\n"
    printf "  public void messageReceived(\n"
    printf "          ChannelHandlerContext ctx, MessageEvent e) throws Exception {\n"
    printf "      XinqiMessage response = (XinqiMessage)e.getMessage();\n"
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
    printf "		ChannelFuture future = channel.write(request);\n"
    printf "		future.awaitUninterruptibly();\n"
    printf "		if ( !future.isSuccess() ) {\n"
    printf "			future.getCause().printStackTrace();\n"
    printf "			fail(\"testBce%s failed\");\n" $class
    printf "		}\n"
    printf "	}\n"
    printf "\n"
    printf "}\n"
}

HANDLER_DIR=com/xinqihd/sns/gameserver/handler
TEST_DIR=com/xinqihd/sns/gameserver/proto
mkdir -p ../gensrc/java/$HANDLER_DIR
mkdir -p ../test/java/$TEST_DIR
genhandlerandtests
cd -
