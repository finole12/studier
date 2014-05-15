package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.AuthenticateUser;
import model.DBHandler;

/**
 * Servlet implementation class JsonServlet
 */
@WebServlet("/JsonServlet")
public class JsonServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public JsonServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		String user = request.getParameter("user");
		String password = request.getParameter("password");
		String command = request.getParameter("command");
		
		if (user != null & password != null) {
			boolean UserAuthentication = AuthenticateUser.getUserAuthentication(user, password);
			
			if(UserAuthentication) {
				
				if (command != null) {
					
					if (command.equals("getMarkers")) {
						String jsonString = DBHandler.getMarkerJsonFromDB();
						out.write(jsonString);
					}
					if (command.equals("getAuthentication")) {
						out.write(Boolean.toString(UserAuthentication));
					}
					if (command.equals("getRoute")) {
						String jsonString = DBHandler.getRouteFromDB(user);
						out.write(jsonString);
					}
					
				}
			
			} else {
				out.write(Boolean.toString(UserAuthentication));
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		String user = request.getParameter("user");
		String password = request.getParameter("password");
		String command = request.getParameter("command");
		String delivered = request.getParameter("delivered");
		String returns = request.getParameter("returns");
		String pointID = request.getParameter("pointID");
		
		
		if (user != null & password != null) {
			boolean UserAuthentication = AuthenticateUser.getUserAuthentication(user, password);
			
			if(UserAuthentication) {
				
				if (command != null) {
					
					if (command.equals("getMarkers")) {
						String jsonString = DBHandler.getMarkerJsonFromDB();
						out.write(jsonString);
					}
					if (command.equals("getAuthentication")) {
						out.write(Boolean.toString(UserAuthentication));
					}
					if (command.equals("getRoute")) {
						String jsonString = DBHandler.getRouteFromDB(user);
						out.write(jsonString);
					}
					if (command.equals("deliveryReport")) {
						if (delivered != null & returns != null & pointID != null) {
							String message = 
									DBHandler.insertReportToDB(pointID, delivered, returns, user);
							out.write(message);
						}
					
					}
			
				} else {
					out.write(Boolean.toString(UserAuthentication));
				}
			}
		}
		
	}

}
