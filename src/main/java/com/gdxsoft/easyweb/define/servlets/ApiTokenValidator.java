package com.gdxsoft.easyweb.define.servlets;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConfAdmin;
import com.gdxsoft.easyweb.conf.ConfAdmins;

/**
 * API Token 安全验证器
 * 
 * 支持两种验证模式：
 * 1. HMAC 签名模式（推荐）- 用于服务端调用
 * 2. JWT Token 模式 - 用于客户端会话
 * 
 * HMAC 签名模式请求格式：
 * Header: 
 *   X-Api-Key: {loginId}
 *   X-Api-Timestamp: {当前时间戳毫秒}
 *   X-Api-Nonce: {随机字符串，防重放}
 *   X-Api-Signature: {HMAC-SHA256签名}
 * 
 * 签名算法：
 *   signature = HMAC-SHA256(secret, method + "\n" + timestamp + "\n" + nonce + "\n" + path + "\n" + sortedQueryParams)
 * 
 * JWT Token 模式：
 *   Header: Authorization: Bearer {jwt_token}
 *   或
 *   Header: X-Api-Token: {jwt_token}
 */
public class ApiTokenValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiTokenValidator.class);

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String SHA256 = "SHA-256";

    // Header 名称
    public static final String HEADER_API_KEY = "X-Api-Key";
    public static final String HEADER_TIMESTAMP = "X-Api-Timestamp";
    public static final String HEADER_NONCE = "X-Api-Nonce";
    public static final String HEADER_SIGNATURE = "X-Api-Signature";
    public static final String HEADER_TOKEN = "X-Api-Token";
    public static final String HEADER_AUTHORIZATION = "Authorization";

    // 时间戳有效期（毫秒），默认 5 分钟
    private static final long TIMESTAMP_TOLERANCE_MS = 5 * 60 * 1000;

    // Nonce 缓存，防重放攻击
    private static final ConcurrentHashMap<String, Long> NONCE_CACHE = new ConcurrentHashMap<>();
    private static final int NONCE_CACHE_MAX_SIZE = 10000;
    private static final long NONCE_EXPIRE_MS = 10 * 60 * 1000; // 10分钟过期

    // JWT Token 缓存
    private static final ConcurrentHashMap<String, TokenInfo> TOKEN_CACHE = new ConcurrentHashMap<>();
    private static final long TOKEN_EXPIRE_MS = 2 * 60 * 60 * 1000; // 2小时过期

    /**
     * Token 信息
     */
    public static class TokenInfo {
        private String loginId;
        private long createTime;
        private long expireTime;
        private String tokenHash;

        public TokenInfo(String loginId, long expireMs) {
            this.loginId = loginId;
            this.createTime = System.currentTimeMillis();
            this.expireTime = this.createTime + expireMs;
        }

        public String getLoginId() {
            return loginId;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }

        public long getExpireTime() {
            return expireTime;
        }
    }

    /**
     * 验证结果
     */
    public static class ValidationResult {
        private boolean valid;
        private String loginId;
        private String errorMessage;
        private int errorCode;
        private ConfAdmin admin;

        private ValidationResult(boolean valid, String loginId, String errorMessage, int errorCode) {
            this.valid = valid;
            this.loginId = loginId;
            this.errorMessage = errorMessage;
            this.errorCode = errorCode;
        }

        public static ValidationResult success(String loginId, ConfAdmin admin) {
            ValidationResult r = new ValidationResult(true, loginId, null, 0);
            r.admin = admin;
            return r;
        }

        public static ValidationResult fail(String errorMessage, int errorCode) {
            return new ValidationResult(false, null, errorMessage, errorCode);
        }

        public boolean isValid() {
            return valid;
        }

        public String getLoginId() {
            return loginId;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public int getErrorCode() {
            return errorCode;
        }

        public ConfAdmin getAdmin() {
            return admin;
        }
    }

    /**
     * 验证请求（自动检测验证模式）
     * 
     * @param request HTTP 请求
     * @return 验证结果
     */
    public static ValidationResult validate(HttpServletRequest request) {
        // 清理过期的 nonce 和 token
        cleanupExpired();

        // 检查 JWT Token 模式
        String jwtToken = extractJwtToken(request);
        if (jwtToken != null) {
            return validateJwtToken(jwtToken);
        }

        // 检查 HMAC 签名模式
        String apiKey = request.getHeader(HEADER_API_KEY);
        if (apiKey != null) {
            return validateHmacSignature(request);
        }

        // 兼容旧的简单 token 模式（不推荐）
        String simpleToken = request.getHeader("token");
        if (simpleToken != null) {
            return validateSimpleToken(simpleToken);
        }

        return ValidationResult.fail("Missing authentication credentials", 401);
    }

    /**
     * HMAC 签名验证
     */
    private static ValidationResult validateHmacSignature(HttpServletRequest request) {
        String apiKey = request.getHeader(HEADER_API_KEY);
        String timestampStr = request.getHeader(HEADER_TIMESTAMP);
        String nonce = request.getHeader(HEADER_NONCE);
        String signature = request.getHeader(HEADER_SIGNATURE);

        // 检查必需参数
        if (StringUtils.isBlank(apiKey)) {
            return ValidationResult.fail("Missing X-Api-Key header", 401);
        }
        if (StringUtils.isBlank(timestampStr)) {
            return ValidationResult.fail("Missing X-Api-Timestamp header", 401);
        }
        if (StringUtils.isBlank(nonce)) {
            return ValidationResult.fail("Missing X-Api-Nonce header", 401);
        }
        if (StringUtils.isBlank(signature)) {
            return ValidationResult.fail("Missing X-Api-Signature header", 401);
        }

        // 验证时间戳
        long timestamp;
        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            return ValidationResult.fail("Invalid timestamp format", 400);
        }

        long currentTime = System.currentTimeMillis();
        if (Math.abs(currentTime - timestamp) > TIMESTAMP_TOLERANCE_MS) {
            return ValidationResult.fail("Timestamp expired or invalid", 401);
        }

        // 验证 nonce（防重放）
        if (NONCE_CACHE.containsKey(nonce)) {
            return ValidationResult.fail("Nonce already used (replay attack detected)", 401);
        }

        // 获取管理员信息和密钥
        ConfAdmin admin = findAdminByLoginId(apiKey);
        if (admin == null) {
            return ValidationResult.fail("Invalid API key", 401);
        }

        String secret = admin.getPassword();
        if (StringUtils.isBlank(secret)) {
            return ValidationResult.fail("Admin has no secret configured", 500);
        }

        // 构建签名字符串
        String method = request.getMethod().toUpperCase();
        String path = request.getRequestURI();
        String sortedParams = getSortedQueryParams(request);
        String stringToSign = method + "\n" + timestampStr + "\n" + nonce + "\n" + path + "\n" + sortedParams;

        // 计算签名
        String expectedSignature;
        try {
            expectedSignature = hmacSha256Hex(secret, stringToSign);
        } catch (Exception e) {
            LOGGER.error("Error calculating signature", e);
            return ValidationResult.fail("Signature calculation error", 500);
        }

        // 验证签名
        if (!constantTimeEquals(signature.toLowerCase(), expectedSignature.toLowerCase())) {
            LOGGER.warn("Signature mismatch. Expected: {}, Received: {}", expectedSignature, signature);
            return ValidationResult.fail("Invalid signature", 401);
        }

        // 记录 nonce
        NONCE_CACHE.put(nonce, currentTime);

        return ValidationResult.success(apiKey, admin);
    }

    /**
     * JWT Token 验证
     */
    private static ValidationResult validateJwtToken(String token) {
        if (StringUtils.isBlank(token)) {
            return ValidationResult.fail("Empty token", 401);
        }

        // 计算 token 哈希作为缓存 key
        String tokenHash = sha256Hex(token);

        // 检查缓存
        TokenInfo tokenInfo = TOKEN_CACHE.get(tokenHash);
        if (tokenInfo == null) {
            return ValidationResult.fail("Invalid or expired token", 401);
        }

        if (tokenInfo.isExpired()) {
            TOKEN_CACHE.remove(tokenHash);
            return ValidationResult.fail("Token expired", 401);
        }

        // 获取管理员信息
        ConfAdmin admin = findAdminByLoginId(tokenInfo.getLoginId());
        if (admin == null) {
            TOKEN_CACHE.remove(tokenHash);
            return ValidationResult.fail("Admin not found", 401);
        }

        return ValidationResult.success(tokenInfo.getLoginId(), admin);
    }

    /**
     * 简单 Token 验证（兼容旧模式）
     */
    private static ValidationResult validateSimpleToken(String token) {
        if (StringUtils.isBlank(token)) {
            return ValidationResult.fail("Empty token", 401);
        }

        ConfAdmins admins = ConfAdmins.getInstance();
        if (admins == null || admins.getLst() == null) {
            return ValidationResult.fail("No admins configured", 500);
        }

        for (ConfAdmin admin : admins.getLst()) {
            if (token.equals(admin.getPassword())) {
                return ValidationResult.success(admin.getLoginId(), admin);
            }
        }

        return ValidationResult.fail("Invalid token", 401);
    }

    /**
     * 生成 JWT Token（登录后调用）
     * 
     * @param loginId 登录ID
     * @param password 密码
     * @return Token 信息，失败返回 null
     */
    public static JSONObject generateToken(String loginId, String password) {
        ConfAdmin admin = findAdminByLoginId(loginId);
        if (admin == null) {
            return null;
        }

        // 验证密码
        if (!password.equals(admin.getPassword())) {
            return null;
        }

        // 生成 token
        String rawToken = generateSecureToken();
        String tokenHash = sha256Hex(rawToken);

        // 创建 token 信息
        TokenInfo tokenInfo = new TokenInfo(loginId, TOKEN_EXPIRE_MS);
        TOKEN_CACHE.put(tokenHash, tokenInfo);

        JSONObject result = new JSONObject();
        result.put("RST", true);
        result.put("token", rawToken);
        result.put("token_type", "Bearer");
        result.put("expires_in", TOKEN_EXPIRE_MS / 1000);
        result.put("login_id", loginId);

        return result;
    }

    /**
     * 撤销 Token（登出）
     * 
     * @param token 要撤销的 token
     */
    public static void revokeToken(String token) {
        if (StringUtils.isBlank(token)) {
            return;
        }
        String tokenHash = sha256Hex(token);
        TOKEN_CACHE.remove(tokenHash);
    }

    /**
     * 从请求中提取 JWT Token
     */
    private static String extractJwtToken(HttpServletRequest request) {
        // 检查 X-Api-Token header
        String token = request.getHeader(HEADER_TOKEN);
        if (token != null) {
            return token;
        }

        // 检查 Authorization header (Bearer token)
        String authHeader = request.getHeader(HEADER_AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }

    /**
     * 根据 loginId 查找管理员
     */
    private static ConfAdmin findAdminByLoginId(String loginId) {
        ConfAdmins admins = ConfAdmins.getInstance();
        if (admins == null || admins.getLst() == null) {
            return null;
        }

        for (ConfAdmin admin : admins.getLst()) {
            if (loginId.equals(admin.getLoginId())) {
                return admin;
            }
        }

        return null;
    }

    /**
     * 获取排序后的查询参数
     */
    private static String getSortedQueryParams(HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();
        if (params == null || params.isEmpty()) {
            return "";
        }

        String[] sortedKeys = params.keySet().toArray(new String[0]);
        Arrays.sort(sortedKeys);

        StringBuilder sb = new StringBuilder();
        for (String key : sortedKeys) {
            // 跳过 method 参数（用于路由）
            if ("method".equalsIgnoreCase(key)) {
                continue;
            }
            String[] values = params.get(key);
            if (values != null) {
                for (String value : values) {
                    if (sb.length() > 0) {
                        sb.append("&");
                    }
                    sb.append(key).append("=").append(value != null ? value : "");
                }
            }
        }

        return sb.toString();
    }

    /**
     * HMAC-SHA256 计算
     */
    private static String hmacSha256Hex(String secret, String data) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hmacBytes);
    }

    /**
     * SHA-256 哈希
     */
    private static String sha256Hex(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA256);
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 calculation failed", e);
        }
    }

    /**
     * 字节数组转十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 常量时间比较（防止时序攻击）
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    /**
     * 生成安全随机 Token
     */
    private static String generateSecureToken() {
        try {
            java.security.SecureRandom random = new java.security.SecureRandom();
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            return bytesToHex(bytes);
        } catch (Exception e) {
            // 回退到 UUID
            return java.util.UUID.randomUUID().toString().replace("-", "");
        }
    }

    /**
     * 清理过期的 nonce 和 token
     */
    private static void cleanupExpired() {
        long now = System.currentTimeMillis();

        // 清理 nonce
        if (NONCE_CACHE.size() > NONCE_CACHE_MAX_SIZE) {
            NONCE_CACHE.entrySet().removeIf(entry -> 
                (now - entry.getValue()) > NONCE_EXPIRE_MS
            );
        }

        // 清理 token
        TOKEN_CACHE.entrySet().removeIf(entry -> 
            entry.getValue().isExpired()
        );
    }

    /**
     * 获取签名算法说明（用于文档）
     */
    public static String getSignatureAlgorithmDoc() {
        return "HMAC Signature Algorithm:\n" +
               "1. Build string to sign: METHOD + \"\\n\" + TIMESTAMP + \"\\n\" + NONCE + \"\\n\" + PATH + \"\\n\" + SORTED_QUERY_PARAMS\n" +
               "2. Calculate: HMAC-SHA256(secret, stringToSign)\n" +
               "3. Convert to lowercase hex string\n\n" +
               "Example:\n" +
               "  X-Api-Key: admin\n" +
               "  X-Api-Timestamp: 1700000000000\n" +
               "  X-Api-Nonce: abc123random\n" +
               "  X-Api-Signature: a1b2c3d4...\n\n" +
               "Headers:\n" +
               "  - X-Api-Key: Admin login ID\n" +
               "  - X-Api-Timestamp: Current time in milliseconds\n" +
               "  - X-Api-Nonce: Random string (prevent replay)\n" +
               "  - X-Api-Signature: HMAC-SHA256 signature";
    }
}