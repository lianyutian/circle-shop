package github.lianyutian.cshop.note.model.param;

import github.lianyutian.cshop.common.model.param.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分页查询笔记入参
 *
 * @author lianyutian
 * @since 2025-01-03 13:51:14
 * @version 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NotePageParam extends PageParam {
  /** 用户id */
  private Long userId;
}
