package com.example.progress_bar_demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author benjamin_5
 * @Description
 * @date 2022/10/12
 */
@RestController
@RequestMapping("file")
public class FileController {

    private final static Logger log = LoggerFactory.getLogger(FileController.class);

    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<String> fileUpload(@RequestParam("file") MultipartFile file) {
        try {
            // 获取资源文件存放路径，用于临时存放生成的excel文件
            String path = Objects.requireNonNull(this.getClass().getClassLoader().getResource("")).getPath();
            // 文件名
            String fileName = path + file.getOriginalFilename();
            // 创建目标文件
            File dest = new File(fileName);
            // 向指定路径写入文件
            file.transferTo(dest);
            // 返回文件访问路径
            return new ResponseEntity<>(fileName, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            log.info(String.format("文件上传失败，原因：%s", e));
            return new ResponseEntity<>("文件上传失败", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 文件下载
     * @return
     */
    @PostMapping("/download")
    @ResponseBody
    public ResponseEntity<String> download(HttpServletResponse response) throws IOException {
        // 获取资源文件存放路径，用于临时存放生成的excel文件
        String path = Objects.requireNonNull(this.getClass().getClassLoader().getResource("")).getPath();
        File pathFile = new File(path);
        File[] files = pathFile.listFiles();
        if (ObjectUtils.isEmpty(files)) {
            return new ResponseEntity<>("文件为空，请先上传文件", HttpStatus.OK);
        }
        InputStream inputStream = null;
        ServletOutputStream ouputStream = null;
        try {
            for (File file : files) {
                if(file.isDirectory()){
                    continue;
                }
                inputStream = new FileInputStream(file);
                response.setContentType("application/x-msdownload");
                response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(file.getName(), "UTF-8"));
                // 设置一个总长度，否则无法估算进度
                response.setHeader("Content-Length",String.valueOf(file.length()));
                ouputStream = response.getOutputStream();
                byte b[] = new byte[1024];
                int n;
                while ((n = inputStream.read(b)) != -1) {
                    ouputStream.write(b, 0, n);
                }
                ouputStream.flush();
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if(inputStream != null){
                inputStream.close();
            }
            if(ouputStream != null){
                ouputStream.close();
            }
        }
        return new ResponseEntity<>("文件下载成功", HttpStatus.OK);
    }

    /**
     * 测试用
     * @param response
     * @throws IOException
     */
    @PostMapping("/test")
    @ResponseBody
    public void test(HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.print("任务处理开始");
        out.flush();
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(1000);//加入每个任务处理需要1秒钟
            } catch (InterruptedException e) {
            }
            out.print(i);
            out.flush();
        }
    }


}
