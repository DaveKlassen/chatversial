package ca.multiversial.api;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.multiversial.websocket.ChatEndpoint;

@WebServlet(urlPatterns = {"/tally/*"} )
public class Tally extends HttpServlet {
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        ConcurrentHashMap<String, Integer> theList = (ConcurrentHashMap<String, Integer>) ChatEndpoint.getScores();
        try
        {
            response.setContentType("text/html");
            ServletOutputStream out = response.getOutputStream();
            out.println("<html>");
            out.println("<body>");
            out.println("<h1>Tally</h1>");
            out.println("<table>");
            out.println("<tr>");
            out.println("<th>Name</th>");
            out.println("<th>Score</th>");
            out.println("</tr>");
            for (String key : theList.keySet() ) {
                out.println("<tr>");
                out.println("<th>" + key + "</th>");
                out.println("<th>" + theList.get(key) + "</th>");
                out.println("</tr>");
            }
            out.println("<br/>");
            out.println("</body>");
            out.println("</html>");
            out.flush();
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
    }

}
