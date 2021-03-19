package insphotob.user;

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

@WebServlet(name = "ReceiveProfileServlet", urlPatterns = "/ReceiveProfileServlet")
public class ReceiveProfileServlet extends HttpServlet {
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
            String newFileName = "";
            boolean isSuccessful = true, isDefault = false;
            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();
                if (item.isFormField()) {
                    if (item.getFieldName().equals("userId")) {
                        // account = URLDecoder.decode(item.getString(),"UTF-8");
                        userId = Integer.parseInt(item.getString());
                    }
                    else {
                        if (item.getString().equals("true")) {
                            isDefault = true;
                        }
                    }
                }
                else {
                    //图片url
                    String basePath = "/root/photo/Insphoto/";
                    String suffix = item.getName().substring(item.getName().lastIndexOf('.'));
                    //新文件名
                    newFileName = userId + "_profile" + suffix;
                    File file = new File(basePath, newFileName);
                    // 如果这个文件存在，就删掉，重新写
                    if (file.exists()) {
                        file.delete();
                    }
                    try {
                        item.write(file);
                    } catch (Exception e) {
                        isSuccessful = false;
                        e.printStackTrace();
                    }
                }
            }
            // 头像是默认头像，就到数据库里更新用户头像url
            if ((userId == -1) || isDefault && !UserDAO.changeProfileImg(userId, newFileName)) {
                isSuccessful = false;
            }
            JSONObject jsonObject = new JSONObject();
            if (isSuccessful) {
                jsonObject.put("Result", "203");
            } else {
                jsonObject.put("Result", "204");
            }
            out.write(jsonObject.toString());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
