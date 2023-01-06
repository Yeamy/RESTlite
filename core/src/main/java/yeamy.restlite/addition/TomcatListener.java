package yeamy.restlite.addition;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;
import yeamy.restlite.RESTfulListener;

import java.lang.reflect.Field;

/**
 * add PATCH, let PATCH, PUT support Body on Tomcat via Reflection
 *
 * @author Yeamy
 */
public class TomcatListener extends RESTfulListener {
    private boolean init = false;

    @Override
    public void createRequest(HttpServletRequest httpReq) {
        if (!init && httpReq instanceof RequestFacade) {
            RequestFacade facade = (RequestFacade) httpReq;
            try {
                Field f_req = RequestFacade.class.getDeclaredField("request");
                f_req.setAccessible(true);
                Request req = (Request) f_req.get(facade);
                Connector conn = req.getConnector();
                conn.setParseBodyMethods("PATCH,POST,PUT");
                init = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.createRequest(httpReq);
    }

    public boolean isEmbed() {
        return true;
    }
}
