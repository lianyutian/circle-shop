package github.lianyutian.cshop.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.lianyutian.cshop.user.model.po.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * UserMapper
 *
 * @author lianyutian
 * @since 2024-12-24 14:19:30
 * @version 1.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {}
