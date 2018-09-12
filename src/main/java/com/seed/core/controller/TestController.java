package com.seed.core.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.seed.annotation.MyController;
import com.seed.annotation.MyRequestMapping;
import com.seed.annotation.MyRequestParam;

@MyController
@MyRequestMapping("/test")
public class TestController {

	@MyRequestMapping("/01")
	public void test01(HttpServletRequest request, HttpServletResponse response, @MyRequestParam("param") String param) {
		try {
			response.getWriter().write("request success,param:" + param);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@MyRequestMapping("/02")
	public void test02(HttpServletRequest request, HttpServletResponse response) {
		try {
			response.getWriter().write("02 is success!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
