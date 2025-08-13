import com.example.common.utils.KataGoInstaller;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestModelPath {
    public static void main(String[] args) {
        KataGoInstaller installer = KataGoInstaller.getInstance();
        
        System.out.println("=== 调试模型文件检查 ===");
        
        String modelPath = installer.getModelPath();
        System.out.println("1. getModelPath() 返回: " + modelPath);
        
        if (modelPath != null) {
            Path path = Paths.get(modelPath);
            System.out.println("2. Files.exists(path): " + Files.exists(path));
            System.out.println("3. Files.isSymbolicLink(path): " + Files.isSymbolicLink(path));
            
            File file = new File(modelPath);
            System.out.println("4. File.exists(): " + file.exists());
            System.out.println("5. File.canRead(): " + file.canRead());
            System.out.println("6. File.length(): " + file.length());
            
            if (Files.isSymbolicLink(path)) {
                try {
                    Path realPath = Files.readSymbolicLink(path);
                    System.out.println("7. 符号链接目标: " + realPath);
                    System.out.println("8. 目标文件存在: " + Files.exists(realPath));
                } catch (Exception e) {
                    System.out.println("7. 读取符号链接失败: " + e.getMessage());
                }
            }
        }
    }
}
