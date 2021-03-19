package insphotob.picture;

import net.sf.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

@WebServlet(name = "InitUserPhotosServlet", urlPatterns = "/InitUserPhotosServlet")
public class InitUserPhotosServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        try (PrintWriter out = response.getWriter()) {
            int userId = Integer.parseInt(request.getParameter("userId").trim());
            int mineId = Integer.parseInt(request.getParameter("mine").trim());
            HashMap<Integer, String> results = PictureDAO.queryRecentUserPhoto(userId);
            JSONObject jsonObject = new JSONObject();
            if (results.containsKey(-1)) {
                jsonObject.put("Result", "320");
            }
            else {
                HashMap<String, Integer> userLSNums = PictureDAO.getUserLSSFNum(userId, mineId);
                if (userLSNums.containsKey("failed")) {
                    jsonObject.put("Result", "320");
                }
                else {
                    jsonObject.put("Result", "319");
                    jsonObject.put("Photos", results);
                    jsonObject.put("LSNums", userLSNums);
                }
            }
            out.write(jsonObject.toString());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
