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

@WebServlet(name = "LoadUserLSPhotoServlet", urlPatterns = "/LoadUserLSPhotoServlet")
public class LoadUserLSPhotoServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        try (PrintWriter out = response.getWriter()) {
            String curSmallestID_str = request.getParameter("smallestImgID").trim();
            int curSmallestID = Integer.parseInt(curSmallestID_str);
            int userId = Integer.parseInt(request.getParameter("userId").trim());
            String type = request.getParameter("type").trim();
            boolean isLike = true;
            if ("star".equals(type)) {
                isLike = false;
            }
            HashMap<Integer, String> results = PictureDAO.loadMoreUserLSPhoto(curSmallestID, userId, isLike);
            JSONObject jsonObject = new JSONObject();
            if (results.containsKey(-1)) {
                jsonObject.put("Result", "326");
            }
            else {
                jsonObject.put("Result", "325");
                jsonObject.put("Photos", results);
            }
            out.write(jsonObject.toString());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
