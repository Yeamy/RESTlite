/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package yeamy.restlite.sentinel;

import com.alibaba.csp.sentinel.*;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import yeamy.restlite.RESTfulRequest;
import yeamy.restlite.sentinel.callback.RequestOriginParser;
import yeamy.restlite.sentinel.callback.WebCallbackManager;
import yeamy.restlite.sentinel.config.WebServletConfig;

import java.io.IOException;

/**
 * Servlet filter that integrates with Sentinel.
 * <pre>{@code
 * @WebFilter(urlPatterns = "/*",
 *         filterName = "sentinelFilter",
 *         dispatcherTypes = DispatcherType.FORWARD)
 * public class SentinelFilter extends CommonFilter {
 * }
 * }</pre>
 */
public class CommonFilter implements Filter {

    /**
     * If enabled, use the default context name, or else use the URL path as the context name,
     * {@link WebServletConfig#WEB_SERVLET_CONTEXT_NAME}. Please pay attention to the number of context (EntranceNode),
     * which may affect the memory footprint.
     *
     * @since 1.7.0
     */
    public static final String WEB_CONTEXT_UNIFY = "WEB_CONTEXT_UNIFY";

    private boolean webContextUnify = false;

    @Override
    public void init(FilterConfig filterConfig) {
        if (filterConfig.getInitParameter(WEB_CONTEXT_UNIFY) != null) {
            webContextUnify = Boolean.parseBoolean(filterConfig.getInitParameter(WEB_CONTEXT_UNIFY));
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        RESTfulRequest req = RESTfulRequest.get(request);
        Entry urlEntry = null;

        try {
            String target = cleanUri(req);
            // Parse the request origin using registered origin parser.
            String origin = parseOrigin(req.getRequest());
            String contextName = webContextUnify ? WebServletConfig.WEB_SERVLET_CONTEXT_NAME : target;
            ContextUtil.enter(contextName, origin);

            // Add HTTP method prefix if necessary.
            urlEntry = SphU.entry(target, ResourceTypeConstants.COMMON_WEB, EntryType.IN);
            chain.doFilter(request, response);
        } catch (BlockException e) {
            HttpServletResponse sResponse = (HttpServletResponse) response;
            // Return the block page, or redirect to another URL.
            WebCallbackManager.getUrlBlockHandler().blocked(req.getRequest(), sResponse, e);
        } catch (IOException | ServletException | RuntimeException e2) {
            Tracer.traceEntry(e2, urlEntry);
            throw e2;
        } finally {
            if (urlEntry != null) {
                urlEntry.exit();
            }
            ContextUtil.exit();
        }
    }

    protected String cleanUri(RESTfulRequest req) {
        return req.getServiceName();
    }

    private String parseOrigin(HttpServletRequest request) {
        RequestOriginParser originParser = WebCallbackManager.getRequestOriginParser();
        String origin = EMPTY_ORIGIN;
        if (originParser != null) {
            origin = originParser.parseOrigin(request);
            if (StringUtil.isEmpty(origin)) {
                return EMPTY_ORIGIN;
            }
        }
        return origin;
    }

    private static final String EMPTY_ORIGIN = "";
}
