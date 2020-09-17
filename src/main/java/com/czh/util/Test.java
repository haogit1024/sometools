package com.czh.util;

import com.czh.util.orm.TableRecordInfo;
import com.czh.util.util.Utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Test {

	public static void main(String[] args) {
		String s = "\\\'";
		System.out.println(s);
		TableRecordInfo info = new TableRecordInfo();
		String className = info.getClass().getName();
		String[] classNames = className.split("\\.");
		String tableName = classNames[classNames.length - 1];
		System.out.println(Utils.toLine(tableName));
	}

}
