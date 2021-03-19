package insphotob.user;

import com.alibaba.fastjson.JSONObject;
import insphotob.picture.PictureDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "SubscribeServlet", urlPatterns = "/SubscribeServlet")
public class SubscribeServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        try (PrintWriter out = response.getWriter()) {
            int fromId = Integer.parseInt(request.getParameter("fromId").trim());
            int toId = Integer.parseInt(request.getParameter("toId").trim());
            boolean isToSubscribe = Boolean.parseBoolean(request.getParameter("subscribe").trim());
            boolean result;
            if (isToSubscribe) {
                result =  UserDAO.addSubscribe(fromId, toId);
            }
            else {
                result =  UserDAO.delSubscribe(fromId, toId);
            }
            JSONObject jsonObject = new JSONObject();
            if (result) {
                jsonObject.put("Result", "393");
            }
            else {
                jsonObject.put("Result", "394");
            }
            out.write(jsonObject.toString());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
