package com.seed.core.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.seed.annotation.MyController;
import com.seed.annotation.MyRequestMapping;
import com.seed.annotation.MyRequestParam;

@MyController
@MyRequestMapping("/user")
public class WhoController {

	@MyRequestMapping("/im")
	public void getUser(HttpServletRequest request, HttpServletResponse response,@MyRequestParam("name") String name){
		try {
			response.getWriter().write("I'm "+name);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
