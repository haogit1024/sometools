package com.czh.util;

import com.czh.util.orm.ORMDataBase;

import java.io.IOException;

public class Test {

	public static void main(String[] args) throws IOException, InterruptedException {
		/*test1();
		System.out.println("创建完成");
		Thread.sleep(1000 * 100);
		System.out.println("程序关闭");*/
		new Thread(TestLock::test1).start();
		new Thread(TestLock::test2).start();
	}

	/**
	 * orm 作为一个局部变量，运行完后，mysql的链接并没有断，所以一定要手动关闭jdbcConnection
	 */
	public static void test1() {
		Test t = new Test();
		t.test();
	}

	public void test() {
		ORMDataBase orm = new ORMDataBase("mydb.properties");
		orm.setPower(true);
	}

	static class TestLock {
		public synchronized static void test1()  {
			try {
				Thread.sleep(1000 * 2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("test1 final");
		}

		public synchronized static void test2() {
			System.out.println("test2 final");
		}
	}
}
