package insphotob.user;

import net.sf.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "LoadUserResultServlet", urlPatterns = "/LoadUserResultServlet")
public class LoadUserResultServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        try (PrintWriter out = response.getWriter()) {
            int biggestUserId = Integer.parseInt(request.getParameter("biggestUserID").trim());
            String keyword = request.getParameter("text").trim();
            JSONObject jsonObject = new JSONObject();
            List<Integer> results = UserDAO.loadMoreUserResult(biggestUserId, keyword);
            if (results.get(0) == -1) {
                jsonObject.put("Result", "334");
            }
            else {
                jsonObject.put("Result", "333");
                results.sort(((o1, o2) -> {
                    if (o1 > o2) {
                        return 1;
                    }
                    else {
                        return -1;
                    }
                }));
                jsonObject.put("UserIds", results);
            }
            out.write(jsonObject.toString());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
