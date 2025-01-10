package github.lianyutian.cshop.note.service;

import github.lianyutian.cshop.common.model.vo.PageVO;
import github.lianyutian.cshop.note.model.param.NotePageParam;
import github.lianyutian.cshop.note.model.vo.NoteShowVO;

/**
 * 用户笔记展示
 *
 * @author lianyutian
 * @since 2025-01-09 10:15:18
 * @version 1.0
 */
public interface NoteShowService {

  /**
   * 获取笔记展示详情
   *
   * @param noteId 笔记ID
   * @return 笔记详情
   */
  NoteShowVO getNoteShow(Long noteId);

  /**
   * 获取笔记展示列表
   *
   * @param notePageParam 查询入参
   * @return 笔记列表
   */
  PageVO<NoteShowVO> getNoteShowList(NotePageParam notePageParam);
}
