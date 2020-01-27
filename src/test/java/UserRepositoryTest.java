import com.wxy.web.favorites.WebFavoritesApplication;
import com.wxy.web.favorites.dao.UserRepository;
import com.wxy.web.favorites.model.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebFavoritesApplication.class)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSave() {
        for (int i = 0; i < 5; i++) {
            userRepository.save(new User(null, "zhangsan" + i, "123" + i));
        }
        Assert.assertEquals(5, userRepository.findAll().size());
    }
}