package com.tydic.filehub.utils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 标题：类名称:UploadUtil
 * 说明：
 * 时间：2022/7/4 22:08
 * 作者 @author 赖思璇
 */
public class UploadUtil {
    protected static final Logger logger = LoggerFactory.getLogger(UploadUtil.class);

    private String uploadSftpIpaddr; // sftp 上传的ip地址

    private String uploadSftpPort; // sftp 上传的端口

    private String uploadSftpUsername; // sftp 上传的用户名

    private String uploadSftpPasswd; // sftp 上传的密码

    private String uploadSftpFileAddr; // sftp 上传的目录

    private String uploadSftpLocalFileAddr; // sftp 上传的目录本地备份


    /**
     * 删除本地文件
     */
    public void deleteLocalFile(){
        File file = new File(uploadSftpLocalFileAddr);
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("----------------删除文件失败：" + e.getMessage() + "--------------");
        }
        logger.info("----------------删除文件完成--------------");
    }

    /**
     * 上传文件
     * @param info  描述
     * @param list  数据
     * @param fileName  文件名
     * @param <T>
     * @throws Exception
     */
    public <T> void upload(String info, List<T> list, String fileName) throws Exception {

        SFTPUtils sFTPUtils = new SFTPUtils(uploadSftpIpaddr, uploadSftpUsername, uploadSftpPasswd);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar ca = Calendar.getInstance();
        // 设置时间为当前时间
        ca.setTime(new Date());
        Date time = ca.getTime();

        fileName = fileName + sdf.format(time) + ".txt";
        logger.info("Time:["
                + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())
                + "], 开始上传"+info+"文件");
        createTFile(fileName, list);

        // 连接服务器
        sFTPUtils.connect();
        //开始上传文件
        logger.info("==========开始上传"+info+"文件：" + fileName + "=============");
        try {
            sFTPUtils.uploadFile(uploadSftpFileAddr, fileName, uploadSftpLocalFileAddr, fileName);
        } catch (Exception e) {
            throw new Exception("上传"+info+"文件失败");
        }
    }


    public <T> void createTFile(String fileName,List<T> list) {
        File f = new File(uploadSftpLocalFileAddr);
        if (!f.exists()) {
            f.mkdirs(); //创建目录
        }
        File file = new File(uploadSftpLocalFileAddr, fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String filePath = file.getAbsolutePath();

        BufferedWriter bufferedWriter = null;
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
        for (int i = 0; i < list.size(); i++) {
            try {
                String message = "";
                T t = noNullStringAttr(list.get(i));

                message = t.toString();

                logger.info("第" + (i + 1) + "行数据: " + message);
                bufferedWriter.write(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                bufferedWriter.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 把对象中的 String 类型的 null 字段，转换为空字符串
     *
     * @param <T> 待转化对象类型
     * @param cls 待转化对象
     * @return 转化好的对象
     */
    public static <T> T noNullStringAttr(T cls) {
        Field[] fields = cls.getClass().getDeclaredFields();
        if (fields == null || fields.length == 0) {
            return cls;
        }
        for (Field field : fields) {
            if ("String".equals(field.getType().getSimpleName())) {
                field.setAccessible(true);
                try {
                    Object value = field.get(cls);
                    if (value == null) {
                        field.set(cls, "");
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return cls;
    }

    public UploadUtil(String uploadSftpIpaddr, String uploadSftpPort, String uploadSftpUsername, String uploadSftpPasswd, String uploadSftpFileAddr, String uploadSftpLocalFileAddr) {
        this.uploadSftpIpaddr = uploadSftpIpaddr;
        this.uploadSftpPort = uploadSftpPort;
        this.uploadSftpUsername = uploadSftpUsername;
        this.uploadSftpPasswd = uploadSftpPasswd;
        this.uploadSftpFileAddr = uploadSftpFileAddr;
        this.uploadSftpLocalFileAddr = uploadSftpLocalFileAddr;
    }
}
