import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestSymlink {
    public static void main(String[] args) {
        String modelPath = "/Users/rremixwang/.katago/models/model.bin.gz";
        
        System.out.println("测试符号链接: " + modelPath);
        
        File file = new File(modelPath);
        System.out.println("File.exists(): " + file.exists());
        System.out.println("File.canRead(): " + file.canRead());
        System.out.println("File.length(): " + file.length());
        
        Path path = Paths.get(modelPath);
        System.out.println("Files.exists(): " + Files.exists(path));
        System.out.println("Files.isSymbolicLink(): " + Files.isSymbolicLink(path));
        
        if (Files.isSymbolicLink(path)) {
            try {
                Path realPath = Files.readSymbolicLink(path);
                System.out.println("符号链接目标: " + realPath);
                System.out.println("目标文件是否存在: " + Files.exists(realPath));
                
                if (!realPath.isAbsolute()) {
                    Path parentDir = path.getParent();
                    realPath = parentDir.resolve(realPath);
                    System.out.println("解析后的绝对路径: " + realPath);
                    System.out.println("绝对路径是否存在: " + Files.exists(realPath));
                }
            } catch (Exception e) {
                System.out.println("读取符号链接失败: " + e.getMessage());
            }
        }
    }
}
