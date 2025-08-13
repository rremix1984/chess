import com.example.common.utils.KataGoInstaller;

public class TestKataGoInstaller {
    public static void main(String[] args) {
        System.out.println("测试KataGo安装检查...");
        
        KataGoInstaller installer = KataGoInstaller.getInstance();
        
        System.out.println("1. 检查可执行文件路径:");
        String execPath = installer.getKataGoExecutablePath();
        System.out.println("   路径: " + execPath);
        
        System.out.println("2. 检查模型文件路径:");
        String modelPath = installer.getModelPath();
        System.out.println("   路径: " + modelPath);
        
        System.out.println("3. 检查配置文件路径:");
        String configPath = installer.getConfigPath();
        System.out.println("   路径: " + configPath);
        
        System.out.println("4. 完整安装检查:");
        boolean isInstalled = installer.isKataGoInstalled();
        System.out.println("   结果: " + isInstalled);
    }
}
