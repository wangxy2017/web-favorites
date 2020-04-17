import com.wxy.web.favorites.WebFavoritesApplication;
import com.wxy.web.favorites.dao.CategoryRepository;
import com.wxy.web.favorites.dao.FavoritesRepository;
import com.wxy.web.favorites.dao.UserRepository;
import com.wxy.web.favorites.model.Category;
import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.model.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.DigestUtils;

/**
 * @Author HL
 * @Date 2020/3/30 14:21
 * @Description TODO
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebFavoritesApplication.class)
@Slf4j
public class DemoTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FavoritesRepository favoritesRepository;

    /**
     * 批量插入测试数据
     */
    @Test
    public void test() {
        for (int k = 0; k < 100; k++) {
            String password = "test" + k;
            String randomKey = "test" + k;
            User user = userRepository.save(new User(null, "test" + k, DigestUtils.md5DigestAsHex((password + randomKey).getBytes()), "test" + k + "@qq.com", randomKey));
            for (int i = 0; i < 1000; i++) {
                Category category = categoryRepository.save(new Category(null, "test" + i, user.getId(), null, null, null));
                for (int j = 0; j < 1000; j++) {
                    favoritesRepository.save(new Favorites(null, "百度一下" + j, "http://www.baidu.com/favicon.ico", "http://www.baidu.com/", category.getId(), user.getId()));
                }
            }
        }
        log.info("批量插入数据完成！！！");
    }
}
