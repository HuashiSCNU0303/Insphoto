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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@WebServlet(name = "RefreshLSCServlet", urlPatterns = "/RefreshLSCServlet")
public class RefreshLSCServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        try (PrintWriter out = response.getWriter()) {
            int picId = Integer.parseInt(request.getParameter("id").trim());
            HashMap<String, Integer> LSCNums = PictureDAO.getLSCNum(picId);
            JSONObject jsonObject = new JSONObject();
            if (LSCNums.containsKey("failed")) {
                jsonObject.put("Result", "314");
            }
            else {
                List<Comment> comments = PictureDAO.getRecentComments(picId);
                if (comments.size() == 1 && comments.get(0).getId() == -1) {
                    jsonObject.put("Result", "315");
                }
                else {
                    comments.sort((o1, o2) -> {
                        if (o1.getId() > o2.getId()) {
                            return 1;
                        } else {
                            return -1;
                        }
                    });
                    jsonObject.put("Result", "313");
                    jsonObject.put("LSCNums", LSCNums);
                    jsonObject.put("Comments", comments);
                }
            }
            out.write(jsonObject.toString());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
