package insphotob.picture;

import com.alibaba.fastjson.*;
import insphotob.user.UserDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "GetPicInfoServlet", urlPatterns = "/GetPicInfoServlet")
public class GetPicInfoServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        try (PrintWriter out = response.getWriter()) {
            //获得请求中传来的用户名和密码
            String picId_str = request.getParameter("id").trim();
            int picId = Integer.parseInt(picId_str);
            int userId = Integer.parseInt(request.getParameter("userId").trim());
            Picture picture = PictureDAO.getPicInfo(picId, userId);
            JSONObject jsonObject = new JSONObject();
            if (picture == null) {
                jsonObject.put("Result", "308");
            }
            else {
                picture.getComments().sort((o1, o2) -> {
                    if (o1.getId() > o2.getId()) {
                        return 1;
                    } else {
                        return -1;
                    }
                });
                jsonObject.put("info", JSON.toJSONString(picture));
                jsonObject.put("Result", "307");
            }
            out.write(jsonObject.toString());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
