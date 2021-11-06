import com.wxy.web.favorites.WebFavoritesApplication;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.dao.*;
import com.wxy.web.favorites.model.*;
import com.wxy.web.favorites.util.PinYinUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Author HL
 * @Date 2020/3/30 14:21
 * @Description TODO
 **/
@Slf4j
@SpringBootTest(classes = WebFavoritesApplication.class)
@Transactional
public class DemoTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FavoritesRepository favoritesRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private UserFileRepository userFileRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SearchTypeRepository searchTypeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 批量插入测试数据
     */
    @Test
    public void test() throws InterruptedException {
        int concurrent = 1;
        int userCount = 1;
        int categoryCount = 1000;
        int favoritesCount = 1000;
        int momentCount = 10000;
        int searchTypeCount = 1000;
        int userFileCount = 1000;
        int taskCount = 10000;
        ExecutorService service = Executors.newFixedThreadPool(concurrent);
        for (int l = 0; l < concurrent; l++) {
            service.execute(() -> {
                long id = Thread.currentThread().getId();
                // 批量创建用户
                for (int k = 0; k < userCount; k++) {
                    User user = userRepository.save(new User(null, id + "test" + k, passwordEncoder.encode(DigestUtils.md5DigestAsHex((id + "test" + k).getBytes(StandardCharsets.UTF_8))), id + "test" + k + "@qq.com", null, null, null, null, null, null));
                    log.info("创建用户：{}", user);
                    // 批量创建分类
                    for (int i = 0; i < categoryCount; i++) {
                        Category category = categoryRepository.save(new Category(null, "test" + i, user.getId(), null, null, null, null, null));
                        // 批量创建收藏
                        for (int j = 0; j < favoritesCount; j++) {
                            favoritesRepository.save(new Favorites(null, "百度一下" + j, "http://www.baidu.com/favicon.ico", "http://www.baidu.com/", category.getId(), user.getId(), PinYinUtils.toPinyin("百度一下" + j), PinYinUtils.toPinyinS("百度一下" + j), null, null, null, null, null, null, null, null,null,null,null));
                        }
                    }
                    // 批量创建瞬间
                    for (int i = 0; i < momentCount; i++) {
                        momentRepository.save(new Moment(null, "测试" + i, "测试" + i, user.getId(), new Date(), null));
                    }
                    // 批量创建搜索
                    for (int i = 0; i < searchTypeCount; i++) {
                        searchTypeRepository.save(new SearchType(null, "百度搜索" + i, "https://www.baidu.com/favicon.ico", "https://www.bing.com/search?q=", user.getId()));
                    }
                    // 批量创建文件
                    for (int i = 0; i < userFileCount; i++) {
                        userFileRepository.save(new UserFile(null, user.getId(), null, new Date(), new Date(), "测试" + i, null, null, PublicConstants.DIR_CODE, null, null));
                    }
                    // 批量创建任务
                    for (int i = 0; i < taskCount; i++) {
                        taskRepository.save(new Task(null, "测试" + i, new Date(), null, null, user.getId(), new Date(), PublicConstants.TASK_LEVEL_0));
                    }
                }
            });
        }
        service.shutdown();// 调用停止命令，等待所有任务执行完成
        while (!service.isTerminated()) {
            TimeUnit.SECONDS.sleep(1);
        }
        log.info("批量插入数据完成！！！");
    }

    /**
     * 查询所有书签
     */
    @Test
    public void test1() {
        List<Favorites> list = favoritesRepository.findAll();
        log.info("查询结果：{}", list.size());
    }

    /**
     * 查询所有用户
     */
    @Test
    public void test2() {
        List<User> list = userRepository.findAll();
        log.info("查询结果：{}", list);
    }
}
