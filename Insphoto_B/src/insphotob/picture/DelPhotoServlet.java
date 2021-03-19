package insphotob.picture;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "DelPhotoServlet", urlPatterns = "/DelPhotoServlet")
public class DelPhotoServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        try (PrintWriter out = response.getWriter()) {
            int picId = Integer.parseInt(request.getParameter("id").trim());
            String picUrl = request.getParameter("url").trim();
            // 删除服务器上的文件
            String basePath = "/root/photo/Insphoto/";
            File file = new File(basePath, picUrl);
            if (file.exists()) {
                file.delete();
            }
            // 删除数据库中的记录
            JSONObject jsonObject = new JSONObject();
            if (PictureDAO.delPhoto(picId)) {
                jsonObject.put("Result", "317");
            }
            else {
                jsonObject.put("Result", "318");
            }
            out.write(jsonObject.toString());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
