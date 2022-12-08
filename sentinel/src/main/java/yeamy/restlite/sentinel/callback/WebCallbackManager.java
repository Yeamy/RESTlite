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
package yeamy.restlite.sentinel.callback;

import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * Registry for URL cleaner and URL block handler.
 *
 * @author youji.zj
 */
public class WebCallbackManager {

    /**
     * URL block handler.
     */
    private static volatile UrlBlockHandler urlBlockHandler = new DefaultUrlBlockHandler();

    private static volatile RequestOriginParser requestOriginParser = null;

    public static UrlBlockHandler getUrlBlockHandler() {
        return urlBlockHandler;
    }

    public static void setUrlBlockHandler(UrlBlockHandler urlBlockHandler) {
        AssertUtil.isTrue(urlBlockHandler != null, "URL block handler should not be null");
        WebCallbackManager.urlBlockHandler = urlBlockHandler;
    }

    public static RequestOriginParser getRequestOriginParser() {
        return requestOriginParser;
    }

    public static void setRequestOriginParser(RequestOriginParser requestOriginParser) {
        WebCallbackManager.requestOriginParser = requestOriginParser;
    }
}
