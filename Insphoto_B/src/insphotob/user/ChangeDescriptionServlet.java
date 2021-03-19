package insphotob.user;

import net.sf.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "ChangeDescriptionServlet", urlPatterns = "/ChangeDescriptionServlet")
public class ChangeDescriptionServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        try (PrintWriter out = response.getWriter()) {
            int userId = Integer.parseInt(request.getParameter("userId").trim());
            String newDescription = request.getParameter("newDescription").trim();
            JSONObject jsonObject = new JSONObject();
            if (UserDAO.changeDescription(userId, newDescription)) {
                jsonObject.put("Result", "385"); // 成功
            }
            else {
                jsonObject.put("Result", "386");
            }
            out.write(jsonObject.toString());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
