import com.example.ai.SemanticTranslatorService;
import com.example.ai.SemanticTranslatorService.ParseResult;
import com.example.ai.SemanticTranslatorService.TranslationResult;
import com.example.ai.SemanticTranslatorService.ValidationResult;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TestSemanticTranslatorLogs {
    public static void main(String[] args) {
        System.out.println("🧪 [测试程序] 开始测试语义翻译器日志功能");
        System.out.println("🔍 [测试程序] 这将验证Java程序是否正确调用了python-chinese-chess库");
        System.out.println("================================================================================");
        
        SemanticTranslatorService service = new SemanticTranslatorService();
        
        // 测试1: 解析单个记谱
        System.out.println("\n🧪 [测试程序] 测试1: 解析单个记谱");
        System.out.println("--------------------------------------------------");
        ParseResult result = service.parseNotation("炮二平五");
        if (result != null) {
            System.out.println("🎉 [测试程序] 解析成功: " + result.toString());
        } else {
            System.out.println("❌ [测试程序] 解析失败");
        }
        
        // 测试2: 批量翻译
        System.out.println("\n🧪 [测试程序] 测试2: 批量翻译记谱");
        System.out.println("--------------------------------------------------");
        List<String> notations = Arrays.asList("马二进三", "车一进一", "兵七进一");
        List<TranslationResult> batchResults = service.translateBatch(notations);
        System.out.println("🎉 [测试程序] 批量翻译完成，结果数量: " + batchResults.size());
        
        // 测试3: 验证记谱
        System.out.println("\n🧪 [测试程序] 测试3: 验证记谱");
        System.out.println("--------------------------------------------------");
        ValidationResult validation = service.validateNotation("士四进五");
        if (validation != null) {
            System.out.println("🎉 [测试程序] 验证完成，有效性: " + validation.isValid());
        } else {
            System.out.println("❌ [测试程序] 验证失败");
        }
        
        // 测试4: 智能解析
        System.out.println("\n🧪 [测试程序] 测试4: 智能解析");
        System.out.println("--------------------------------------------------");
        Map<String, Object> smartResult = service.smartParse("象三进五");
        System.out.println("🎉 [测试程序] 智能解析完成，结果: " + smartResult);
        
        System.out.println("\n=================================================================================");
        System.out.println("🏁 [测试程序] 所有测试完成！");
        System.out.println("🔍 [测试程序] 请查看上方日志确认python-chinese-chess库被正确调用");
    }
}