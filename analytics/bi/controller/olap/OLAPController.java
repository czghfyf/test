package cn.bgotech.analytics.bi.controller.olap;

import cn.bgotech.analytics.bi.exception.BIException;
import cn.bgotech.analytics.bi.exception.BIRuntimeException;
import cn.bgotech.analytics.bi.service.olap.OLAPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by ChenZhiGang on 2017/5/19.
 */
@Controller
@RequestMapping("/olap")
public class OLAPController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OLAPService olapService;

    @PostMapping("/importMeasureFile")
    public String importMeasureFile(HttpServletRequest request, Model model) {

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Iterator<String> fileNames = multipartRequest.getFileNames();

        if (fileNames.hasNext()) {
            String fileName = fileNames.next();
            MultipartFile file = multipartRequest.getFile(fileName);
            if (file != null) {
                try {
                    byte[] fileBytes = file.getBytes();
                    if (fileBytes == null || fileBytes.length < 1) {
                        throw new BIException("导入数据失败，错误原因：[" + fileName + "]可能是一个空文件。");
                    }
                    olapService.importMeasure(fileBytes);
                } catch (IOException | BIException e) {
                    e.printStackTrace();
                    throw new BIRuntimeException(e);
                }
            }
        }

        model.addAttribute("msg", "导入完成");
        return "resultInfo";
    }

    @PostMapping("/import")
    public String importCube(HttpServletRequest request, Model model,
                             String fileType, String cubeName) {

        logger.info("$>>>>>>>>>>>> import cube data: enter controller");

        if (cubeName == null || cubeName.trim().isEmpty()) {
            throw new BIRuntimeException("多维数据集名称不能为空，请输入多维数据集名称。");
        }
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        if (multipartResolver.isMultipart(request) && (request instanceof MultipartHttpServletRequest)) {

            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            Iterator<String> fileNames = multipartRequest.getFileNames();

            if/*while*/ (fileNames.hasNext()) {
                String fileName = fileNames.next();
                MultipartFile file = multipartRequest.getFile(fileName);
                if (file != null) {
                    try {
                        byte[] fileBytes = file.getBytes();
                        if (fileBytes == null || fileBytes.length < 1) {
                            throw new BIException("导入数据失败，错误原因：[" + fileName + "]可能是一个空文件。");
                        }
//                            ThreadLocalTool.setCurrentUser(u);
                        olapService.importCube(fileType, cubeName, fileBytes);
//                            importResult = "import [" + cubeName + "] data success";
                    } catch (IOException | BIException e) {
                        e.printStackTrace();
                        throw new BIRuntimeException(e);
//                            importResult = "import [" + cubeName + "] data failed:\n" + e.getMessage();
                    }
                }
            }


        }

        model.addAttribute("msg", "正在导入[" + cubeName + "]数据");
        return "resultInfo";

    }
}
