package com.aembootcamp.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import javax.jcr.Node;
import javax.jcr.Session;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component(service = {javax.servlet.Servlet.class},
        property = {
                "sling.servlet.resourceTypes=/apps/aem-bootcamp/components/structure/subscribe",
                "sling.servlet.selectors=subscribe",
                "sling.servlet.extensions=json",
                "sling.servlet.methods=GET"
        })
public class subscribeServlet extends SlingAllMethodsServlet {

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        // Parse parameters
        String email = request.getParameter("email");
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String categories = request.getParameter("categories");

        // Get ResourceResolver
        Map<String, Object> param = new HashMap<>();
        param.put(ResourceResolverFactory.SUBSERVICE, "subscribeUser");
        try (ResourceResolver resourceResolver = resolverFactory.getServiceResourceResolver(param)) {
            Session session = resourceResolver.adaptTo(Session.class);
            if (session != null) {
                // Create the JCR structure
                String date = new SimpleDateFormat("ddMMyyyy").format(new Date());
                Node root = session.getNode("/content/aem-bootcamp/subscribers");
                if (!root.hasNode(date)) {
                    root.addNode(date, "nt:unstructured");
                }
                Node dateNode = root.getNode(date);
                if (!dateNode.hasNode(email)) {
                    Node emailNode = dateNode.addNode(email, "nt:unstructured");
                    emailNode.setProperty("firstName", firstName);
                    emailNode.setProperty("lastName", lastName);
                    emailNode.setProperty("categories", categories);
                    session.save();
                }
            }
        } catch (Exception e) {
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}