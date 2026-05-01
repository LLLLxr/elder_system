package org.smart_elder_system.user.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class IdCardUtil {
    
    private static final String ID_CARD_PATTERN = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$";
    
    private static final Map<String, String> PROVINCE_CODES = new HashMap<>();
    
    static {
        PROVINCE_CODES.put("11", "北京市");
        PROVINCE_CODES.put("12", "天津市");
        PROVINCE_CODES.put("13", "河北省");
        PROVINCE_CODES.put("14", "山西省");
        PROVINCE_CODES.put("15", "内蒙古自治区");
        PROVINCE_CODES.put("21", "辽宁省");
        PROVINCE_CODES.put("22", "吉林省");
        PROVINCE_CODES.put("23", "黑龙江省");
        PROVINCE_CODES.put("31", "上海市");
        PROVINCE_CODES.put("32", "江苏省");
        PROVINCE_CODES.put("33", "浙江省");
        PROVINCE_CODES.put("34", "安徽省");
        PROVINCE_CODES.put("35", "福建省");
        PROVINCE_CODES.put("36", "江西省");
        PROVINCE_CODES.put("37", "山东省");
        PROVINCE_CODES.put("41", "河南省");
        PROVINCE_CODES.put("42", "湖北省");
        PROVINCE_CODES.put("43", "湖南省");
        PROVINCE_CODES.put("44", "广东省");
        PROVINCE_CODES.put("45", "广西壮族自治区");
        PROVINCE_CODES.put("46", "海南省");
        PROVINCE_CODES.put("50", "重庆市");
        PROVINCE_CODES.put("51", "四川省");
        PROVINCE_CODES.put("52", "贵州省");
        PROVINCE_CODES.put("53", "云南省");
        PROVINCE_CODES.put("54", "西藏自治区");
        PROVINCE_CODES.put("61", "陕西省");
        PROVINCE_CODES.put("62", "甘肃省");
        PROVINCE_CODES.put("63", "青海省");
        PROVINCE_CODES.put("64", "宁夏回族自治区");
        PROVINCE_CODES.put("65", "新疆维吾尔自治区");
    }
    
    /**
     * 验证身份证号码格式
     */
    public static boolean validateIdCardFormat(String idCard) {
        if (idCard == null || !idCard.matches(ID_CARD_PATTERN)) {
            return false;
        }
        
        // 验证省份代码
        String provinceCode = idCard.substring(0, 2);
        if (!PROVINCE_CODES.containsKey(provinceCode)) {
            return false;
        }
        
        // 验证校验位
        return validateCheckDigit(idCard);
    }
    
    /**
     * 验证身份证号码（主方法）
     */
    public static boolean validateIdCard(String idCard) {
        if (idCard == null || idCard.trim().isEmpty()) {
            return false;
        }
        
        // 去除空格
        idCard = idCard.trim();
        
        // 15位身份证转换为18位
        if (idCard.length() == 15) {
            idCard = convert15To18(idCard);
        }
        
        // 验证18位身份证
        return validateIdCardFormat(idCard);
    }
    
    /**
     * 15位身份证转换为18位
     */
    private static String convert15To18(String idCard15) {
        if (idCard15 == null || idCard15.length() != 15) {
            return idCard15;
        }
        
        // 在15位身份证的第6位后添加'19'变成17位
        StringBuilder idCard17 = new StringBuilder(idCard15.substring(0, 6))
                .append("19")
                .append(idCard15.substring(6));
        
        // 计算校验位
        char[] weights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char[] checkDigits = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
        
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (idCard17.charAt(i) - '0') * weights[i];
        }
        
        int mod = sum % 11;
        char checkDigit = checkDigits[mod];
        
        return idCard17.append(checkDigit).toString();
    }
    
    /**
     * 验证身份证校验位
     */
    private static boolean validateCheckDigit(String idCard) {
        if (idCard.length() != 18) {
            return false;
        }
        
        String idCard17 = idCard.substring(0, 17);
        char[] weights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char[] checkDigits = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
        
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (idCard17.charAt(i) - '0') * weights[i];
        }
        
        int mod = sum % 11;
        char checkDigit = checkDigits[mod];
        
        return Character.toUpperCase(idCard.charAt(17)) == checkDigit;
    }
    
    /**
     * 从身份证号码提取生日
     */
    public static LocalDate getBirthdayFromIdCard(String idCard) {
        try {
            String birthdayStr;
            if (idCard.length() == 18) {
                birthdayStr = idCard.substring(6, 14);
            } else {
                birthdayStr = "19" + idCard.substring(6, 12);
            }
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            return LocalDate.parse(birthdayStr, formatter);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse birthday from ID card: {}", idCard, e);
            return null;
        }
    }
    
    /**
     * 从身份证号码提取性别
     */
    public static String getGenderFromIdCard(String idCard) {
        if (idCard.length() == 18) {
            char genderDigit = idCard.charAt(16);
            return (genderDigit - '0') % 2 == 1 ? "1" : "2";
        }
        return null;
    }
    
    /**
     * 从身份证号码提取年龄
     */
    public static int getAgeFromIdCard(String idCard) {
        LocalDate birthday = getBirthdayFromIdCard(idCard);
        if (birthday == null) {
            return 0;
        }
        
        LocalDate now = LocalDate.now();
        int age = now.getYear() - birthday.getYear();
        
        // 如果生日还没过，年龄减1
        if (now.getDayOfYear() < birthday.getDayOfYear()) {
            age--;
        }
        
        return age;
    }
    
    /**
     * 获取省份名称
     */
    public static String getProvinceName(String idCard) {
        if (idCard == null || idCard.length() < 2) {
            return null;
        }
        String provinceCode = idCard.substring(0, 2);
        return PROVINCE_CODES.get(provinceCode);
    }
    
    /**
     * 获取身份证号码信息
     */
    public static IdCardInfo getIdCardInfo(String idCard) {
        if (!validateIdCardFormat(idCard)) {
            return null;
        }
        
        IdCardInfo info = new IdCardInfo();
        info.setIdCardNo(idCard);
        info.setBirthday(getBirthdayFromIdCard(idCard));
        info.setGender(getGenderFromIdCard(idCard));
        info.setAge(getAgeFromIdCard(idCard));
        info.setProvince(getProvinceName(idCard));
        
        return info;
    }
    
    /**
     * 身份证信息类
     */
    public static class IdCardInfo {
        private String idCardNo;
        private LocalDate birthday;
        private String gender;
        private Integer age;
        private String province;
        
        // Getters and Setters
        public String getIdCardNo() {
            return idCardNo;
        }
        
        public void setIdCardNo(String idCardNo) {
            this.idCardNo = idCardNo;
        }
        
        public LocalDate getBirthday() {
            return birthday;
        }
        
        public void setBirthday(LocalDate birthday) {
            this.birthday = birthday;
        }
        
        public String getGender() {
            return gender;
        }
        
        public void setGender(String gender) {
            this.gender = gender;
        }
        
        public Integer getAge() {
            return age;
        }
        
        public void setAge(Integer age) {
            this.age = age;
        }
        
        public String getProvince() {
            return province;
        }
        
        public void setProvince(String province) {
            this.province = province;
        }
    }
}