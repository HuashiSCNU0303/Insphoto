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
import java.util.List;

@WebServlet(name = "LoadPhotoResultServlet", urlPatterns = "/LoadPhotoResultServlet")
public class LoadPhotoResultServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        try (PrintWriter out = response.getWriter()) {
            int smallestImgId = Integer.parseInt(request.getParameter("smallestImgID").trim());
            String keyword = request.getParameter("text").trim();
            JSONObject jsonObject = new JSONObject();
            HashMap<Integer, String> results = PictureDAO.loadMorePhotoResult(smallestImgId, keyword);
            if (results.containsKey(-1)) {
                jsonObject.put("Result", "334");
            }
            else {
                jsonObject.put("Result", "333");
                jsonObject.put("Photos", results);
            }
            out.write(jsonObject.toString());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
