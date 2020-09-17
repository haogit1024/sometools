package com.czh.util;

import com.czh.util.orm.TableRecordInfo;
import com.czh.util.util.Utils;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Test {

	public static void main(String[] args) throws IOException {
		File file = new File("D:\\tc_codes\\etc-pay-platform");
		System.out.println(file.getParent());
		System.out.println(file.getAbsolutePath());
		System.out.println(file.getCanonicalPath());
		String str = "\"";
		System.out.println(StringEscapeUtils.escapeJava(str));
	}

}
