/**
 * 
 */
package com.tydic.filehub.utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp.LsEntry;

/**
 * sftp工具类
 * @author zhangrong
 *
 * 2017年8月23日
 */
public class SFTPUtils {
	private static final Logger log = LoggerFactory.getLogger(SFTPUtils.class);
	private String host;// 服务器连接ip
	private String username;// 用户名
	private String password;// 密码
	private int port = 22;// 端口号
	private ChannelSftp sftp = null;
	private Session sshSession = null;

	public SFTPUtils() {
	}

	public SFTPUtils(String host, int port, String username, String password) {
		this.host = host;
		this.username = username;
		this.password = password;
		this.port = port;
	}

	public SFTPUtils(String host, String username, String password) {
		this.host = host;
		this.username = username;
		this.password = password;
	}

	/**
	 * 通过SFTP连接服务器
	 */
	public void connect() {
		try {
			JSch jsch = new JSch();
			jsch.getSession(username, host, port);
			sshSession = jsch.getSession(username, host, port);
			if (log.isInfoEnabled()) {
				log.info("Session created.");
			}
			sshSession.setPassword(password);
			Properties sshConfig = new Properties();
			sshConfig.put("StrictHostKeyChecking", "no");

            // 按服务器支持的顺序来配置
            sshConfig.put("kex", "curve25519-sha256@libssh.org," +
                    "diffie-hellman-group-exchange-sha256," +
                    "curve25519-sha256");
			sshSession.setConfig(sshConfig);
			sshSession.connect();
			if (log.isInfoEnabled()) {
				log.info("Session connected.");
			}
			Channel channel = sshSession.openChannel("sftp");
			channel.connect();
			if (log.isInfoEnabled()) {
				log.info("Opening Channel.");
			}
			sftp = (ChannelSftp) channel;
			if (log.isInfoEnabled()) {
				log.info("Connected to " + host + ".");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭连接
	 */
	public void disconnect() {
		if (this.sftp != null) {
			if (this.sftp.isConnected()) {
				this.sftp.disconnect();
				if (log.isInfoEnabled()) {
					log.info("sftp is closed already");
				}
			}
		}
		if (this.sshSession != null) {
			if (this.sshSession.isConnected()) {
				this.sshSession.disconnect();
				if (log.isInfoEnabled()) {
					log.info("sshSession is closed already");
				}
			}
		}
	}

	/**
	 * 批量下载文件
	 * 
	 * @param remotPath
	 *            ：远程下载目录(以路径符号结束,可以为相对路径eg:/assess/sftp/jiesuan_2/2014/)
	 * @param localPath
	 *            ：本地保存目录(以路径符号结束,D:\Duansha\sftp\)
	 * @param fileFormat
	 *            ：下载文件格式(以特定字符开头,为空不做检验)
	 * @param fileEndFormat
	 *            ：下载文件格式(文件格式)
	 * @param del
	 *            ：下载后是否删除sftp文件
	 * @return
	 */
	public List<String> batchDownLoadFile(String remotePath, String localPath,
			String fileFormat, String fileEndFormat, boolean del) {
		List<String> filenames = new ArrayList<String>();
		try {
			// connect();
			@SuppressWarnings("rawtypes")
			Vector v = listFiles(remotePath);
			// sftp.cd(remotePath);
			if (v.size() > 0) {
				System.out.println("本次处理文件个数不为零,开始下载...fileSize=" + v.size());
				@SuppressWarnings("rawtypes")
				Iterator it = v.iterator();
				while (it.hasNext()) {
					LsEntry entry = (LsEntry) it.next();
					String filename = entry.getFilename();
					SftpATTRS attrs = entry.getAttrs();
					if (!attrs.isDir()) {
						boolean flag = false;
						String localFileName = localPath + filename;
						fileFormat = fileFormat == null ? "" : fileFormat
								.trim();
						fileEndFormat = fileEndFormat == null ? ""
								: fileEndFormat.trim();
						// 三种情况
						if (fileFormat.length() > 0
								&& fileEndFormat.length() > 0) {
							if (filename.startsWith(fileFormat)
									&& filename.endsWith(fileEndFormat)) {
								flag = downloadFile(remotePath, filename,
										localPath, filename);
								if (flag) {
									filenames.add(localFileName);
									if (flag && del) {
										deleteSFTP(remotePath, filename);
									}
								}
							}
						} else if (fileFormat.length() > 0
								&& "".equals(fileEndFormat)) {
							if (filename.startsWith(fileFormat)) {
								flag = downloadFile(remotePath, filename,
										localPath, filename);
								if (flag) {
									filenames.add(localFileName);
									if (flag && del) {
										deleteSFTP(remotePath, filename);
									}
								}
							}
						} else if (fileEndFormat.length() > 0
								&& "".equals(fileFormat)) {
							if (filename.endsWith(fileEndFormat)) {
								flag = downloadFile(remotePath, filename,
										localPath, filename);
								if (flag) {
									filenames.add(localFileName);
									if (flag && del) {
										deleteSFTP(remotePath, filename);
									}
								}
							}
						} else {
							flag = downloadFile(remotePath, filename,
									localPath, filename);
							if (flag) {
								filenames.add(localFileName);
								if (flag && del) {
									deleteSFTP(remotePath, filename);
								}
							}
						}
					}
				}
			}
			if (log.isInfoEnabled()) {
				log.info("download file is success:remotePath=" + remotePath
						+ "and localPath=" + localPath + ",file size is"
						+ v.size());
			}
		} catch (SftpException e) {
			e.printStackTrace();
		} finally {
			// this.disconnect();
		}
		return filenames;
	}

	/**
	 * 下载单个文件
	 * 
	 * @param remotPath
	 *            ：远程下载目录(以路径符号结束)
	 * @param remoteFileName
	 *            ：下载文件名
	 * @param localPath
	 *            ：本地保存目录(以路径符号结束)
	 * @param localFileName
	 *            ：保存文件名
	 * @return
	 */
	public boolean downloadFile(String remotePath, String remoteFileName,
			String localPath, String localFileName) {
		FileOutputStream fieloutput = null;
		try {
			// sftp.cd(remotePath);
			File file = new File(localPath + localFileName);
			//查看目录是否存在，不存在则创建
			File toPath = file.getParentFile();
			if (!toPath.exists()) {
				toPath.mkdirs();
			}
			// mkdirs(localPath + localFileName);
			fieloutput = new FileOutputStream(file);
			sftp.get(remotePath + remoteFileName, fieloutput);
			if (log.isInfoEnabled()) {
				log.info("===DownloadFile:" + remoteFileName
						+ " success from sftp.");
			}
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SftpException e) {
			e.printStackTrace();
		} finally {
			if (null != fieloutput) {
				try {
					fieloutput.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	/**
	 * 以流方式读取远程文件，调用方负责关闭返回的输入流，并在关闭后断开 SFTP 连接。
	 */
	public InputStream getFileInputStream(String remotePath, String remoteFileName) throws SftpException {
		return sftp.get(remotePath + remoteFileName);
	}

	/**
	 * 上传单个文件
	 * 
	 * @param remotePath
	 *            ：远程保存目录
	 * @param remoteFileName
	 *            ：保存文件名
	 * @param localPath
	 *            ：本地上传目录(以路径符号结束)
	 * @param localFileName
	 *            ：上传的文件名
	 * @return
	 */
	public boolean uploadFile(String remotePath, String remoteFileName,
			String localPath, String localFileName) {
		FileInputStream in = null;
		try {
			createDir(remotePath);
			File file = new File(localPath + localFileName);
			in = new FileInputStream(file);
			sftp.put(in, remoteFileName);
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SftpException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	/**
	 * 批量上传文件
	 * 
	 * @param remotePath
	 *            ：远程保存目录
	 * @param localPath
	 *            ：本地上传目录(以路径符号结束)
	 * @param del
	 *            ：上传后是否删除本地文件
	 * @return
	 */
	public boolean bacthUploadFile(String remotePath, String localPath,
			boolean del) {
		try {
			connect();
			File file = new File(localPath);
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()
						&& files[i].getName().indexOf("bak") == -1) {
					if (this.uploadFile(remotePath, files[i].getName(),
							localPath, files[i].getName()) && del) {
						deleteFile(localPath + files[i].getName());
					}
				}
			}
			if (log.isInfoEnabled()) {
				log.info("upload file is success:remotePath=" + remotePath
						+ "and localPath=" + localPath + ",file size is "
						+ files.length);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.disconnect();
		}
		return false;
	}

	/**
	 * 删除本地文件
	 * 
	 * @param filePath
	 * @return
	 */
	public boolean deleteFile(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			return false;
		}
		if (!file.isFile()) {
			return false;
		}
		boolean rs = file.delete();
		if (rs && log.isInfoEnabled()) {
			log.info("delete file success from local.");
		}
		return rs;
	}

	/**
	 * 创建目录
	 * 
	 * @param createpath
	 * @return
	 */
	public boolean createDir(String createpath) {
		try {
			if (isDirExist(createpath)) {
				this.sftp.cd(createpath);
				return true;
			}
			String pathArry[] = createpath.split("/");
			StringBuffer filePath = new StringBuffer("/");
			for (String path : pathArry) {
				if (path.equals("")) {
					continue;
				}
				filePath.append(path + "/");
				if (isDirExist(filePath.toString())) {
					sftp.cd(filePath.toString());
				} else {
					// 建立目录
					sftp.mkdir(filePath.toString());
					// 进入并设置为当前目录
					sftp.cd(filePath.toString());
				}
			}
			this.sftp.cd(createpath);
			return true;
		} catch (SftpException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 判断目录是否存在
	 * 
	 * @param directory
	 * @return
	 */
	public boolean isDirExist(String directory) {
		boolean isDirExistFlag = false;
		try {
			SftpATTRS sftpATTRS = sftp.lstat(directory);
			isDirExistFlag = true;
			return sftpATTRS.isDir();
		} catch (Exception e) {
			if (e.getMessage().toLowerCase().equals("no such file")) {
				isDirExistFlag = false;
			}
		}
		return isDirExistFlag;
	}

	/**
	 * 删除stfp文件
	 * 
	 * @param directory
	 *            ：要删除文件所在目录
	 * @param deleteFile
	 *            ：要删除的文件
	 * @param sftp
	 */
	public void deleteSFTP(String directory, String deleteFile) {
		try {
			// sftp.cd(directory);
			sftp.rm(directory + deleteFile);
			if (log.isInfoEnabled()) {
				log.info("delete file success from sftp.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 如果目录不存在就创建目录
	 * 
	 * @param path
	 */
	public void mkdirs(String path) {
		File f = new File(path);
		String fs = f.getParent();
		f = new File(fs);
		if (!f.exists()) {
			f.mkdirs();
		}
	}

	/**
	 * 列出目录下的文件
	 * 
	 * @param directory
	 *            ：要列出的目录
	 * @param sftp
	 * @return
	 * @throws SftpException
	 */
	@SuppressWarnings("rawtypes")
	public Vector listFiles(String directory) throws SftpException {
		return sftp.ls(directory);
	}

	/**
	 * 在 SFTP 服务器上执行 Shell 命令（通过 SSH Exec），返回标准输出行列表。
	 * 用于批量获取文件列表，如：ls -1 DWD_D_EVT_KF_GD_CASE_MAIN* 2>/dev/null | grep -v '\.MD5'
	 *
	 * @param remotePath 远程工作目录，执行前会 cd 到该目录；为空则不切换
	 * @param shellCommand Shell 命令
	 * @return 标准输出每行一个元素，空行已过滤
	 */
	public List<String> execCommand(String remotePath, String shellCommand) throws Exception {
		if (sshSession == null || !sshSession.isConnected()) {
			throw new IllegalStateException("SSH session not connected");
		}
		String fullCommand = (remotePath != null && !remotePath.trim().isEmpty())
				? "cd " + remotePath.trim().replace("'", "'\\''") + " && " + shellCommand
				: shellCommand;
		ChannelExec channel = null;
		BufferedReader reader = null;
		try {
			channel = (ChannelExec) sshSession.openChannel("exec");
			channel.setCommand(fullCommand);
			channel.setInputStream(null);
			channel.setErrStream(System.err);
			java.io.InputStream in = channel.getInputStream();
			channel.connect(120000);  // 2 分钟超时，防止 Shell 命令挂起
			reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			List<String> lines = new ArrayList<>();
			String line;
			while ((line = reader.readLine()) != null) {
				String trimmed = line.trim();
				if (!trimmed.isEmpty()) {
					// ls -l 格式取最后一列（文件名），ls -1 格式整行即文件名
					String filename = trimmed.contains(" ") && (trimmed.startsWith("-") || trimmed.startsWith("d") || trimmed.startsWith("l"))
							? trimmed.substring(trimmed.lastIndexOf(' ') + 1).trim()
							: trimmed;
					if (!filename.isEmpty()) {
						lines.add(filename);
					}
				}
			}
			return lines;
		} finally {
			if (reader != null) {
				try { reader.close(); } catch (IOException ignored) {}
			}
			if (channel != null && channel.isConnected()) {
				channel.disconnect();
			}
		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public ChannelSftp getSftp() {
		return sftp;
	}

	public void setSftp(ChannelSftp sftp) {
		this.sftp = sftp;
	}

	/**
	 * 下载文件
	 * 
	 * @param ipAddress
	 *            IP地址
     * @param port
	 *            端口           
	 * @param userName
	 *            用户名
	 * @param password
	 *            密码
	 * @param remoteFilePath
	 *            远程文件目录
	 * @param remoteFileName
	 *            远程文件名称
	 * @param localFilePath
	 *            下载的写入文件路径
	 * @param localFileName
	 *             本地文件名称           
	 */
	public static void downloadFile(String ipAddress,int port,String userName, String password, String remoteFilePath,
			String remoteFileName,String localFilePath,String localFileName){
		SFTPUtils sftp  = null;
		try {
			sftp =new SFTPUtils(ipAddress,port,userName, password);
			sftp.connect();
			sftp.downloadFile(remoteFilePath, remoteFileName, localFilePath, localFileName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			sftp.disconnect();
		}
		
	}
	
	/**
	 * 上传文件
	 * 
	 * @param ipAddress
	 *            IP地址
     * @param port
	 *            端口           
	 * @param userName
	 *            用户名
	 * @param password
	 *            密码
	 * @param remoteFilePath
	 *            远程文件目录
	 * @param remoteFileName
	 *            远程文件名称
	 * @param localFilePath
	 *             本地文件目录
	 * @param localFileName
	 *             本地文件名称           
	 */
	public static void uploadFile(String ipAddress,int port,String userName, String password, String remoteFilePath,
			String remoteFileName,String localFilePath,String localFileName){
		SFTPUtils sftp  = null;
		try {
			sftp =new SFTPUtils(ipAddress,port,userName, password);
			sftp.connect();
			sftp.uploadFile(remoteFilePath, remoteFileName, localFilePath, localFileName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			sftp.disconnect();
		}
		
	}
	/**
	 * 下载文件
	 * <p>
	 * fileAddr         下载目录
	 * fileName         下载的文件
	 * filePath         下载文件的路径
	 * localPath        存在本地的路径
	 *
	 */
	public static boolean download(Map map, String localPath) throws Exception{
		ChannelSftp sftp = connects(map);
//        String fileAddr = (String) map.get("fileAddr");
//        String fileName = (String) map.get("fileName");
		String filePath = (String)map.get("filePath");
		try {
//            sftp.cd(fileAddr);
			//查看目录是否存在，不存在则创建
			File file = new File(localPath);
			File toPath = file.getParentFile();
			if (!toPath.exists()) {
				toPath.mkdirs();
			}
			FileOutputStream fileOutputStream = new FileOutputStream(file);
//            sftp.get(fileName, fileOutputStream);
			sftp.get(filePath, fileOutputStream);
			fileOutputStream.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			disconnects(sftp);
		}
	}
	/**
	 * @Description: 关闭SFTP连接
	 * @Param: [sftp]
	 * @return: void
	 * @Author: xiangshuailong
	 * @Date: 2022/3/31
	 */
	public static void disconnects(ChannelSftp sftp) {
		try {
			sftp.getSession().disconnect();
			log.info("--------SFTP Session disconnected...");
		} catch (JSchException e) {
			log.error("-------关闭SFTP连接异常：" + e.getMessage());
			e.printStackTrace();
		}
		sftp.quit();
		sftp.disconnect();
	}
	/**
	 * 连接sftp服务器
	 *
	 * @param map 服务器信息
	 *            serverIp          主机
	 *            serverPort        端口
	 *            serverUsername    用户名
	 *            serverPassword    密码
	 * @return
	 */
	public static ChannelSftp connects(Map<String, Object> map){
		String serverIp = (String) map.get("serverIp");
		int serverPort = Integer.valueOf((String) map.get("serverPort"));
		String serverUsername = (String) map.get("serverUsername");
		String serverPassword = (String) map.get("serverPassword");
		ChannelSftp sftp = null;
		try {
			JSch jsch = new JSch();
			jsch.getSession(serverUsername, serverIp, serverPort);
			Session sshSession = jsch.getSession(serverUsername, serverIp, serverPort);
			sshSession.setPassword(serverPassword);
			Properties sshConfig = new Properties();
			sshConfig.put("StrictHostKeyChecking", "no");

            // 按服务器支持的顺序来配置
            sshConfig.put("kex", "curve25519-sha256@libssh.org," +
                    "diffie-hellman-group-exchange-sha256," +
                    "curve25519-sha256");
			sshSession.setConfig(sshConfig);
			sshSession.connect();
			log.info("SFTP Session Connecting..." + map.get("serverIp"));
			Channel channel = sshSession.openChannel("sftp");
			channel.connect();
			sftp = (ChannelSftp) channel;
			log.info("Connected to ..." + map.get("serverIp"));
		} catch (Exception e) {
			e.printStackTrace();
			log.error("SFTP连接服务器失败：" + e.getMessage());
		}
		return sftp;
	}
	/** 测试 */
	public static void main(String[] args) {
		SFTPUtils sftp = null;
		// 本地存放地址
		String localPath = "D:/bak/2017/08/";
		// Sftp下载路径
		String sftpPath = "/dmcs/consistency/BSS/";
		@SuppressWarnings("unused")
		List<String> filePathList = new ArrayList<String>();
		try {
			sftp = new SFTPUtils("135.24.251.51", "dmcs", "dmcs@123");
			sftp.connect();
			// 下载
			sftp.downloadFile(sftpPath, "DATA_GPRS_BSS_A_201708.dat", localPath, "DATA_GPRS_BSS_A_201708.dat");
//			sftp.batchDownLoadFile(sftpPath, localPath, "ASSESS", ".txt", true);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sftp.disconnect();
		}
	}
}
