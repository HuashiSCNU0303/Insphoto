package insphotob.picture;

import insphotob.user.UserDAO;
import net.sf.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

@WebServlet(name = "LoadPhotoServlet", urlPatterns = "/LoadPhotoServlet")
public class LoadPhotoServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        try (PrintWriter out = response.getWriter()) {
            String curSmallestID_str = request.getParameter("smallestImgID").trim();
            int curSmallestID = Integer.parseInt(curSmallestID_str);
            HashMap<Integer, String> results = PictureDAO.loadMorePhoto(curSmallestID);
            JSONObject jsonObject = new JSONObject();
            if (results.containsKey(-1)) {
                jsonObject.put("Result", "306");
            }
            else {
                jsonObject.put("Result", "305");
                jsonObject.put("Photos", results);
            }
            out.write(jsonObject.toString());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
