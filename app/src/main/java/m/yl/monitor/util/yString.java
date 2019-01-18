package m.yl.monitor.util;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.text.format.DateFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by Yl on 16/11/28.
 */

public class yString {
    private static final char[] HEX = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static Random rnd = new Random();
    private static final String[] COMMENT_RATE = new String[]{"", "万", "亿"};
    private static Calendar currentTime = Calendar.getInstance();
    private static Calendar publicCal = Calendar.getInstance();
    private static DecimalFormat priceDF = new DecimalFormat("#,###");

    public yString() {
    }

    public static String getRandomNum() {
        return rnd.nextInt(2147483647) + "";
    }

    public static String getFileSizeStr(long size) {
        if(size > 1048576L) {
            int mb = (int)(size / 1024L / 1024L);
            int kb = (int)(size - (long)(mb * 1024 * 1024)) * 100 / 1024 / 1024;
            return "" + mb + "." + kb + "MB";
        } else {
            return size > 1024L?"" + size / 1024L + "KB":size + "B";
        }
    }

    public static String getFixLenFloat(float fvalue, int len) {
        int ivalue = (int)fvalue;
        int flitter = (int)((fvalue - (float)ivalue) * 100.0F);
        return flitter >= 10?"" + ivalue + "." + flitter:(flitter > 0 && flitter < 10?"" + ivalue + "." + flitter + "0":"" + ivalue + ".00");
    }

    public static String signUrl(String url, String appver, String devid, String random) {
        try {
            URI e = URI.create(url);
            String p = e.getPath();
            String[] ps = p.split("\\/");
            if(ps != null && ps.length > 0) {
                HashMap hs = new HashMap();
                hs.put("appver", appver);
                hs.put("devid", devid);
                hs.put("cgi", ps[ps.length - 1]);
                hs.put("qn-rid", random);
                hs.put("secret", "qn123456");
                Object[] keys = hs.keySet().toArray();
                Arrays.sort(keys);
                StringBuffer buff = new StringBuffer();
                buff.append(keys[0]);
                buff.append("=");
                buff.append((String)hs.get(keys[0]));
                int i = 1;

                for(int j = keys.length; i < j; ++i) {
                    buff.append("&");
                    buff.append(keys[i]);
                    buff.append("=");
                    buff.append((String)hs.get(keys[i]));
                }

                return toMd5(buff.toString());
            }
        } catch (Exception var12) {
            ;
        }

        return "";
    }

    public static String stringToColor(String str) {
        String strColor = "";

        try {
            if(str.contains("#")) {
                strColor = str;
            } else {
                strColor = "#" + Long.toHexString(Long.parseLong(str));
            }

            return strColor;
        } catch (Exception var3) {
            var3.printStackTrace();
            return strColor;
        }
    }

    public static String stringToColor(String str, float alpha) {
        String strColor = "";

        try {
            if(str.contains("#")) {
                strColor = str;
            } else {
                strColor = "#" + Long.toHexString((long)(255.0F * alpha)) + Long.toHexString(Long.parseLong(str));
            }

            return strColor;
        } catch (Exception var4) {
            var4.printStackTrace();
            return strColor;
        }
    }

    public static String toMd5(String src) {
        if(src == null) {
            return "";
        } else {
            try {
                MessageDigest e = MessageDigest.getInstance("MD5");
                e.reset();
                e.update(src.getBytes());
                return toHexStringNew(e.digest());
            } catch (NoSuchAlgorithmException var2) {
                var2.printStackTrace();
                return "error";
            } catch (ArrayStoreException var3) {
                var3.printStackTrace();
                return "error";
            }
        }
    }

    public static String getFileMD5(File file) {
        if(file != null && file.isFile()) {
            MessageDigest digest = null;
            FileInputStream in = null;
            byte[] buffer = new byte[1024];
            boolean isError = false;

            try {
                digest = MessageDigest.getInstance("MD5");
                in = new FileInputStream(file);

                int len;
                while((len = in.read(buffer, 0, 1024)) != -1) {
                    digest.update(buffer, 0, len);
                }
            } catch (Exception var15) {
                var15.printStackTrace();
                isError = true;
            } finally {
                if(in != null) {
                    try {
                        in.close();
                    } catch (IOException var14) {
                        var14.printStackTrace();
                    }
                }

            }

            return isError?null:toHexStringNew(digest.digest());
        } else {
            return null;
        }
    }

    public static String toHexString(byte[] bytes, String separator) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        byte[] var3 = bytes;
        int var4 = bytes.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            byte b = var3[var5];
            int byteValue = 255 & b;
            if(byteValue < 16) {
                hexString.append("0" + Integer.toHexString(255 & b)).append(separator);
            } else {
                hexString.append(Integer.toHexString(255 & b)).append(separator);
            }
        }

        return hexString.toString();
    }

    public static String toHexStringNew(byte[] bytes) {
        int nBytes = bytes.length;
        StringBuilder hexString = new StringBuilder(2 * nBytes);

        for(int i = 0; i < nBytes; ++i) {
            hexString.append(HEX[(240 & bytes[i]) >>> 4]);
            hexString.append(HEX[15 & bytes[i]]);
        }

        return hexString.toString();
    }

    public static String urlEncode(String str) throws UnsupportedEncodingException {
        if(str == null) {
            str = "";
        }

        return URLEncoder.encode(str, "utf-8").replaceAll("\\+", "%20").replaceAll("%7E", "~").replaceAll("\\*", "%2A");
    }

    public static String urlDecode(String str) throws UnsupportedEncodingException {
        return URLDecoder.decode(str, "utf-8");
    }

    public static String cutDateString(String str) {
        if(!TextUtils.isEmpty(str)) {
            str = str.substring(5, str.length() - 3);
        }

        return str;
    }

    public static String StringFilter(String str) {
        Matcher m = null;

        try {
            str = str.replaceAll("【", "[").replaceAll("】", "]").replaceAll("！", "!");
            String e = "[『』]";
            Pattern p = Pattern.compile(e);
            m = p.matcher(str);
        } catch (PatternSyntaxException var4) {
            return str;
        }

        return m.replaceAll("").trim();
    }

    public static String ToDBC(String input) {
        if(isNullOrEmpty(input)) {
            return "";
        } else {
            char[] c = input.toCharArray();

            for(int i = 0; i < c.length; ++i) {
                if(c[i] == 12288) {
                    c[i] = 32;
                } else if(c[i] > '\uff00' && c[i] < '｟') {
                    c[i] -= 'ﻠ';
                }
            }

            return new String(c);
        }
    }

    public static int getTypeOfChar(char a) {
        return isCnHz(a)?0:(Character.isDigit(a)?1:(Character.isLetter(a)?2:(isChinese(a) && !isCnHz(a)?4:3)));
    }

    public static boolean isCnHz(char a) {
        return String.valueOf(a).matches("[一-龿]");
    }

    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION;
    }

    public static String replaceBlank(String str) {
        String dest = "";
        if(str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }

        return dest;
    }

    public static String escapeJavaScript(String str) {
        return escapeJavaStyleString(str, true, true);
    }

    private static String escapeJavaStyleString(String str, boolean escapeSingleQuotes, boolean escapeForwardSlash) {
        if(str == null) {
            return null;
        } else {
            try {
                StringWriter ioe = new StringWriter(str.length() * 2);
                escapeJavaStyleString(ioe, str, escapeSingleQuotes, escapeForwardSlash);
                return ioe.toString();
            } catch (IOException var4) {
                return null;
            }
        }
    }

    private static String hex(char ch) {
        return String.format("%04x", new Object[]{Integer.valueOf(ch)});
    }

    private static void escapeJavaStyleString(Writer out, String str, boolean escapeSingleQuote, boolean escapeForwardSlash) throws IOException {
        if(out == null) {
            throw new IllegalArgumentException("The Writer must not be null");
        } else if(str != null) {
            int sz = str.length();

            for(int i = 0; i < sz; ++i) {
                char ch = str.charAt(i);
                if(ch > 4095) {
                    out.write("\\u" + hex(ch));
                } else if(ch > 255) {
                    out.write("\\u0" + hex(ch));
                } else if(ch > 127) {
                    out.write("\\u00" + hex(ch));
                } else if(ch < 32) {
                    switch(ch) {
                        case '\b':
                            out.write(92);
                            out.write(98);
                            break;
                        case '\t':
                            out.write(92);
                            out.write(116);
                            break;
                        case '\n':
                            out.write(92);
                            out.write(110);
                            break;
                        case '\u000b':
                        default:
                            if(ch > 15) {
                                out.write("\\u00" + hex(ch));
                            } else {
                                out.write("\\u000" + hex(ch));
                            }
                            break;
                        case '\f':
                            out.write(92);
                            out.write(102);
                            break;
                        case '\r':
                            out.write(92);
                            out.write(114);
                    }
                } else {
                    switch(ch) {
                        case '\"':
                            out.write(92);
                            out.write(34);
                            break;
                        case '\'':
                            if(escapeSingleQuote) {
                                out.write(92);
                            }

                            out.write(39);
                            break;
                        case '/':
                            if(escapeForwardSlash) {
                                out.write(92);
                            }

                            out.write(47);
                            break;
                        case '\\':
                            out.write(92);
                            out.write(92);
                            break;
                        default:
                            out.write(ch);
                    }
                }
            }

        }
    }

    public static String subString(String str, int subLength) {
        int n = 0;
        boolean i = false;
        int j = 0;
        int byteNum = subLength * 2;
        boolean flag = true;
        if(str == null) {
            return "";
        } else {
            int var7;
            for(var7 = 0; var7 < str.length(); ++var7) {
                if(str.charAt(var7) < 128) {
                    ++n;
                } else {
                    n += 2;
                }

                if(n > byteNum && flag) {
                    j = var7;
                    flag = false;
                }

                if(n >= byteNum + 2) {
                    break;
                }
            }

            if(n >= byteNum + 2 && var7 != str.length() - 1) {
                str = str.substring(0, j);
                str = str + "...";
            }

            return str;
        }
    }

    public static int showLength(String str) {
        int length = 0;
        boolean i = false;

        for(int var3 = 0; var3 < str.length(); ++var3) {
            if(str.charAt(var3) < 128) {
                ++length;
            } else {
                length += 2;
            }
        }

        return length;
    }

    public static String tenTh2wan(long num) {
        return tenTh2wan(String.valueOf(num));
    }

    public static String tenTh2wan(String num) {
        int numLen = num.length();
        if(numLen < 5) {
            return num;
        } else {
            byte hideNum = 0;
            int rateIndex = 0;
            if(numLen >= 5) {
                hideNum = 3;
                ++rateIndex;
            }

            if(numLen >= 8) {
                hideNum = 6;
                ++rateIndex;
            }

            StringBuilder showOutStr = new StringBuilder(num.substring(0, num.length() - hideNum));
            char r = showOutStr.charAt(showOutStr.length() - 1);
            if(r != 48) {
                showOutStr.insert(showOutStr.length() - 1, ".");
            } else {
                showOutStr.deleteCharAt(showOutStr.length() - 1);
            }

            return showOutStr.append(COMMENT_RATE[rateIndex]).toString();
        }
    }

    public static String List2String(Object list) {
        if(list == null) {
            return "";
        } else {
            String result = list.toString().replaceAll("[\\[| |\\]]", "");
            return result;
        }
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static long paseLong(String num) {
        long ret = 0L;

        try {
            ret = Long.valueOf(num).longValue();
        } catch (Exception var4) {
            ;
        }

        return ret;
    }

    @SuppressLint({"SimpleDateFormat"})
    public static String getTimeDisplayNameNormal(long ctimelong) {
        String r = "";
        long currentTimelong = System.currentTimeMillis();
        publicCal.setTimeInMillis(ctimelong);
        long timeDelta = currentTimelong - ctimelong;
        if(timeDelta < 60000L) {
            r = "刚刚";
        } else if(timeDelta < 3600000L) {
            r = timeDelta / 60000L + "分钟前";
        } else if(timeDelta < 86400000L) {
            r = (new SimpleDateFormat("HH:mm")).format(Long.valueOf(ctimelong));
        } else if(timeDelta < 172800000L) {
            if(currentTime.get(6) == publicCal.get(6) + 1) {
                r = "昨天";
            } else {
                r = "前天";
            }
        } else if(timeDelta < 259200000L) {
            if(currentTime.get(6) == publicCal.get(6) + 2) {
                r = "前天";
            } else {
                r = (new SimpleDateFormat("MM月dd日")).format(Long.valueOf(ctimelong));
            }
        } else {
            r = (new SimpleDateFormat("MM月dd日")).format(Long.valueOf(ctimelong));
        }

        return r;
    }

    @SuppressLint({"SimpleDateFormat"})
    public static String getTimeDisplayNameCompact(long ctimelong) {
        String r = "";
        Calendar currentTime = Calendar.getInstance();
        long currentTimelong = System.currentTimeMillis();
        Calendar publicCal = Calendar.getInstance();
        publicCal.setTimeInMillis(ctimelong);
        long timeDelta = currentTimelong - ctimelong;
        if(timeDelta <= 0L) {
            r = "刚刚";
        } else if(timeDelta < 60000L) {
            r = timeDelta / 1000L + "秒前";
        } else if(timeDelta < 3600000L) {
            r = timeDelta / 60000L + "分钟前";
        } else if(timeDelta < 86400000L) {
            r = timeDelta / 3600000L + "小时前";
        } else if(timeDelta < 172800000L) {
            if(currentTime.get(6) == publicCal.get(6) + 1) {
                r = "昨天" + (new SimpleDateFormat("HH:mm")).format(Long.valueOf(ctimelong));
            } else {
                r = "前天" + (new SimpleDateFormat("HH:mm")).format(Long.valueOf(ctimelong));
            }
        } else if(timeDelta < 259200000L) {
            if(currentTime.get(6) == publicCal.get(6) + 2) {
                r = "前天" + (new SimpleDateFormat("HH:mm")).format(Long.valueOf(ctimelong));
            } else {
                r = (new SimpleDateFormat("MM月dd日")).format(Long.valueOf(ctimelong));
            }
        } else {
            r = (new SimpleDateFormat("MM月dd日")).format(Long.valueOf(ctimelong));
        }

        return r;
    }

    public static String getNowFormatted() {
        return getTimeFormatted(System.currentTimeMillis());
    }

    @SuppressLint({"SimpleDateFormat"})
    public static String getTimeFormatted(long ctimelong) {
        return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(Long.valueOf(ctimelong));
    }

    public static long strToDateLong(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            Date e = formatter.parse(strDate);
            return e.getTime();
        } catch (ParseException var3) {
            var3.printStackTrace();
            return System.currentTimeMillis();
        }
    }

    public static String getFormattedNow(SimpleDateFormat formatter) {
        return formatter.format(Long.valueOf(System.currentTimeMillis()));
    }

    public static String getFormattedNow() {
        return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(Long.valueOf(System.currentTimeMillis()));
    }

    public static String getTimeZoneStr() {
        TimeZone timeZone = TimeZone.getDefault();
        return timeZone.getID();
    }

    public static String getTimeZoneNameAndId() {
        TimeZone timeZone = TimeZone.getDefault();
        return timeZone.getDisplayName(false, 0) + " " + timeZone.getID();
    }

    public static int CompareVersion(String version1, String version2) {
        if(version1 == null) {
            version1 = "";
        }

        if(version2 == null) {
            version2 = "";
        }

        String letter_pattern = "[^0-9]";
        String[] versionLefStrs = version1.split("\\.");
        String[] versionRigStrs = version2.split("\\.");
        boolean splitNum = false;
        int var9;
        if(versionLefStrs.length > versionRigStrs.length) {
            var9 = versionRigStrs.length;
        } else {
            var9 = versionLefStrs.length;
        }

        for(int i = 0; i < var9; ++i) {
            int leftInt = Integer.valueOf("0" + versionLefStrs[i].replaceAll(letter_pattern, "")).intValue();
            int rightInt = Integer.valueOf("0" + versionRigStrs[i].replaceAll(letter_pattern, "")).intValue();
            if(leftInt > rightInt) {
                return 1;
            }

            if(leftInt < rightInt) {
                return -1;
            }
        }

        if(versionLefStrs.length > versionRigStrs.length) {
            return 1;
        } else if(versionLefStrs.length < versionRigStrs.length) {
            return -1;
        } else {
            return 0;
        }
    }

    public static String getNonNullString(String str) {
        if(str == null) {
            str = "";
        }

        return str;
    }

    public static String getNonNullNumString(String str) {
        if(str == null) {
            str = "0";
        }

        return str;
    }

    public static int getNumberInt(String str, int defaultVlaue) {
        int value = defaultVlaue;

        try {
            value = Integer.valueOf(str).intValue();
        } catch (Exception var4) {
            ;
        }

        return value;
    }

    public static long getNumberLong(String str, long defaultVlaue) {
        long value = defaultVlaue;

        try {
            value = Long.valueOf(str).longValue();
        } catch (Exception var6) {
            ;
        }

        return value;
    }

    public static String getNonNullFloatString(String str) {
        return str == null?"0.0":str;
    }

    public static String[] getNonNullStringArray(String[] str) {
        if(str == null) {
            str = new String[0];
        }

        return str;
    }

    public static String join(List<String> countParams, String flag) {
        StringBuffer str_buff = new StringBuffer();
        int i = 0;

        for(int len = countParams.size(); i < len; ++i) {
            str_buff.append(String.valueOf(countParams.get(i)));
            if(i < len - 1) {
                str_buff.append(flag);
            }
        }

        return str_buff.toString();
    }

    public static boolean startWithIgnoreCase(String str, String prefix) {
        if(str != null && prefix != null) {
            str = str.toLowerCase(Locale.US);
            prefix = prefix.toLowerCase(Locale.US);
            return str.startsWith(prefix);
        } else {
            return false;
        }
    }

    public static String timeFormat(long milliseconds) {
        String res = "";

        try {
            SimpleDateFormat e = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            res = e.format(Long.valueOf(milliseconds));
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return res;
    }

    public static String priceConversion(String price) {
        try {
            return priceDF.format(Double.valueOf(price));
        } catch (Exception var2) {
            return price;
        }
    }

    public static String formatDate(Calendar cal, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
        return sdf.format(cal.getTime());
    }

    public static String utcToLocal(String utctime) {
        try {
            SimpleDateFormat e = new SimpleDateFormat("yyyy\'-\'MM\'-\'dd\'T\'HH\':\'mm\':\'ss\'Z\'");
            e.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date gpsUTCDate = null;

            try {
                gpsUTCDate = e.parse(utctime);
            } catch (ParseException var5) {
                var5.printStackTrace();
            }

            SimpleDateFormat localFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            localFormater.setTimeZone(TimeZone.getDefault());
            String localTime = localFormater.format(Long.valueOf(gpsUTCDate.getTime()));
            return localTime;
        } catch (Exception var6) {
            yLog.e("utcToLocal Exception= ", var6);
            return utctime.replace("T", " ").replace("Z", " ");
        }
    }

    public static long getLocalToUtcDelta() {
        Calendar local = Calendar.getInstance();
        local.clear();
        local.set(1970, 0, 1, 0, 0, 0);
        return local.getTimeInMillis();
    }

    public static String localToUtc(long timeSinceLocalEpoch) {
        long utcTimestamp = timeSinceLocalEpoch + getLocalToUtcDelta();
        SimpleDateFormat utcFormater = new SimpleDateFormat("yyyy\'-\'MM\'-\'dd\'T\'HH\':\'mm\':\'ss\'Z\'");
        return utcFormater.format(Long.valueOf(utcTimestamp));
    }

    public static String localToUtc(String datestr) {
        long utcTimestamp = strToDateLong(datestr) + getLocalToUtcDelta();
        SimpleDateFormat utcFormater = new SimpleDateFormat("yyyy\'-\'MM\'-\'dd\'T\'HH\':\'mm\':\'ss\'Z\'");
        return utcFormater.format(Long.valueOf(utcTimestamp));
    }

    public CharSequence getUtcNow() {
        Calendar cal = Calendar.getInstance(Locale.CHINA);
        int zoneOffset = cal.get(15);
        int dstOffset = cal.get(16);
        cal.add(14, -(zoneOffset + dstOffset));
        return DateFormat.format("yyyy\'-\'MM\'-\'dd\'T\'HH\':\'mm\':\'ss\'Z\'", cal);
    }

    public static boolean isPhoneNumber(String phone) {
        return TextUtils.isEmpty(phone)?false:phone.matches("^0{0,1}(13[0-9]|14[0-9]|15[0-9]|18[0-9])[0-9]{8}");
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    public static boolean isLetterDigitOrChinese(String str) {
        String regex = "^[a-z0-9A-Z一-龥]+$";
        return str.matches(regex);
    }
}
