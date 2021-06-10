import cn.hutool.core.util.RandomUtil;
import com.wxy.web.favorites.WebFavoritesApplication;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.dao.CategoryRepository;
import com.wxy.web.favorites.dao.FavoritesRepository;
import com.wxy.web.favorites.dao.UserRepository;
import com.wxy.web.favorites.model.Category;
import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.util.PinYinUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @Author HL
 * @Date 2020/3/30 14:21
 * @Description TODO
 **/
@Slf4j
@SpringBootTest(classes = WebFavoritesApplication.class)
public class DemoTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FavoritesRepository favoritesRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 批量插入测试数据
     */
    @Test
    public void test() {
        for (int k = 0; k < 100; k++) {
            String key = RandomUtil.randomString(16);
            User user = userRepository.save(new User(null, "test" + k, passwordEncoder.encode("test" + k), "test" + k + "@qq.com", null, null, null, null));
            for (int i = 0; i < 100; i++) {
                Category category = categoryRepository.save(new Category(null, "test" + i, user.getId(), null, null, null, null));
                for (int j = 0; j < 100; j++) {
                    favoritesRepository.save(new Favorites(null, "百度一下" + j, "http://www.baidu.com/favicon.ico", "http://www.baidu.com/", category.getId(), user.getId(), PinYinUtils.toPinyin("百度一下" + j), PinYinUtils.toPinyinS("百度一下" + j), null, null, null, null, null, null, null, null));
                }
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
