package insphotob.picture;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "SetLikeAndStarServlet", urlPatterns = "/SetLikeAndStarServlet")
public class SetLikeAndStarServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        try (PrintWriter out = response.getWriter()) {
            String picId_str = request.getParameter("id").trim();
            int picId = Integer.parseInt(picId_str);
            int userId = Integer.parseInt(request.getParameter("userId").trim());
            boolean isSetLike = Boolean.parseBoolean(request.getParameter("like").trim());
            boolean isAdd = Boolean.parseBoolean(request.getParameter("add").trim());
            boolean result;
            if (isSetLike) {
                if (isAdd) {
                    result = PictureDAO.addLikeToPic(picId, userId);
                }
                else {
                    result = PictureDAO.delLike(picId, userId);
                }
            }
            else {
                if (isAdd) {
                    result = PictureDAO.addStarToPic(picId, userId);
                }
                else {
                    result = PictureDAO.delStar(picId, userId);
                }
            }
            JSONObject jsonObject = new JSONObject();
            if (result) {
                jsonObject.put("Result", "309");
            }
            else {
                jsonObject.put("Result", "310");
            }
            out.write(jsonObject.toString());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
