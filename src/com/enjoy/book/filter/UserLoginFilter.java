package com.enjoy.book.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 用户登录的过滤器
 */
@WebFilter("*.jsp")
public class UserLoginFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest  req=  (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse)servletResponse;
        resp.setContentType("text/html;charset=utf-8");
        PrintWriter out = resp.getWriter();
        //判断用户是否登录:已经登录,放行  没有登录，跳回到登录界面
        HttpSession session = req.getSession();
        if(session.getAttribute("user")!=null){
            filterChain.doFilter(req,resp);
        }else{
             out.println("<script>alert('请登录');location.href = 'login.html';</script>");
        }

    }

    @Override
    public void destroy() {

    }
}