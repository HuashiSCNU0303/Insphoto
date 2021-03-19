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

@WebServlet(name = "ChangePhotoDescriptionServlet", urlPatterns = "/ChangePhotoDescriptionServlet")
public class ChangePhotoDescriptionServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        try (PrintWriter out = response.getWriter()) {
            int imageId = Integer.parseInt(request.getParameter("imgId").trim());
            String newDescription = request.getParameter("newDescription").trim();
            JSONObject jsonObject = new JSONObject();
            if (PictureDAO.changeDescription(imageId, newDescription)) {
                jsonObject.put("Result", "387"); // 成功
            }
            else {
                jsonObject.put("Result", "388");
            }
            out.write(jsonObject.toString());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
