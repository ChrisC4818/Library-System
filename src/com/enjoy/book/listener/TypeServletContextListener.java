package com.enjoy.book.listener;

import com.enjoy.book.biz.TypeBiz;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import com.enjoy.book.bean.*;;import java.util.List;

@WebListener
public class TypeServletContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        //1.获取当前数据库中所有的类型信息
        TypeBiz biz = new TypeBiz();
        List<Type> types = biz.getAll();

        //2.获取application对象
        ServletContext application = servletContextEvent.getServletContext();

        //3.将信息存在application对象中
        application.setAttribute("types",types);

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
