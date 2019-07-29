package com.czh.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Search_code {
	public static void main(String[] args){
		Scanner in = new Scanner(System.in);
		System.out.println("Enter base directory");
		String directory = in.nextLine();
		System.out.println("Enter keyword");
		String keyword = in.nextLine();
		
		final int FILE_QUEUE_SIZE = 10;
		final int SEARCH_THREADS = 100;
		
		BlockingQueue<File> queue = new ArrayBlockingQueue<>(FILE_QUEUE_SIZE);
		FileEnumerationTask enumerator = new FileEnumerationTask(queue, new File(directory));
		new Thread(enumerator).start();
		for(int i = 1; i <= SEARCH_THREADS; i++){
			new Thread(new SearchTask(queue, keyword)).start();
		}
	}
}

class FileEnumerationTask implements Runnable{
	public static File DUMMY = new File("");
	private BlockingQueue<File> queue;
	private File startingDirectory;
	
	public FileEnumerationTask(BlockingQueue<File> queue, File startingDirecotry) {
		// TODO Auto-generated constructor stub
		this.queue = queue;
		this.startingDirectory = startingDirecotry;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			enumerate(startingDirectory);
			queue.put(DUMMY);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void enumerate(File directory) throws InterruptedException{
		File[] files = directory.listFiles();
		for(File file : files){
			if (file.isDirectory()) {
				enumerate(file);
			}else{
				queue.put(file);
			}
		}
	}	
}

class SearchTask implements Runnable{
	private BlockingQueue<File> queue;
	private String keyword;
	
	public SearchTask(BlockingQueue<File> queue, String keyword) {
		this.queue = queue;
		this.keyword = keyword;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		boolean done = false;
		while(!done){
			File file;
			try {
				file = queue.take();
				if (file == FileEnumerationTask.DUMMY) {
					queue.put(file);
					done = true;
				}else{
					search(file);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void search(File file) throws FileNotFoundException{
		try(Scanner in = new Scanner(file)){
			int lineNumber = 0;
			while(in.hasNextLine()){
				lineNumber++;
				String line = in.nextLine();
				if (line.contains(keyword)) {
					System.out.printf("%s:%d:%s%n", file.getPath(), lineNumber, line);
				}
			}
		}
	}
}
