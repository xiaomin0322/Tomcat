package com.lx.controller;

import com.lx.rest.Request;
import com.lx.rest.Response;

public class LoginServlet {
	public void service(Request request, Response response) {
		String userName = request.getParamName("userName");
		String pwd = request.getParamName("pwd");
		if (userName != null && userName.equals("admin") && pwd != null && pwd.equals("123456")) {
			System.out.println("登陆成功");
			response.outWrite("登陆成功");

		} else {
			System.out.println(String.format("失败userName=%s，password=%s", userName, pwd));
		}
	}
}
