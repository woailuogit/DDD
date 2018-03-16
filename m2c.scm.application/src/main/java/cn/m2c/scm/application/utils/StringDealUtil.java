package cn.m2c.scm.application.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class StringDealUtil {
	private static final String NumOrEnglishRegex="^[a-zA-Z0-9\u4E00-\u9FA5]+$";
	
	/**
	 * 如果满足是英文或者数字组合，返回true 否则返回false
	 * @param str
	 * @return
	 */
	public static boolean InputNumOrEnglish(String str){
		boolean result = false;
		if(StringUtils.isEmpty(str)){
			Pattern pattern = Pattern.compile(NumOrEnglishRegex);
			Matcher match=pattern.matcher(str);
			result =  match.matches();
		}
		return result;
		
	}
}