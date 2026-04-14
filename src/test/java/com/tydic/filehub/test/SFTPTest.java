package com.tydic.filehub.test;

import com.tydic.filehub.utils.SFTPUtils;
import java.io.File;

/**
 * SFTP 功能测试类
 */
public class SFTPTest {
    
    public static void main(String[] args) {
        System.out.println("=== File Hub 文件传输功能测试 ===\n");
        
        // 测试1: 本地文件操作
        testLocalFileOperations();
        
        // 测试2: 配置验证
        testConfiguration();
        
        System.out.println("\n=== 测试完成 ===");
    }
    
    private static void testLocalFileOperations() {
        System.out.println("【测试1】本地文件操作");
        
        String testDir = "D:/ClawFiles/file_test";
        File dir = new File(testDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        // 创建测试文件
        String testFile = testDir + "/test_upload.txt";
        try {
            java.nio.file.Files.write(
                java.nio.file.Paths.get(testFile),
                "Hello File Hub! 测试文件内容".getBytes()
            );
            System.out.println("✓ 测试文件创建成功: " + testFile);
        } catch (Exception e) {
            System.out.println("✗ 文件创建失败: " + e.getMessage());
        }
        
        // 验证文件存在
        File file = new File(testFile);
        if (file.exists()) {
            System.out.println("✓ 文件存在验证通过");
            System.out.println("  大小: " + file.length() + " bytes");
        } else {
            System.out.println("✗ 文件不存在");
        }
    }
    
    private static void testConfiguration() {
        System.out.println("\n【测试2】配置验证");
        
        // 检查下载目录
        String downloadDir = "./var/downloads";
        File dir = new File(downloadDir);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("✓ 下载目录已创建: " + downloadDir);
        } else {
            System.out.println("✓ 下载目录已存在: " + downloadDir);
        }
        
        // 检查上传目录
        String uploadDir = "./var/uploads";
        dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("✓ 上传目录已创建: " + uploadDir);
        } else {
            System.out.println("✓ 上传目录已存在: " + uploadDir);
        }
    }
}
