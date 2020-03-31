import com.wxy.web.favorites.WebFavoritesApplication;
import com.wxy.web.favorites.dao.FavoritesRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author HL
 * @Date 2020/3/31 21:38
 * @Description TODO
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebFavoritesApplication.class)
@Slf4j
public class FavoritesRepositoryTest {

    @Autowired
    private FavoritesRepository favoritesRepository;

    public void queryFavorites(){
    }
}
