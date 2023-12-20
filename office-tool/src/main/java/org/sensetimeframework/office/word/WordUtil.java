package org.sensetimeframework.office.word;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.*;

@Slf4j
public class WordUtil {

    /**
     * 将html转换为word文档
     * @param htmlString html字符串
     * @param wordFilePath 输出word文档路径(doc结尾)
     */
    public static void convertHtmlStringToWord(String htmlString, String wordFilePath) throws IOException {
        File outputFile = new File(wordFilePath);

        if (!outputFile.getParentFile().exists()) {
            FileUtils.forceMkdirParent(outputFile);
        }

        try (POIFSFileSystem pfs = new POIFSFileSystem()) {
            try (InputStream is = new ByteArrayInputStream(htmlString.getBytes())) {
                DirectoryEntry directory = pfs.getRoot();
                directory.createDocument("WordDocument", is);
                try (FileOutputStream fos = new FileOutputStream(wordFilePath)) {
                    pfs.writeFilesystem(fos);
                }
            }
        }

        log.info("转换word文件完成!文档地址：{}", wordFilePath);
    }
}
