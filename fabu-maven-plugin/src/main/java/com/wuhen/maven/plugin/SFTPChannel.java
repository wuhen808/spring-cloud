package com.wuhen.maven.plugin;

import java.util.Properties;

import org.apache.maven.plugin.logging.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SFTPChannel {
	private Log log;
	
	public SFTPChannel(Log log) {
		this.log=log;
	}

	private Session session = null;
	private Channel channel = null;

	public ChannelSftp getChannel(String host,String port,String userName,String passWord,int timeout) throws JSchException {

		JSch jsch = new JSch();
		session = jsch.getSession(userName, host, Integer.parseInt(port)); 
		session.setPassword(passWord);
		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config); 
		session.setTimeout(timeout);
		session.connect();
		log.info("登录服务器成功");
		channel = session.openChannel("sftp");
		channel.connect();
		return (ChannelSftp) channel;
	}

	public void closeChannel() throws Exception {
		if (channel != null) {
			channel.disconnect();
		}
		if (session != null) {
			session.disconnect();
		}
	}
}