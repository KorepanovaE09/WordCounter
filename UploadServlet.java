import java.io.*;
import java.nio.file.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@MultipartConfig
public class UploadServlet extends HttpServlet {

    private static final String SAVE_DIR = "D:\\ПОЛИТЕХ\\мага\\2\\WordCounter\\data";

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain; charset=UTF-8");
        Part filePart = request.getPart("file");

        if (filePart == null || filePart.getSize() == 0) {
            response.getWriter().println("Ошибка: файл не выбран");
            return;
        }

        String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
        File saveFile = new File(SAVE_DIR, fileName);
        System.out.println("Сохранение файла: " + saveFile.getAbsolutePath());

        try (InputStream input = filePart.getInputStream();
             OutputStream output = new FileOutputStream(saveFile)) {

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }
        System.out.println("Файл сохранён");
        FileIndexer.processFile(saveFile.getAbsolutePath());
        response.getWriter().println("Файл успешно загружен: " + fileName);
    }
}