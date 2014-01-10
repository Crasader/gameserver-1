package com.xinqihd.sns.gameserver.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class used to generate protocol buffer classes.
 * 
 * @author wangqi
 *
 */
public class ProtocolBufUtil {
	
	public static final int bseBase = 512;
	public static final int bceBase = 1024;
	
	/**
	 * Generate all id to message mapping file for
	 * lua script language.
	 */
	public static final void generateIdMessage(String projectDir, String pbDir) 
			throws Exception {
		//Read the mapping file
		int bseMax = bseBase;
		int bceMax = bceBase;
		File mappingFile = new File(projectDir, "src/gensrc/pb.properties");
		Map<String, Integer> messageMap = new LinkedHashMap<String, Integer>();
		if ( mappingFile.exists() && mappingFile.isFile() ) {
			FileReader fr = new FileReader(mappingFile);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			while ( line != null ) {
				String[] fields = line.split("=");
				String messageName = fields[0].trim();
				Integer messageId = new Integer(fields[1].trim());
				System.out.println(messageName+" = " +messageId);
				messageMap.put(messageName, messageId);
				if ( messageId < bceBase ) {
					if ( messageId > bseMax ) {
						bseMax = messageId;
					}
				} else if ( messageId > bceMax ) {
					bceMax = messageId;
				}
				
				line = br.readLine();
			}
		}
		bseMax++;
		bceMax++;
		System.out.println("Max bse:"+bseMax+", max bce:"+bceMax);
		//Read the protocol list
		ArrayList<String> normalPbList = new ArrayList<String>();
		ArrayList<String> xinqiPbList = new ArrayList<String>();
		ArrayList<String> extPbList = new ArrayList<String>();
		
		File pbFile = new File(projectDir, pbDir);
		File xinqiDir = new File(pbFile, "xinqihd");
		File extDir = new File(pbFile, "extend");
		File guildDir = new File(pbFile, "guild");
		int[] max = {bceMax, bseMax};
		parsePbDir(xinqiDir, max, messageMap, normalPbList);
		parsePbDir(extDir, max, messageMap, normalPbList);
		parsePbDir(guildDir, max, messageMap, normalPbList);
		
		luaScript(projectDir, "src/gensrc/lua/ProtocolMgr.lua", normalPbList, messageMap);
		javaIdToMessage(projectDir, "src/gensrc/java/com/xinqihd/sns/gameserver/transport/IdToMessage.java", normalPbList, messageMap);
		javaMessageToId(projectDir, "src/gensrc/java/com/xinqihd/sns/gameserver/transport/MessageToId.java", normalPbList, messageMap);
		javaMessageToHandler(projectDir, "src/gensrc/java/com/xinqihd/sns/gameserver/transport/MessageToHandler.java", normalPbList, messageMap);
		//save mapping data.
		saveMappingFile(mappingFile, messageMap);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String projectDir = ".";
		if ( args != null && args.length>0 ) {
			projectDir = args[0];
		}
		ProtocolBufUtil.generateIdMessage(projectDir, "src/main/protoc");
	}
	
	private static final void saveMappingFile(File mappingFile, Map<String, Integer> messageMap) 
			throws Exception {
		FileWriter fw = new FileWriter(mappingFile);
		for ( String message : messageMap.keySet() ) {
			Integer id = messageMap.get(message);
			fw.append(message).append("=").append(id.toString()).append("\n");
		}
		fw.close();
	}
	
	/**
	 * Extract the message name from file name.
	 * @param fileName
	 * @return
	 */
	private static final String extractMessageName(String fileName) {
		if ( fileName != null && fileName.length()>0 ) {
			return fileName.substring(0, fileName.length()-6);
		}
		return fileName;
	}
	
	/**
	 * Parse the protocol files dir and generate nw id.
	 * @param pbFile
	 * @param bceMax
	 * @param bseMax
	 * @param messageMap
	 * @param normalPbList
	 */
	private static final void parsePbDir(File pbFile, int[] max, 
			Map<String, Integer> messageMap, List<String> normalPbList) {
		int bceMax = max[0];
		int bseMax = max[1];
		File[] xinqiPbFiles = pbFile.listFiles(	new ProtoFileFilter() );
		for ( File f : xinqiPbFiles ) {
			String fileName = f.getName();
			String messageName = extractMessageName(fileName);
			Integer id = messageMap.get(messageName);
			if ( id == null ) {
				//new protocol file.
				if ( messageName.startsWith("Bce") ) {
					id = new Integer(bceMax++);
				} else if ( messageName.startsWith("Bse") ) {
					id = new Integer(bseMax++);
				} else {
					normalPbList.add(messageName);
				}
				if ( id != null ) {
					messageMap.put(messageName, id);
					System.out.println("New " + messageName + ", id: " + id);
				}
			}
		}
	}
	
	/**
	 * Generate the java source.
	 * @param projectDir
	 * @param javaDir
	 * @param normalList
	 * @param map
	 */
	private static final void javaIdToMessage(String projectDir, String javaDir,
			List<String> normalList, Map<String, Integer> map) throws Exception  {
		File idFile = new File(projectDir, javaDir);
		FileWriter fw = new FileWriter(idFile);
		fw.append("package com.xinqihd.sns.gameserver.transport;\n");
		fw.append("\n");
		fw.append("import org.apache.commons.logging.Log;\n");
		fw.append("import org.apache.commons.logging.LogFactory;\n");
		fw.append("import com.google.protobuf.MessageLite;\n");
		fw.append("import com.xinqihd.sns.gameserver.proto.*;\n");
		fw.append("\n");
		fw.append("/**\n");
		fw.append(" * GENERATED SOURCE CODE DO NOT MODIFY!\n");
		fw.append(" * Translate the given int id to its coresponding message. \n");
		fw.append(" * @author wangqi \n");
		fw.append(" */ \n");
		fw.append("public class IdToMessage {\n");
		fw.append("\n");
		fw.append("  private static Log log = LogFactory.getLog(IdToMessage.class); \n");
		fw.append("\n");
		fw.append("  public static MessageLite idToMessage(int id) { \n");
		fw.append("    MessageLite message = null;\n" );
		fw.append("    switch(id) {\n");
		for ( String message : map.keySet() ) {
			Integer id = map.get(message);
			fw.append("    case "+id+": \n");  
			fw.append("      message = Xinqi"+message+"."+message
					+".getDefaultInstance(); \n");
			fw.append("      break;\n");
		}
		fw.append("    default:\n");
		fw.append("      log.error(\"No message type for id: \" + id);\n");
		fw.append("    }\n");
		fw.append("    return message;\n");
		fw.append("  }\n");
		fw.append("}\n");
		fw.close();
	}
	
	/**
	 * 
	 * @param projectDir
	 * @param javaDir
	 * @param normalList
	 * @param map
	 */
	private static final void javaMessageToId(String projectDir, String javaDir,
			List<String> normalList, Map<String, Integer> map) throws Exception { 
		File idFile = new File(projectDir, javaDir);
		FileWriter fw = new FileWriter(idFile);
		fw.append("package com.xinqihd.sns.gameserver.transport;\n");
		fw.append("\n");
		fw.append("import org.apache.commons.logging.Log;\n");
		fw.append("import org.apache.commons.logging.LogFactory;\n");
		fw.append("import com.google.protobuf.MessageLite;\n");
		fw.append("import com.xinqihd.sns.gameserver.proto.*;\n");
		fw.append("\n");
		fw.append("/**\n");
		fw.append(" * GENERATED SOURCE CODE DO NOT MODIFY!\n");
		fw.append(" * Translate the given message to its corresponding id. \n");
		fw.append(" * @author wangqi \n");
		fw.append(" */ \n");
		fw.append("public class MessageToId {\n");
		fw.append("\n");
		fw.append("  private static Log log = LogFactory.getLog(MessageToId.class); \n");
		fw.append("\n");
		fw.append("  public static int messageToId(MessageLite msg) { \n");
		boolean first = true;
		for ( String message : map.keySet() ) {
			Integer id = map.get(message);
			if ( first ) {
				fw.append("    if (msg instanceof Xinqi"+message+"."+message+" ) {\n");
				first = false;
			} else {
				fw.append("    else if (msg instanceof Xinqi"+message+"."+message+" ) {\n");
			}
			fw.append("      return "+id+"; \n");
			fw.append("    }\n");
		}
		fw.append("    else {\n");
		fw.append("      log.error(\"No id for message: \"+msg.getClass().getName());\n" );
		fw.append("    }\n");
		fw.append("    return -1;\n");
		fw.append("  }\n"  );
		fw.append("}\n");
		fw.close();
	}
	
	/**
	 * 
	 * @param projectDir
	 * @param javaDir
	 * @param normalList
	 * @param map
	 * @throws Exception
	 */
	private static final void javaMessageToHandler(String projectDir, String javaDir,
			List<String> normalList, Map<String, Integer> map) throws Exception { 
		File idFile = new File(projectDir, javaDir);
		FileWriter fw = new FileWriter(idFile);
    fw.append("package com.xinqihd.sns.gameserver.transport;\n");
    fw.append("\n");
    fw.append("import org.apache.commons.logging.Log;\n");
    fw.append("import org.apache.commons.logging.LogFactory;\n");
    fw.append("\n");
    fw.append("import com.xinqihd.sns.gameserver.handler.*;\n");
    fw.append("import com.xinqihd.sns.gameserver.proto.*;\n");
    fw.append("\n");
    fw.append("/**\n");
    fw.append(" * GENERATE SOURCE CODE. DO NOT MODIFY!\n");
    fw.append(" * Get to proper message object according to the given message type.\n");
    fw.append(" * @author wangqi\n");
    fw.append(" *\n");
    fw.append(" */\n");
    fw.append("public class MessageToHandler extends SimpleChannelHandler {\n");
    fw.append("\n");
    fw.append("  private static Log log = LogFactory.getLog(MessageToHandler.class); \n");
    fw.append("\n");
    fw.append("  public static SimpleChannelHandler messageToHandler(Object msgObject) {\n");
    fw.append("  	XinqiMessage message = null;\n");
    fw.append("  	if ( msgObject instanceof XinqiMessage ) {\n");
    fw.append("  		message = (XinqiMessage)msgObject;\n");
    fw.append("  	} else {\n");
    fw.append("  		if ( log.isWarnEnabled() ) {\n");
    fw.append("  			log.warn(\"msgObject is not XinqiMessage.\");\n");
    fw.append("  		}\n");
    fw.append("  	}\n");
		for ( String message : map.keySet() ) {
			int id = map.get(message);
			if ( id<1024 ) continue;
			if ( id==1024 ) {
				fw.append("    if (message.payload instanceof Xinqi"+message+"."+message+" ) {\n");
			} else {
				fw.append("    else if (message.payload instanceof Xinqi"+message+"."+message+" ) {\n");
			}
			fw.append("  		return "+message+"Handler.getInstance();\n");
			fw.append("    }\n");
		}
    fw.append("  	return null;\n");
    fw.append("  }\n");
    fw.append("  \n");
    fw.append("}\n");
    fw.close();
	}
	
	private static final void luaScript(String projectDir, String luaDir,
			List<String> normalList, Map<String, Integer> map) 
			throws Exception {
		File luaFile = new File(projectDir, luaDir);
		FileWriter fw = new FileWriter(luaFile);
		fw.append("require(\"com/xinqihd/common/base.lua\")\n");
		fw.append("require(\"com/xinqihd/common/PkgMap.lua\")\n");
		for ( String message : normalList ) {
			fw.append("require(\"com/xinqihd/bombbaby/protocol/"+message+"_pb.lua\")\n");
		}
		for ( String message : map.keySet() ) {
			fw.append("require(\"com/xinqihd/bombbaby/protocol/").append(message).append("_pb.lua\")\n");
		}
		for ( String message : map.keySet() ) {
			Integer value = map.get(message);
			fw.append("ID_"+message+" = " + value).append("\n");
		}
		fw.append("ProtocolMgr = class(\"ProtocolMgr\")\n");
		fw.append("function ProtocolMgr:InitPkgMap()\n");
		for ( String message : map.keySet() ) {
			Integer value = map.get(message);
			fw.append("    self._pkgMap:AddPkg("+value +
					",\t").append(message).append("_pb.").append(message).append("());\n");
		}
		fw.append("end\n\n");
		
		fw.append("function ProtocolMgr:initialize()\n");
		fw.append("  self._pkgMap = PkgMap:new()\n");
	  fw.append("  self:InitPkgMap()\n");
	  fw.append("end\n\n");

	  fw.append("function ProtocolMgr:GetPkgMap()\n");
	  fw.append("  return self._pkgMap\n");
	  fw.append("end\n");
	  fw.close();
	}

	/**
	 * Filter all the files with '.proto' extension.
	 * @author wangqi
	 *
	 */
	private static final class ProtoFileFilter implements FilenameFilter {

		/* (non-Javadoc)
		 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
		 */
		@Override
		public boolean accept(File dir, String name) {
			if ( name.endsWith(".proto") ) {
				return true;
			}
			return false;
		}
		
	}
}
