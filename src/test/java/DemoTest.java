import cn.hutool.core.util.RandomUtil;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
    public void test() {
        // 批量创建用户
        for (int k = 0; k < 100; k++) {
            User user = userRepository.save(new User(null, "test" + k, passwordEncoder.encode("test" + k), "test" + k + "@qq.com", null, null, null, null));
            // 批量创建分类
            for (int i = 0; i < 100; i++) {
                Category category = categoryRepository.save(new Category(null, "test" + i, user.getId(), null, null, null, null,null));
                // 批量创建收藏
                for (int j = 0; j < 100; j++) {
                    favoritesRepository.save(new Favorites(null, "百度一下" + j, "http://www.baidu.com/favicon.ico", "http://www.baidu.com/", category.getId(), user.getId(), PinYinUtils.toPinyin("百度一下" + j), PinYinUtils.toPinyinS("百度一下" + j), null, null, null, null, null, null, null, null));
                }
            }
            // 批量创建瞬间
            for (int i = 0; i < 100; i++) {
                momentRepository.save(new Moment(null,"测试"+i,"测试"+i,user.getId(),new Date(),null));
            }
            // 批量创建搜索
            for (int i = 0; i < 100; i++) {
                searchTypeRepository.save(new SearchType(null,"百度搜索"+i,"https://www.baidu.com/favicon.ico","https://www.bing.com/search?q=",user.getId()));
            }
            // 批量创建文件
            for (int i = 0; i < 100; i++) {
                userFileRepository.save(new UserFile(null,user.getId(),null,new Date(),new Date(),"测试"+i,null,null,PublicConstants.DIR_CODE,null,null));
            }
            // 批量创建任务
            for (int i = 0; i < 100; i++) {
                taskRepository.save(new Task(null,"测试"+i,new Date(),null,null,user.getId(),new Date(),PublicConstants.TASK_LEVEL_0));
            }
        }
        log.info("批量插入数据完成！！！");
    }

    @Test
    public void test1() {
        List<Favorites> list = favoritesRepository.findAll();
        log.info("查询结果：{}", list.size());
    }

    @Test
    public void test2() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(PublicConstants.FORMAT_DATETIME_PATTERN);
        favoritesRepository.deleteByDeleteFlagAndDeleteTimeBefore(PublicConstants.DELETE_CODE, sdf.parse("2021-06-10 01:45:03"));
    }
}
