package example.undertow;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Hostet einen einfachen Microservice unter <a href="http://localhost:8080/micro">localhost/micro</a>
 */
public class UndertowMicroserviceGET {
    private static final Logger logger = LoggerFactory.getLogger(UndertowResourceHosting.class);

    @SuppressWarnings("DuplicatedCode") // example code. Duplication is allowed
    public static void main(String[] args) throws ServletException {
        DeploymentInfo deployment = Servlets.deployment()
                .setClassLoader(UndertowResourceHosting.class.getClassLoader())
                .setContextPath("")
                .setDeploymentName("Example_UndertowMicroserviceGET")
                .addServlet(Servlets.servlet("ExampleMicroservice", MicroserviceImpl.class).addMapping("/micro"));

        DeploymentManager manager = Servlets.defaultContainer().addDeployment(deployment);
        manager.deploy();
        HttpHandler handler = manager.start();
        PathHandler pathHandler = Handlers.path(handler);

        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(pathHandler)
                .build();
        server.start();
        logger.info("Server started");
    }

    public static final class MicroserviceImpl extends HttpServlet {
        private int numberOfRequests = 0;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            numberOfRequests++;
            String message = "Your visitor-number is #" + numberOfRequests;

            resp.setContentType("text/html");
            PrintWriter responseWriter = resp.getWriter();
            responseWriter.println("<html><body><h2>" + message + "</h2></body/></html>");
            responseWriter.close();
        }
    }
}
