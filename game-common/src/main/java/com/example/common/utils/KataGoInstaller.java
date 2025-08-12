package com.example.common.utils;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * KataGo自动安装和检查工具类
 * 
 * 功能：
 * 1. 检查KataGo是否已安装
 * 2. 自动下载和安装KataGo
 * 3. 验证KataGo安装是否正确
 * 4. 下载必需的模型文件
 */
public class KataGoInstaller {
    
    // KataGo下载URLs (GitHub releases)
    private static final String KATAGO_GITHUB_API = "https://api.github.com/repos/lightvector/KataGo/releases/latest";
    private static final String KATAGO_MACOS_ARM64_URL = "https://github.com/lightvector/KataGo/releases/download/v1.13.2/katago-v1.13.2-opencl-macos.zip";
    private static final String KATAGO_MACOS_X64_URL = "https://github.com/lightvector/KataGo/releases/download/v1.13.2/katago-v1.13.2-opencl-macos.zip";
    private static final String KATAGO_LINUX_X64_URL = "https://github.com/lightvector/KataGo/releases/download/v1.13.2/katago-v1.13.2-opencl-linux-x64.zip";
    private static final String KATAGO_WINDOWS_X64_URL = "https://github.com/lightvector/KataGo/releases/download/v1.13.2/katago-v1.13.2-opencl-windows-x64.zip";
    
    // 模型文件URL
    private static final String KATAGO_MODEL_URL = "https://github.com/lightvector/KataGo/releases/download/v1.4.5/g170-b6c96-s175395328-d26788732.bin.gz";
    private static final String KATAGO_CONFIG_URL = "https://raw.githubusercontent.com/lightvector/KataGo/v1.13.2/cpp/configs/gtp_example.cfg";
    
    // 安装目录
    private static final String KATAGO_DIR = System.getProperty("user.home") + "/.katago";
    private static final String KATAGO_BIN_DIR = KATAGO_DIR + "/bin";
    private static final String KATAGO_MODELS_DIR = KATAGO_DIR + "/models";
    private static final String KATAGO_CONFIGS_DIR = KATAGO_DIR + "/configs";
    
    private static KataGoInstaller instance;
    
    public static synchronized KataGoInstaller getInstance() {
        if (instance == null) {
            instance = new KataGoInstaller();
        }
        return instance;
    }
    
    private KataGoInstaller() {
        // Private constructor
    }
    
    /**
     * 检查KataGo是否已安装且可用
     * 
     * @return 如果KataGo可用返回true，否则返回false
     */
    public boolean isKataGoInstalled() {
        try {
            String executablePath = getKataGoExecutablePath();
            if (executablePath == null) {
                return false;
            }
            
            File executable = new File(executablePath);
            if (!executable.exists() || !executable.canExecute()) {
                return false;
            }
            
            // 尝试运行KataGo验证
            ProcessBuilder pb = new ProcessBuilder(executablePath, "version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            int exitCode = process.waitFor();
            
            return exitCode == 0 && line != null && line.toLowerCase().contains("katago");
            
        } catch (Exception e) {
            System.err.println("检查KataGo安装状态时出错: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取KataGo可执行文件路径
     */
    public String getKataGoExecutablePath() {
        String osName = System.getProperty("os.name").toLowerCase();
        String executable;
        
        if (osName.contains("windows")) {
            executable = "katago.exe";
        } else {
            executable = "katago";
        }
        
        // 首先检查本地安装目录
        String localPath = KATAGO_BIN_DIR + File.separator + executable;
        if (new File(localPath).exists()) {
            return localPath;
        }
        
        // 检查系统PATH
        String[] paths = System.getenv("PATH").split(File.pathSeparator);
        for (String path : paths) {
            File file = new File(path, executable);
            if (file.exists() && file.canExecute()) {
                return file.getAbsolutePath();
            }
        }
        
        // 检查常见安装位置
        String[] commonPaths = {
            "/usr/local/bin/" + executable,
            "/usr/bin/" + executable,
            "/opt/homebrew/bin/" + executable,
            System.getProperty("user.home") + "/bin/" + executable
        };
        
        for (String path : commonPaths) {
            if (new File(path).exists()) {
                return path;
            }
        }
        
        return null;
    }
    
    /**
     * 自动安装KataGo
     * 
     * @param progressCallback 进度回调函数
     * @return 安装成功返回true，失败返回false
     */
    public boolean installKataGo(ProgressCallback progressCallback) {
        try {
            progressCallback.onProgress(0, "开始安装KataGo...");
            
            // 创建安装目录
            createDirectories();
            progressCallback.onProgress(10, "创建安装目录...");
            
            // 检测操作系统并选择合适的下载URL
            String downloadUrl = getDownloadUrl();
            if (downloadUrl == null) {
                progressCallback.onProgress(100, "不支持的操作系统");
                return false;
            }
            
            progressCallback.onProgress(20, "正在下载KataGo...");
            
            // 下载KataGo
            String tempFile = downloadFile(downloadUrl, "katago.zip");
            if (tempFile == null) {
                progressCallback.onProgress(100, "下载KataGo失败");
                return false;
            }
            
            progressCallback.onProgress(60, "正在解压KataGo...");
            
            // 解压文件
            if (!extractKataGo(tempFile)) {
                progressCallback.onProgress(100, "解压KataGo失败");
                return false;
            }
            
            progressCallback.onProgress(70, "正在下载模型文件...");
            
            // 下载模型文件
            String modelFile = downloadFile(KATAGO_MODEL_URL, "model.bin.gz");
            if (modelFile != null) {
                Files.move(Paths.get(modelFile), Paths.get(KATAGO_MODELS_DIR, "model.bin.gz"), StandardCopyOption.REPLACE_EXISTING);
            }
            
            progressCallback.onProgress(85, "正在下载配置文件...");
            
            // 下载配置文件
            String configFile = downloadFile(KATAGO_CONFIG_URL, "gtp_example.cfg");
            if (configFile != null) {
                Files.move(Paths.get(configFile), Paths.get(KATAGO_CONFIGS_DIR, "gtp_example.cfg"), StandardCopyOption.REPLACE_EXISTING);
            }
            
            progressCallback.onProgress(95, "设置文件权限...");
            
            // 设置可执行权限（Unix系统）
            setExecutablePermissions();
            
            // 清理临时文件
            new File(tempFile).delete();
            
            progressCallback.onProgress(100, "KataGo安装完成");
            
            return isKataGoInstalled();
            
        } catch (Exception e) {
            progressCallback.onProgress(100, "安装失败: " + e.getMessage());
            System.err.println("安装KataGo时出错: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void createDirectories() throws IOException {
        Files.createDirectories(Paths.get(KATAGO_BIN_DIR));
        Files.createDirectories(Paths.get(KATAGO_MODELS_DIR));
        Files.createDirectories(Paths.get(KATAGO_CONFIGS_DIR));
    }
    
    private String getDownloadUrl() {
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();
        
        if (osName.contains("mac")) {
            if (osArch.contains("aarch64") || osArch.contains("arm")) {
                return KATAGO_MACOS_ARM64_URL;
            } else {
                return KATAGO_MACOS_X64_URL;
            }
        } else if (osName.contains("linux")) {
            return KATAGO_LINUX_X64_URL;
        } else if (osName.contains("windows")) {
            return KATAGO_WINDOWS_X64_URL;
        }
        
        return null;
    }
    
    private String downloadFile(String urlString, String fileName) {
        try {
            URL url = new URL(urlString);
            String tempFile = System.getProperty("java.io.tmpdir") + File.separator + fileName;
            
            try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                 FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            }
            
            return tempFile;
            
        } catch (Exception e) {
            System.err.println("下载文件失败 " + urlString + ": " + e.getMessage());
            return null;
        }
    }
    
    private boolean extractKataGo(String zipFilePath) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            
            while ((entry = zis.getNextEntry()) != null) {
                String fileName = entry.getName();
                
                // 只提取可执行文件
                if (fileName.contains("katago") && !entry.isDirectory()) {
                    String targetPath = KATAGO_BIN_DIR + File.separator;
                    
                    if (fileName.contains("katago.exe")) {
                        targetPath += "katago.exe";
                    } else if (fileName.contains("katago")) {
                        targetPath += "katago";
                    } else {
                        continue;
                    }
                    
                    Files.copy(zis, Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
                    break;
                }
                zis.closeEntry();
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("解压文件失败: " + e.getMessage());
            return false;
        }
    }
    
    private void setExecutablePermissions() {
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            if (!osName.contains("windows")) {
                Path executablePath = Paths.get(KATAGO_BIN_DIR, "katago");
                if (Files.exists(executablePath)) {
                    Set<PosixFilePermission> perms = new HashSet<>();
                    perms.add(PosixFilePermission.OWNER_READ);
                    perms.add(PosixFilePermission.OWNER_WRITE);
                    perms.add(PosixFilePermission.OWNER_EXECUTE);
                    perms.add(PosixFilePermission.GROUP_READ);
                    perms.add(PosixFilePermission.GROUP_EXECUTE);
                    perms.add(PosixFilePermission.OTHERS_READ);
                    perms.add(PosixFilePermission.OTHERS_EXECUTE);
                    Files.setPosixFilePermissions(executablePath, perms);
                }
            }
        } catch (Exception e) {
            System.err.println("设置可执行权限失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取模型文件路径
     */
    public String getModelPath() {
        String modelPath = KATAGO_MODELS_DIR + File.separator + "model.bin.gz";
        return Files.exists(Paths.get(modelPath)) ? modelPath : null;
    }
    
    /**
     * 获取配置文件路径
     */
    public String getConfigPath() {
        String configPath = KATAGO_CONFIGS_DIR + File.separator + "gtp_example.cfg";
        return Files.exists(Paths.get(configPath)) ? configPath : null;
    }
    
    /**
     * 进度回调接口
     */
    public interface ProgressCallback {
        void onProgress(int percentage, String message);
    }
    
    /**
     * 检查并安装KataGo（如果需要）
     * 
     * @param progressCallback 进度回调
     * @return 成功返回true，失败返回false
     */
    public boolean ensureKataGoInstalled(ProgressCallback progressCallback) {
        if (isKataGoInstalled()) {
            if (progressCallback != null) {
                progressCallback.onProgress(100, "KataGo已安装并可用");
            }
            return true;
        }
        
        if (progressCallback != null) {
            progressCallback.onProgress(0, "检测到KataGo未安装，开始自动安装...");
        }
        
        return installKataGo(progressCallback != null ? progressCallback : new ProgressCallback() {
            @Override
            public void onProgress(int percentage, String message) {
                System.out.println(String.format("[%d%%] %s", percentage, message));
            }
        });
    }
}
