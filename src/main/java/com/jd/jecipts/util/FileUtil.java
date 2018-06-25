package com.jd.jecipts.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 操作文件及文件夹的一些工具方法
 *
 * @author zhongjinyan
 * @date 2018-03-07
 */
public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    /**
     * 确认指定文件夹是否存在 不存在则创建
     * @param path
     */
    public static boolean checkDirExist(String path) {
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            return file.mkdir();
        }

        return true;
    }

    /**
     * 确保表示路径的字符串最后带有"/"符号 用于字符串的拼接
     * @param path
     * @return
     */
    public static String getWholeDirPath(String path) {
        String lastChar = path.substring(path.length() - 2, path.length() - 1);
        if (!lastChar.equals("/")) {
            return path + "/";
        }
        return path;
    }

    /**
     * 删除单个文件
     * @param filePath
     * @return
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                return true;
            } else {
                logger.error("target file(" + filePath + ") is exist, but execute delete failure");
                return false;
            }
        } else {
            logger.error("target file(" + filePath + ") is not file or is not exist");
            return false;
        }
    }
}
