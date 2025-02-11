package github.lianyutian.cshop.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.lianyutian.cshop.social.model.po.UserFollower;
import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * UserFollowerMapper
 *
 * @author lianyutian
 * @since 2025-01-14 09:49:05
 * @version 1.0
 */
@Mapper
public interface UserFollowerMapper extends BaseMapper<UserFollower> {
  /**
   * 根据关注用户集合查询用户粉丝id集合
   *
   * @param userFollowerSet 关注用户集合
   * @return 粉丝id集合
   */
  List<Long> selectFollowerIdList(@Param("userFollowerSet") Set<UserFollower> userFollowerSet);

  /**
   * 批量插入用户粉丝列表
   *
   * @param batchList 粉丝列表
   */
  void batchInsert(@Param("batchList") List<UserFollower> batchList);

  /**
   * 批量更新用户粉丝列表
   *
   * @param batchList 粉丝列表
   */
  void batchUpdate(@Param("batchList") List<UserFollower> batchList);

  /**
   * 根据用户id查询用户粉丝id集合
   *
   * @param userId 用户id
   * @return 粉丝id集合
   */
  List<Long> selectFollowerIdListByUserId(
      @Param("userId") long userId, @Param("offset") int offset, @Param("limit") int limit);
}
