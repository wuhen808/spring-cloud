package com.wuhen.maven.plugin;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.maven.plugin.logging.Log;

import com.jcraft.jsch.SftpProgressMonitor;

public class FileProgressMonitor extends TimerTask implements SftpProgressMonitor {
	private Log log;

	private long progressInterval = 1 * 1000;

	private boolean isEnd = false; // 记录传输是否结束

	private long transfered; // 记录已传输的数据总大小

	private long fileSize; // 记录文件总大小

	private File file; // 文件

	private Timer timer; // 定时器对象

	private boolean isScheduled = false; // 记录是否已启动timer记时器

	public FileProgressMonitor(Log log, File file) {
		this.log = log;
		this.file = file;
		this.fileSize = file.length();
	}

	@Override
	public void run() {
		if (!isEnd()) { // 判断传输是否已结束
			long transfered = getTransfered();
			if (transfered != fileSize) { // 判断当前已传输数据大小是否等于文件总大小
				sendProgressMessage(transfered);
			} else {
				setEnd(true); // 如果当前已传输数据大小等于文件总大小，说明已完成，设置end
			}
		} else {
			stop(); // 如果传输结束，停止timer记时器
			return;
		}
	}

	public void stop() {
		if (timer != null) {
			timer.cancel();
			timer.purge();
			timer = null;
			isScheduled = false;
		}
	}

	public void start() {
		if (timer == null) {
			timer = new Timer();
		}
		timer.schedule(this, 1000, progressInterval);
		isScheduled = true;
	}

	/**
	 * 打印progress信息
	 * 
	 * @param transfered
	 */
	private void sendProgressMessage(long transfered) {
		if (fileSize != 0) {
			double d = ((double) transfered * 100) / (double) fileSize;
			DecimalFormat df = new DecimalFormat("#.##");
			log.info(file.getName() + "已完成: " + df.format(d) + "%");
		}
	}

	/**
	 * 传输中
	 */
	public boolean count(long count) {
		if (isEnd())
			return false;
		if (!isScheduled) {
			start();
		}
		add(count);
		return true;
	}

	public void end() {
		setEnd(true);
		log.info(file.getName() + "上传结束");
	}

	private synchronized void add(long count) {
		transfered = transfered + count;
	}

	private synchronized long getTransfered() {
		return transfered;
	}

	public synchronized void setTransfered(long transfered) {
		this.transfered = transfered;
	}

	private synchronized void setEnd(boolean isEnd) {
		this.isEnd = isEnd;
	}

	private synchronized boolean isEnd() {
		return isEnd;
	}

	public void init(int op, String src, String dest, long max) {
		log.info("开始上传文件：" + file.getName());
	}
}