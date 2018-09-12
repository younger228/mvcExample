package com.seed.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.seed.annotation.MyController;
import com.seed.annotation.MyRequestMapping;

/**
 * Servlet implementation class MyDispatcherServlet
 */
@WebServlet(urlPatterns = "/*", loadOnStartup = 1, initParams = @WebInitParam(name = "contextConfigLocation", value = "application.properties"))
public class MyDispatcherServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Properties properties = new Properties();
	private List<String> classNames = new ArrayList<String>();
	private Map<String, Object> ioc = new HashMap<String, Object>();
	private Map<String, Method> handlerMapping = new HashMap<String, Method>();
	private Map<String, Object> controllerMap = new HashMap<String, Object>();

	public MyDispatcherServlet() {
		super();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		// 加载配置文件
		doLoadConfig(config.getInitParameter("contextConfigLocation"));
		// 扫描要被注册的类
		doScanner(properties.getProperty("scanPackage"));
		// 通过反射实例化扫描到的类
		doInstance();
		// 初始化handlerMapping，关联url和method
		initHandlerMapping();

	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doDispather(request, response);
	}

	private void doDispather(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (handlerMapping.isEmpty()) {
			return;
		}
		String uri = request.getRequestURI();
		String contextPath = request.getContextPath();
		uri = uri.replace(contextPath, "").replaceAll("/+", "/");
		if (!handlerMapping.containsKey(uri)) {
			response.getWriter().write("404: request is not found!");
			return;
		}
		Method method = handlerMapping.get(uri);
		Class<?>[] parameterTypes = method.getParameterTypes();
		Map<String, String[]> parameterMap = request.getParameterMap();
		// 保存值
		Object[] params = new Object[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			// 参数名称
			String name = parameterTypes[i].getSimpleName();
			if (name.equals("HttpServletRequest")) {
				params[i] = request;
				continue;
			}
			if (name.equals("HttpServletResponse")) {
				params[i] = response;
				continue;
			}
			if (name.equals("String")) {
				for (Entry<String, String[]> entry : parameterMap.entrySet()) {
					params[i] = Arrays.toString(entry.getValue()).replaceAll("\\[|\\]", "").replaceAll(",\\s", ",");
				}
			}
		}
		try {
			method.invoke(controllerMap.get(uri), params);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	private void initHandlerMapping() {
		if (ioc.isEmpty()) {
			return;
		}

		try {
			for (Entry<String, Object> entry : ioc.entrySet()) {
				Class<? extends Object> clazz = entry.getValue().getClass();
				if (!clazz.isAnnotationPresent(MyController.class)) {
					continue;
				}
				// 拼接url
				String baseUrl = "";
				if (clazz.isAnnotationPresent(MyRequestMapping.class)) {
					baseUrl = clazz.getAnnotation(MyRequestMapping.class).value();
				}
				Method[] methods = clazz.getMethods();
				for (Method method : methods) {
					if (!method.isAnnotationPresent(MyRequestMapping.class)) {
						continue;
					}
					String url = method.getAnnotation(MyRequestMapping.class).value();
					url = (baseUrl + "/" + url).replaceAll("/+", "/");
					handlerMapping.put(url, method);
					controllerMap.put(url, clazz.newInstance());
					System.out.println(url + ", " + method);
				}
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private void doInstance() {
		if (classNames.isEmpty()) {
			return;
		}
		for (String className : classNames) {
			try {
				Class<?> clazz = Class.forName(className);
				// 是否被controller类注解
				if (clazz.isAnnotationPresent(MyController.class)) {
					ioc.put(toLowFirstChar(clazz.getSimpleName()), clazz.newInstance());
				} else {
					continue;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String toLowFirstChar(String simpleName) {
		char[] array = simpleName.toCharArray();
		array[0] += 32;
		return String.valueOf(array);
	}

	private void doScanner(String scanpackage) {
		URL url = getClass().getClassLoader().getResource("/" + scanpackage.replaceAll("\\.", "/"));
		File dir = new File(url.getFile());
		StringBuilder className = new StringBuilder();
		for (File file : dir.listFiles()) {
			className.setLength(0);
			if (file.isDirectory()) {
				doScanner(scanpackage + "." + file.getName());
			} else {
				className.append(scanpackage).append(".").append(file.getName().replace(".class", ""));
				classNames.add(className.toString());
			}
		}
	}

	private void doLoadConfig(String initParameter) {
		InputStream in = getClass().getClassLoader().getResourceAsStream(initParameter);
		try {
			properties.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
