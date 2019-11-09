package com.czh.util;

import java.util.HashMap;
import java.util.Map;

public class Test {

	public static void main(String[] args) {
	    String s = "fuck";
         StringBuilder sb = new StringBuilder();
        sb.append("\'").append(s).append("\'").append(",");
        System.out.println(sb.toString());
	}

}
