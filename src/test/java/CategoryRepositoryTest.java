import com.wxy.web.favorites.WebFavoritesApplication;
import com.wxy.web.favorites.dao.CategoryRepository;
import com.wxy.web.favorites.model.Category;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author HL
 * @Date 2020/3/31 16:56
 * @Description TODO
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebFavoritesApplication.class)
@Slf4j
public class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    public void queryByPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.DESC, "sort");
        Page<Category> page = categoryRepository.findByUserId(1, pageable);
        log.info("分页查询：list:[{}],pages:[{}],total:[{}]", page.getContent().size(), page.getTotalPages(), page.getTotalElements());
    }
}
