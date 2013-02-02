package com.open.perf.test;

import java.util.HashMap;
public class TestClass {
	public void setup(){
		System.out.println("This is setup!!");
	}
	public void method1(HashMap<String, String> config){
		System.out.println("This is method1.");
	}
	public void method2(HashMap<String, String> config){
		System.out.println("This is method2.");
	}
	public void method3(HashMap<String, String> config){
		System.out.println("This is method3");
	}
}
