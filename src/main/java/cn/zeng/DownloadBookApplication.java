package cn.zeng;

import lombok.Cleanup;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
@RestController
public class DownloadBookApplication {

    public static void main(String[] args) {
        SpringApplication.run(DownloadBookApplication.class, args);
    }

    @GetMapping(path = "/download")
    public ResponseEntity<?> downloadFile(@RequestParam String filename, HttpServletRequest request, HttpServletResponse response) {
        String path = String.format(Consts.downloadPATH, filename);
        File file = new File(path);
        if (file.exists()) {
            String userAgent = request.getHeader("User-Agent");
            byte[] bytes = userAgent.contains("MSIE") ? filename.getBytes() : filename.getBytes(StandardCharsets.UTF_8); //处理safari的乱码问题
            String outName = new String(bytes, StandardCharsets.ISO_8859_1); // 各浏览器基本都支持ISO编码
            response.setHeader("Content-disposition", "attachment; filename=\"" + outName + "\".txt");

            try {
                response.setCharacterEncoding("UTF-8");

                @Cleanup BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                @Cleanup PrintWriter writer = response.getWriter();

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    writer.write(line);
                    writer.write("\n");
                    writer.flush();
                }
                return ResponseEntity.ok().build();
            } catch (IOException e) {
                throw new RuntimeException("download file exception");
            }

        }
        Consts.bookName = filename;
        new Thread(new BookProcessor()).start();
        @SuppressWarnings("unchecked")
        ResponseEntity responseEntity = new ResponseEntity("正在查找资源，请稍后10分钟后再下载", HttpStatus.CREATED);
        return responseEntity;
    }


}
