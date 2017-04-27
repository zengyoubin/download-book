package cn.zeng;

import lombok.extern.slf4j.Slf4j;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author ZYB
 * @since 2017-04-26 下午5:38
 */
@Slf4j
public class WriteBookPipeline implements Pipeline {

    @Override
    public void process(ResultItems resultItems, Task task) {
        String path = String.format(Consts.downloadPATH, Consts.bookName);
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path, true))) {
            Chapter chapter = resultItems.get("chapter");
            bufferedWriter.write(chapter.getName());
            bufferedWriter.write("\n\t");
            bufferedWriter.write(chapter.getContent());
            bufferedWriter.write("\n\t");
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            log.warn("write file error", e);
        }

    }
}
