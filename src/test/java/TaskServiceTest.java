import com.wxy.web.favorites.Application;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.dao.TaskRepository;
import com.wxy.web.favorites.model.Task;
import com.wxy.web.favorites.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * @Author HL
 * @Date 2020/3/30 14:21
 * @Description TODO
 **/
@Slf4j
@SpringBootTest(classes = Application.class)
@Transactional
public class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    public void findByAlarmTime() throws ParseException {
        List<Task> taskList = taskService.findByAlarmTime("2021-02-20 10:00:00");
        log.info("查询结果：taskList = {}", taskList);
    }


    @Test
    public void findById() {
        Task task = taskService.findById(83);
        log.info("查询结果：task = {}", task);
    }

    @Test
    public void taskCountByDayBetween() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(PublicConstants.FORMAT_DATE_PATTERN);
        List<Map<String, Object>> mapList = taskRepository.taskCountByDayBetween(30,sdf.parse("2021-07-01"),sdf.parse("2021-07-05"));
        log.info("查询结果：{}",mapList);
    }

}
