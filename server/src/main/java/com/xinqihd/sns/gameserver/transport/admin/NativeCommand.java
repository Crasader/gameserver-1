package com.xinqihd.sns.gameserver.transport.admin;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class NativeCommand implements Command {
	
	public static final String COMMAND = "!";

	@Override
	public String execute(String[] args) {
		String[] cmd = new String[args.length - 1];
		System.arraycopy(args, 1, cmd, 0, cmd.length);
		StringBuilder buf = new StringBuilder(64);
		try {
			Process process = new ProcessBuilder(cmd).start();
			OutputStream os = process.getOutputStream();
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = br.readLine();
			while ( line != null ) {
				buf.append(line).append("\n");
				line = br.readLine();
			}
			
			return buf.append('>').toString();
		} catch (IOException e) {
			e.printStackTrace();
			buf.append(e.getMessage()).append("\n");
			return buf.toString();
		}
	}

	/**
	 * Get the help content.
	 */
	public String toString() {
		return "execute native shell command";
	}
	
	public static void main(String[] args) throws Exception {
		String bashCmd = "/bin/bash";
		File file = new File(bashCmd);
		if ( !file.exists() ) {
			bashCmd = "/bin/sh";
			file = new File(bashCmd);
			if ( !file.exists() ) {
				System.out.println("bash is not found.");
			}
		}
		Process process = new ProcessBuilder(bashCmd).start();
		try {
			InputStream cmdOut = process.getInputStream();			
			OutputStream cmdIn = process.getOutputStream();
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream(200);
			System.out.print('$');
			while ( true ) {
				if ( cmdOut.available()>0 ) {
					byte[] bytes = new byte[cmdOut.available()];
					cmdOut.read(bytes);
					baos.write(bytes);
				} else {
					if ( baos.size() > 0 ) {
						String str = new String(baos.toByteArray(), "utf8");
						System.out.print(str);
						System.out.print('$');
						baos.reset();
					}
				}
				
				if ( System.in.available() > 0 ) {
					byte[] bytes = new byte[System.in.available()];
					System.in.read(bytes);
					cmdIn.write(bytes);
					cmdIn.flush();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
