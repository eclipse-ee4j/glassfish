package org.glassfish.main.test.app.ejb;

import jakarta.ejb.EJB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "AccessRemoteEJBByCDIServlet", urlPatterns = {"/remote_ejb_cdi"})
public class AccessRemoteEJBByCDIServlet extends HttpServlet {

    @EJB(lookup = "java:global/echoservice/EchoServiceEJB!org.glassfish.main.test.app.ejb.EchoServiceRemote")
    private EchoService service;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        String message = request.getParameter("message");
        try (PrintWriter out = response.getWriter()) {
            out.println(service.echo(message));
        }
    }
}
