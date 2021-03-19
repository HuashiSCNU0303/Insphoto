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
import java.util.List;

@WebServlet(name = "LoadCommentServlet", urlPatterns = "/LoadCommentServlet")
public class LoadCommentServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        try (PrintWriter out = response.getWriter()) {
            int picId = Integer.parseInt(request.getParameter("id").trim());
            int commentId = Integer.parseInt(request.getParameter("commentid").trim());
            List<Comment> comments = PictureDAO.loadMoreComments(picId, commentId);
            JSONObject jsonObject = new JSONObject();
            if (comments.size() == 1 && comments.get(0).getId() == -1) {
                jsonObject.put("Result", "316");
            }
            else {
                comments.sort((o1, o2) -> {
                    if (o1.getId() > o2.getId()) {
                        return 1;
                    } else {
                        return -1;
                    }
                });
                jsonObject.put("Result", "315");
                jsonObject.put("Comments", comments);
            }
            out.write(jsonObject.toString());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
