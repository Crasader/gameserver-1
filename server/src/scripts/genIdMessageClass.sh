#!/bin/bash
genidtomessage() {
	echo "package com.xinqihd.sns.gameserver.transport;"
	echo ""
	echo "import org.apache.commons.logging.Log;"
	echo "import org.apache.commons.logging.LogFactory;"
	echo "import com.google.protobuf.MessageLite;"
	echo "import com.xinqihd.sns.gameserver.proto.*;"
	echo ""
	echo "/**"
	echo " * GENERATED SOURCE CODE DO NOT MODIFY!"
	echo " * Translate the given int id to its coresponding message. "
	echo " * @author wangqi "
	echo " */ "
	echo "public class IdToMessage {"
	echo ""
	echo "  private static Log log = LogFactory.getLog(IdToMessage.class); "
	echo ""
	echo "  public static MessageLite idToMessage(int id) { "
	echo "    MessageLite message = null;" 
	echo "    switch(id) {"

	cd $path

	let num=512;
	for file in Bse*.proto
	do
		printf "    case %4d: \n" $num 
		class=`echo $file | sed -e 's/.proto//'`
		printf "      message = Xinqi%s.%s.getDefaultInstance(); \n" $class $class
		echo   "      break;"
		let num+=1
	done

	let num=1024;
	for file in Bce*.proto
	do
		printf "    case %4d: \n" $num 
		class=`echo $file | sed -e 's/.proto//'`
		printf "      message = Xinqi%s.%s.getDefaultInstance(); \n" $class $class
		echo   "      break;"
		let num+=1
	done

	echo "    default:"
	echo "      log.error(\"No message type for id: \" + id);"
	echo "    }" #//switch
	echo "    return message;"
	echo "  }"  #//IdToMessage
	echo "}"

	echo
}

genmessagetoid() {
	echo "package com.xinqihd.sns.gameserver.transport;"
	echo ""
	echo "import org.apache.commons.logging.Log;"
	echo "import org.apache.commons.logging.LogFactory;"
	echo "import com.google.protobuf.MessageLite;"
	echo "import com.xinqihd.sns.gameserver.proto.*;"
	echo ""
	echo "/**"
	echo " * GENERATED SOURCE CODE DO NOT MODIFY!"
	echo " * Translate the given message to its corresponding id. "
	echo " * @author wangqi "
	echo " */ "
	echo "public class MessageToId {"
	echo ""
	echo "  private static Log log = LogFactory.getLog(MessageToId.class); "
	echo ""
	echo "  public static int messageToId(MessageLite msg) { "

	cd $path/
	let num=512;
	for file in Bse*.proto
	do
		class=`echo $file | sed -e 's/.proto//'`
		if [ $num == 512 ]; then
			printf "    if (msg instanceof Xinqi%s.%s ) {\n" $class $class
		else
			printf "    else if (msg instanceof Xinqi%s.%s ) {\n" $class $class
		fi
		printf "      return %4d; \n" $num 
		echo   "    } "
		let num+=1
	done

	let num=1024;
	for file in Bce*.proto
	do
		class=`echo $file | sed -e 's/.proto//'`
		if [ $num == 1024 ]; then
			printf "    if (msg instanceof Xinqi%s.%s ) {\n" $class $class
		else
			printf "    else if (msg instanceof Xinqi%s.%s ) {\n" $class $class
		fi
		printf "      return %4d; \n" $num 
		echo   "    } "
		let num+=1
	done

	echo "    else {"
	echo "      log.error(\"No id for message: \"+msg.getClass().getName());" 
	echo "    }" #//if ... else...
	echo "    return -1;"
	echo "  }"  #//MessageToId
	echo "}"

	echo
}


genmessagetohandler() {
    printf "package com.xinqihd.sns.gameserver.transport;\n"
    printf "\n"
    printf "import org.apache.commons.logging.Log;\n"
    printf "import org.apache.commons.logging.LogFactory;\n"
    printf "\n"
    printf "import com.xinqihd.sns.gameserver.handler.*;\n"
    printf "import com.xinqihd.sns.gameserver.proto.*;\n"
    printf "\n"
    printf "/**\n"
    printf " * GENERATE SOURCE CODE. DO NOT MODIFY!\n"
    printf " * Get to proper message object according to the given message type.\n"
    printf " * @author wangqi\n"
    printf " *\n"
    printf " */\n"
    printf "public class MessageToHandler extends SimpleChannelHandler {\n"
    printf "\n"
    printf "  private static Log log = LogFactory.getLog(MessageToHandler.class); \n"
    printf "\n"
    printf "  public static SimpleChannelHandler messageToHandler(Object msgObject) {\n"
    printf "  	XinqiMessage message = null;\n"
    printf "  	if ( msgObject instanceof XinqiMessage ) {\n"
    printf "  		message = (XinqiMessage)msgObject;\n"
    printf "  	} else {\n"
    printf "  		if ( log.isWarnEnabled() ) {\n"
    printf "  			log.warn(\"msgObject is not XinqiMessage.\");\n"
    printf "  		}\n"
    printf "  	}\n"
	cd $path/
	let num=1024;
	for file in Bce*.proto
	do
		class=`echo $file | sed -e 's/.proto//'`
		if [ $num == 1024 ]; then
			printf "    if (message.payload instanceof Xinqi%s.%s ) {\n" $class $class
		else
			printf "    else if (message.payload instanceof  Xinqi%s.%s ) {\n" $class $class
		fi
		printf "  		return %sHandler.getInstance();\n" $class
		echo   "    } "
		let num+=1
	done
    printf "  	return null;\n"
    printf "  }\n"
    printf "  \n"
    printf "}\n"
}

mkdir -p ../gensrc/java/com/xinqihd/sns/gameserver/transport 
#Create the tmp dir
export path=../../target/proto
cp -pr ../main/protoc/xinqihd/*.proto $path/
cp -pr ../main/protoc/extend/*.proto $path/
mkdir -p $path
#genidtomessage > ../gensrc/java/com/xinqihd/sns/gameserver/transport/IdToMessage.java
#cd -
#genmessagetoid > ../gensrc/java/com/xinqihd/sns/gameserver/transport/MessageToId.java
#cd -
#Don't generate handler again
#genmessagetohandler > ../gensrc/java/com/xinqihd/sns/gameserver/transport/MessageToHandler.java
#cd -
java -cp ../../target/classes/ com.xinqihd.sns.gameserver.util.ProtocolBufUtil ../..

