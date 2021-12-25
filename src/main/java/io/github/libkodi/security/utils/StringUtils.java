package io.github.libkodi.security.utils;

import java.util.ArrayList;
import java.util.Collections;

import io.github.libkodi.security.variables.ApiCheckMode;

public class StringUtils {
	public static boolean isEmpty(Object obj) {
		return obj == null || "".equals(obj);
	}
	
	public static String join(String joinChar, String... args) {
		String res = "";
		
		if (args.length < 1) {
			return "";
		} else if (args.length == 1) {
			return args[0];
		}
		
		for (String arg : args) {
			res += String.format("%s%s", joinChar, arg);
		}
		
		return res.substring(joinChar.length());
	}

	public static boolean isMatch(String[] on, String[] from, String mode) {
		if (ApiCheckMode.OR.equals(mode)) {
			ArrayList<String> targetArr = new ArrayList<String>();
			Collections.addAll(targetArr, from);
			
			for (String s : on) {
				if (targetArr.contains(s)) {
					return true;
				}
			}
		} else if (ApiCheckMode.AND.equals(mode)) {
			ArrayList<String> sourceArr = new ArrayList<String>();
			Collections.addAll(sourceArr, on);
			
			for (String s : from) {
				if (!sourceArr.contains(s)) {
					return false;
				}
			}
			
			return true;
		}
		
		return false;
	}
}
