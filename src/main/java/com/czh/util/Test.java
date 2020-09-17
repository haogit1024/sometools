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
		long l = 66760424535L;
		double k = 1024.0;
		System.out.println(l / k / k / k);
	}

}
