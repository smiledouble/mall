package com.cxs.controller;

import com.cxs.utils.FastDfsUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/25 17:13
 * 文件上传下载
 */
@Controller
public class FileController {

    /**
     * 服务器的前缀
     */
    @Value("http://192.168.234.134")
    private String fileServerPath;


    @RequestMapping("/pic/upload")
    @ResponseBody
    public Object uploadFile(String dir, MultipartFile uploadFile) {
        //这里dir貌似没用到
        //获得文件的原始名字
        String oldName = uploadFile.getOriginalFilename();
        //截取拓展名
        String extName = oldName.substring(oldName.lastIndexOf(".") + 1);
        String filePath = null;
        try {
            //调用静态方法 传入字节数组，和拓展名
            filePath = FastDfsUtil.uploadFile(uploadFile.getBytes(), extName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //阿里规约真的难受 new的时候要求指定集合大小 哈哈
        Map<String, Object> map = new HashMap<>(10);
        if (filePath != null) {
            //说明上传成功了
            map.put("error", 0);
            //比如  http://192.168.234.134/g1/M00/00/00/wKjnjFyYkkOANAQNAACmsXK7IkU547.jpg
            map.put("url", fileServerPath + "/" + filePath);
        } else {
            map.put("error", 1);
            map.put("message", "图片上传失败");
        }
        return map;
    }

}
