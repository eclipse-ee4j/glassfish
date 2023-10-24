package org.glassfish.main.test.app.ejb;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

@WebServlet(name = "AccessLocalEJBByJNDIServlet", urlPatterns = {"/local_ejb_jndi"})
public class AccessLocalEJBByJNDIServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        String message = request.getParameter("message");
        try (PrintWriter out = response.getWriter()) {
            try {
                Hashtable<String, String> env = new Hashtable<String, String>();
                Context context = new InitialContext(env);
                EchoService service = (EchoService) context
                        .lookup("java:app/echoservice/EchoServiceEJB!org.glassfish.main.test.app.ejb.EchoServiceLocal");
                out.println(service.echo(message));
            } catch (Exception e) {
                e.printStackTrace(out);
            }
        }
    }
}
