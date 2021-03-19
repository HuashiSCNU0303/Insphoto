package insphotob.user;

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

// 简单版本，后续再考虑使用全文搜索引擎
@WebServlet(name = "SearchUserServlet", urlPatterns = "/SearchUserServlet")
public class SearchUserServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        try (PrintWriter out = response.getWriter()) {
            String keyword = request.getParameter("text").trim();
            JSONObject jsonObject = new JSONObject();
            List<Integer> results = UserDAO.searchUser(keyword);
            if (results.get(0) == -1) {
                jsonObject.put("Result", "332");
            }
            else {
                jsonObject.put("Result", "331");
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
