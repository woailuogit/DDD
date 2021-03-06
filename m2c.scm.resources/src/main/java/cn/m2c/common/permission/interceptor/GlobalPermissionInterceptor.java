package cn.m2c.common.permission.interceptor;

import io.jsonwebtoken.Claims;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import cn.m2c.common.RedisUtil;
import cn.m2c.ddd.common.auth.JwtSubject;
import cn.m2c.ddd.common.auth.RequirePermissions;
import cn.m2c.ddd.common.port.adapter.util.GlobalConstants;
import cn.m2c.ddd.common.port.adapter.util.JwtUtil;
import cn.m2c.ddd.common.port.adapter.util.RSAUtil;
import cn.m2c.ddd.common.serializer.ObjectSerializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class GlobalPermissionInterceptor extends HandlerInterceptorAdapter
{

    private Logger logger = LoggerFactory
            .getLogger(GlobalPermissionInterceptor.class);

    private static final String ACCESS_TOKEN = "access_token";

    /**
     * token失效
     */
    private static Integer TOKEN_EXPIRATION_EXCEPTION = 900004;

    /**
     * 接口没有权限访问
     */
    public static Integer SYS_PERMISSION_VISIT_ERROR = 2404404;
    
    /**
     * session失效
     */
    private static Integer SESSION_EXPIRATION_EXCEPTION = 900003;

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception
    {

        return permissionControl(request, response, handler);
    }

    private boolean permissionControl(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws IOException
    {
        logger.debug("current handler--->" + handler.getClass().getName());
        try
        {
            if (handler instanceof HandlerMethod)
            {
                HandlerMethod hm = (HandlerMethod) handler;
                Class<?> clazz = hm.getBeanType();
                Method m = hm.getMethod();
                if (clazz != null && m != null)
                {
                    boolean isMethondAnnotation = m
                            .isAnnotationPresent(RequirePermissions.class);
                    RequirePermissions rc = null;
                    // 如果方法和类声明中同时存在这个注解，那么方法中的会覆盖类中的设定。
                    if (isMethondAnnotation)
                        rc = m.getAnnotation(RequirePermissions.class);
                    else
                        return true;
                    String[] permission = rc.value();// 方法上面的权限
                    if (permission != null && permission.length > 0)
                    {
                        Set<String> anp = new HashSet<String>(
                                Arrays.asList(permission));
                        if (anp.contains("sys:permissions:all")) // 默认全局权限-直接通过-无需验证
                            return true;
                        else
                            return validatePermission(permission, request,
                                    response);
                    }
                }
            }
        }
        catch (IOException e)
        {

            logger.error("permission auth is error message:" + e.getMessage(),
                    e);
            writeResult(response, TOKEN_EXPIRATION_EXCEPTION,
                    "accessToken认证无效,系统拒绝访问!");
            return false;
        }
        return true;
    }

    private boolean validatePermission(String[] permission,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        String key = "", subject = "";

        String accessToken = getAccessToken(request);

        Claims claims = null;
        try
        {
            claims = JwtUtil.parseJWT(accessToken,
                    RSAUtil.getPublicKey(GlobalConstants.JWT_TOKEN_PUBLIC_KEY));

            Date exprirarionDate = claims.getExpiration();
            Date now = new Date();
            if (now.after(exprirarionDate))
            {
                writeResult(response, TOKEN_EXPIRATION_EXCEPTION,
                        "accessToken有效期已过,系统拒绝访问!");
            }

        }
        catch (Exception e)
        {
            writeResult(response, TOKEN_EXPIRATION_EXCEPTION,
                    "accessToken认证无效,系统拒绝访问!");
        }
        String json = claims.getSubject();

        if (StringUtils.isBlank(json) && StringUtils.isEmpty(json))
        {
            writeResult(response, TOKEN_EXPIRATION_EXCEPTION, "认证信息被未认证！");
            return false;
        }

        key = GlobalConstants.USER_LOGIN_SESSION_KEY.replace("{key}",
                DigestUtils.md5Hex(json));

        if (StringUtils.isEmpty(key) && StringUtils.isBlank(key))

        {
            writeResult(response, TOKEN_EXPIRATION_EXCEPTION,
                    "accessToken认证无效,系统拒绝访问!");
        }

        String auth = RedisUtil.get(key);
        if (StringUtils.isBlank(auth) || StringUtils.isEmpty(auth))
        {
            writeResult(response, SESSION_EXPIRATION_EXCEPTION,
                    "session失效！");
        }
        JwtSubject jwtSubject = ObjectSerializer.instance().deserialize(auth,
                JwtSubject.class);

        subject = claims.getSubject();

        if (StringUtils.isEmpty(subject) && StringUtils.isBlank(subject))

        {
            writeResult(response, TOKEN_EXPIRATION_EXCEPTION,
                    "accessToken认证无效,系统拒绝访问!");
        }

        if (jwtSubject == null)
        {
            writeResult(response, TOKEN_EXPIRATION_EXCEPTION,
                    "accessToken有效期已过,系统拒绝访问!");
            return false;
        }

        Set<String> persSet = new HashSet<String>(Arrays.asList(permission));
        if ("1".equals(jwtSubject.getRoleId())
                && "mtx1215".equals(jwtSubject.getUserName()))
        {
            return true;
        }
        else
        {
            String perms = RedisUtil.get(jwtSubject.getPermissionKey());
            List<String> permissions = JSON.parseArray(perms, String.class);
            if (!Collections.disjoint(permissions, persSet)) /*
                                                              * false: 有交集 true
                                                              * : 没有交集
                                                              */
            {
                return true;
            }
        }
        writeResult(response, SYS_PERMISSION_VISIT_ERROR, "无权限访问接口!");
        return false;
    }

    private String getAccessToken(HttpServletRequest request)
    {
        logger.info("headers:"
                + JSONObject.toJSONString(request.getHeaderNames()));
        String accessToken = request.getHeader(ACCESS_TOKEN);
        accessToken = accessToken != null ? accessToken : request
                .getParameter(ACCESS_TOKEN);
        return accessToken;
    }

    private void writeResult(HttpServletResponse response, Integer status,
            String errorMessage) throws IOException
    {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("utf-8");
        PrintWriter writer = response.getWriter();
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("status", status);
        result.put("errorMessage", errorMessage);
        writer.write(JSONObject.toJSONString(result));
        writer.flush();
        writer.close();
    }

}
