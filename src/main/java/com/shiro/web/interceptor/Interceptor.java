package com.shiro.web.interceptor;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.shiro.Constants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @ClassName Interceptor.java
 * @Description 拦截器（主要获取url地址）
 *
 * @author 1904
 * @version V1.0 
 * @Date 2015年7月22日 下午1:36:02
 */
public class Interceptor extends HandlerInterceptorAdapter {

    private final PathMatcher pathMatcher = new AntPathMatcher();

    private static final String[] DEFAULT_EXCLUDE_PARAMETER_PATTERN = new String[]{
            "\\&\\w*page.pn=\\d+",
            "\\?\\w*page.pn=\\d+",
            "\\&\\w*page.size=\\d+"
    };

    private String[] excludeParameterPatterns = DEFAULT_EXCLUDE_PARAMETER_PATTERN;
    private String[] excludeUrlPatterns = null;

    public void setExcludeParameterPatterns(String[] excludeParameterPatterns) {
        this.excludeParameterPatterns = excludeParameterPatterns;
    }

    public void setExcludeUrlPatterns(final String[] excludeUrlPatterns) {
        this.excludeUrlPatterns = excludeUrlPatterns;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if(isExclude(request)) {
            return true;
        }

        if (request.getAttribute(Constants.CONTEXT_PATH) == null) {
            request.setAttribute(Constants.CONTEXT_PATH, request.getContextPath());
        }
        if (request.getAttribute(Constants.CURRENT_URL) == null) {
            request.setAttribute(Constants.CURRENT_URL, extractCurrentURL(request, true));
        }
        if (request.getAttribute(Constants.NO_QUERYSTRING_CURRENT_URL) == null) {
            request.setAttribute(Constants.NO_QUERYSTRING_CURRENT_URL, extractCurrentURL(request, false));
        }
        if (request.getAttribute(Constants.BACK_URL) == null) {
            request.setAttribute(Constants.BACK_URL, extractBackURL(request));
        }

        return true;
    }

    private boolean isExclude(final HttpServletRequest request) {
        if(excludeUrlPatterns == null) {
            return false;
        }
        for(String pattern : excludeUrlPatterns) {
            if(pathMatcher.match(pattern, request.getServletPath())) {
                return true;
            }
        }
        return false;
    }


    /**
     * 获取当前url地址
     * @param request
     * @param needQueryString
     * @return
     */
    private String extractCurrentURL(HttpServletRequest request, boolean needQueryString) {
        String url = request.getRequestURI();
        String queryString = request.getQueryString();
        if (!StringUtils.isEmpty(queryString)) {
            queryString = "?" + queryString;
            for (String pattern : excludeParameterPatterns) {
                queryString = queryString.replaceAll(pattern, "");
            }
            if (queryString.startsWith("&")) {
                queryString = "?" + queryString.substring(1);
            }
        }
        if (!StringUtils.isEmpty(queryString) && needQueryString) {
            url = url + queryString;
        }
        return getBasePath(request) + url;
    }
    
    

    /**
     * 上一次请求的地址
     * 1、先从request.parameter中查找BackURL
     * 2、获取header中的 referer
     *
     * @param request
     * @return
     */
    private String extractBackURL(HttpServletRequest request) {
        String url = request.getParameter(Constants.BACK_URL);

        if (StringUtils.isEmpty(url)) {
            url = request.getHeader("Referer");
        }

        if(!StringUtils.isEmpty(url) && (url.startsWith("http://") || url.startsWith("https://"))) {
            return url;
        }

        if (!StringUtils.isEmpty(url) && url.startsWith(request.getContextPath())) {
            url = getBasePath(request) + url;
        }
        return url;
    }

    
    private String getBasePath(HttpServletRequest req) {
        StringBuffer baseUrl = new StringBuffer();
        String scheme = req.getScheme();
        int port = req.getServerPort();

        baseUrl.append(scheme);        // http, https
        baseUrl.append("://");
        baseUrl.append(req.getServerName());
        if ((scheme.equals("http") && port != 80) || (scheme.equals("https") && port != 443)) {
            baseUrl.append(':');
            baseUrl.append(req.getServerPort());
        }
        return baseUrl.toString();
    }


}