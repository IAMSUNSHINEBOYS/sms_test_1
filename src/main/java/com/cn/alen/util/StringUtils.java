package com.cn.alen.util;

/**
 * 字符串工具类
 * 
 * @author heshengquan
 * @date 2017-10-16下午5:23:55
 */
public class StringUtils {

	public static boolean isMobile(String phone) {
		return phone != null && phone.matches("[1][34578][0-9]{9}$");
	}

	public static String formatCardNo(String cardNo) {
		if (isMobile(cardNo)) {
			return cardNo;
		}
		return formatPrefix(cardNo, "0", 14);
	}

	public static String formatBikeNum(String bikeNum) {
		return formatPrefix(bikeNum, "0", 8);
	}

	public static String formatPrefix(String str, String pre, int len) {
		if (str != null && str.length() < len) {
			while (str.length() < len) {
				str = pre + str;
			}
		}
		return str;
	}

	public static String formatSuffix(String str, String suf, int len) {
		if (str != null && str.length() < len) {
			while (str.length() < len) {
				str += suf;
			}
		}
		return str;
	}

	public static boolean isEmpty(Object str) {
		return str == null || "".equals(str);
	}

	public static boolean isBlank(Object str) {
		return str == null || (str + "").trim().equals("");
	}

	public static String parseNull(Object str) {
		return isBlank(str) ? null : str + "";
	}

	public static String parseEmpty(Object str) {
		return isBlank(str) ? "" : str + "";
	}

	public static String add(String src, String dest) {
		if (!isEmpty(src)) {
			if (dest != null) {
				return dest + "," + src;
			} else {
				return src;
			}
		}
		return dest;
	}
}
