package ca.multiversial.api;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import ca.multiversial.websocket.ChatEndpoint;

@WebServlet(urlPatterns = {"/topics/*"} )
public class Topics extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		Set<String> theList = (Set<String>) ChatEndpoint.getTopics();
		
	    String json = new Gson().toJson(theList);
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
	    response.getWriter().write(json);		
	}
}
