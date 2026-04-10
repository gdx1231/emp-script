package com.gdxsoft.easyweb.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * SQL标识符验证器
 * 用于验证表名、字段名等SQL标识符，防止SQL注入
 */
public class SqlIdentifierValidator {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlIdentifierValidator.class);
    
    /**
     * 严格的SQL标识符正则：只允许字母、数字、下划线，且以字母或下划线开头
     */
    private static final Pattern STRICT_IDENTIFIER_PATTERN = 
        Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]{0,63}$");
    
    /**
     * 允许带数据库前缀的标识符：database.table
     */
    private static final Pattern QUALIFIED_IDENTIFIER_PATTERN = 
        Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]{0,63}(\\.[a-zA-Z_][a-zA-Z0-9_]{0,63})?$");
    
    /**
     * 允许的数据库关键字（白名单）
     */
    private static final Set<String> ALLOWED_KEYWORDS = new HashSet<>();
    
    static {
        // 常用的安全关键字
        ALLOWED_KEYWORDS.add("DUAL");
        ALLOWED_KEYWORDS.add("NULL");
        // 可以根据需要添加更多
    }
    
    /**
     * 验证SQL标识符（严格模式）
     * 只允许字母、数字、下划线，且以字母或下划线开头
     * 
     * @param identifier 要验证的标识符
     * @return 验证后的标识符
     * @throws IllegalArgumentException 如果标识符不合法
     */
    public static String validateStrict(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL identifier cannot be null or empty");
        }
        
        String trimmed = identifier.trim();
        
        // 检查是否为允许的关键字
        if (ALLOWED_KEYWORDS.contains(trimmed.toUpperCase())) {
            return trimmed;
        }
        
        // 严格验证
        if (!STRICT_IDENTIFIER_PATTERN.matcher(trimmed).matches()) {
            LOGGER.error("Invalid SQL identifier: {}", identifier);
            throw new IllegalArgumentException(
                "Invalid SQL identifier: " + identifier + 
                ". Only letters, numbers, and underscores are allowed, starting with a letter or underscore."
            );
        }
        
        return trimmed;
    }
    
    /**
     * 验证SQL标识符（允许database.table格式）
     * 
     * @param identifier 要验证的标识符
     * @return 验证后的标识符
     * @throws IllegalArgumentException 如果标识符不合法
     */
    public static String validateQualified(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL identifier cannot be null or empty");
        }
        
        String trimmed = identifier.trim();
        
        if (!QUALIFIED_IDENTIFIER_PATTERN.matcher(trimmed).matches()) {
            LOGGER.error("Invalid qualified SQL identifier: {}", identifier);
            throw new IllegalArgumentException(
                "Invalid SQL identifier: " + identifier + 
                ". Format should be: [database.]table, using only letters, numbers, and underscores."
            );
        }
        
        return trimmed;
    }
    
    /**
     * 验证~参数（用于动态表名替换）
     * 
     * @param paramName 参数名（如 TB）
     * @param paramValue 参数值
     * @param fromTrustedSource 是否来自可信源（SESSION/SYSTEM等）
     * @return 验证后的值
     * @throws SecurityException 如果验证失败
     */
    public static String validateTildeParam(String paramName, String paramValue, boolean fromTrustedSource) {
        // 可信源完全信任，允许任意SQL
        // 例如SESSION中的值来自管理员配置，SYSTEM中的值来自硬编码配置
        if (fromTrustedSource) {
            if (paramValue == null || paramValue.trim().isEmpty()) {
                throw new IllegalArgumentException("SQL identifier cannot be null or empty");
            }
            return paramValue.trim();
        }

        // 不可信源必须严格验证 - 先检查SQL注入模式
        if (containsSqlInjectionPattern(paramValue)) {
            LOGGER.error("Security violation: Tilde parameter {} contains SQL injection pattern: {}", 
                paramName, paramValue);
            throw new SecurityException(
                "Invalid parameter ~" + paramName + ": contains SQL injection pattern"
            );
        }
        
        // 严格验证
        String validated = validateStrict(paramValue);
        
        return validated;
    }
    
    /**
     * 检查字符串是否包含SQL注入特征
     * 
     * @param input 输入字符串
     * @return 如果包含SQL注入特征返回true
     */
    public static boolean containsSqlInjectionPattern(String input) {
        if (input == null) {
            return false;
        }
        
        String upper = input.toUpperCase();
        
        // 危险模式列表
        String[] dangerousPatterns = {
            ";\\s*(DROP|DELETE|UPDATE|INSERT|ALTER|CREATE|EXEC|EXECUTE)",
            "UNION\\s+(ALL\\s+)?SELECT",
            "OR\\s+1\\s*=\\s*1",
            "AND\\s+1\\s*=\\s*1",
            "--",
            "/\\*.*\\*/",
            "EXEC\\s*\\(",
            "EXECUTE\\s*\\(",
            "xp_",
            "sp_",
            "INFORMATION_SCHEMA",
            "SYSOBJECTS",
            "SYSCOLUMNS"
        };
        
        for (String pattern : dangerousPatterns) {
            if (upper.matches(".*" + pattern + ".*")) {
                return true;
            }
        }
        
        return false;
    }
}
