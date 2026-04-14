package com.tydic.filehub.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "filehub")
public class FileHubProperties {

    private final Scheduler scheduler = new Scheduler();
    private final Storage storage = new Storage();
    private final Crypto crypto = new Crypto();
    private final Oss oss = new Oss();

    @Data
    public static class Scheduler {
        private int poolSize = 10;
    }

    @Data
    public static class Storage {
        private String localDownloadDir = "./var/downloads";
        private String localUploadDir = "./var/uploads";
    }

    @Data
    public static class Crypto {
        private String decryptType = "jasypt";
        private String aesKey = "changeit-changeit";
    }

    @Data
    public static class Oss {
        private boolean enabled;
        private String endpoint;
        private String accessKeyId;
        private String accessKeySecret;
        private String bucketName;
        private String folderName;
        private String address;
    }
}
