package github.lianyutian.cshop.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.lianyutian.cshop.social.model.po.UserAttention;
import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * UserAttentionMapper
 *
 * @author lianyutian
 * @since 2025-01-14 09:49:05
 * @version 1.0
 */
@Mapper
public interface UserAttentionMapper extends BaseMapper<UserAttention> {
  /**
   * 根据userID和attentionID查询关注用户列表
   *
   * @param userAttentionList 关注列表
   * @return 关注用户列表
   */
  List<Long> selectAttentionIdList(
      @Param("userAttentionList") Set<UserAttention> userAttentionList);

  /**
   * 批量插入关注用户列表
   *
   * @param batchList 关注列表
   */
  void batchInsert(@Param("batchList") List<UserAttention> batchList);

  /**
   * 批量更新关注用户列表
   *
   * @param batchList 关注列表
   */
  void batchUpdate(@Param("batchList") List<UserAttention> batchList);

  /**
   * 根据userID查询关注用户列表
   *
   * @param userId 用户ID
   * @return 关注用户列表ID
   */
  List<Long> selectAttentionIdListByUserId(@Param("userId") long userId);
}
