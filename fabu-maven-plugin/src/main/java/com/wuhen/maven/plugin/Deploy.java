package com.wuhen.maven.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;

import com.jcraft.jsch.ChannelSftp;

@Mojo(name = "fabu", defaultPhase = LifecyclePhase.NONE)
public class Deploy extends AbstractMojo {

	@Parameter(required = true)
	private String[] servers;
	@Parameter(required = true)
	private String copyDir;
	@Parameter(required = true)
	private String serverDir;
	@Parameter(required = true)
	private String serverId;

	@Parameter(defaultValue = "${settings}", readonly = true, required = true)
	private Settings settings;
	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;
	@Parameter(defaultValue = "${project.build.directory}", required = true)
	private File targetDir;

	private static Map<String,Artifact> map = new HashMap<String,Artifact>();
	private static Integer size = null;
	private static boolean flag = false;

	public void execute() throws MojoExecutionException {
		File copyDirectory = new File(copyDir);
		if (!flag) {
			flag = true;
			for (String server : servers) {
				if(!server.contains(":")){
					throw new RuntimeException("server不是有效的 ip:port");
				}
			}
			if (!copyDirectory.isDirectory()) {
				throw new RuntimeException("目标不是一个文件夹");
			}
			if (copyDirectory.list().length != 0) {
				throw new RuntimeException("目标文件夹不为空");
			}
		}
		
		boolean bl = false;
		if (size == null) {
			size = project.getCollectedProjects().size();
		}
		if (null != project.getModel()) {
			List<Plugin> li = project.getModel().getBuild().getPlugins();
			for (Plugin plugin : li) {
				if (plugin.getArtifactId().equals("spring-boot-maven-plugin")) {
					bl = true;
					break;
				}
			}
		}
		// 不纯在要排除
		File[] files = targetDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		});
		if (files == null || files.length == 0) {
			return;
		}

		if (bl) {
			map.put(files[0].getName(),project.getArtifact());
		}
		try {
			FileUtils.copyFileToDirectory(files[0], copyDirectory);
		} catch (IOException e) {
			getLog().error(e);
		}
		// size==文件数量。表示所有打包完成。要 开始上传
		File[] targetFiles = copyDirectory.listFiles();
		if (size == targetFiles.length) {
			getLog().info("文件复制完成，开始上传文件");
			String dst = serverDir;
			SFTPChannel channel = new SFTPChannel(getLog());
			try {
				Server server = settings.getServer(serverId);
				for (String s : servers) {
					ChannelSftp chSftp = channel.getChannel(s.split(":")[0], s.split(":")[1], server.getUsername(),server.getPassword(), 60000);
					for (File file2 : targetFiles) {
						if (map.containsKey(file2.getName())) {
							try{
								chSftp.cd(dst+map.get(file2.getName()).getArtifactId());
							}catch (Exception e) {
								getLog().warn("不存在服务:"+file2.getName());
								continue;
							}
							chSftp.put(file2.getAbsolutePath(), file2.getName(),
									new FileProgressMonitor(getLog(), file2), ChannelSftp.OVERWRITE); // 代码段2
						}
					}
					chSftp.quit();
				}
			} catch (Exception e) {
				getLog().error(e);
			} finally {
				try {
					channel.closeChannel();
				} catch (Exception e) {
					getLog().error(e);
				}
			}
		}

	}
}
