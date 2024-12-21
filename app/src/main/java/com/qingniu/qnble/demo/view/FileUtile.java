package com.qingniu.qnble.demo.view;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtile {

    // 获取 assets 目录下 v09.ufw 文件的 File 对象
    public static File getUfwFile(Context context, String fileName) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(fileName); // 访问 assets 目录下的 v09.ufw 文件
            File cacheFile = new File(context.getCacheDir(), fileName); // 创建一个 File 对象指向缓存目录

            try (FileOutputStream outputStream = new FileOutputStream(cacheFile)) {
                byte[] buffer = new byte[1024];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read); // 将输入流的数据写入到文件
                }
            } finally {
                inputStream.close(); // 关闭输入流
            }

            return cacheFile; // 返回文件对象
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] fileToByteArray(File file) {
        // 检查文件是否存在
        if (!file.exists() || !file.isFile()) {
            System.err.println("File does not exist or is not a file.");
            return null; // 或者返回一个空数组: return new byte[0];
        }

        // 创建 FileInputStream 和 ByteArrayOutputStream
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024]; // 可以根据需要调整缓冲区大小
            int bytesRead;

            // 读取文件数据并写入到 ByteArrayOutputStream
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            // 将 ByteArrayOutputStream 转换为字节数组并返回
            return bos.toByteArray();
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return null; // 或者返回一个空数组: return new byte[0];
        }
    }
}
