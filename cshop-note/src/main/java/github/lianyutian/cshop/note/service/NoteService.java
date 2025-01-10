package github.lianyutian.cshop.note.service;

import github.lianyutian.cshop.note.model.param.NoteAddParam;
import github.lianyutian.cshop.note.model.param.NoteEditParam;
import github.lianyutian.cshop.note.model.vo.NoteDetailVO;

/**
 * 笔记服务
 *
 * @author lianyutian
 * @since 2025-01-03 10:53:40
 * @version 1.0
 */
public interface NoteService {

  /**
   * 添加笔记
   *
   * @param noteAddParam 笔记入参
   */
  int addNote(NoteAddParam noteAddParam);

  /**
   * 更新笔记
   *
   * @param noteAddParam 笔记更新入参
   */
  int updateNote(NoteEditParam noteAddParam);

  /**
   * 删除笔记
   *
   * @param id 笔记id
   */
  int deleteNote(long id);

  /**
   * 获取笔记详情
   *
   * @param noteId 笔记id
   */
  NoteDetailVO getNoteDetail(Long noteId);
}
