import com.wxy.web.favorites.WebFavoritesApplication;
import com.wxy.web.favorites.model.Task;
import com.wxy.web.favorites.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @Author HL
 * @Date 2020/3/30 14:21
 * @Description TODO
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebFavoritesApplication.class)
@Slf4j
@Transactional
public class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Test
    public void findByAlarmTime() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Task> taskList = taskService.findByAlarmTime("2021-02-20 10:00:00");
        log.info("查询结果：taskList = {}", taskList);
    }


    @Test
    public void findById() {
        Task task = taskService.findById(83);
        log.info("查询结果：task = {}", task);
    }

}
