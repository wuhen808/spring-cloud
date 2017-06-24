package com.test;

import java.io.File;

import org.apache.maven.plugin.logging.SystemStreamLog;

import com.jcraft.jsch.ChannelSftp;
import com.wuhen.maven.plugin.FileProgressMonitor;
import com.wuhen.maven.plugin.SFTPChannel;

public class SFTPTest {

	public static void main(String[] args) throws Exception {
		SystemStreamLog s=new SystemStreamLog();
		String src = "C:\\Users\\Administrator\\Desktop\\111.pdf"; 
		String dst = "/home/wuhen/tt/1111.pdf";
		SFTPChannel channel = new SFTPChannel(s);
		ChannelSftp chSftp = channel.getChannel("192.168.189.128", "22", "wuhen", "wuhen", 60000);
		chSftp.cd("/home/wuhen/eureka-server/");
		chSftp.put(src, dst, new FileProgressMonitor(s,new File(src)), ChannelSftp.OVERWRITE);
		chSftp.quit();
		channel.closeChannel();
	}
}