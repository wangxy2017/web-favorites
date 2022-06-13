import com.wxy.web.favorites.Application;
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
@SpringBootTest(classes = Application.class)
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
                    User user = userRepository.save(new User().setUsername(id + "test" + k).setPassword(passwordEncoder.encode(DigestUtils.md5DigestAsHex((id + "test" + k).getBytes(StandardCharsets.UTF_8)))).setEmail(id + "test" + k + "@qq.com"));
                    log.info("创建用户：{}", user);
                    // 批量创建分类
                    for (int i = 0; i < categoryCount; i++) {
                        Category category = categoryRepository.save(new Category().setName("test" + i).setUserId(user.getId()));
                        // 批量创建收藏
                        for (int j = 0; j < favoritesCount; j++) {
                            favoritesRepository.save(new Favorites().setName("百度一下" + j).setIcon("http://www.baidu.com/favicon.ico").setUrl("http://www.baidu.com/").setCategoryId(category.getId()).setUserId(user.getId()).setPinyin( PinYinUtils.toPinyin("百度一下" + j)).setPinyinS( PinYinUtils.toPinyinS("百度一下" + j)));
                        }
                    }
                    // 批量创建瞬间
                    for (int i = 0; i < momentCount; i++) {
                        momentRepository.save(new Moment().setContent("测试" + i).setText("测试" + i).setUserId(user.getId()).setCreateTime(new Date()));
                    }
                    // 批量创建搜索
                    for (int i = 0; i < searchTypeCount; i++) {
                        searchTypeRepository.save(new SearchType().setName("百度搜索" + i).setIcon("https://www.baidu.com/favicon.ico").setUrl("https://www.bing.com/search?q=").setUserId(user.getId()));
                    }
                    // 批量创建文件
                    for (int i = 0; i < userFileCount; i++) {
                        userFileRepository.save(new UserFile().setUserId(user.getId()).setCreateTime(new Date()).setUpdateTime(new Date()).setFilename("测试" + i).setIsDir(PublicConstants.DIR_CODE));
                    }
                    // 批量创建任务
                    for (int i = 0; i < taskCount; i++) {
                        taskRepository.save(new Task().setContent("测试" + i).setTaskDate(new Date()).setUserId(user.getId()).setCreateTime(new Date()).setLevel(PublicConstants.TASK_LEVEL_0));
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

    /**
     * 分享所有书签
     */
    @Test
    @Transactional(rollbackFor = Exception.class)
    public void test3() {
        userRepository.updateShareAll();
        log.info("分享所有书签...");
    }
}
