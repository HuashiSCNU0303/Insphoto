package insphotob.picture;

import insphotob.user.UserDAO;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;

@WebServlet(name = "ReceivePhotoServlet", urlPatterns = "/ReceivePhotoServlet")
public class ReceivePhotoServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        try (PrintWriter out = response.getWriter()) {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            List items = null;
            try {
                items = upload.parseRequest(request);
            } catch (FileUploadException e) {
                e.printStackTrace();
            }
            Iterator iter = items.iterator();
            int userId = -1;
            String newFileName = "", description = "";
            boolean isSuccessful = true;
            JSONObject jsonObject = new JSONObject();
            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();
                if (item.isFormField()) {
                    if (item.getFieldName().equals("userId")) {
                        // account = URLDecoder.decode(item.getString(),"UTF-8");
                        userId = Integer.parseInt(item.getString());
                    }
                    else if (item.getFieldName().equals("description")) {
                        description = URLDecoder.decode(item.getString(),"UTF-8");
                    }
                }
                else {
                    // 图片url
                    String basePath = "/root/photo/Insphoto/";
                    String suffix = item.getName().substring(item.getName().lastIndexOf('.'));
                    String curTime = String.valueOf(System.currentTimeMillis());
                    // 新文件名
                    newFileName = curTime + suffix;
                    // String suppressedFileName = curTime + "_thumbnail" + suffix;
                    File file = new File(basePath, newFileName);
                    try {
                        item.write(file);
                        if (!PictureDAO.addPhoto(userId, newFileName, description)) {
                            isSuccessful = false;
                        }
                    } catch (Exception e) {
                        isSuccessful = false;
                    }
                }
            }
            if (isSuccessful) {
                jsonObject.put("Result", "210");
            } else {
                jsonObject.put("Result", "211");
            }
            out.write(jsonObject.toString());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
