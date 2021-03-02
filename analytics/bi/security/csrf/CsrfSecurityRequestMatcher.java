package cn.bgotech.analytics.bi.security.csrf;

import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by ChenZhiGang on 2017/5/15.
 * 废弃原因：由于可以将页面常量_CSRF_动态拼接在POST方式的REST请求的URL后，所以不再使用此类。
 */
@Deprecated
public class CsrfSecurityRequestMatcher implements RequestMatcher {

    private Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");

    // exclude the list of URLs
    private List<String> excludedUrls;

    public void setExcludedUrls(List<String> excludedUrls) {
        this.excludedUrls = excludedUrls;
    }

    @Override
    public boolean matches(HttpServletRequest request) {

        if (excludedUrls != null /*&& execludeUrls.size() > 0*/) {
            String servletPath = request.getServletPath();
            for (String url : excludedUrls) {
                if (servletPath.startsWith(url)) {
                    return false;
                }
            }
        }
        return !allowedMethods.matcher(request.getMethod()).matches();
    }
}
