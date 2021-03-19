package insphotob.picture;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "DelCommentServlet", urlPatterns = "/DelCommentServlet")
public class DelCommentServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        try (PrintWriter out = response.getWriter()) {
            String commentId_str = request.getParameter("id").trim();
            int commentId = Integer.parseInt(commentId_str);
            JSONObject jsonObject = new JSONObject();
            if (PictureDAO.delComment(commentId)) {
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
