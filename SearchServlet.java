import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SearchServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String word = request.getParameter("word");

        response.setContentType("text/html; charset=UTF-8");

        PrintWriter out = response.getWriter();

        if (word == null || word.isEmpty()) {

            out.println("<h2>Введите слово</h2>");
            return;
        }

        word = word.toLowerCase();

        try (Connection conn = DB.getConnection()) {

            String sql =
                    "SELECT f.file_path, wc.count " +
                    "FROM word_counts wc " +
                    "JOIN files f ON wc.file_id = f.id " +
                    "WHERE wc.word = ? " +
                    "ORDER BY wc.count DESC";

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setString(1, word);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                String filePath = rs.getString("file_path");
                String fileName = new java.io.File(filePath).getName();
                int count = rs.getInt("count");

                out.println("<div class='result'>");

                out.println("<h3>Файл:</h3>");
                out.println("<p>" + fileName + "</p>");

                out.println("<h3>Количество:</h3>");
                out.println("<p>" + count + "</p>");

                out.println("</div>");

            } else {

                out.println("<p class='error'>Слово не найдено</p>");
            }

        } catch (Exception e) {

            out.println("<h2>Ошибка БД</h2>");

            e.printStackTrace();
        }
    }
}